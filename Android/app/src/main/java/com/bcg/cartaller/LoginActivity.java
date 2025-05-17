package com.bcg.cartaller;

import android.content.SharedPreferences;
import android.net.Uri;
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
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;


/**
 * El usuario de la app (mecánicos del taller) debe loguearse en la app con
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

// NO CONSIGO QUE ME FUNCIONE EL AUTH,ES UN PROBLEMA DE LO QUE ESPERA SUPABASE
// Y LO QUE RECIBE. AUNQUE INTENTO PARSEARLO NO HAY MANERA.
// ASI QUE ME LOGUEO CON EL MAIL Y ES LO QUE USARÉ
public class LoginActivity extends AppCompatActivity {
    private EditText etMail, etPassword;
    private Button btnRegister, btnLogin;
    private RequestQueue queue;
    //Añado las url a una variable para no tener que estar copiando todo el codigo contantemente:
    private final String SUPABASE_URL = "https://gtiqlopkoiconeivobxa.supabase.co";
    private final String API_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imd0aXFsb3Brb2ljb25laXZvYnhhIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDYxMjMyMTAsImV4cCI6MjA2MTY5OTIxMH0.T5MFUR9KAWXQOnoeZChYXu-FQ9LGClPp1lrSX8q733o";


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
     * SOLO SE EJECUTA UNA VEZ POR USUARIO (CUANDO TE DAS DE ALTA EN LA APP)
     * Los usuarios de la app están metidos en la BD de Supabase (mecánicos) pero no están
     * registrados. Se registran con este método al pulsar el botón, que se comunica con la url
     * del proyecto en Supabase
     */
    public void registerUser() {
        String email = etMail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Debes introducir email y contraseña.", Toast.LENGTH_SHORT).show();
            return;
        }

        // url de supabase para registrarse. Esto hace que los usuarios registrados
        // pasen a formar parte de Autenticación (sino, están en la BD solamente):
        // (RESUMEN: crea el UID en la tabla auth.users)
        String url = SUPABASE_URL + "/auth/v1/signup";

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("email", email);
            jsonBody.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error con los datos", Toast.LENGTH_SHORT).show();
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                jsonBody,
                response -> {
                    Log.d("SUPABASE", "Usuario registrado: " + response.toString());
                    try {
                        String accessToken = response.getString("access_token");
                        String userUid = response.getJSONObject("user").getString("id"); // es UID de auth.users

                        // no me está funcionando bien, necesito guardarlo en el login igualmente para que funcione
                        SharedPreferences prefs = getSharedPreferences("SupabasePrefs", MODE_PRIVATE);
                        prefs.edit()
                                .putString("access_token", accessToken)
                                .putString("user_id", userUid)
                                .apply();

                        Toast.makeText(this, "Enhorabuena, te has registrado correctamente!!", Toast.LENGTH_SHORT).show();

                        insertUIDinMecanicos();

                        Toast.makeText(this, "INICIA SESIÓN (PULSA EL BOTÓN Y ACCEDE A LA APP)", Toast.LENGTH_SHORT).show();
                        loginUser(); //fuerzo que vaya al login porque sino queda raro
                        /**
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();*/

                    } catch (JSONException e) {
                        Toast.makeText(this, "Error en el registro", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    if (error.networkResponse != null) {
                        String body = new String(error.networkResponse.data);
                        Log.e("SUPABASE", "Código HTTP: " + error.networkResponse.statusCode);
                        Log.e("SUPABASE", "Respuesta Supabase: " + body);
                        if (body.contains("User already registered")) {
                            Toast.makeText(this, "Este correo ya está registrado.", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(this, "Error al registrarte: " + body, Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(this, "Fallo de red al registrarte.", Toast.LENGTH_SHORT).show();
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("apikey", API_ANON_KEY);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        queue.add(request);
    }

    // Necesito que el UID que se ha creado en la tabla auth.users de Supabase,
    // se inserte en la tabla mecanicos para que funciones la autenticacion

    public void insertUIDinMecanicos(){
        SharedPreferences prefs = getSharedPreferences("SupabasePrefs", MODE_PRIVATE);
        String userUid = prefs.getString("user_id", ""); // recupero el UID para poder guardarlo en tabla mecanicos
        String accessToken = prefs.getString("access_token", "");

        //String urlMecanicosUpdate = SUPABASE_URL + "/rest/v1/mecanicos";
        String urlMecanicosUpdate = SUPABASE_URL + "/rest/v1/mecanicos?email=eq." + Uri.encode(etMail.getText().toString().trim());

        JSONObject updateMecanico = new JSONObject();
        try {
            updateMecanico.put("auth_id", userUid);
        } catch (JSONException e) {
            Toast.makeText(this, "Error al preparar el UID.", Toast.LENGTH_SHORT).show();
            return;
        }

        JsonArrayRequest requestUpdateMecanico = new JsonArrayRequest(
                Request.Method.PATCH,
                urlMecanicosUpdate,
                new JSONArray().put(updateMecanico),
                response -> {
                    Log.d("SUPABASE", "Mecánico actualizado");
                    Toast.makeText(this, "Perfil enlazado correctamente.", Toast.LENGTH_SHORT).show();

                    //loginUser();
                },
                error -> {
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        String body = new String(error.networkResponse.data);
                        Log.e("SUPABASE", "Error al actualizar mecánico - Respuesta: " + body);
                        Toast.makeText(this, "Error al enlazar tu perfil.", Toast.LENGTH_LONG).show(); // muestra el error real si hay respuesta
                    } else {
                        Log.w("SUPABASE", "Actualización de auth_id exitosa (sin respuesta del servidor), pero Volley reportó error.");
                        //POR AHORA, NO MOSTRAR ESTE TOAST PQ SIEMPRE SALE AUNQUE TODO FUNCIONA BIEN:
                        //Toast.makeText(this, "Error al enlazar tu perfil.", Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("apikey", API_ANON_KEY);
                headers.put("Authorization", "Bearer " + accessToken);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        queue.add(requestUpdateMecanico);
    }


    /**
     * SE EJECUTA SIEMPRE PARA ENTRAR
     * Método para entrar en la app después de haberte registrado
     * Quizás le cambio el nombre a ENTRAR
     */

    public void loginUser() {
        String email = etMail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        Log.d("LOGIN", "Iniciando loginUser con email: " + email);

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Debes introducir email y contraseña.", Toast.LENGTH_SHORT).show();
            Log.w("LOGIN", "Email o contraseña vacíos.");
            return;
        }

        //url de supabase para loguerase con token:
        String url = SUPABASE_URL + "/auth/v1/token?grant_type=password";
        Log.d("LOGIN", "URL de login: " + url);

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("email", email);
            jsonBody.put("password", password);
            Log.d("LOGIN", "Cuerpo JSON de login: " + jsonBody.toString());
        } catch (JSONException e) {
            Log.e("SUPABASE", "JSON error: " + e.getMessage());
            Log.e("LOGIN", "Error al crear cuerpo JSON de login: " + e.getMessage());
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                jsonBody,
                response -> {
                    Log.d("LOGIN", "Respuesta de login exitosa: " + response.toString());
                    try {
                        String accessToken = response.getString("access_token");
                        Log.d("LOGIN", "Token de acceso obtenido: " + accessToken);


                        // guardo el tokken, porque :
                        SharedPreferences authPrefs = getSharedPreferences("SupabasePrefs", MODE_PRIVATE);
                        authPrefs.edit().putString("access_token", accessToken).apply();
                        Log.d("LOGIN", "Token de acceso guardado en SharedPreferences.");


                        // llamada a metodo
                        Log.d("LOGIN", "Llamando a obtenerMecanicoPorEmail con token: " + accessToken + " y email: " + email);
                        obtenerMecanicoPorEmail(accessToken, email);


                    } catch (JSONException e) {
                        Log.e("SUPABASE", "Error de login: " + e.getMessage());
                        Log.e("LOGIN", "Error al procesar respuesta de login: " + e.getMessage());
                    }
                },
                error -> {
                    Log.e("LOGIN", "Error al iniciar sesión: " + error.toString());
                    if (error.networkResponse != null) {
                        int statusCode = error.networkResponse.statusCode;
                        String body = new String(error.networkResponse.data);
                        Log.e("LOGIN", "Código HTTP de error: " + statusCode);
                        Log.e("LOGIN", "Cuerpo de error de Supabase: " + body);
                    }
                    Toast.makeText(this, "Error al iniciar sesión", Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                //api key anon:
                headers.put("apikey", API_ANON_KEY);
                headers.put("Content-Type", "application/json");
                Log.d("LOGIN", "Encabezados de la petición de login: " + headers.toString());
                return headers;
            }
        };

        queue.add(request);
    }

    //Este metodo es usado por loginUser:
    private void obtenerMecanicoPorEmail(String token, String email) {
        String url = SUPABASE_URL + "/rest/v1/mecanicos?select=id&email=eq." + Uri.encode(email);
        Log.d("OBTENER_MECANICO", "URL para obtener mecánico: " + url);
        Log.d("OBTENER_MECANICO", "Token usado: " + token);


        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    Log.d("OBTENER_MECANICO", "Respuesta para obtener mecánico: " + response.toString());

                    try {
                        if (response.length() > 0) {
                            String mecanicoId = response.getJSONObject(0).getString("id");
                            Log.d("OBTENER_MECANICO", "ID del mecánico obtenido: " + mecanicoId);
                            SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
                            prefs.edit().putString("mecanico_id", mecanicoId).apply();
                            Log.d("OBTENER_MECANICO", "ID del mecánico guardado en SharedPreferences.");

                            Toast.makeText(this, "Login correcto", Toast.LENGTH_SHORT).show();

                            //Como el login se ha hecho correctamente, se inicia el main activity:
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                            Log.d("OBTENER_MECANICO", "Inicio de MainActivity.");

                        } else {
                            Toast.makeText(this, "No se encontró mecánico con ese email.", Toast.LENGTH_SHORT).show();
                            Log.w("OBTENER_MECANICO", "No se encontró mecánico con el email: " + email);
                        }
                    } catch (JSONException e) {
                        Toast.makeText(this, "Error al procesar datos del mecánico.", Toast.LENGTH_SHORT).show();
                        Log.e("OBTENER_MECANICO", "Error JSON al procesar datos del mecánico: " + e.getMessage());
                    }
                },
                error -> {
                    Log.e("OBTENER_MECANICO", "Error al obtener datos del mecánico: " + error.toString());
                    if (error.networkResponse != null) {
                        int statusCode = error.networkResponse.statusCode;
                        String body = new String(error.networkResponse.data);
                        Log.e("OBTENER_MECANICO", "Código HTTP de error: " + statusCode);
                        Log.e("OBTENER_MECANICO", "Cuerpo de error de Supabase: " + body);
                    }
                    Toast.makeText(this, "Error al obtener datos del mecánico.", Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("apikey", API_ANON_KEY);
                headers.put("Authorization", "Bearer " + token);
                Log.d("OBTENER_MECANICO", "Encabezados de la petición para obtener mecánico: " + headers.toString());
                return headers;
            }
        };

        queue.add(request);
    }

}





















