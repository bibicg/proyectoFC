package com.bcg.cartaller;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Desde este fragment el mecánico podrá crear un nuevo cliente cubriendo un formulario
 * con nombre, apellidos, dni, domicilio, teléfono y vehículos a su nombre.
 */
public class ClientsNewFragment extends Fragment {
    private EditText etNombre, etApellidos, etDni, etTlf, etMail, etDireccion;
    private Button btnGuardarCliente, btnModificarCliente;
    private RequestQueue queue;
    private final String SUPABASE_URL = "https://gtiqlopkoiconeivobxa.supabase.co";
    private final String API_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imd0aXFsb3Brb2ljb25laXZvYnhhIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDYxMjMyMTAsImV4cCI6MjA2MTY5OTIxMH0.T5MFUR9KAWXQOnoeZChYXu-FQ9LGClPp1lrSX8q733o";

    public ClientsNewFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_clients_new, container, false);

        etNombre = view.findViewById(R.id.editTextNombre);
        etApellidos = view.findViewById(R.id.editTextApellidos);
        etDni = view.findViewById(R.id.editTextDni);
        etTlf = view.findViewById(R.id.editTextTlf);
        etMail = view.findViewById(R.id.editTextMail);
        etDireccion = view.findViewById(R.id.editTextDirec);
        btnGuardarCliente = view.findViewById(R.id.guardarClienteButton);
        btnModificarCliente = view.findViewById(R.id.modificarDatosButton);


        queue = Volley.newRequestQueue(requireContext());

        btnGuardarCliente.setOnClickListener(v -> {
            btnGuardarCliente.setEnabled(false);

            String dni = etDni.getText().toString().trim();
            String urlCheck = SUPABASE_URL + "/rest/v1/clientes?dni=eq." + Uri.encode(dni);

            JsonArrayRequest checkDni = new JsonArrayRequest(
                    Request.Method.GET,
                    urlCheck,
                    null,
                    response -> {
                        if (response.length() > 0) {
                            Toast.makeText(getContext(), "Ya existe un cliente con ese DNI", Toast.LENGTH_SHORT).show();
                            btnGuardarCliente.setEnabled(true);
                        } else {
                            guardarCliente();
                        }
                    },
                    error -> {
                        Toast.makeText(getContext(), "Error al verificar DNI", Toast.LENGTH_SHORT).show();
                        btnGuardarCliente.setEnabled(true);
                        if (error.networkResponse != null) {
                            Log.e("SUPABASE", "Código: " + error.networkResponse.statusCode);
                            Log.e("SUPABASE", new String(error.networkResponse.data));
                        }
                    }) {
                @Override
                public Map<String, String> getHeaders() {
                    SharedPreferences prefs = requireContext().getSharedPreferences("SupabasePrefs", Context.MODE_PRIVATE);
                    String token = prefs.getString("access_token", "");

                    Map<String, String> headers = new HashMap<>();
                    headers.put("apikey", API_ANON_KEY);
                    headers.put("Authorization", "Bearer " + token);
                    headers.put("Content-Type", "application/json");
                    return headers;
                }
            };


            queue.add(checkDni);
        });

        return view;
    }

    private void guardarCliente() {
        // 1 - extraigo los datos de los campos del formulario
        String nombre = etNombre.getText().toString().trim();
        String apellidos = etApellidos.getText().toString().trim();
        String dni = etDni.getText().toString().trim();
        String telefono = etTlf.getText().toString().trim();
        String email = etMail.getText().toString().trim();
        String direccion = etDireccion.getText().toString().trim();

        // 2 - se comprueba que los obligatorios estén cubiertos
        if (nombre.isEmpty() || apellidos.isEmpty() || dni.isEmpty()) {
            Toast.makeText(getContext(), "Nombre, apellidos y DNI son obligatorios", Toast.LENGTH_SHORT).show();
            btnGuardarCliente.setEnabled(true);
            return;
        }

        // 3 - creo el objeto cliente con todos sus datos
        JSONObject clienteJson = new JSONObject();
        try {
            clienteJson.put("nombre", nombre);
            clienteJson.put("apellidos", apellidos);
            clienteJson.put("dni", dni);
            clienteJson.put("telefono", telefono);
            clienteJson.put("email", email);
            clienteJson.put("direccion", direccion);
        } catch (JSONException e) {
            Toast.makeText(getContext(), "Error preparando datos", Toast.LENGTH_SHORT).show();
            btnGuardarCliente.setEnabled(true);
            return;
        }

        String url = SUPABASE_URL + "/rest/v1/clientes";


        //OPCION CON STRING REQUEST, POR SI NO SE RECIBE UN JSON, PORQUE VOLLEY LO ESPERA
        /**
        StringRequest request = new StringRequest(
                Request.Method.POST,
                url,
                response -> {
                    Toast.makeText(getContext(), "Cliente guardado con éxito", Toast.LENGTH_SHORT).show();
                    btnGuardarCliente.setEnabled(true);
                },
                error -> {
                    Toast.makeText(getContext(), "Error al guardar cliente", Toast.LENGTH_SHORT).show();
                    btnGuardarCliente.setEnabled(true);
                    if (error.networkResponse != null) {
                        Log.e("SUPABASE", "Código: " + error.networkResponse.statusCode);
                        Log.e("SUPABASE", new String(error.networkResponse.data));
                    }
                }
        ) {
            @Override
            public byte[] getBody() {
                return clienteJson.toString().getBytes(StandardCharsets.UTF_8);
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }

            @Override
            public Map<String, String> getHeaders() {
                SharedPreferences prefs = requireContext().getSharedPreferences("SupabasePrefs", Context.MODE_PRIVATE);
                String token = prefs.getString("access_token", "");

                Map<String, String> headers = new HashMap<>();
                headers.put("apikey", API_ANON_KEY);
                headers.put("Authorization", "Bearer " + token);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };*/

/**OPCION CON JSON OBJECT REQUEST:
        // Hay que añadir return=representation en los headers,para que devuelva:
        Status: 201 Created
        Body: [
        {
            "id": 12,
             "dni": "33333333A",
            ...
        }
        ]
         Sino, devuleve un body vacio y volley lo interpreta como error, entonces el cliente
         se guarda en supabase, pero la app no lo muestra
         */

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                clienteJson,
                response -> {

                    Toast.makeText(getContext(), "Cliente guardado con éxito: " + response.optString("dni"), Toast.LENGTH_SHORT).show();
                    btnGuardarCliente.setEnabled(true);
                    //btnModificarCliente.setVisibility(View.VISIBLE);

                    limpiarFormulario();
                },
                error -> {
                    Toast.makeText(getContext(), "Error al guardar cliente", Toast.LENGTH_SHORT).show();
                    btnGuardarCliente.setEnabled(true);
                    if (error.networkResponse != null) {
                        Log.e("SUPABASE", "Código: " + error.networkResponse.statusCode);
                        Log.e("SUPABASE", new String(error.networkResponse.data));
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                SharedPreferences prefs = requireContext().getSharedPreferences("SupabasePrefs", Context.MODE_PRIVATE);
                String token = prefs.getString("access_token", "");

                Map<String, String> headers = new HashMap<>();
                headers.put("apikey", API_ANON_KEY);
                headers.put("Authorization", "Bearer " + token);
                headers.put("Content-Type", "application/json");
                headers.put("Prefer", "return=representation"); // para que funcione el JSONobjectrequest hay que ponerle esto
                return headers;
            }
        };

//me vuelve a dar error con JSONobjectrequest..... no se porqué .....




        queue.add(request);
    }

    private void limpiarFormulario(){
        etNombre.setText("");
        etApellidos.setText("");
        etDni.setText("");
        etTlf.setText("");
        etMail.setText("");
        etDireccion.setText("");

    }
}

