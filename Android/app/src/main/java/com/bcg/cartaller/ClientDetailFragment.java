package com.bcg.cartaller;

import android.widget.TextView;

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

/**
 * Cuando  se pulsa en un cliente de los encontrados, vas a su detalle
 */
public class ClientDetailFragment extends Fragment {
    private TextView txtDni, txtNombre, txtTelefono;
    private final String SUPABASE_URL = "https://gtiqlopkoiconeivobxa.supabase.co";
    private final String API_ANON_KEY = "<API_KEY_AQUI>";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_client_detail, container, false);

        txtDni = view.findViewById(R.id.txtDetalleDni);
        txtNombre = view.findViewById(R.id.txtDetalleNombre);
        txtTelefono = view.findViewById(R.id.txtDetalleTelefono);

        String dni = getArguments() != null ? getArguments().getString("cliente_dni") : null;
        if (dni != null) {
            cargarDetalleCliente(dni);
        }

        return view;
    }

    /**
     * Buscamos por dni de cliente
     * @param dni
     */
    private void cargarDetalleCliente(String dni) {
        String url = SUPABASE_URL + "/rest/v1/clientes?dni=eq." + dni;

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    if (response.length() > 0) {
                        try {
                            JSONObject clienteJson = response.getJSONObject(0);
                            txtDni.setText("DNI: " + clienteJson.getString("dni"));
                            txtNombre.setText("Nombre: " + clienteJson.optString("nombre", ""));
                            txtTelefono.setText("Teléfono: " + clienteJson.optString("telefono", ""));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                error -> Log.e("SUPABASE", "Error al obtener cliente", error)) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                SharedPreferences prefs = requireContext().getSharedPreferences("SupabasePrefs", Context.MODE_PRIVATE);
                String token = prefs.getString("access_token", "");
                Map<String, String> headers = new HashMap<>();
                headers.put("apikey", API_ANON_KEY);
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        Volley.newRequestQueue(requireContext()).add(request);
    }
}