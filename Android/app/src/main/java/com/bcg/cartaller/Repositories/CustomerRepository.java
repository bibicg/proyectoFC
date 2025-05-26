package com.bcg.cartaller.Repositories;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.bcg.cartaller.Models.Customer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Para separa la lógica de la interfaz, implemento Repositories.
 * Como mejora futura habría que implementar el patrón MVVM (con repositories + viewModel)
 */
public class CustomerRepository {
    private RequestQueue queue;
    private final String SUPABASE_URL = "https://gtiqlopkoiconeivobxa.supabase.co";
    private final String API_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imd0aXFsb3Brb2ljb25laXZvYnhhIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDYxMjMyMTAsImV4cCI6MjA2MTY5OTIxMH0.T5MFUR9KAWXQOnoeZChYXu-FQ9LGClPp1lrSX8q733o";
    private Context context;

    public CustomerRepository(Context context) {
        this.context = context;
        this.queue = Volley.newRequestQueue(context.getApplicationContext());
    }

    public interface CustomerCallback {
        void onSuccess(List<Customer> customers);
        void onError(String message);
    }

    /**
     * BÚSQUEDA DE CLIENTE POR DNI / MATRÍCULA. Antes en CustomersSearchFragment.
     * @param tipo
     * @param valor
     * @param callback
     */
    public void searchCustomers(String tipo, String valor, CustomerCallback callback) {
        String url;
        if ("dni".equals(tipo)) {
            url = SUPABASE_URL + "/rest/v1/clientes?dni=eq." + valor + "&select=id,dni,nombre,apellidos,telefono,email,direccion";
        } else {
            url = SUPABASE_URL + "/rest/v1/vehiculos?matricula=eq." + valor + "&select=cliente(*)";
        }

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    List<Customer> result = new ArrayList<>();
                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject clienteJson = "dni".equals(tipo)
                                    ? response.getJSONObject(i)
                                    : response.getJSONObject(i).getJSONObject("cliente");

                            Customer customer = new Customer(
                                    clienteJson.getInt("id"),
                                    clienteJson.getString("dni"),
                                    clienteJson.optString("nombre", ""),
                                    clienteJson.optString("apellidos", ""),
                                    clienteJson.optString("telefono", ""),
                                    clienteJson.optString("email", ""),
                                    clienteJson.optString("direccion", "")
                            );

                            result.add(customer);
                        }
                        callback.onSuccess(result);
                    } catch (JSONException e) {
                        callback.onError("Error parsing JSON");
                    }
                },
                error -> {
                    callback.onError("Error de red al buscar clientes");
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                SharedPreferences prefs = context.getSharedPreferences("SupabasePrefs", Context.MODE_PRIVATE);
                String token = prefs.getString("access_token", "");

                Map<String, String> headers = new HashMap<>();
                headers.put("apikey", API_ANON_KEY);
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        queue.add(request);
    }
}