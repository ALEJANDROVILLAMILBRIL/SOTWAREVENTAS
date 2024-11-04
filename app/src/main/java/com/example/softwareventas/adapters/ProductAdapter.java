package com.example.softwareventas.adapters;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.softwareventas.R;
import com.example.softwareventas.models.Cart;
import com.example.softwareventas.models.Product;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private List<Product> productList;
    private boolean isUser;
    private DatabaseReference cartsRef, productsRef;
    private String currentUserId;

    public ProductAdapter(List<Product> productList, boolean isUser) {
        this.productList = productList;
        this.isUser = isUser;
        // Referencias de Firebase
        cartsRef = FirebaseDatabase.getInstance().getReference("carts");
        productsRef = FirebaseDatabase.getInstance().getReference("products");
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.productNameTextView.setText(product.getName());
        holder.productPriceTextView.setText("Precio: $" + product.getPrice());
        holder.productStockTextView.setText("Stock: " + product.getStock());
        holder.productStatusTextView.setText(product.isActive() ? "Activo" : "Inactivo");

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        holder.productDateTextView.setText("Creado: " + dateFormat.format(product.getCreatedAt()));

        // Show "Comprar" button only if the user role is "USUARIO"
        if (isUser) {
            holder.buyButton.setVisibility(View.VISIBLE);
            holder.buyButton.setOnClickListener(v -> showConfirmationDialog(holder, product));
        } else {
            holder.buyButton.setVisibility(View.GONE);
        }
    }

    private void showConfirmationDialog(ProductViewHolder holder, Product product) {
        new AlertDialog.Builder(holder.itemView.getContext())
                .setTitle("Confirmar Compra")
                .setMessage("¿Estás seguro de que quieres comprar " + product.getName() + "?")
                .setPositiveButton("Sí", (dialog, which) -> processPurchase(holder, product))
                .setNegativeButton("No", null)
                .show();
    }

    private void processPurchase(ProductViewHolder holder, Product product) {
        if (product.getStock() > 0) {
            String cartId = cartsRef.push().getKey();
            Cart cartItem = new Cart(
                    cartId,
                    product.getId(),
                    product.getName(),
                    product.getCategoryId(),
                    currentUserId,
                    product.getPrice(),
                    1,
                    System.currentTimeMillis()
            );

            // Guardar el cartItem en la referencia `carts` en Firebase
            cartsRef.child(cartId).setValue(cartItem).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // Actualizar el stock del producto
                    int updatedStock = product.getStock() - 1;
                    productsRef.child(product.getId()).child("stock").setValue(updatedStock)
                            .addOnCompleteListener(stockTask -> {
                                if (stockTask.isSuccessful()) {
                                    Toast.makeText(holder.itemView.getContext(), "Compra realizada exitosamente", Toast.LENGTH_SHORT).show();
                                    product.setStock(updatedStock);
                                    notifyItemChanged(holder.getAdapterPosition());
                                } else {
                                    Toast.makeText(holder.itemView.getContext(), "Error al actualizar stock", Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    Toast.makeText(holder.itemView.getContext(), "Error al registrar compra", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(holder.itemView.getContext(), "Stock no disponible", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView productNameTextView, productPriceTextView, productStockTextView, productStatusTextView, productDateTextView;
        Button buyButton;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productNameTextView = itemView.findViewById(R.id.productNameTextView);
            productPriceTextView = itemView.findViewById(R.id.productPriceTextView);
            productStockTextView = itemView.findViewById(R.id.productStockTextView);
            productStatusTextView = itemView.findViewById(R.id.productStatusTextView);
            productDateTextView = itemView.findViewById(R.id.productDateTextView);
            buyButton = itemView.findViewById(R.id.buyButton);
        }
    }
}