package com.bcg.cartaller;

import androidx.fragment.app.Fragment;
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
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class CarNewFragment extends Fragment {
    private RequestQueue queue;
    private EditText etMatricula, etMarca, etModelo;
    private Button btnGuardar;
    private final String SUPABASE_URL = "https://gtiqlopkoiconeivobxa.supabase.co";
    private final String API_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imd0aXFsb3Brb2ljb25laXZvYnhhIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDYxMjMyMTAsImV4cCI6MjA2MTY5OTIxMH0.T5MFUR9KAWXQOnoeZChYXu-FQ9LGClPp1lrSX8q733o";

    public CarNewFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_car_new, container, false);

        etMatricula = view.findViewById(R.id.editTextMatricula);
        etMarca = view.findViewById(R.id.editTextMarca);
        etModelo = view.findViewById(R.id.editTextModelo);
        btnGuardar = view.findViewById(R.id.guardarVehiculoButton);

        queue = Volley.newRequestQueue(requireContext());

        /**
         * El usuario busca el cliente por su dni, pero en supabase la relación entre las tablas
         * de los vehículos y de los clientes a los que pertenecen, se hace mediante el id del cliente.
         * QUIZÁS DEBERÍA HABERLO HECHO POR DNI, PORQUE ME ESTÁ DANDO ERRORES!!!!!!!
         */
        //String dni = getArguments() != null ? getArguments().getString("cliente_dni") : null;
        int clienteId = getArguments() != null ? getArguments().getInt("cliente_id", -1) : -1;


        btnGuardar.setOnClickListener(v -> {
            String matricula = etMatricula.getText().toString().trim();
            String marca = etMarca.getText().toString().trim();
            String modelo = etModelo.getText().toString().trim();

            /**
            if (matricula.isEmpty() || marca.isEmpty() || modelo.isEmpty() || dni == null) {
                Toast.makeText(getContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }*/
            if (matricula.isEmpty() || marca.isEmpty() || modelo.isEmpty() || clienteId == -1) {
                Toast.makeText(getContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            //guardarVehiculo(matricula, marca, modelo, dni);
            guardarVehiculo(matricula, marca, modelo, clienteId);
        });

        return view;
    }

    //private void guardarVehiculo(String matricula, String marca, String modelo, String dni) {
    private void guardarVehiculo(String matricula, String marca, String modelo, int clienteId){

        String url = SUPABASE_URL + "/rest/v1/vehiculos";

        Log.d("SUPABASE", "Guardando vehículo para cliente ID: " + clienteId);

        JSONObject vehiculoJson = new JSONObject();
        try {
            vehiculoJson.put("matricula", matricula);
            vehiculoJson.put("marca", marca);
            vehiculoJson.put("modelo", modelo);
            //vehiculoJson.put("cliente_dni", dni); // No está usando el dni realmente
            vehiculoJson.put("cliente_id", clienteId); // por eso cambio al id, que es lo que se usa

        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        /**
         * IMPORTANTE PARA TODAS LAS PETICIONES POST (insert en la BD):
         * JsonObjectRequest no funciona, en principio, porque supabase devuelve siempre un array aunque se
         * introduzca solo un objeto. Pero tampoco funciona JsonArrayRequest (porque Supabase no está
         * devolviendo nada) y JsonArrayRequest espera parsear un json
         * REALMENTE SÍ QUE FUNCIONAN PORQUE EL COHCE ES INTRODUCIDO EN LA TABLA DE SUPABASE, PERO LA APP
         * MUESTRA UN TOAST DE ERROR PORQUE VOLLEY NO CONTROLA BIEN ESTA SITUACIÓN "INESPERADA"
        */

        StringRequest request = new StringRequest( // aunque supabase devuelva vacío no da error
                Request.Method.POST,
                url,
                response -> {
                    Toast.makeText(getContext(), "Vehículo guardado en base de datos", Toast.LENGTH_SHORT).show();
                    requireActivity().getSupportFragmentManager().popBackStack();
                },
                error -> { // el bloque de error se ejecuta SIEMPRE que Volley cree que hay error
                    Log.e("SUPABASE", "Error guardando vehículo", error);

                    if (error.networkResponse != null) {
                        int statusCode = error.networkResponse.statusCode;
                        Log.e("SUPABASE", "Código de estado del error: " + statusCode);

                        // esto me sirve para capturar y separar  los códigos 201 (created) y 204 (no content)
                        // que NO SON errores reales pq si se está guardando en supabase:
                        if (statusCode == 201 || statusCode == 204) {
                            Toast.makeText(getContext(), "Vehículo guardado con éxito", Toast.LENGTH_SHORT).show();
                            requireActivity().getSupportFragmentManager().popBackStack();
                            return;
                        }

                        //si no es uno de esos errores, deduzco que sí es un error, por lo que dejo que se muestre el mensaje:
                        String errorMessage = new String(error.networkResponse.data);
                        Log.e("SUPABASE", "Cuerpo del error: " + errorMessage);
                    }

                    Toast.makeText(getContext(), "Error al guardar vehículo", Toast.LENGTH_SHORT).show();
                }) {

            // transforma el json en texto plano (y luego en bytes) para que volley lo envíe
            @Override
            public byte[] getBody() {
                return vehiculoJson.toString().getBytes(StandardCharsets.UTF_8);
            }

            // esto es para que supabase sepa que el contenido, aunque le esté llegando transformado,
            // es de tipo json, que es lo que espera para los inserts:
            @Override
            public String getBodyContentType() {
                return "application/json";
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                SharedPreferences prefs = requireContext().getSharedPreferences("SupabasePrefs", Context.MODE_PRIVATE);
                String token = prefs.getString("access_token", "");

                Map<String, String> headers = new HashMap<>();
                headers.put("apikey", API_ANON_KEY);
                headers.put("Authorization", "Bearer " + token);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        queue.add(request);
    }
}

