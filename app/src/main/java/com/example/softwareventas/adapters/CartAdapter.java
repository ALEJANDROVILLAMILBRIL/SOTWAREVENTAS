package com.example.softwareventas.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.softwareventas.R;
import com.example.softwareventas.models.Cart;
import com.example.softwareventas.utils.InvoiceGenerator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CartAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_ITEM = 0;
    private static final int TYPE_BUTTON = 1;

    private List<Cart> cartList;
    private static Context context;
    private static DatabaseReference cartsRef;
    private static DatabaseReference productsRef;
    private FirebaseAuth mAuth;

    public CartAdapter(List<Cart> cartList, Context context) {
        this.cartList = cartList;
        this.context = context;
        this.cartsRef = FirebaseDatabase.getInstance().getReference("carts");
        this.productsRef = FirebaseDatabase.getInstance().getReference("products");
        this.mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public int getItemViewType(int position) {
        return (position == cartList.size()) ? TYPE_BUTTON : TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_ITEM) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
            return new CartViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order_button, parent, false);
            return new OrderButtonViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof CartViewHolder) {
            Cart cart = cartList.get(position);
            ((CartViewHolder) holder).bind(cart);
        } else if (holder instanceof OrderButtonViewHolder) {
            ((OrderButtonViewHolder) holder).orderButton.setOnClickListener(v -> processOrder());
        }
    }

    @Override
    public int getItemCount() {
        return cartList.size() + 1;
    }

    private void processOrder() {
        // Consolidar productos con processPurchase: true en un solo mapa
        Map<String, Cart> consolidatedItems = new HashMap<>();
        for (Cart cart : cartList) {
            if (cart.isProcessPurchase()) {
                if (consolidatedItems.containsKey(cart.getProductId())) {
                    // Si el producto ya está en el mapa, sumamos la cantidad
                    Cart existingCart = consolidatedItems.get(cart.getProductId());
                    existingCart.setQuantity(existingCart.getQuantity() + cart.getQuantity());
                } else {
                    // Si el producto no está en el mapa, lo añadimos con la cantidad inicial
                    Cart consolidatedCart = new Cart(cart.getProductId(), cart.getProductName(), cart.getQuantity(), cart.getPrice(), true);
                    consolidatedItems.put(cart.getProductId(), consolidatedCart);
                }
            }
        }

        // Convertir el mapa consolidado a una lista para generar la factura
        List<Cart> itemsToInvoice = new ArrayList<>(consolidatedItems.values());

        // Generar la factura y eliminar los elementos del carrito en Firebase
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userEmail = currentUser.getEmail();
            String userName = currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "Cliente";
            String userId = currentUser.getUid(); // Obtener el ID del usuario actual

            // Generar la factura en PDF solo para los elementos consolidados
            File invoiceFile = InvoiceGenerator.generateInvoice(context, itemsToInvoice, userName, userEmail);
            Toast.makeText(context, "Pedido realizado con éxito. Factura generada.", Toast.LENGTH_SHORT).show();

            // Eliminar todos los elementos con `processPurchase: true` y que pertenezcan al usuario actual en Firebase
            cartsRef.orderByChild("userId").equalTo(userId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                                Cart item = itemSnapshot.getValue(Cart.class);
                                // Verificar que el elemento tiene processPurchase: true antes de eliminar
                                if (item != null && item.isProcessPurchase()) {
                                    itemSnapshot.getRef().removeValue(); // Elimina solo los elementos del usuario actual con processPurchase: true
                                }
                            }

                            // Eliminar los elementos procesados del carrito local
                            cartList.removeIf(cart -> cart.isProcessPurchase() && cart.getUserId().equals(userId));
                            notifyDataSetChanged(); // Actualizar la vista del carrito
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(context, "Error al procesar el pedido.", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(context, "Error: Usuario no autenticado.", Toast.LENGTH_SHORT).show();
        }
    }

    private static void deleteCartItem(Cart cart) {
        // Eliminar un solo elemento y restablecer el stock solo si processPurchase es true
        if (!cart.isProcessPurchase()) {
            return;
        }

        int quantityToRestore = 1;
        cartsRef.orderByChild("productId").equalTo(cart.getProductId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        boolean deletedOne = false;
                        for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                            Cart item = itemSnapshot.getValue(Cart.class);
                            if (item != null && item.getUserId().equals(cart.getUserId()) && item.getCategoryId().equals(cart.getCategoryId()) && item.isProcessPurchase()) {
                                if (!deletedOne) {
                                    itemSnapshot.getRef().removeValue();
                                    restoreProductStock(cart.getProductId(), quantityToRestore);
                                    deletedOne = true;
                                }
                            }
                        }
                        if (!deletedOne) {
                            Toast.makeText(context, "No se encontró el elemento para eliminar", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "Elemento eliminado", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(context, "Error al eliminar el elemento", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private static void deleteAllCartItems(Cart cart) {
        // Eliminar todos los elementos con el mismo productId y categoryId
        cartsRef.orderByChild("userId").equalTo(cart.getUserId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int totalQuantityToRestore = 0;
                        for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                            Cart item = itemSnapshot.getValue(Cart.class);
                            if (item != null && item.getProductId().equals(cart.getProductId()) && item.getCategoryId().equals(cart.getCategoryId()) && item.isProcessPurchase()) {
                                totalQuantityToRestore += item.getQuantity();
                                itemSnapshot.getRef().removeValue();
                            }
                        }
                        restoreProductStock(cart.getProductId(), totalQuantityToRestore);
                        Toast.makeText(context, "Todos los elementos eliminados", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(context, "Error al eliminar todos los elementos", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private static void restoreProductStock(String productId, int quantity) {
        // Restaurar el stock en la colección de productos
        productsRef.child(productId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Integer currentStock = snapshot.child("stock").getValue(Integer.class);
                    if (currentStock != null) {
                        productsRef.child(productId).child("stock").setValue(currentStock + quantity);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Error al restaurar el stock", Toast.LENGTH_SHORT).show();
            }
        });
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        TextView productNameTextView, productQuantityTextView, productPriceTextView, productTotalPriceTextView;
        ImageButton buttonDeleteOne, buttonDeleteAll;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            productNameTextView = itemView.findViewById(R.id.productNameTextView);
            productQuantityTextView = itemView.findViewById(R.id.productQuantityTextView);
            productPriceTextView = itemView.findViewById(R.id.productPriceTextView);
            productTotalPriceTextView = itemView.findViewById(R.id.productTotalPriceTextView);
            buttonDeleteOne = itemView.findViewById(R.id.buttonDeleteOne);
            buttonDeleteAll = itemView.findViewById(R.id.buttonDeleteAll);
        }

        public void bind(Cart cart) {
            productNameTextView.setText(cart.getProductName());
            productQuantityTextView.setText("Cantidad: " + cart.getQuantity());
            productPriceTextView.setText("Precio: $" + cart.getPrice());

            // Calcula el precio total y muestra
            double totalPrice = cart.getPrice() * cart.getQuantity();
            productTotalPriceTextView.setText("Precio Total: $" + String.format("%.2f", totalPrice));

            // Configura el botón para eliminar uno
            buttonDeleteOne.setOnClickListener(v -> deleteCartItem(cart));

            // Configura el botón para eliminar todo
            buttonDeleteAll.setOnClickListener(v -> deleteAllCartItems(cart));
        }
    }

    static class OrderButtonViewHolder extends RecyclerView.ViewHolder {
        Button orderButton;

        public OrderButtonViewHolder(@NonNull View itemView) {
            super(itemView);
            orderButton = itemView.findViewById(R.id.orderButton);
        }
    }
}