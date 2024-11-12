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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_ITEM = 0;
    private static final int TYPE_BUTTON = 1;

    private List<Cart> cartList;
    private static Context context;
    private static DatabaseReference cartsRef;
    private static DatabaseReference productsRef;

    public CartAdapter(List<Cart> cartList, Context context) {
        this.cartList = cartList;
        this.context = context;
        this.cartsRef = FirebaseDatabase.getInstance().getReference("carts");
        this.productsRef = FirebaseDatabase.getInstance().getReference("products");
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
        for (Cart cart : cartList) {
            cart.setProcessPurchase(false);
            cartsRef.child(cart.getId()).child("processPurchase").setValue(false);
        }
        Toast.makeText(context, "Pedido realizado con éxito", Toast.LENGTH_SHORT).show();
        notifyDataSetChanged();
    }

    private static void deleteCartItem(Cart cart) {
        // Eliminar un solo elemento y restablecer el stock
        int quantityToRestore = 1;
        cartsRef.orderByChild("productId").equalTo(cart.getProductId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        boolean deletedOne = false;
                        for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                            Cart item = itemSnapshot.getValue(Cart.class);
                            if (item != null && item.getUserId().equals(cart.getUserId()) && item.getCategoryId().equals(cart.getCategoryId())) {
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
                            if (item != null && item.getProductId().equals(cart.getProductId()) && item.getCategoryId().equals(cart.getCategoryId())) {
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