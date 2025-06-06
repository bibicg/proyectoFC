package com.bcg.cartaller;

import android.widget.TextView;

import androidx.fragment.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Cuando  se pulsa en un customer de los encontrados, vas a su detalle
 */
public class CustomerDetailFragment extends Fragment {
/**
    private TextView txtDni, txtName, txtPhone;
    private final String SUPABASE_URL = "https://gtiqlopkoiconeivobxa.supabase.co";
    private final String API_ANON_KEY = "<API_KEY_AQUI>";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_customer_detail, container, false);

        txtDni = view.findViewById(R.id.txtDetailDni);
        txtName = view.findViewById(R.id.txtDetailName);
        txtPhone = view.findViewById(R.id.txtDetailPhone);

        String dni = getArguments() != null ? getArguments().getString("cliente_dni") : null;
        if (dni != null) {
            showDetailCustomer(dni);
        }

        return view;
    }

    /**
     * Buscamos por dni de customer
     * cargarDetalleCliente
     * @param dni
     */
/**    private void showDetailCustomer(String dni) {
        String url = SUPABASE_URL + "/rest/v1/clientes?dni=eq." + dni;

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    if (response.length() > 0) {
                        try {
                            JSONObject clienteJson = response.getJSONObject(0);
                            txtDni.setText("DNI: " + clienteJson.getString("dni"));
                            txtName.setText("Nombre: " + clienteJson.optString("nombre", ""));
                            txtPhone.setText("Teléfono: " + clienteJson.optString("telefono", ""));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                error -> Log.e("SUPABASE", "Error al obtener customer", error)) {
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
    }*/
}