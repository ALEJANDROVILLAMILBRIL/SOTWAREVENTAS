package com.example.softwareventas.activitys;

import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.example.softwareventas.MainActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import com.example.softwareventas.R;

public class HomeActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private BottomNavigationView bottomNavigationView;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Inicializar vistas
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        bottomNavigationView = findViewById(R.id.bottom_nav);
        toolbar = findViewById(R.id.toolbar);

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
                // Manejar la navegación a Profile
                Toast.makeText(HomeActivity.this, "Profile", Toast.LENGTH_SHORT).show();
            } else if (item.getItemId() == R.id.nav_settings) {
                // Manejar la navegación a Settings
                Toast.makeText(HomeActivity.this, "Settings", Toast.LENGTH_SHORT).show();
            } else if (item.getItemId() == R.id.nav_logout) {
                handleLogout();
            }
            drawerLayout.closeDrawer(GravityCompat.START); // Cierra el drawer
            return true;
        });

        // Configurar BottomNavigationView
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                // Manejar la navegación a Home
                Toast.makeText(HomeActivity.this, "Home", Toast.LENGTH_SHORT).show();
            } else if (item.getItemId() == R.id.nav_search) {
                // Manejar la navegación a Search
                Toast.makeText(HomeActivity.this, "Search", Toast.LENGTH_SHORT).show();
            } else if (item.getItemId() == R.id.nav_notifications) {
                // Manejar la navegación a Notifications
                Toast.makeText(HomeActivity.this, "Notifications", Toast.LENGTH_SHORT).show();
            } else if (item.getItemId() == R.id.nav_profile) {
                // Manejar la navegación a Profile
                Toast.makeText(HomeActivity.this, "Profile", Toast.LENGTH_SHORT).show();
            }
            return true;
        });
    }

    private void handleLogout() {
        // Redirigir al LoginActivity
        Intent intent = new Intent(HomeActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish(); // Terminar la actividad actual
    }
}