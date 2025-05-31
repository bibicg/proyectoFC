package com.bcg.cartaller.Repositories;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bcg.cartaller.Models.Car;
import com.bcg.cartaller.Models.Customer;
import com.bcg.cartaller.Models.Job;

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
     * Este callback es para llamar al método encargado de mostrar los trabajos dependiendo del filtro
     */
    public interface JobByFilterShowCallback {
        void onSuccess(List<Job> jobList);
        void onError(String message);
    }

    /**
     * Este callback es para llamar al método encargado de mostrar las tareas asociadas a un trabajo
     */
    public interface TaskLoadCallback {
        void onSuccess(List<String> tareas);
        void onError(String message);
    }

    /**
     * Este callback es para llamar al método encargado de mostrar el trabajo actualizado
     * despyes de haberse modificado en el formulario de JobsNewFragment

    public interface JobDetailCallback {
        void onSuccess(JSONObject jobData);
        void onError(String message);
    }*/


    /**
     * CREAR UN NUEVO TRABAJO, GUARDÁNDOLO EN LA BD DE SUPABASE - guardarTrabajo
     * @param jobJson
     * @param callback
     */
    public void saveJob(JSONObject jobJson, JobSaveCallback callback) {
        JSONArray dataArray = new JSONArray();
        dataArray.put(jobJson);

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
                        JSONObject job = responseArray.getJSONObject(0);
                        int jobId = job.getInt("id");
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

        /**
         * Necesito el conteo de tareas para que no falle el toast de la app,
         * aunque se guardan correctamente en Supabase, Android muestra un toast de error
         */
        if (taskDescriptions.isEmpty()) {
            callback.onSuccess();
            return;
        }

        final int totalTasks = taskDescriptions.size();
        final int[] successCount = {0};
        final boolean[] errorReported = {false};

        for (String descripcion : taskDescriptions) {
            JSONObject taskJson = new JSONObject();
            try {
                taskJson.put("trabajo_id", jobId);
                taskJson.put("descripcion", descripcion);

                JsonObjectRequest request = new JsonObjectRequest(
                        Request.Method.POST,
                        url,
                        taskJson,
                        response -> {
                            successCount[0]++;
                            if (successCount[0] == totalTasks && !errorReported[0]) {
                                callback.onSuccess();  //solo se llama si todo ha ido bien
                            }
                        },
                        error -> {
                            if (!errorReported[0]) {
                                errorReported[0] = true;
                                //String msg = "Error al guardar tarea: ";
                                if (error.networkResponse != null) {
                                    //msg += new String(error.networkResponse.data);
                                }
                                //callback.onError(msg);
                            }
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
                if (!errorReported[0]) {
                    errorReported[0] = true;
                    callback.onError("Error preparando tarea JSON: " + e.getMessage());
                }
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
                            callback.onError("Error al procesar vehículo");
                        }
                    } else {
                        callback.onNotFound();
                    }
                },
                error -> {
                    callback.onError("Error al buscar vehículo");
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
                            callback.onError("Error recibiendo tareas tipo");
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

    /**
     * Método que recupera los trabajos del mecánico, dependiendo del filtro con el que se haga
     * la búsqueda (estado , dni cliente, matrícula coche) con HTTP - GET:
     * @param estado
     * @param dniCliente
     * @param matriculaVehiculo
     * @param callback
     */

    public void showJobsByFilter(String estado, String dniCliente, String matriculaVehiculo, JobByFilterShowCallback callback) {
        SharedPreferences prefs = context.getSharedPreferences("prefs", Context.MODE_PRIVATE);
        String mecanicoId = prefs.getString("mecanico_id", null);

        if (mecanicoId == null) {
            callback.onError("ID mecánico no encontrado");
            return;
        }

        //String baseUrl = SUPABASE_URL + "/rest/v1/trabajos?select=*,vehiculos(*,clientes(*))";
        //Uso !inner para relaciones anidadas pq no me está funcionando bien el filtrado:
        String baseUrl = SUPABASE_URL +
                "/rest/v1/trabajos?select=id,estado,descripcion,fecha_inicio,fecha_fin,comentarios,imagen,mecanico_id," +
                "vehiculos!inner(matricula,clientes!inner(dni))";

        List<String> filter = new ArrayList<>();
        filter.add("mecanico_id=eq." + mecanicoId);

        if (estado != null && !estado.equals("todos")) {
            filter.add("estado=eq." + estado);
        }
        if (dniCliente != null && !dniCliente.isEmpty()) {
            filter.add("vehiculos.clientes.dni=ilike.*" + dniCliente + "*");
        }
        if (matriculaVehiculo != null && !matriculaVehiculo.isEmpty()) {
            filter.add("vehiculos.matricula=ilike.*" + matriculaVehiculo + "*");
        }

        String url = baseUrl + "&" + TextUtils.join("&", filter);
        Log.d("JOB_FILTER_URL", url);

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    List<Job> jobs = new ArrayList<>();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject trabajoJson = response.getJSONObject(i);
                            JSONObject vehiculoJson = trabajoJson.optJSONObject("vehiculos");
                            JSONObject clienteJson = vehiculoJson != null ? vehiculoJson.optJSONObject("clientes") : null;

                            String dni = clienteJson != null ? clienteJson.optString("dni", "") : "";
                            String matricula = vehiculoJson != null ? vehiculoJson.optString("matricula", "") : "";

                            Customer customer = new Customer(dni);
                            Car car = new Car(matricula, customer);

                            Job job = new Job(
                                    String.valueOf(trabajoJson.getInt("id")),
                                    trabajoJson.optString("estado", ""),
                                    trabajoJson.optString("descripcion", ""),
                                    car
                            );
                            job.startDate = trabajoJson.optString("fecha_inicio", null);
                            job.endDate = trabajoJson.optString("fecha_fin", null);
                            job.comment = trabajoJson.optString("comentarios", null);
                            job.image = trabajoJson.optString("imagen", null);
                            job.mechanicId = trabajoJson.optString("mecanico_id", null);

                            jobs.add(job);
                        } catch (JSONException e) {
                            callback.onError("No se encontraron trabajos.");
                            return;
                        }
                    }
                    callback.onSuccess(jobs);
                },
                error -> {
                    String msg = "Error al cargar trabajos";
                    if (error.networkResponse != null)
                        msg += ": " + new String(error.networkResponse.data);
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
                return headers;
            }
        };

        Volley.newRequestQueue(context).add(request);
    }


    /**
     * Método HTTP-GET para recuperar de la BD las tareas asociadas a un trabajo concreto,
     * para poder verlas tanto en el detail job como en la edición del trabajo:
     * @param jobId
     * @param callback
     */
    public void getTasksByJobId(int jobId, TaskLoadCallback callback) {
        String url = SUPABASE_URL + "/rest/v1/tareas?trabajo_id=eq." + jobId + "&select=descripcion";

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
                            callback.onError("Error leyendo tareas");
                            return;
                        }
                    }
                    callback.onSuccess(tareas);
                },
                error -> {
                    callback.onError("Error cargando tareas");
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
     * Metodo que recarga la info del JobDetailFragment después de haber hecho una modificacion en el trabajo

    public void getJobById(String jobId, JobDetailCallback callback) {
        String url = SUPABASE_URL + "/rest/v1/trabajos?id=eq." + jobId +
                "&select=descripcion,fecha_inicio,fecha_fin,estado,comentarios,matricula,dni_cliente,imagen";

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    if (response.length() > 0) {
                        callback.onSuccess(response.optJSONObject(0));
                    } else {
                        callback.onError("No se encontró el trabajo.");
                    }
                },
                error -> {
                    callback.onError("Error de red al obtener datos.");
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

        Volley.newRequestQueue(context).add(request);
    }*/
}


