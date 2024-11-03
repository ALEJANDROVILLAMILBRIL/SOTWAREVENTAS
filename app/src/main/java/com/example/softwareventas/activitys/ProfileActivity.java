package com.example.softwareventas.activitys;

import android.content.Intent;
import android.os.Bundle;
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

import com.example.softwareventas.MainActivity;
import com.example.softwareventas.R;
import com.example.softwareventas.adapters.ProductAdapter;
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

public class ProfileActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private BottomNavigationView bottomNavigationView;
    private Toolbar toolbar;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    private String userRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Inicializar FirebaseAuth
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            handleLogout();
            return;
        }
        userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());

        // Inicializar vistas
        drawerLayout = findViewById(R.id.drawer_layout_profile);
        navigationView = findViewById(R.id.nav_view_profile);
        bottomNavigationView = findViewById(R.id.bottom_nav_profile);
        toolbar = findViewById(R.id.toolbar_profile);

        // Configurar Toolbar
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_home); // Asegúrate de que ic_home sea el icono que deseas
        toolbar.setNavigationOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        // Configurar NavigationView
        navigationView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                Intent intent = new Intent(ProfileActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            } else if (item.getItemId() == R.id.nav_profile) {
                Intent intent = new Intent(ProfileActivity.this, ProfileActivity.class);
                startActivity(intent);
                finish();
            } else if (item.getItemId() == R.id.nav_logout) {
                handleLogout();
            }
            drawerLayout.closeDrawer(GravityCompat.START); // Cierra el drawer
            return true;
        });

        bottomNavigationView.setSelectedItemId(R.id.nav_profile);

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
                Toast.makeText(ProfileActivity.this, "Error al cargar el rol del usuario", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleLogout() {
        // Cerrar sesión en Firebase Auth
        mAuth.signOut();

        // Redirigir al LoginActivity
        Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void configureNavigationMenu(String role) {
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                startActivity(new Intent(ProfileActivity.this, HomeActivity.class));
            } else if ("ADMIN".equals(role) && item.getItemId() == R.id.nav_product) {
                startActivity(new Intent(ProfileActivity.this, ProductActivity.class));
            } else if ("ADMIN".equals(role) && item.getItemId() == R.id.nav_category) {
                startActivity(new Intent(ProfileActivity.this, CategoryActivity.class));
            } else if (item.getItemId() == R.id.nav_profile) {
                startActivity(new Intent(ProfileActivity.this, ProfileActivity.class));
            } else {
                Toast.makeText(ProfileActivity.this, "No tienes acceso a esta opción", Toast.LENGTH_SHORT).show();
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
        }
    }
}