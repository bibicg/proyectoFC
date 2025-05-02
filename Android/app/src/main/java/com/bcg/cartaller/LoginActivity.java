package com.bcg.cartaller;

import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

/**
 * El usuario de la app (mecánicos del taller) deben loguearse en la app con
 * su cuenta de correo corporativa y una contraseña
 *
 * Hago las primeras pruebas (aun no hay autenticacion con supabase) con:
 * bibi@gmail.com
 * 654321
 *
 * Después usaré el mecanico de prueba (tester) ya creado para esto.
 */

public class LoginActivity extends AppCompatActivity {
    private EditText etMail, etPassword;
    private Button btnLogin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        etMail = findViewById(R.id.editTextMail);
        etPassword = findViewById(R.id.editTextPassword);

        btnLogin = findViewById(R.id.buttonLogin);
        btnLogin.setOnClickListener(v -> loginUser());
    }

    private void loginUser() {
        String email = etMail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Debes introducir email y contraseña.", Toast.LENGTH_SHORT).show();
            return;
        }
        // Si introduce los dos datos requeridos, se abre el main:
        Toast.makeText(this, "Inicio de sesión exitoso.", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();

    }
}
