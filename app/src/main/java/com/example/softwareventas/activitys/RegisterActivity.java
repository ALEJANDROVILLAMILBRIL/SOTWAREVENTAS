package com.example.softwareventas.activitys;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.softwareventas.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.example.softwareventas.R;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private EditText usernameEditText, passwordEditText, emailEditText, addressEditText, phoneEditText, birthdateEditText;
    private Button registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Vincular las vistas con el diseño XML
        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        emailEditText = findViewById(R.id.emailEditText);
        addressEditText = findViewById(R.id.addressEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        birthdateEditText = findViewById(R.id.birthdateEditText);
        registerButton = findViewById(R.id.registerButton);

        // Boton para regresar a inicio de sesion
        ImageButton loginPrompt = findViewById(R.id.backButton);
        loginPrompt.setOnClickListener(v -> finish());

        // Configurar el botón de registro
        registerButton.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        // Obtener los datos de los campos
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String address = addressEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        String birthdate = birthdateEditText.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty() || email.isEmpty() || address.isEmpty() || phone.isEmpty() || birthdate.isEmpty()) {
            Toast.makeText(this, "Por favor, completa todos los campos.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Crear usuario en Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                // Registro exitoso, obtenemos el usuario actual
                FirebaseUser firebaseUser = mAuth.getCurrentUser();
                if (firebaseUser != null) {
                    String userId = firebaseUser.getUid();

                    // Crear un nuevo usuario con información adicional en Realtime Database
                    User user = new User(username, email, address, phone, birthdate, "USUARIO");
                    mDatabase.child("users").child(userId).setValue(user).addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            Toast.makeText(RegisterActivity.this, "Usuario registrado con éxito.", Toast.LENGTH_SHORT).show();
                            // Redirigir a HomeActivity
                            Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(RegisterActivity.this, "Error al guardar datos de usuario.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } else {
                Toast.makeText(RegisterActivity.this, "Error al registrar el usuario: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}