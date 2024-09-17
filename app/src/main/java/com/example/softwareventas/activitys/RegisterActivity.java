package com.example.softwareventas.activitys;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.softwareventas.models.User;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.example.softwareventas.R;

public class RegisterActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;

    private EditText usernameEditText, passwordEditText, emailEditText, addressEditText, phoneEditText, birthdateEditText;
    private Button registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

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

        String safeEmail = email.replace(".", ",");

        // Crear un nuevo usuario
        User user = new User(username, password, email, address, phone, birthdate);

        // Guardar el usuario en Firebase Realtime Database
        mDatabase.child("users").child(safeEmail).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().exists()) {
                    // El usuario ya existe
                    Toast.makeText(RegisterActivity.this, "Este usuario ya está registrado.", Toast.LENGTH_SHORT).show();
                } else {
                    // El usuario no existe, lo creamos
                    mDatabase.child("users").child(safeEmail).setValue(user).addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            Toast.makeText(RegisterActivity.this, "Usuario registrado con éxito.", Toast.LENGTH_SHORT).show();
                            // Redirigir a HomeActivity
                            Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
                            startActivity(intent);
                            finish(); // Opcional: cerrar RegisterActivity para que no se pueda volver a esta actividad con el botón de retroceso
                        } else {
                            Toast.makeText(RegisterActivity.this, "Error al registrar el usuario.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } else {
                Toast.makeText(RegisterActivity.this, "Error al registrar el usuario.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}