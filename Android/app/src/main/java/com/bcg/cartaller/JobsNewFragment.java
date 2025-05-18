package com.bcg.cartaller;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.content.SharedPreferences;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Desde este fragment el mecánico podrá crear trabajos.
 * Debe seleccionar una matrícula ya que están asociados a un vehículo concreto.
 * Si aún no existe ese vehículo, se debe crear en el apartado correspondiente.
 * Lo ideal sería que se pudiera crear desde aqui (en una app profesional), pero yo por ahora lo voy a hacer asi,
 * si me da tiempo, lo añado.
 *
 * Añado los campos que me faltan en el formulario (fecha fin y estado). A mayores de los contemplados al principio,
 * quiero añadir comentarios y subida de imagen desde la app.
 */
public class JobsNewFragment extends Fragment {
    private RequestQueue queue;
    private final String SUPABASE_URL = "https://gtiqlopkoiconeivobxa.supabase.co";
    private final String API_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imd0aXFsb3Brb2ljb25laXZvYnhhIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDYxMjMyMTAsImV4cCI6MjA2MTY5OTIxMH0.T5MFUR9KAWXQOnoeZChYXu-FQ9LGClPp1lrSX8q733o";
    private List<String> tareasSeleccionadas = new ArrayList<>();

    private EditText etMatricula, etDescripcionTrabajo, etFechaInicio, etFechaFin, etComentarios;
    private Button btnBuscarVehiculo, btnFechaInicio, btnFechaFin, btnGuardarTrabajo, btnAnadirTarea, btnSeleccionarImagen;
    private TextView tvInfoVehiculo;
    private Spinner spinnerEstado;

    //para la iamgen:
    private ImageView imageViewTrabajo;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri selectedImageUri = null;
    private Bitmap bitmap;
    private String base64Image = null;

