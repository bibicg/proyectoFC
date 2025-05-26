package com.bcg.cartaller.Repositories;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Para separa la lógica de la interfaz, implemento Repositories.
 * Todos los métodos han sido "traidos" desde JobsNewFragment y JobsSearchFragment
 *
 * SE ENCARGA DE LAS PETICIONES HTTP PARA:
 * - BUSCAR TRABAJO (POR ESTADO (pendiente - en curso - finalizado) / DNI CLIENTE / MATRÍCULA),
 * - CREAR TRABAJO,
 * - MODIFICAR TRABAJO,
 * - BORRAR TRABAJO.
 *
 * Como mejora futura habría que implementar el patrón MVVM (con repositories + viewModel)
 */
public class JobRepository {
    private RequestQueue queue;
    private final String SUPABASE_URL = "https://gtiqlopkoiconeivobxa.supabase.co";
    private final String API_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imd0aXFsb3Brb2ljb25laXZvYnhhIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDYxMjMyMTAsImV4cCI6MjA2MTY5OTIxMH0.T5MFUR9KAWXQOnoeZChYXu-FQ9LGClPp1lrSX8q733o";
    private Context context;

    public JobRepository(Context context) {
        this.context = context;
        this.queue = Volley.newRequestQueue(context.getApplicationContext());
    }

    /**
     * Este callback es para llamar al método encargado de la creación de un nuevo trabajo
     */
    public interface JobSaveCallback {
        void onSuccess(int jobId);
        void onError(String message);
    }

    /**
     * Este callback es para llamar al método encargado de la modificación de un trabajo existente
     */
    public interface JobUpdateCallback {
        void onSuccess();
        void onError(String message);
    }

    /**
     * Este callback es para llamar al método encargado de la guardar las tareas que perteneces a un trabajo
     */
    public interface TaskSaveCallback {
        void onSuccess();
        void onError(String message);
    }

    /**
     * Este callback es para llamar al método encargado de buscar la matrícula de un coche
     */
    public interface CarSearchCallback {
        void onFound(int carId, String marca, String modelo);
        void onNotFound();
        void onError(String message);
    }

    /**
     * Este callback es para llamar al método encargado de cargar las tareas tipo
     */
    public interface TypeTaskLoadCallback {
        void onLoaded(List<String> tareas);
        void onError(String message);
    }

    /**
     * CREAR UN NUEVO TRABAJO, GUARDÁNDOLO EN LA BD DE SUPABASE - guardarTrabajo
     * @param trabajoJson
     * @param callback
     */
    public void saveJob(JSONObject trabajoJson, JobSaveCallback callback) {
        JSONArray dataArray = new JSONArray();
        dataArray.put(trabajoJson);

        String url = SUPABASE_URL + "/rest/v1/trabajos";

        //para peticiones POST (insertar) en las que necesito obtener algo para seguir (aqui, el id del trabajo
        //insertado,para luego poder vincularle las tareas) puedo usar StringRequest y return=representation en las cabeceras,
        // que hace que Supabase devuelva un JSON. Además, hay que hacer un parseo a JSONarray:

        StringRequest request = new StringRequest(
                Request.Method.POST,
                url,
                responseString -> {
                    try {
                        // Supabase devuelve un array JSON como string, por eso hay que parsearlo:
                        JSONArray responseArray = new JSONArray(responseString);
                        JSONObject trabajo = responseArray.getJSONObject(0);
                        int jobId = trabajo.getInt("id");
                        callback.onSuccess(jobId);
                    } catch (JSONException e) {
                        callback.onError("Error parseando respuesta JSON");
                    }
                },
                error -> {
                    String msg = "Error al guardar trabajo";
                    if (error.networkResponse != null) {
                        msg += ": " + new String(error.networkResponse.data);
                    }
                    callback.onError(msg);
                }
        ) {
            //transforma el json en texto plano y luego en bytes (como en CarNewFragmennt)
            @Override
            public byte[] getBody() {
                return dataArray.toString().getBytes(StandardCharsets.UTF_8);
            }

            //para que supabase sepa que el contenido es un json aunque no esté en ese formato
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
                headers.put("Prefer", "return=representation"); //para recuperar el id del trabajo
                return headers;
            }
        };

