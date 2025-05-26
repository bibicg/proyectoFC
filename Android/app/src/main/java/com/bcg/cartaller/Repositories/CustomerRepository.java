package com.bcg.cartaller.Repositories;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bcg.cartaller.Models.Customer;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
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

    /**
     * Este callback es para la búsqueda de cliente
     */
    public interface CustomerCallback {
        void onSuccess(List<Customer> customers);
        void onError(String message);
    }

    /**
     * Este callback es para la creación de nuevos clientes
     */
    public interface CustomerSaveCallback {
        void onSuccess();
        void onError(String message);
    }

    /**
     * Este callback es para la modificación de clientes existentes
     */
    public interface CustomerModifyCallback {
        void onSuccess();
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

    /**
     * CREACIÓN DE CLIENTE. Antes en CustomersNewFragment.
     * @param customer
     * @param callback
     */
    public void saveCustomer(Customer customer, CustomerSaveCallback callback) {
        String url = SUPABASE_URL + "/rest/v1/clientes";

        JSONObject customerJson = new JSONObject();
        try {

            customerJson.put("nombre", customer.getName());
            customerJson.put("apellidos", customer.getSurname());
            customerJson.put("dni", customer.getDni());
            customerJson.put("telefono", customer.getPhone());
            customerJson.put("email", customer.getEmail());
            customerJson.put("direccion", customer.getAddress());
        } catch (JSONException e) {
            callback.onError("Error preparando datos JSON");
            return;
        }

        StringRequest request = new StringRequest(
                Request.Method.POST,
                url,
                response -> callback.onSuccess(),
                error -> {
                    callback.onError("Error al guardar cliente");
                    if (error.networkResponse != null) {
                        Log.e("SUPABASE", "Código: " + error.networkResponse.statusCode);
                        Log.e("SUPABASE", new String(error.networkResponse.data));
                    }
                }
        ) {
            @Override
            public byte[] getBody() {
                return customerJson.toString().getBytes(StandardCharsets.UTF_8);
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }

            @Override
            public Map<String, String> getHeaders() {
                SharedPreferences prefs = context.getSharedPreferences("SupabasePrefs", Context.MODE_PRIVATE);
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

    /**
     * MODIFICACIÓN DE CLIENTE. Antes en CustomersNewFragment.
     * @param customer
     * @param callback
     */
    public void modifyCustomer(String dni, Customer customer, CustomerModifyCallback callback) {
        String url = SUPABASE_URL + "/rest/v1/clientes?dni=eq." + Uri.encode(dni);

        JSONObject updateCustomer = new JSONObject();
        try {
            updateCustomer.put("nombre", customer.getName());
            updateCustomer.put("apellidos", customer.getSurname());
            updateCustomer.put("telefono", customer.getPhone());
            updateCustomer.put("email", customer.getEmail());
            updateCustomer.put("direccion", customer.getAddress());
        } catch (JSONException e) {
            callback.onError("Error preparando datos JSON");
            return;
        }

        StringRequest request = new StringRequest(
                Request.Method.PATCH,
                url,
                response -> callback.onSuccess(),
                error -> {
                    callback.onError("Error al actualizar cliente");
                    if (error.networkResponse != null) {
                        Log.e("SUPABASE", "Código: " + error.networkResponse.statusCode);
                        Log.e("SUPABASE", new String(error.networkResponse.data));
                    }
                }
        ) {
            @Override
            public byte[] getBody() {
                return updateCustomer.toString().getBytes(StandardCharsets.UTF_8);
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }

            @Override
            public Map<String, String> getHeaders() {
                SharedPreferences prefs = context.getSharedPreferences("SupabasePrefs", Context.MODE_PRIVATE);
                String token = prefs.getString("access_token", "");
                Map<String, String> headers = new HashMap<>();
                headers.put("apikey", API_ANON_KEY);
                headers.put("Authorization", "Bearer " + token);
                headers.put("Prefer", "return=representation");
                return headers;
            }
        };

        queue.add(request);
    }


}