    private int vehiculoIdSeleccionado = -1; // Inicializar con un valor que no pueda haber

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)  {
        View view = inflater.inflate(R.layout.fragment_jobs_new, container, false);

        etMatricula = view.findViewById(R.id.editTextMatricula);
        btnBuscarVehiculo = view.findViewById(R.id.buscarVehiculoButton);
        tvInfoVehiculo = view.findViewById(R.id.textViewVehiculoInfo);
        etDescripcionTrabajo = view.findViewById(R.id.editTextDescripcion);
        btnFechaInicio = view.findViewById(R.id.fechaInicioButton);
        etFechaInicio = view.findViewById(R.id.editTextFechaInicio);
        btnGuardarTrabajo = view.findViewById(R.id.guardarButton);
        btnAnadirTarea = view.findViewById(R.id.anadirTareaButton);
        //Los campos que añado (fechaFin y estado ya estaban contemplados al principio):
        btnFechaFin = view.findViewById(R.id.fechaFinButton);
        etFechaFin = view.findViewById(R.id.editTextFechaFin);
        spinnerEstado = view.findViewById(R.id.spinnerEstado);
        etComentarios = view.findViewById(R.id.editTextComentarios);
        imageViewTrabajo = view.findViewById(R.id.imageViewTrabajo);
        btnSeleccionarImagen = view.findViewById(R.id.btnSeleccionarImagen);


        queue = Volley.newRequestQueue(requireContext());

        btnBuscarVehiculo.setOnClickListener(v -> {
            String matricula = etMatricula.getText().toString().trim();
            if (!matricula.isEmpty()) {
                searchCarByMatricula(matricula);
            } else {
                Toast.makeText(getContext(), "Por favor, introduce la matrícula del vehículo", Toast.LENGTH_SHORT).show();
            }
        });

        /**
         * Por ahora lo hago con botón, pero mejor buscar otro elemento más estético
         */
        btnFechaInicio.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                    (view1, year1, month1, dayOfMonth) -> {
                        //tiene que tener el formato que usa supabase para las fechas, sino lo guarda igual pero me sale mensa de error en la app:
                        String fechaSeleccionada = String.format(Locale.getDefault(), "%04d-%02d-%02d", year1, month1 + 1, dayOfMonth);

                        etFechaInicio.setText(fechaSeleccionada);
                    },
                    year, month, day);
            datePickerDialog.show();
        });

        btnFechaFin.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                    (view1, year1, month1, dayOfMonth) -> {
                        //tiene que tener el formato que usa supabase para las fechas, sino lo guarda igual pero me sale mensa de error en la app:
                        String fechaSeleccionada = String.format(Locale.getDefault(), "%04d-%02d-%02d", year1, month1 + 1, dayOfMonth);

                        etFechaFin.setText(fechaSeleccionada);
                    },
                    year, month, day);
            datePickerDialog.show();
        });

        //Para la imagen:
        btnSeleccionarImagen.setOnClickListener(v -> selectImage());

        String[] estados = {"pendiente", "en curso", "finalizado"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, estados);
        spinnerEstado.setAdapter(adapter);


        // Al pulsar este botón se llama al metodo que carga en pantalla las tareas tipo guardadas anteriormente en BD:
        btnAnadirTarea.setOnClickListener(v -> loadTypeTask());

        //Este boton solo funciona si se ha elegido una matri (!=0):
        btnGuardarTrabajo.setOnClickListener(v -> {
            if (vehiculoIdSeleccionado != -1) {
                String descripcion = etDescripcionTrabajo.getText().toString().trim();
                String fechaInicio = etFechaInicio.getText().toString().trim();
                String fechaFin = etFechaFin.getText().toString().trim();
                String estado = spinnerEstado.getSelectedItem().toString();
                String comentarios = etComentarios.getText().toString().trim();

                saveJob(vehiculoIdSeleccionado, descripcion, fechaInicio, fechaFin, estado, comentarios, base64Image, tareasSeleccionadas);
            } else {
                Toast.makeText(getContext(), "Busca y selecciona un vehículo primero", Toast.LENGTH_LONG).show();
            }
        });

        return view;
    }

    /**
     * PARA LOS CAMPOS NUEVOS: SPINNER PARA EL ESTADO, IMAGEN Y COMENTARIOS
     */

    //para que el usuario pueda seleccionar una foto de su galeria de imágenes y subirla a la app:
    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), selectedImageUri);
                imageViewTrabajo.setImageBitmap(bitmap);
                convertImageToBase64(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "Error al cargar imagen", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void convertImageToBase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        base64Image = Base64.encodeToString(imageBytes, Base64.NO_WRAP);
    }

    /**
     * Método para buscar una matrícula ya guardada en BD. Si no se encuentra, no deja guardar el trabajo.
     * Si el vehículo aún no existe, hay que crearlo previamente.
     * @param matricula
     */
    private void searchCarByMatricula(String matricula) {
        String url = SUPABASE_URL + "/rest/v1/vehiculos?select=id,marca,modelo&matricula=eq." + matricula;

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    if (response.length() > 0) {
                        try {
                            JSONObject vehiculoJson = response.getJSONObject(0);
                            vehiculoIdSeleccionado = vehiculoJson.getInt("id");
                            if (vehiculoIdSeleccionado != -1){
                                String marca = vehiculoJson.getString("marca");
                                String modelo = vehiculoJson.getString("modelo");
                                Toast.makeText(getContext(), "Vehículo encontrado: " + marca + " " + modelo, Toast.LENGTH_SHORT).show();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getContext(), "Error al procesar información del vehículo", Toast.LENGTH_SHORT).show();
                            // Como hubo un error, vuelve a ser -1:
                            vehiculoIdSeleccionado = -1;
                        }
                    } else {
                        Toast.makeText(getContext(), "No se encontró ningún vehículo con la matrícula: " + matricula + ". Por favor, añádelo desde la sección de Clientes.", Toast.LENGTH_LONG).show();
                        // Como no se ha seleccionado, vuelve a ser -1:
                        vehiculoIdSeleccionado = -1;
                    }
                },
                error -> {
                    Toast.makeText(getContext(), "Error al buscar vehículo", Toast.LENGTH_SHORT).show();
                    Log.e("Volley", "Error al buscar vehículo", error);
                }
        ) {
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
        queue.add(request);
    }

    /**
     * Método para, a través de una consulta GET a la BD de supabase, conseguir el listado de todas las tareas guardadas
     */
    private void loadTypeTask() {
        String url = SUPABASE_URL + "/rest/v1/tareas_tipo?select=descripcion";

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    List<String> tareas = new ArrayList<>(); //debe ser una lista de strings porque no es una clase POJO
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            tareas.add(response.getJSONObject(i).getString("descripcion"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    //Lllama al método que permite seleccionar las tareas:
                    showDialogSelectTask(tareas);
                },
                error -> {
                    Toast.makeText(getContext(), "Error al cargar tareas guardadas en base de datos", Toast.LENGTH_SHORT).show();
                    Log.e("Volley", "Error cargando tareas guardadas en base de datos", error);
                }) {
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
        queue.add(request);
    }


    /**
     * Desde este método se pueden elegir una/varias tareas
     * @param tareas
     */
    private void showDialogSelectTask(List<String> tareas) {
        String[] items = tareas.toArray(new String[0]);
        boolean[] checkedItems = new boolean[items.length];

        new AlertDialog.Builder(requireContext())
                .setTitle("Selecciona una o varias tareas:")
                .setMultiChoiceItems(items, checkedItems, (dialog, which, isChecked) -> checkedItems[which] = isChecked)
                .setPositiveButton("Añadir", (dialog, which) -> {
                    for (int i = 0; i < checkedItems.length; i++) {
                        if (checkedItems[i] && !tareasSeleccionadas.contains(items[i])) {
                            tareasSeleccionadas.add(items[i]);
                        }
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    //guardarTareas():
    private void saveTask(int trabajoId, List<String> tareasDescripcion) {
        String urlTareas = SUPABASE_URL + "/rest/v1/tareas";
        for (String descripcionTarea : tareasDescripcion) {
            JSONObject tareaJson = new JSONObject();
            try {
                tareaJson.put("trabajo_id", trabajoId);
                tareaJson.put("descripcion", descripcionTarea);
                // realizada por defecto en false

                JsonObjectRequest requestTarea = new JsonObjectRequest(
                        Request.Method.POST,
                        urlTareas,
                        tareaJson,
                        responseTarea -> {
                            Log.d("SUPABASE", "Tarea guardada con éxito (Trabajo ID: " + trabajoId + ")");
                        },
                        errorTarea -> {
                            Log.e("SUPABASE", "Error al guardar tarea (Trabajo ID: " + trabajoId + ")", errorTarea);
                            if (errorTarea.networkResponse != null) {
                                Log.e("SUPABASE", "Código: " + errorTarea.networkResponse.statusCode);
                                Log.e("SUPABASE", new String(errorTarea.networkResponse.data));
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
                        headers.put("Prefer", "return=representation"); //siempre hace falta al usar JsonObjectRequest
                        return headers;
                    }
                };
                queue.add(requestTarea);

            } catch (JSONException e) {
                Toast.makeText(getContext(), "Error preparando datos de la tarea", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    /**
     * GUARDAR/CREAR UN NUEVO TRABAJO - guardarTrabajo
     * @param vehiculoId
     * @param descripcion
     * @param fechaInicio
     * @param tareasDescripcion
     *
     * Añadir los nuevos campos en el guardado del trabajo
     */
    private void saveJob(int vehiculoId, String descripcion, String fechaInicio, String fechaFin, String estado, String comentarios, String base64Imagen, List<String> tareasDescripcion) {
        if (vehiculoId == -1) {
            Toast.makeText(getContext(), "Por favor, selecciona un vehículo primero", Toast.LENGTH_LONG).show();
            return;
        }

        SharedPreferences prefs = requireContext().getSharedPreferences("prefs", Context.MODE_PRIVATE);
        String mecanicoId = prefs.getString("mecanico_id", null);

        if (mecanicoId == null) {
            Toast.makeText(getContext(), "ID mecánico no encontrado. Por favor, inicie sesión nuevamente.", Toast.LENGTH_LONG).show();
            return;
        }

        JSONObject trabajoJson = new JSONObject();
        try {
            trabajoJson.put("vehiculo_id", vehiculoId);
            trabajoJson.put("mecanico_id", mecanicoId); // Usar el ID de SharedPreferences
            trabajoJson.put("descripcion", descripcion);
            trabajoJson.put("fecha_inicio", fechaInicio);
            // El estado se establece por defecto en pendiente en la BD
            if (!fechaFin.isEmpty()) trabajoJson.put("fecha_fin", fechaFin);
            if (!estado.isEmpty()) trabajoJson.put("estado", estado);
            if (!comentarios.isEmpty()) trabajoJson.put("comentarios", comentarios);
            if (base64Imagen != null) trabajoJson.put("imagen", "data:image/jpeg;base64," + base64Imagen);


            //lo añado al usar JsonArrayRequest, ya que con JsonObjectRequest da error (caso como el de clientes):
            JSONArray dataArray = new JSONArray();
            dataArray.put(trabajoJson);

            String urlTrabajos = SUPABASE_URL + "/rest/v1/trabajos";

            /** DA ERROR, COMO EN CLIENTE
             JsonObjectRequest requestTrabajo = new JsonObjectRequest(
             Request.Method.POST,
             urlTrabajos,
             trabajoJson,
             responseTrabajo -> {
             int trabajoId = responseTrabajo.optInt("id"); // Obtener el ID del trabajo recién creado
             Toast.makeText(getContext(), "Trabajo guardado con éxito (ID: " + trabajoId + ")", Toast.LENGTH_SHORT).show();

             if (tareasDescripcion != null && !tareasDescripcion.isEmpty()) {
             guardarTareas(trabajoId, tareasDescripcion);
             }

             },
             errorTrabajo -> {
             Toast.makeText(getContext(), "Error al guardar el trabajo", Toast.LENGTH_SHORT).show();
             Log.e("SUPABASE", "Error al guardar trabajo", errorTrabajo);

             if (errorTrabajo.networkResponse != null) {
             int statusCode = errorTrabajo.networkResponse.statusCode;
             String responseBody = new String(errorTrabajo.networkResponse.data);
             Log.e("SUPABASE", "Código HTTP: " + statusCode);
             Log.e("SUPABASE", "Respuesta del servidor: " + responseBody);
             } else {
             Log.e("SUPABASE", "No hay respuesta del servidor (posible problema de red o CORS)");
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
            headers.put("Prefer", "return=representation");
            return headers;
            }
            };*/

            //A VER SI CON JsonArrayRequest.... FUNCIONA EN SUPABASE PERO MUESTRA ERRORES EN LA APP
            /**
            JsonArrayRequest requestTrabajo = new JsonArrayRequest(
                    Request.Method.POST,
                    urlTrabajos,
                    dataArray,
                    responseArray -> {
                        try {
                            JSONObject trabajo = responseArray.getJSONObject(0);
                            int trabajoId = trabajo.getInt("id");
                            Toast.makeText(getContext(), "Trabajo guardado con éxito (ID: " + trabajoId + ")", Toast.LENGTH_SHORT).show();

                            if (tareasDescripcion != null && !tareasDescripcion.isEmpty()) {
                                guardarTareas(trabajoId, tareasDescripcion);
                            }
                        } catch (JSONException e) {
                            Log.e("SUPABASE", "Error parseando respuesta JSON", e);
                        }
                    },
                    errorTrabajo -> {
                        Toast.makeText(getContext(), "Error al guardar el trabajo", Toast.LENGTH_SHORT).show();
                        Log.e("SUPABASE", "Código: " + (errorTrabajo.networkResponse != null ? errorTrabajo.networkResponse.statusCode : -1));
                        if (errorTrabajo.networkResponse != null) {
                            Log.e("SUPABASE", new String(errorTrabajo.networkResponse.data));
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
                    headers.put("Prefer", "return=representation");
                    return headers;
                }
            };
            queue.add(requestTrabajo);*/


            //para peticiones POST (insertar) en las que necesito obtener algo para seguir (aqui, el id del trabajo
            //insertado,para luego poder vincularle las tareas) puedo usar StringRequest y return=representation en las cabeceras,
            // que hace que Supabase devuelva un JSON. Además, hay que hacer un parseo a JSONarray:

            StringRequest request = new StringRequest(
                    Request.Method.POST,
                    urlTrabajos,
                    responseString -> {
                        try {
                            // Supabase devuelve un array JSON como string, por eso hay que parsearlo:
                            JSONArray responseArray = new JSONArray(responseString);
                            JSONObject trabajo = responseArray.getJSONObject(0);
                            int trabajoId = trabajo.getInt("id");

                            Toast.makeText(getContext(), "Trabajo guardado con éxito (ID: " + trabajoId + ")", Toast.LENGTH_SHORT).show();

                            if (tareasDescripcion != null && !tareasDescripcion.isEmpty()) {
                                saveTask(trabajoId, tareasDescripcion);
                            }
                        } catch (JSONException e) {
                            Log.e("SUPABASE", "Erroral parsear la respuesta JSON", e);
                        }
                    },
                    errorTrabajo -> {
                        Log.e("SUPABASE", "Error al guardar trabajo", errorTrabajo);
                        int statusCode = errorTrabajo.networkResponse != null ? errorTrabajo.networkResponse.statusCode : -1;

                        if (statusCode == 201 || statusCode == 204) {
                            Toast.makeText(getContext(), "Trabajo guardado con éxito", Toast.LENGTH_SHORT).show();
                            requireActivity().getSupportFragmentManager().popBackStack();
                            return;
                        }

                        Toast.makeText(getContext(), "Error al guardar el trabajo", Toast.LENGTH_SHORT).show();
                        if (errorTrabajo.networkResponse != null) {
                            Log.e("SUPABASE", "Código: " + statusCode);
                            Log.e("SUPABASE", new String(errorTrabajo.networkResponse.data));
                        }
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
                public Map<String, String> getHeaders() throws AuthFailureError {
                    SharedPreferences prefs = requireContext().getSharedPreferences("SupabasePrefs", Context.MODE_PRIVATE);
                    String token = prefs.getString("access_token", "");

                    Map<String, String> headers = new HashMap<>();
                    headers.put("apikey", API_ANON_KEY);
                    headers.put("Authorization", "Bearer " + token);
                    headers.put("Prefer", "return=representation"); //para recuperar el id del trabajo
                    return headers;
                }
            };

            queue.add(request);



        } catch (JSONException e) {
            Toast.makeText(getContext(), "Error preparando datos del trabajo", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }


}