        queue.add(request);
    }


    /**
     * Método que permite actualizar un tabajo ya existente en BD
     * Uso PATCH en lugar de PUT
     * @param jobId
     * @param updateJson
     * @param callback
     */
    public void updateJob(String jobId, JSONObject updateJson, JobUpdateCallback callback) {
        String url = SUPABASE_URL + "/rest/v1/trabajos?id=eq." + Uri.encode(jobId);

        StringRequest request = new StringRequest(
                Request.Method.PATCH, //más seguro que PUT para supabase, ya que no modifica toda la fila sino solo los campos modificados
                url,
                response -> callback.onSuccess(),
                error -> {
                    String msg = "Error al actualizar trabajo";
                    if (error.networkResponse != null) {
                        msg += ": " + new String(error.networkResponse.data);
                    }
                    callback.onError(msg);
                }
        ) {
            @Override
            public byte[] getBody() {
                return updateJson.toString().getBytes(StandardCharsets.UTF_8);
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
                return headers;
            }
        };

        queue.add(request);
    }

    /**
     * Método que permite guardar las tareas asociadas a un trabajo
     * @param jobId
     * @param taskDescriptions
     * @param callback
     */
    public void saveTasks(int jobId, List<String> taskDescriptions, TaskSaveCallback callback) {
        String url = SUPABASE_URL + "/rest/v1/tareas";
        for (String descripcion : taskDescriptions) {
            JSONObject tareaJson = new JSONObject();
            try {
                tareaJson.put("trabajo_id", jobId);
                tareaJson.put("descripcion", descripcion);

                JsonObjectRequest request = new JsonObjectRequest(
                        Request.Method.POST,
                        url,
                        tareaJson,
                        response -> {
                            callback.onSuccess();
                        },
                        error -> {
                            String msg = "Error al guardar tarea: ";
                            if (error.networkResponse != null) {
                                msg += new String(error.networkResponse.data);
                            }
                            callback.onError(msg);
                        }
                ) {
                    @Override
                    public Map<String, String> getHeaders() {
                        SharedPreferences prefs = context.getSharedPreferences("SupabasePrefs", Context.MODE_PRIVATE);
                        String token = prefs.getString("access_token", "");
                        Map<String, String> headers = new HashMap<>();
                        headers.put("apikey", API_ANON_KEY);
                        headers.put("Authorization", "Bearer " + token);
                        headers.put("Content-Type", "application/json");
                        headers.put("Prefer", "return=representation");
                        return headers;
                    }
                };

                queue.add(request);

            } catch (JSONException e) {
                callback.onError("Error preparando tarea JSON: " + e.getMessage());
                return;
            }
        }

        callback.onSuccess();
    }

    /**
     * Método para buscar una matrícula ya guardada en BD. Si no se encuentra, no deja guardar el trabajo.
     * Si el vehículo aún no existe, hay que crearlo previamente.
     * @param matricula
     * @param callback
     */
    public void searchCarByMatricula(String matricula, CarSearchCallback callback) {
        String url = SUPABASE_URL + "/rest/v1/vehiculos?select=id,marca,modelo&matricula=eq." + Uri.encode(matricula);

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    if (response.length() > 0) {
                        try {
                            JSONObject car = response.getJSONObject(0);
                            int id = car.getInt("id");
                            String marca = car.optString("marca", "");
                            String modelo = car.optString("modelo", "");
                            callback.onFound(id, marca, modelo);
                        } catch (JSONException e) {
                            callback.onError("Error al procesar JSON de vehículo");
                        }
                    } else {
                        callback.onNotFound();
                    }
                },
                error -> {
                    callback.onError("Error de red al buscar vehículo");
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
     * Método que recupera las tareas tipo (ya guardaddas en supabase en una tabla) con HTTP - GET:
     * @param callback
     */
    public void loadTypeTasks(TypeTaskLoadCallback callback) {
        String url = SUPABASE_URL + "/rest/v1/tareas_tipo?select=descripcion";

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    List<String> tareas = new ArrayList<>();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            tareas.add(response.getJSONObject(i).getString("descripcion"));
                        } catch (JSONException e) {
                            callback.onError("Error leyendo JSON de tareas tipo");
                            return;
                        }
                    }
                    callback.onLoaded(tareas);
                },
                error -> {
                    callback.onError("Error cargando tareas tipo desde Supabase");
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

