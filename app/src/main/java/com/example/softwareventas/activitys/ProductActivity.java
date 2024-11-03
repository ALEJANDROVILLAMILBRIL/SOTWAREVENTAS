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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.example.softwareventas.MainActivity;
import com.example.softwareventas.R;
import com.example.softwareventas.adapters.ProductAdapter;
import com.example.softwareventas.models.Category;
import com.example.softwareventas.models.Product;
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

public class ProductActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private BottomNavigationView bottomNavigationView;
    private Toolbar toolbar;
    private FirebaseAuth mAuth;


    // UI Elements
    private Spinner categorySpinner;
    private EditText productNameEditText, productPriceEditText, productStockEditText, productDiscountEditText;
    private Switch productActiveSwitch;
    private Button addProductButton;
    private RecyclerView productRecyclerView;

    // Firebase Database References
    private DatabaseReference categoryRef, productRef;
    private List<Category> categoryList;
    private List<Product> productList;
    private ProductAdapter productAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);

        // Inicializar FirebaseAuth
        mAuth = FirebaseAuth.getInstance();
        categoryRef = FirebaseDatabase.getInstance().getReference("categories");
        productRef = FirebaseDatabase.getInstance().getReference("products");

        // Inicializar vistas
        drawerLayout = findViewById(R.id.drawer_layout_product);
        navigationView = findViewById(R.id.nav_view_product);
        bottomNavigationView = findViewById(R.id.bottom_nav_product);
        toolbar = findViewById(R.id.toolbar_product);

        categorySpinner = findViewById(R.id.categorySpinner);
        productNameEditText = findViewById(R.id.productNameEditText);
        productPriceEditText = findViewById(R.id.productPriceEditText);
        productStockEditText = findViewById(R.id.productStockEditText);
        productDiscountEditText = findViewById(R.id.productDiscountEditText);
        productActiveSwitch = findViewById(R.id.productActiveSwitch);
        addProductButton = findViewById(R.id.addProductButton);
        productRecyclerView = findViewById(R.id.productRecyclerView);

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

        productList = new ArrayList<>();
        productAdapter = new ProductAdapter(productList);
        productRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        productRecyclerView.setAdapter(productAdapter);

        // Load Categories into Spinner
        loadCategories();

        // Load Products into RecyclerView
        loadProducts();

        // Add Product Button Listener
        addProductButton.setOnClickListener(v -> addProductToFirebase());
    }

    private void loadCategories() {
        categoryList = new ArrayList<>();
        ArrayAdapter<Category> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoryList);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);

        // Cargar categorías desde Firebase
        categoryRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryList.clear(); // Limpia la lista antes de agregar nuevas categorías
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Category category = dataSnapshot.getValue(Category.class);
                    if (category != null && category.isActive()) {
                        categoryList.add(category); // Agrega solo categorías activas
                    }
                }
                categoryAdapter.notifyDataSetChanged(); // Actualiza el adaptador después de cambiar la lista
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProductActivity.this, "Error al cargar categorías", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadProducts() {
        productRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                productList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Product product = dataSnapshot.getValue(Product.class);
                    productList.add(product);
                }
                productAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProductActivity.this, "Error al cargar productos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addProductToFirebase() {
        String productName = productNameEditText.getText().toString().trim();
        String priceString = productPriceEditText.getText().toString().trim();
        String stockString = productStockEditText.getText().toString().trim();
        String discountString = productDiscountEditText.getText().toString().trim();
        boolean isActive = productActiveSwitch.isChecked();
        Category selectedCategory = (Category) categorySpinner.getSelectedItem();

        if (productName.isEmpty() || priceString.isEmpty() || stockString.isEmpty() || selectedCategory == null) {
            Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        double price = Double.parseDouble(priceString);
        int stock = Integer.parseInt(stockString);
        double discount = discountString.isEmpty() ? 0 : Double.parseDouble(discountString);
        long currentTime = System.currentTimeMillis();

        String productId = productRef.push().getKey();
        Product product = new Product(productId, productName, selectedCategory.getId(), currentTime, currentTime, isActive, stock, price, discount);

        productRef.child(productId).setValue(product).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Producto agregado exitosamente", Toast.LENGTH_SHORT).show();
                productNameEditText.setText("");
                productPriceEditText.setText("");
                productStockEditText.setText("");
                productDiscountEditText.setText("");
                productActiveSwitch.setChecked(false);
            } else {
                Toast.makeText(this, "Error al agregar el producto", Toast.LENGTH_SHORT).show();
            }
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