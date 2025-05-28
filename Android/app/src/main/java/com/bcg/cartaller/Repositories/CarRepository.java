package com.bcg.cartaller.Repositories;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class CarRepository {
    private final RequestQueue queue;
    private final Context context;
    private final String SUPABASE_URL = "https://gtiqlopkoiconeivobxa.supabase.co";
    private final String API_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imd0aXFsb3Brb2ljb25laXZvYnhhIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDYxMjMyMTAsImV4cCI6MjA2MTY5OTIxMH0.T5MFUR9KAWXQOnoeZChYXu-FQ9LGClPp1lrSX8q733o";


    public CarRepository(Context context) {
        this.context = context;
        this.queue = Volley.newRequestQueue(context);
    }

    /**
     * Este callback es para llamar al método encargado de la creación de un nuevo vehículo
     */
    public interface CarSaveCallback {
        void onSuccess();
        void onError(String message);
    }


    /**
     * CREAR UN NUEVO VEHÍCULO, GUARDÁNDOLO EN LA BD DE SUPABASE (antes en CarNewFragment)
     * @param carJson
     * @param callback
     */
    public void saveCar(JSONObject carJson, CarSaveCallback callback) {
        JSONArray dataArray = new JSONArray();
        dataArray.put(carJson);

        String url = SUPABASE_URL + "/rest/v1/vehiculos";

        StringRequest request = new StringRequest(
                Request.Method.POST,
                url,
                responseString -> {
                    callback.onSuccess();
                },
                error -> {
                    String msg = "Error al guardar vehículo";
                    if (error.networkResponse != null) {
                        int code = error.networkResponse.statusCode;
                        if (code == 201 || code == 204) {
                            callback.onSuccess();
                            return;
                        }
                        msg += ": " + new String(error.networkResponse.data);
                    }
                    callback.onError(msg);
                }
        ) {
            @Override
            public byte[] getBody() {
                return dataArray.toString().getBytes(StandardCharsets.UTF_8);
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
}
