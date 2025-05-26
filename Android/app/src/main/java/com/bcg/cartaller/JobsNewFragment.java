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
import com.bcg.cartaller.Repositories.JobRepository;

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
 * MÉTODOS HTTP TRASLADADOS AL REPOSITORY.
 * AHORA ESTE FRAGMENT MANEJA LA UI, PERO NO LA COMUNICACIÓN CON LA BD.
 *
 *
 * EN ORIGEN:
 * Desde este fragment el mecánico podrá crear trabajos.
 * Debe seleccionar una matrícula ya que están asociados a un vehículo concreto.
 * Si aún no existe ese vehículo, se debe crear en el apartado correspondiente (desde Clientes).
 */

public class JobsNewFragment extends Fragment {
    //private RequestQueue queue;
    //private final String SUPABASE_URL = "https://gtiqlopkoiconeivobxa.supabase.co";
    //private final String API_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imd0aXFsb3Brb2ljb25laXZvYnhhIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDYxMjMyMTAsImV4cCI6MjA2MTY5OTIxMH0.T5MFUR9KAWXQOnoeZChYXu-FQ9LGClPp1lrSX8q733o";
    private List<String> tareasSeleccionadas = new ArrayList<>();

    //para el repository:
    private JobRepository jobRepository;

    private EditText etLicensePlate, etDescriptionJob, etStartDate, etEndDate, etComments;
    private Button btnSearchCar, btnStartDate, btnEndDate, btnSaveJob, btnAddTask, btnSelectImage, btnModifyJob;
    private TextView tvCarInfo;
    private Spinner spinnerStatus;

    //para la iamgen:
    private ImageView imageViewJob;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri selectedImageUri = null;
    private Bitmap bitmap;
    private String base64Image = null;

