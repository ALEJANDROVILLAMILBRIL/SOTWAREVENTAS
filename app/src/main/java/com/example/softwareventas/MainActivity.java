package com.example.softwareventas;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.softwareventas.activitys.HomeActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import com.example.softwareventas.activitys.RegisterActivity;

public class MainActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;

    // Declarar las vistas
    private EditText emailEditText, passwordEditText;
    private Button loginButton;
    private TextView registerPrompt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Vincular las vistas con el diseño XML
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        registerPrompt = findViewById(R.id.registerPrompt);

        // Configurar el botón de inicio de sesión
        loginButton.setOnClickListener(v -> loginUser());

        // Configurar el botón de registro
        registerPrompt.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void loginUser() {
        // Obtener los datos de los campos
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // Validar que los campos no estén vacíos
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor, completa todos los campos.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Convertir el correo a una clave única (reemplazar caracteres no válidos)
        String safeEmail = email.replace(".", ",");

        // Verificar las credenciales en Firebase Realtime Database
        mDatabase.child("users").child(safeEmail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // El usuario existe, verificar la contraseña
                    String storedPassword = dataSnapshot.child("password").getValue(String.class);
                    if (storedPassword != null && storedPassword.equals(password)) {
                        // Contraseña correcta, redirigir a HomeActivity
                        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                        startActivity(intent);
                        finish(); // Opcional: cerrar MainActivity para que no se pueda volver a esta actividad con el botón de retroceso
                    } else {
                        Toast.makeText(MainActivity.this, "Correo o contraseña incorrectos.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Usuario no encontrado.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Error de base de datos.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}