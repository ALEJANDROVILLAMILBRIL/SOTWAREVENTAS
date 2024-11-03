package com.example.softwareventas.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.softwareventas.R;
import com.example.softwareventas.models.Product;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private List<Product> productList;
    private boolean isUser;

    public ProductAdapter(List<Product> productList, boolean isUser) {
        this.productList = productList;
        this.isUser = isUser;
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
            holder.buyButton.setOnClickListener(v -> {
                Toast.makeText(holder.itemView.getContext(), "Compraste " + product.getName(), Toast.LENGTH_SHORT).show();
            });
        } else {
            holder.buyButton.setVisibility(View.GONE);
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