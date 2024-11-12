package com.example.softwareventas.activitys;

import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.softwareventas.MainActivity;
import com.example.softwareventas.R;
import com.example.softwareventas.adapters.CartAdapter;
import com.example.softwareventas.models.Cart;
import com.example.softwareventas.models.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CartActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private BottomNavigationView bottomNavigationView;
    private Toolbar toolbar;

    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    private String userRole;

    private RecyclerView recyclerViewCart;
    private CartAdapter cartAdapter;
    private List<Cart> cartList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        // Inicializar vistas
        drawerLayout = findViewById(R.id.drawer_layout_cart);
        navigationView = findViewById(R.id.nav_view_cart);
        bottomNavigationView = findViewById(R.id.bottom_nav_cart);
        toolbar = findViewById(R.id.toolbar_cart);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            handleLogout();
            return;
        }
        userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());

        // Configurar Toolbar
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_home); // Asegúrate de que ic_home sea el icono que deseas
        toolbar.setNavigationOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        // Configurar RecyclerView
        recyclerViewCart = findViewById(R.id.recyclerView_cart);
        recyclerViewCart.setLayoutManager(new LinearLayoutManager(this));

        // Inicializar adaptador y configurarlo en RecyclerView
        cartAdapter = new CartAdapter(cartList, this);
        recyclerViewCart.setAdapter(cartAdapter);

        // Load Cart Items
        DatabaseReference cartsRef = FirebaseDatabase.getInstance().getReference("carts");
        cartsRef.orderByChild("userId").equalTo(currentUser.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        cartList.clear();

                        // Mapa temporal para combinar productos con el mismo productId y categoryId
                        Map<Pair<String, String>, Cart> combinedCartMap = new HashMap<>();

                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Cart cartItem = dataSnapshot.getValue(Cart.class);

                            if (cartItem != null && cartItem.isProcessPurchase()) {
                                // Crear una clave usando productId y categoryId
                                Pair<String, String> key = new Pair<>(cartItem.getProductId(), cartItem.getCategoryId());

                                if (combinedCartMap.containsKey(key)) {
                                    // Si ya existe, sumamos el quantity
                                    Cart existingCart = combinedCartMap.get(key);
                                    existingCart.setQuantity(existingCart.getQuantity() + cartItem.getQuantity());
                                } else {
                                    // Si no existe, lo añadimos al mapa
                                    combinedCartMap.put(key, cartItem);
                                }
                            }
                        }

                        // Convertimos el mapa a una lista para el adaptador
                        cartList.addAll(combinedCartMap.values());
                        cartAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(CartActivity.this, "Error al cargar el carrito", Toast.LENGTH_SHORT).show();
                    }
                });

        // Configurar NavigationView
        navigationView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                Intent intent = new Intent(CartActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            } else if (item.getItemId() == R.id.nav_profile) {
                // Manejar la navegación a Profile
                Intent intent = new Intent(CartActivity.this, ProfileActivity.class);
                startActivity(intent);
                finish();
            } else if (item.getItemId() == R.id.nav_logout) {
                handleLogout();
            }
            drawerLayout.closeDrawer(GravityCompat.START); // Cierra el drawer
            return true;
        });

        bottomNavigationView.setSelectedItemId(R.id.nav_cart);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    userRole = user.getRole();
                    configureNavigationMenu(userRole);
                    adjustMenuOptions(userRole);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CartActivity.this, "Error al cargar el rol del usuario", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadCartItems() {

    }

    private void handleLogout() {
        // Cerrar sesión en Firebase Auth
        mAuth.signOut();

        // Redirigir al LoginActivity
        Intent intent = new Intent(CartActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void configureNavigationMenu(String role) {
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                startActivity(new Intent(CartActivity.this, HomeActivity.class));
            } else if ("ADMIN".equals(role) && item.getItemId() == R.id.nav_product) {
                startActivity(new Intent(CartActivity.this, ProductActivity.class));
            } else if ("ADMIN".equals(role) && item.getItemId() == R.id.nav_category) {
                startActivity(new Intent(CartActivity.this, CategoryActivity.class));
            } else if ("USUARIO".equals(role) && item.getItemId() == R.id.nav_cart) {
                startActivity(new Intent(CartActivity.this, CartActivity.class));
            } else if (item.getItemId() == R.id.nav_profile) {
                startActivity(new Intent(CartActivity.this, ProfileActivity.class));
            } else {
                Toast.makeText(CartActivity.this, "No tienes acceso a esta opción", Toast.LENGTH_SHORT).show();
                return false;
            }
            return true;
        });
    }

    private void adjustMenuOptions(String role) {
        if (!"ADMIN".equals(role)) {
            // Remover las opciones de admin si el rol no es ADMIN
            bottomNavigationView.getMenu().removeItem(R.id.nav_product);
            bottomNavigationView.getMenu().removeItem(R.id.nav_category);
            navigationView.getMenu().removeItem(R.id.nav_product);
            navigationView.getMenu().removeItem(R.id.nav_category);
        }else{
            bottomNavigationView.getMenu().removeItem(R.id.nav_cart);
            navigationView.getMenu().removeItem(R.id.nav_cart);
        }
    }
}