    private int carIdSelect = -1; // se inicia con -1 pq es un valor que no puede haber


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)  {
        View view = inflater.inflate(R.layout.fragment_jobs_new, container, false);

        etLicensePlate = view.findViewById(R.id.editTextLicensePlate);
        btnSearchCar = view.findViewById(R.id.searchCarButton);
        tvCarInfo = view.findViewById(R.id.textViewCarInfo);
        etDescriptionJob = view.findViewById(R.id.editTextDescription);
        btnStartDate = view.findViewById(R.id.startDateButton);
        etStartDate = view.findViewById(R.id.editTextStartDate);
        btnSaveJob = view.findViewById(R.id.saveButton);
        btnAddTask = view.findViewById(R.id.addTaskButton);
        //Los campos que añado (fechaFin y estado ya estaban contemplados al principio):
        btnEndDate = view.findViewById(R.id.endDateButton);
        etEndDate = view.findViewById(R.id.editTextEndDate);
        spinnerStatus = view.findViewById(R.id.spinnerStatus);
        etComments = view.findViewById(R.id.editTextComments);
        imageViewJob = view.findViewById(R.id.imageViewJob);
        btnSelectImage = view.findViewById(R.id.btnSelectImage);
        btnModifyJob = view.findViewById(R.id.modifyJobButton);

        //queue = Volley.newRequestQueue(requireContext());

        //inicializo el repository:
        jobRepository = new JobRepository(requireContext());

        btnSearchCar.setOnClickListener(v -> {
            String licensePlate = etLicensePlate.getText().toString().trim();
            if (!licensePlate.isEmpty()) {
                //searchCarByMatricula(licensePlate); //era la llamada al método propio

                //Ahora se llama al método que, a su vez, llama al método que está en el repository:
                callSearchCarByMatricula(licensePlate);
            } else {
                Toast.makeText(getContext(), "Por favor, introduce la matrícula del vehículo", Toast.LENGTH_SHORT).show();
            }
        });

        Bundle args = getArguments();
        Log.d("SUPABASE", "Args recibidos: " + (args != null ? args.toString() : "null"));
        boolean isEditMode = args != null && args.containsKey("trabajo_id");

        if (isEditMode) {
            // Rellenar campos
            etDescriptionJob.setText(args.getString("descripcion", ""));
            etStartDate.setText(args.getString("fecha_inicio", ""));
            etEndDate.setText(args.getString("fecha_fin", ""));
            etComments.setText(args.getString("comentarios", ""));
            etLicensePlate.setText(args.getString("matricula", ""));
            // Spinner set estado según args.getString("estado")

            btnSaveJob.setVisibility(View.GONE);
            btnModifyJob.setVisibility(View.VISIBLE);

            btnModifyJob.setOnClickListener(v -> {
                //updateJob(args.getString("trabajo_id")); //era la llamada al metod propio

                //Llamada al método que, a su vez, encapsula la llamada a JobsRepository,
                //que es quien realiza ahora el HTTP - PATCH
                callUpdateJob(args.getString("trabajo_id"));
            });

        } else {
            btnSaveJob.setVisibility(View.VISIBLE);
            btnModifyJob.setVisibility(View.GONE);

            //Este boton solo funciona si se ha elegido una matri (!=0):
            btnSaveJob.setOnClickListener(v -> {
                if (carIdSelect != -1) {
                    /**
                    String description = etDescriptionJob.getText().toString().trim();
                    String startDate = etStartDate.getText().toString().trim();
                    String endDate = etEndDate.getText().toString().trim();
                    String status = spinnerStatus.getSelectedItem().toString();
                    String comment = etComments.getText().toString().trim();

                    //Esta era la llamada al método de la propia lase para guardar un trabajo.
                    //saveJob(carIdSelect, description, startDate, endDate, status, comment, base64Image, tareasSeleccionadas);
                     */

                    //Ahora se llama al método que hay en JobRepository (con un método intermedio):
                    callSaveJob(); // en esta clase se encapsula la llamada al repository

                } else {
                    Toast.makeText(getContext(), "Busca y selecciona un vehículo primero", Toast.LENGTH_LONG).show();
                }
            });
        }


        /**
         * Por ahora lo hago con botón, pero mejor buscar otro elemento más estético
         */
        btnStartDate.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                    (view1, year1, month1, dayOfMonth) -> {
                        //tiene que tener el formato que usa supabase para las fechas, sino lo guarda igual pero me sale mensa de error en la app:
                        String selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year1, month1 + 1, dayOfMonth);

                        etStartDate.setText(selectedDate);
                    },
                    year, month, day);
            datePickerDialog.show();
        });

        btnEndDate.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                    (view1, year1, month1, dayOfMonth) -> {
                        //tiene que tener el formato que usa supabase para las fechas, sino lo guarda igual pero me sale mensa de error en la app:
                        String selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year1, month1 + 1, dayOfMonth);

                        etEndDate.setText(selectedDate);
                    },
                    year, month, day);
            datePickerDialog.show();
        });

        //Para la imagen:
        btnSelectImage.setOnClickListener(v -> selectImage());

        String[] status = {"pendiente", "en curso", "finalizado"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, status);
        spinnerStatus.setAdapter(adapter);


        // Al pulsar este botón se llama al metodo que carga en pantalla las tareas tipo guardadas anteriormente en BD:
        btnAddTask.setOnClickListener(v ->
                //loadTypeTask()
                callLoadTypeTask() //LLAMADA AL METODO QUE, A SU VEZ, LLAMA AL REPOSTORIO PARA CARGAR (GET) TAREAS TIPO
        );

