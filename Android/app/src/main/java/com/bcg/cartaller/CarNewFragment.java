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

public class CarNewFragment extends Fragment {
    private EditText etMatricula, etMarca, etModelo;
    private Button btnGuardar;
    private final String SUPABASE_URL = "https://gtiqlopkoiconeivobxa.supabase.co";
    private final String API_ANON_KEY = "<API_KEY_AQUI>";

    public CarNewFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_car_new, container, false);

        etMatricula = view.findViewById(R.id.editTextMatricula);
        etMarca = view.findViewById(R.id.editTextMarca);
        etModelo = view.findViewById(R.id.editTextModelo);
        btnGuardar = view.findViewById(R.id.guardarVehiculoButton);

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

        JSONObject vehiculoJson = new JSONObject();
        try {
            vehiculoJson.put("matricula", matricula);
            vehiculoJson.put("marca", marca);
            vehiculoJson.put("modelo", modelo);
            //vehiculoJson.put("cliente_dni", dni);
            vehiculoJson.put("cliente_id", clienteId); // No está usando el dni realmente


        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                vehiculoJson,
                response -> {
                    Toast.makeText(getContext(), "Vehículo guardado con éxito", Toast.LENGTH_SHORT).show();
                    requireActivity().getSupportFragmentManager().popBackStack();
                },
                error -> {
                    Toast.makeText(getContext(), "Error al guardar vehículo", Toast.LENGTH_SHORT).show();
                    Log.e("SUPABASE", "Error guardando vehículo", error);
                }) {
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

        Volley.newRequestQueue(requireContext()).add(request);
    }
}

