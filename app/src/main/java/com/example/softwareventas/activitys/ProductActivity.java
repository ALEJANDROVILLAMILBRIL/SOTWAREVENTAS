package com.example.softwareventas.activitys;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.example.softwareventas.MainActivity;
import com.example.softwareventas.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class ProductActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private BottomNavigationView bottomNavigationView;
    private Toolbar toolbar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);

        // Inicializar FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        // Inicializar vistas
        drawerLayout = findViewById(R.id.drawer_layout_product);
        navigationView = findViewById(R.id.nav_view_product);
        bottomNavigationView = findViewById(R.id.bottom_nav_product);
        toolbar = findViewById(R.id.toolbar_product);

        // Configurar Toolbar
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_home); // Asegúrate de que ic_home sea el icono que deseas
        toolbar.setNavigationOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        // Configurar NavigationView
        navigationView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                Intent intent = new Intent(ProductActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            } else if (item.getItemId() == R.id.nav_profile) {
                // Manejar la navegación a Profile
                Toast.makeText(ProductActivity.this, "Profile", Toast.LENGTH_SHORT).show();
            } else if (item.getItemId() == R.id.nav_settings) {
                // Manejar la navegación a Settings
                Toast.makeText(ProductActivity.this, "Settings", Toast.LENGTH_SHORT).show();
            } else if (item.getItemId() == R.id.nav_logout) {
                handleLogout();
            }
            drawerLayout.closeDrawer(GravityCompat.START); // Cierra el drawer
            return true;
        });

        bottomNavigationView.setSelectedItemId(R.id.nav_product);

        // Configurar BottomNavigationView
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                // Navegar a HomeActivity
                Intent intent = new Intent(ProductActivity.this, HomeActivity.class);
                startActivity(intent);
            } else if (item.getItemId() == R.id.nav_product) {
                // Navegar a ProductActivity
                Intent intent = new Intent(ProductActivity.this, ProductActivity.class);
                startActivity(intent);
            } else if (item.getItemId() == R.id.nav_category) {
                // Navegar a CategoryActivity
                Intent intent = new Intent(ProductActivity.this, CategoryActivity.class);
                startActivity(intent);
            } else if (item.getItemId() == R.id.nav_profile) {
                // Manejar la navegación a Profile
                Toast.makeText(ProductActivity.this, "Profile", Toast.LENGTH_SHORT).show();
            }
            return true;
        });
    }

    private void handleLogout() {
        // Cerrar sesión en Firebase Auth
        mAuth.signOut();

        // Redirigir al LoginActivity
        Intent intent = new Intent(ProductActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}