package com.bcg.cartaller;


import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

/**
 * El usuario de la app (mecánicos del taller) deben loguearse en la app con
 * su cuenta de correo corporativa y una contraseña
 *
 * Hago las primeras pruebas (aun no hay autenticacion con supabase) con:
 * bibi@gmail.com
 * 654321
 *
 * Después usaré el mecanico de prueba (tester) ya creado para esto:
 * prueba@demo.com
 * 123456
 */

public class LoginActivity extends AppCompatActivity {
    private EditText etMail, etPassword;
    private Button btnRegister, btnLogin;
    private RequestQueue queue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        etMail = findViewById(R.id.editTextMail);
        etPassword = findViewById(R.id.editTextPassword);

        btnRegister = findViewById(R.id.buttonRegister);
        btnRegister.setOnClickListener(v -> registerUser());

        btnLogin = findViewById(R.id.buttonLogin);
        btnLogin.setOnClickListener(v -> loginUser());

        queue = Volley.newRequestQueue(this); // inicializo la cola de peticiones
    }

    /**
     * SE EJECUTA SIEMPRE PARA ENTRAR
     * Método para registrarte en la app después de haberte registrado
     * Quizás le cambio el nombre a ENTRAR
     */
    public void loginUser() {
        String email = etMail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Debes introducir email y contraseña.", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = "https://gtiqlopkoiconeivobxa.supabase.co/auth/v1/token";

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("email", email);
            jsonBody.put("password", password);
            jsonBody.put("grant_type", "password");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                jsonBody,
                response -> {
                    Log.d("SUPABASE", "Login correcto: " + response.toString());

                    try {
                        String accessToken = response.getString("access_token");
                        String userId = response.getJSONObject("user").getString("id");

                        Log.d("SUPABASE", "Access token: " + accessToken);
                        Log.d("SUPABASE", "User ID: " + userId);

                        // Guardar token???? comprobar cómo se guarda en supabase
                        // por si también usa SharedPreferences o alguna otra cosa

                        Toast.makeText(this, "Te has logueado correctamente", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    Log.e("SUPABASE", "Error al iniciar sesión: " + error.toString());

                    if (error.networkResponse != null) {
                        int statusCode = error.networkResponse.statusCode;
                        String body = new String(error.networkResponse.data);
                        Log.e("SUPABASE", "Código HTTP: " + statusCode);
                        Log.e("SUPABASE", "Respuesta Supabase: " + body);
                    }

                    Toast.makeText(this, "Error al iniciar sesión: " + error.toString(), Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                // Api Key anon de mi ProyectoFC en Supabase:
                headers.put("apikey", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imd0aXFsb3Brb2ljb25laXZvYnhhIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDYxMjMyMTAsImV4cCI6MjA2MTY5OTIxMH0.T5MFUR9KAWXQOnoeZChYXu-FQ9LGClPp1lrSX8q733o");
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        queue.add(request);
    }



    /**
     * SOLO SE EJECUTA UNA VEZ POR USUARIO (REGISTRO PRIMERA VEZ)
     */
    public void registerUser() {
        String email = etMail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Debes introducir email y contraseña.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Si introduce los dos datos requeridos, se ejecuta el metodo para registrarse en la app por primera vez:
        registerUserInSupabase(email, password);

    }

    /**
     * @param email
     * @param password
     * Los usuarios de la app están metidos en la BD de Supabase (mecánicos) pero no están
     * registrados. Se registran con este método al pulsar el botón, que se comunica con la url
     * del proyecto en Supabase
     *
     * SOLO SE EJECUTA LA PRIMERA VEZ QUE TE DAS DE ALTA EN LA APP
     */
    private void registerUserInSupabase(String email, String password) {
        //URL de la API de mi ProyectoFC en Supabase:
        String url = "https://gtiqlopkoiconeivobxa.supabase.co/auth/v1/signup";

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("email", email);
            jsonBody.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                jsonBody,
                response -> {
                    Log.d("SUPABASE", "Usuario registrado: " + response.toString());

                    try {
                        //String userId = response.getString("id");
                        String userId = response.getJSONObject("user").getString("id");


                        Log.d("SUPABASE", "ID del usuario: " + userId);

                        Toast.makeText(this, "Te has registrado correctamente. Usa la app para gestionar tu trabajo.", Toast.LENGTH_SHORT).show();

                        // Como todo ha ido ok, se lanza el main ACTIVITY (no es necesario que se loguee después de haberse registrado):
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();

                    } catch (JSONException e) {
                        Log.e("SUPABASE", "Error al parsear el usuario: " + e.getMessage());
                    }
                },
                error -> {
                    Log.e("SUPABASE", "Error de registro: " + error.toString());
                    if (error.networkResponse != null) {
                        int statusCode = error.networkResponse.statusCode;
                        String body = new String(error.networkResponse.data);
                        Log.e("SUPABASE", "Código HTTP: " + statusCode);
                        Log.e("SUPABASE", "Respuesta Supabase: " + body);

                        if (statusCode == 400 && body.contains("User already registered")) {
                            Toast.makeText(this, "Este correo ya está registrado. Prueba a iniciar sesión.", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(this, "Error al registrarte.", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(this, "Fallo de red al registrarte.", Toast.LENGTH_SHORT).show();
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                // Api Key anon de mi ProyectoFC en Supabase:
                headers.put("apikey", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imd0aXFsb3Brb2ljb25laXZvYnhhIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDYxMjMyMTAsImV4cCI6MjA2MTY5OTIxMH0.T5MFUR9KAWXQOnoeZChYXu-FQ9LGClPp1lrSX8q733o");
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        queue.add(request);
    }


}
