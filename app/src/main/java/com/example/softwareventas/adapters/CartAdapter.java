package com.example.softwareventas.adapters;

import android.content.Context;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_ITEM = 0;
    private static final int TYPE_BUTTON = 1;

    private List<Cart> cartList;
    private Context context;
    private DatabaseReference cartsRef;

    public CartAdapter(List<Cart> cartList, Context context) {
        this.cartList = cartList;
        this.context = context;
        this.cartsRef = FirebaseDatabase.getInstance().getReference("carts");
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

    static class CartViewHolder extends RecyclerView.ViewHolder {
        TextView productNameTextView, productQuantityTextView, productPriceTextView;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            productNameTextView = itemView.findViewById(R.id.productNameTextView);
            productQuantityTextView = itemView.findViewById(R.id.productQuantityTextView);
            productPriceTextView = itemView.findViewById(R.id.productPriceTextView);
        }

        public void bind(Cart cart) {
            productNameTextView.setText(cart.getProductName());
            productQuantityTextView.setText("Cantidad: " + cart.getQuantity());
            productPriceTextView.setText("Precio: $" + cart.getPrice());
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