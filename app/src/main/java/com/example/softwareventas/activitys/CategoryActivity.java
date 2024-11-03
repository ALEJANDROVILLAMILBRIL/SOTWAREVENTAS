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
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.example.softwareventas.MainActivity;
import com.example.softwareventas.R;
import com.example.softwareventas.adapters.CategoryAdapter;
import com.example.softwareventas.models.Category;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CategoryActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private BottomNavigationView bottomNavigationView;
    private Toolbar toolbar;
    private FirebaseAuth mAuth;
    private RecyclerView categoryRecyclerView;
    private EditText categoryNameEditText;
    private Switch categoryActiveSwitch;
    private Button addCategoryButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        // Inicializar FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        // Formularios y botones
        categoryNameEditText = findViewById(R.id.categoryNameEditText);
        categoryActiveSwitch = findViewById(R.id.categoryActiveSwitch);
        addCategoryButton = findViewById(R.id.addCategoryButton);

        // Inicializar vistas
        drawerLayout = findViewById(R.id.drawer_layout_category);
        navigationView = findViewById(R.id.nav_view_category);
        bottomNavigationView = findViewById(R.id.bottom_nav_category);
        toolbar = findViewById(R.id.toolbar_category);

        // Configurar Toolbar
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_home); // Asegúrate de que ic_home sea el icono que deseas
        toolbar.setNavigationOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        // Configurar NavigationView
        navigationView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                Intent intent = new Intent(CategoryActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            } else if (item.getItemId() == R.id.nav_profile) {
                // Manejar la navegación a Profile
                Intent intent = new Intent(CategoryActivity.this, ProfileActivity.class);
                startActivity(intent);
                finish();
            } else if (item.getItemId() == R.id.nav_logout) {
                handleLogout();
            }
            drawerLayout.closeDrawer(GravityCompat.START); // Cierra el drawer
            return true;
        });

        bottomNavigationView.setSelectedItemId(R.id.nav_category);

        // Configurar BottomNavigationView
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                // Navegar a HomeActivity
                Intent intent = new Intent(CategoryActivity.this, HomeActivity.class);
                startActivity(intent);
            } else if (item.getItemId() == R.id.nav_product) {
                // Navegar a ProductActivity
                Intent intent = new Intent(CategoryActivity.this, ProductActivity.class);
                startActivity(intent);
            } else if (item.getItemId() == R.id.nav_category) {
                // Navegar a CategoryActivity
                Intent intent = new Intent(CategoryActivity.this, CategoryActivity.class);
                startActivity(intent);
            } else if (item.getItemId() == R.id.nav_profile) {
                // Navegar a Profile
                Intent intent = new Intent(CategoryActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
            return true;
        });

        addCategoryButton.setOnClickListener(v -> {
            addCategoryToFirebase();
        });

        // RecyclerView configuración
        categoryRecyclerView = findViewById(R.id.categoryRecyclerView);
        categoryRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<Category> categoryList = new ArrayList<>();
        CategoryAdapter adapter = new CategoryAdapter(categoryList);
        categoryRecyclerView.setAdapter(adapter);

        // Cargar categorías desde Firebase
        DatabaseReference categoryRef = FirebaseDatabase.getInstance().getReference("categories");
        categoryRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryList.clear();
                for (DataSnapshot categorySnapshot : snapshot.getChildren()) {
                    Category category = categorySnapshot.getValue(Category.class);
                    categoryList.add(category);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CategoryActivity.this, "Error al cargar categorías", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleLogout() {
        // Cerrar sesión en Firebase Auth
        mAuth.signOut();

        // Redirigir al LoginActivity
        Intent intent = new Intent(CategoryActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void addCategoryToFirebase() {
        String categoryName = categoryNameEditText.getText().toString().trim();
        boolean isActive = categoryActiveSwitch.isChecked();

        if (!categoryName.isEmpty()) {
            DatabaseReference categoryRef = FirebaseDatabase.getInstance().getReference("categories");
            String categoryId = categoryRef.push().getKey(); // Genera un ID único

            long currentTime = System.currentTimeMillis();
            Category category = new Category(categoryId, categoryName, isActive, currentTime, currentTime);

            categoryRef.child(categoryId).setValue(category).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Categoría agregada exitosamente", Toast.LENGTH_SHORT).show();
                    categoryNameEditText.setText(""); // Limpiar el campo de entrada
                    categoryActiveSwitch.setChecked(false); // Restablecer el switch
                } else {
                    Toast.makeText(this, "Error al agregar categoría", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "Introduce un nombre para la categoría", Toast.LENGTH_SHORT).show();
        }
    }
}