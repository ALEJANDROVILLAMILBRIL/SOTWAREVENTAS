package com.example.softwareventas.activitys;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.example.softwareventas.MainActivity;
import com.example.softwareventas.adapters.ProductAdapter;
import com.example.softwareventas.models.Product;
import com.example.softwareventas.models.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import com.example.softwareventas.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private BottomNavigationView bottomNavigationView;
    private Toolbar toolbar;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    private String userRole;
    private RecyclerView productRecyclerView;
    private ProductAdapter productAdapter;
    private List<Product> productList;
    private DatabaseReference productRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Inicializar FirebaseAuth
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            redirectToLogin();
            return;
        }
        userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());
        productRef = FirebaseDatabase.getInstance().getReference("products");

        // Inicializar vistas
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        bottomNavigationView = findViewById(R.id.bottom_nav);
        toolbar = findViewById(R.id.toolbar);
        productRecyclerView = findViewById(R.id.productRecyclerViewHome);

        bottomNavigationView.setSelectedItemId(R.id.nav_home);

        // Configurar Toolbar
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_home); // Asegúrate de que ic_home sea el icono que deseas
        toolbar.setNavigationOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        // Configurar NavigationView
        navigationView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                // Manejar la navegación a Home
                Toast.makeText(HomeActivity.this, "Home", Toast.LENGTH_SHORT).show();
            } else if (item.getItemId() == R.id.nav_profile) {
                Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
                startActivity(intent);
                finish();
            } else if (item.getItemId() == R.id.nav_logout) {
                handleLogout();
            }
            drawerLayout.closeDrawer(GravityCompat.START); // Cierra el drawer
            return true;
        });

        bottomNavigationView.setSelectedItemId(R.id.nav_home);

        productList = new ArrayList<>();
        productRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    userRole = user.getRole();
                    configureNavigationMenu(userRole);
                    adjustMenuOptions(userRole);

                    boolean isUser = "USUARIO".equals(userRole);
                    productAdapter = new ProductAdapter(productList, isUser);
                    productRecyclerView.setAdapter(productAdapter);

                    if (isUser) {
                        loadActiveProducts();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HomeActivity.this, "Error al cargar el rol del usuario", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void configureNavigationMenu(String role) {
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                Toast.makeText(HomeActivity.this, "Home", Toast.LENGTH_SHORT).show();
            } else if ("ADMIN".equals(role) && item.getItemId() == R.id.nav_product) {
                startActivity(new Intent(HomeActivity.this, ProductActivity.class));
            } else if ("ADMIN".equals(role) && item.getItemId() == R.id.nav_category) {
                startActivity(new Intent(HomeActivity.this, CategoryActivity.class));
            } else if (item.getItemId() == R.id.nav_profile) {
                startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
            } else if ("USUARIO".equals(role) && item.getItemId() == R.id.nav_cart) {
                startActivity(new Intent(HomeActivity.this, CartActivity.class));
            } else {
                Toast.makeText(HomeActivity.this, "No tienes acceso a esta opción", Toast.LENGTH_SHORT).show();
                return false;
            }
            return true;
        });
    }

    private void loadActiveProducts() {
        productRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                productList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Product product = dataSnapshot.getValue(Product.class);
                    if (product != null && product.isActive()) {
                        productList.add(product);
                    }
                }
                productAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HomeActivity.this, "Error al cargar productos", Toast.LENGTH_SHORT).show();
            }
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

    private void handleLogout() {
        // Cerrar sesión en Firebase Auth
        mAuth.signOut();
        redirectToLogin();
    }

    private void redirectToLogin() {
        Intent intent = new Intent(HomeActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}