/**
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
        });*/

        return view;
    }

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
                imageViewJob.setImageBitmap(bitmap);
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
     * Desde este método se llama a JobsRepository, que es donde está el método para cargar (GET) las tareas preestablecidas
     */
    private void callLoadTypeTask() {
        jobRepository.loadTypeTasks(new JobRepository.TypeTaskLoadCallback() {
            @Override
            public void onLoaded(List<String> tareas) {
                showDialogSelectTask(tareas);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }



    /**
     * Desde este método se llama a JobsRepository, que es donde está el método para buscar coches por matrícula
     * @param licensePlate
     */
    private void callSearchCarByMatricula(String licensePlate) {
        jobRepository.searchCarByMatricula(licensePlate, new JobRepository.CarSearchCallback() {
            @Override
            public void onFound(int carId, String marca, String modelo) {
                carIdSelect = carId;
                Toast.makeText(getContext(), "Vehículo encontrado: " + marca + " " + modelo, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNotFound() {
                carIdSelect = -1;
                Toast.makeText(getContext(), "No se encontró ningún vehículo con esa matrícula. Añádelo desde Clientes.", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(String message) {
                carIdSelect = -1;
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
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

    /**
     * LLAMADA AL REPOSITORY JobRepository, que es quien ejecuta el método HTTP - POST para guardar un nuevo trabajo
     */
    private void callSaveJob() {
        SharedPreferences prefs = requireContext().getSharedPreferences("prefs", Context.MODE_PRIVATE);
        String mecanicoId = prefs.getString("mecanico_id", null);

        if (mecanicoId == null) {
            Toast.makeText(getContext(), "ID mecánico no encontrado. Por favor, inicie sesión nuevamente.", Toast.LENGTH_LONG).show();
            return;
        }

        String descripcion = etDescriptionJob.getText().toString().trim();
        String fechaInicio = etStartDate.getText().toString().trim();
        String fechaFin = etEndDate.getText().toString().trim();
        String estado = spinnerStatus.getSelectedItem().toString();
        String comentarios = etComments.getText().toString().trim();

        JSONObject jobJson = new JSONObject();
        try {
            jobJson.put("vehiculo_id", carIdSelect);
            jobJson.put("mecanico_id", mecanicoId);
            jobJson.put("descripcion", descripcion);
            jobJson.put("fecha_inicio", fechaInicio);
            if (!fechaFin.isEmpty()) jobJson.put("fecha_fin", fechaFin);
            if (!estado.isEmpty()) jobJson.put("estado", estado);
            if (!comentarios.isEmpty()) jobJson.put("comentarios", comentarios);
            if (base64Image != null) jobJson.put("imagen", "data:image/jpeg;base64," + base64Image);
        } catch (JSONException e) {
            Toast.makeText(getContext(), "Error preparando datos del trabajo", Toast.LENGTH_SHORT).show();
            return;
        }

        jobRepository.saveJob(jobJson, new JobRepository.JobSaveCallback() { //LLLAMADA AL REPOSITORY
            @Override
            public void onSuccess(int jobId) {
                Toast.makeText(getContext(), "Trabajo guardado con éxito (ID: " + jobId + ")", Toast.LENGTH_SHORT).show();
                if (!tareasSeleccionadas.isEmpty()) {
                    //saveTask(jobId, tareasSeleccionadas); //metodo directo para guardar tareas
                    callSaveTask(jobId); //llamada al metodo que a su vez llama al repository, que es quien hace la llamada http

                }
            }

            @Override
            public void onError(String message) {
                Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * LLAMADA AL REPOSITOY JobRepository, que es quien ejecuta el método HTTP - POST para guardar las tareas
     */
    private void callSaveTask(int jobId){
        jobRepository.saveTasks(jobId, tareasSeleccionadas, new JobRepository.TaskSaveCallback() {
            @Override
            public void onSuccess() {
                Log.d("SUPABASE", "Tareas guardadas correctamente.");
            }

            @Override
            public void onError(String message) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * LLAMADA AL REPOSITORY JobRepository, que es quien ejecuta el método HTTP - PATCH para actualizar un trabajo existente
     */
    private void callUpdateJob(String jobId) {
        JSONObject data = new JSONObject();
        try {
            data.put("descripcion", etDescriptionJob.getText().toString());
            data.put("fecha_inicio", etStartDate.getText().toString());
            data.put("fecha_fin", etEndDate.getText().toString());
            data.put("estado", spinnerStatus.getSelectedItem().toString());
            data.put("comentarios", etComments.getText().toString());
            if (base64Image != null) data.put("imagen", "data:image/jpeg;base64," + base64Image);
        } catch (JSONException e) {
            Toast.makeText(getContext(), "Error al preparar la actualización", Toast.LENGTH_SHORT).show();
            return;
        }

        jobRepository.updateJob(jobId, data, new JobRepository.JobUpdateCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(getContext(), "Trabajo actualizado correctamente", Toast.LENGTH_SHORT).show();
                requireActivity().getSupportFragmentManager().popBackStack();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }



    /**
     * guardarTareas()
     * Ya no se hace desde aqui, sino que se hace en el repository

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
    } */



    /**
     * Desactualizado, ahora se hace desde el repository
     * GUARDAR/CREAR UN NUEVO TRABAJO - guardarTrabajo
     * @param vehiculoId
     * @param descripcion
     * @param fechaInicio
     * @param tareasDescripcion
     *
     * Añadir los nuevos campos en el guardado del trabajo
     */
    /**
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
             Toast.makeText(getContext(), "Job guardado con éxito (ID: " + trabajoId + ")", Toast.LENGTH_SHORT).show();

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
                            Toast.makeText(getContext(), "Job guardado con éxito (ID: " + trabajoId + ")", Toast.LENGTH_SHORT).show();

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

/**            StringRequest request = new StringRequest(
                    Request.Method.POST,
                    urlTrabajos,
                    responseString -> {
                        try {
                            // Supabase devuelve un array JSON como string, por eso hay que parsearlo:
                            JSONArray responseArray = new JSONArray(responseString);
                            JSONObject trabajo = responseArray.getJSONObject(0);
                            int trabajoId = trabajo.getInt("id");

                            Toast.makeText(getContext(), "Job guardado con éxito (ID: " + trabajoId + ")", Toast.LENGTH_SHORT).show();

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
    }*/





    /**
     * AHORA ESTE MÉTODO SE EJECUTA EN JobREPOSITORY
     * Método que permite actualizar un tabajo ya existente en BD
     * Uso PATCH en lugar de PUT
     * @param id

    private void updateJob(String id) {
        try {
            JSONObject data = new JSONObject();
            data.put("descripcion", etDescriptionJob.getText().toString());
            data.put("fecha_inicio", etStartDate.getText().toString());
            data.put("fecha_fin", etEndDate.getText().toString());
            data.put("estado", spinnerStatus.getSelectedItem().toString());
            data.put("comentarios", etComments.getText().toString());
            data.put("imagen", base64Image); // puede ser null si no has seleccionado nueva

            String url = SUPABASE_URL + "/rest/v1/trabajos?id=eq." + id;

            StringRequest request = new StringRequest(
                    Request.Method.PATCH, //más seguro que PUT para supabase, ya que no modifica toda la fila sino solo los campos modificados
                    url,
                    response -> {
                        Toast.makeText(getContext(), "Trabajo actualizado correctamente", Toast.LENGTH_SHORT).show();
                        requireActivity().getSupportFragmentManager().popBackStack();
                    },
                    error -> {
                        Log.e("SUPABASE", "Error al actualizar trabajo", error);
                        Toast.makeText(getContext(), "Error al actualizar trabajo", Toast.LENGTH_SHORT).show();
                        if (error.networkResponse != null) {
                            Log.e("SUPABASE", new String(error.networkResponse.data));
                        }
                    }
            ) {
                @Override
                public byte[] getBody() {
                    return data.toString().getBytes(StandardCharsets.UTF_8);
                }

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
                    return headers;
                }
            };

            queue.add(request);

        } catch (JSONException e) {
            Toast.makeText(getContext(), "Error al preparar la actualización", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }*/



}
