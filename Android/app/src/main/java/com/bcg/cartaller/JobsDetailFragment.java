package com.bcg.cartaller;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentResultListener;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bcg.cartaller.Repositories.JobRepository;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * PASO INTERMEDIO ENTRE EL CLIC DE UN TRABAJO EN EL RV Y VISUALIZARLO EN EL FORMULARIO PARA EDITARLO.
 * SIRVE PARA VISUALIZAR LA INFORMACIÓN DETALLADA DE UN TRABAJO (el item del RV muestra un resumen).
 * Se viene aqui desde el RV de JobsSearchFragment y el RV de ProfileFragment.
 * Desde aqui se da la posibilidad de eliminar un trabajo (único objeto que se permite borrar desde la app)
 * y de modificarlo, abriendo los detalles del trabajo en el formulario de trabajos (el mismo que se usa para crear)
 */
public class JobsDetailFragment extends Fragment {
    private RequestQueue queue;
    private final String API_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imd0aXFsb3Brb2ljb25laXZvYnhhIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDYxMjMyMTAsImV4cCI6MjA2MTY5OTIxMH0.T5MFUR9KAWXQOnoeZChYXu-FQ9LGClPp1lrSX8q733o";
    private TextView tvDescription, tvStartDate, tvEndDate, tvStatus, tvComments, tvLicensePlate, tvDniCustomer;
    private TextView tvTaskTitle, tvTaskList;
    private ImageView ivJob;
    private Button btnUpdate, btnDelete;
    private String trabajoId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_jobs_detail, container, false);

        tvDescription = view.findViewById(R.id.textViewDescription);
        tvStartDate = view.findViewById(R.id.textViewStartDate);
        tvEndDate = view.findViewById(R.id.textViewEndDate);
        tvStatus = view.findViewById(R.id.textViewStatus);
        tvComments = view.findViewById(R.id.textViewComments);
        tvTaskTitle = view.findViewById(R.id.textViewTaskTitle);
        tvTaskList = view.findViewById(R.id.textViewTaskList);
        tvLicensePlate = view.findViewById(R.id.textViewLicensePlate);
        tvDniCustomer = view.findViewById(R.id.textViewDni);
        ivJob = view.findViewById(R.id.imageViewJob);
        btnUpdate = view.findViewById(R.id.modifyJobButton);
        btnDelete = view.findViewById(R.id.deleteJobButton);

        queue = Volley.newRequestQueue(getContext());

        Bundle args = getArguments();
        if (args != null) {
            tvDescription.setText(args.getString("descripcion", ""));
            tvStartDate.setText(args.getString("fecha_inicio", ""));
            tvEndDate.setText(args.getString("fecha_fin", ""));
            tvStatus.setText(args.getString("estado", ""));
            tvComments.setText(args.getString("comentarios", ""));
            //tvTaskTitle.setText(args.getString("descripcion", ""));
            tvTaskList.setText(args.getString("tareas", ""));
            tvLicensePlate.setText(args.getString("matricula", ""));
            tvDniCustomer.setText(args.getString("dni_cliente", ""));
            trabajoId = args.getString("trabajo_id", null);

            //para mostrar las tareas:
            int jobId = Integer.parseInt(args.getString("trabajo_id", "0"));
            loadTasksForJob(jobId); //llamada al método

            //Para asegurar que la img se vea tanto si la hay como si no:
            String base64Image = args.getString("imagen", null);

            if (base64Image != null && !base64Image.trim().isEmpty()) {
                try {
                    if (base64Image.contains(",")) {
                        base64Image = base64Image.substring(base64Image.indexOf(",") + 1);
                    }

                    byte[] decodedBytes = Base64.decode(base64Image, Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                    if (bitmap != null) {
                        ivJob.setVisibility(View.VISIBLE); //iv visible solo si hay imagen asociada
                        ivJob.setImageBitmap(bitmap);
                    } else {
                        ivJob.setVisibility(View.GONE); //no muestro el iv x defecto si no hay imagen real
                    }
                } catch (IllegalArgumentException e) {
                    Log.e("IMAGEN", "Base64 malformado: " + e.getMessage());
                    ivJob.setVisibility(View.GONE);
                }
            } else {
                ivJob.setVisibility(View.GONE); //no muestro el iv x defecto si no hay imagen real
            }



            /**
             * ESTE BOTÓN ABRE EL FORMULARIO, DENTRO DE JOBS NEW FRAGMENT, CON TODA LA INFO DEL TRABAJO CARGADA
             * EN LOS CAMPOS CORRESPONDIENTES. DESDE AHÍ SERÁ DESDE DONDE SE PERMITA EDITAR EL TRABAJO Y ENVIAR
             * EL RESULTADO DE DICHA EDICIÓN A SUPABASE A TRAVÉS DE UN PATCH.
             */
            btnUpdate.setOnClickListener(v -> {
                JobsNewFragment editFragment = new JobsNewFragment();
                editFragment.setArguments(args);
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragmentContainer, editFragment) //container de fragments de MainActivity
                        .addToBackStack(null)
                        .commit();
            });


            /**
             * ESTE BOTÓN ABRE UNA CONFIRMACIÓN PARA ELIMINAR EL TRABAJO
             */
            btnDelete.setOnClickListener(v ->
                    showConfirmationDelete(args.getString("trabajo_id")));
        }

        /**
         * Para que, una vez hechas las modificaciones, al volver atrás, se vean los datos del trabajo modificados.
         * Sin esto, se muestran en el estado previo a la modificación, y es confuso.
         */
        getParentFragmentManager().setFragmentResultListener("update_success", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                boolean update = result.getBoolean("trabajo_actualizado", false);
                if (update) {
                    Toast.makeText(getContext(), "Trabajo actualizado", Toast.LENGTH_SHORT).show();
                    reloadData(); // metodo que vuelve a cargar el trabajo para que los datos se muestren modificados
                }
            }
        });


        return view;
    }

    /**
     * mostrarConfirmacionEliminar
     * @param trabajoId
     * PERMITE QUE EL USUARIO HAGA UNA DOBLE CONFIRMACIÓN ANTES DE ELIMINAR UN TRABAJO.
     * Si la acepta, se llama al método que elimina.
     */
    private void showConfirmationDelete(String trabajoId) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Eliminar trabajo")
                .setMessage("¿Estás seguro de que deseas eliminar este trabajo?")
                .setIcon(R.drawable.warning_dialog)
                .setPositiveButton("Sí", (dialog, which) -> deleteJob(trabajoId)) //llamada a eliminar
                .setNegativeButton("Cancelar", null)
                .show();
    }


    /**
     * eliminarTrabajo
     * @param trabajoId
     */
    private void deleteJob(String trabajoId) {
        String url = "https://gtiqlopkoiconeivobxa.supabase.co/rest/v1/trabajos?id=eq." + trabajoId;
        Log.d("SUPABASE", "Intentando eliminar trabajo con ID: " + trabajoId);
        Log.d("SUPABASE", "URL: " + url);

        SharedPreferences prefs = requireContext().getSharedPreferences("prefs", Context.MODE_PRIVATE);
        String mechanicId = prefs.getString("mecanico_id", null);
        Log.d("SUPABASE", "Verificando mecanico_id actual (SharedPreferences): " + mechanicId);

        StringRequest request = new StringRequest(
                Request.Method.DELETE,
                url,
                response -> {
                    //Toast.makeText(getContext(), "Job eliminado correctamente", Toast.LENGTH_SHORT).show();
                    //requireActivity().getSupportFragmentManager().popBackStack();

                    //se muestra un dialog de confirmacion en lugar de un toast:
                    new AlertDialog.Builder(requireContext())
                            .setTitle("Trabajo eliminado")
                            .setMessage("El trabajo ha sido eliminado correctamente.")
                            .setIcon(R.drawable.ok_dialog)
                            .setPositiveButton("Aceptar", (dialog, which) -> {
                                FragmentManager fm = requireActivity().getSupportFragmentManager();
                                if (fm.getBackStackEntryCount() > 0) {
                                    fm.popBackStack(); //vuelta a la pantalla anterior
                                }
                            })
                            .show();
                },
                error -> {
                    Toast.makeText(getContext(), "Error al eliminar el trabajo", Toast.LENGTH_SHORT).show();
                    if (error.networkResponse != null) {
                        Log.e("SUPABASE", "Código: " + error.networkResponse.statusCode);
                        Log.e("SUPABASE", new String(error.networkResponse.data));
                    } else {
                        Log.e("SUPABASE", "Error sin respuesta de red: " + error.toString());
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                SharedPreferences prefs = requireContext().getSharedPreferences("SupabasePrefs", Context.MODE_PRIVATE);
                String token = prefs.getString("access_token", "");
                Log.d("SUPABASE", "Token: " + token);

                Map<String, String> headers = new HashMap<>();
                headers.put("apikey", API_ANON_KEY);
                headers.put("Authorization", "Bearer " + token);
                headers.put("Prefer", "return=minimal");
                return headers;
            }
        };

        queue.add(request);
    }

    /**
    private void recargarDatos() {
        String trabajoId = getArguments().getString("trabajo_id");
        if (trabajoId == null) return;

        String url = "https://gtiqlopkoiconeivobxa.supabase.co/rest/v1/trabajos?id=eq." + trabajoId + "&select=*";

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    if (response.length() > 0) {
                        JSONObject job = response.optJSONObject(0);
                        if (job != null) {
                            tvDescription.setText(job.optString("descripcion", ""));
                            tvStartDate.setText(job.optString("fecha_inicio", ""));
                            tvEndDate.setText(job.optString("fecha_fin", ""));
                            tvStatus.setText(job.optString("estado", ""));
                            tvComments.setText(job.optString("comentarios", ""));
                            tvLicensePlate.setText(job.optString("matricula", ""));
                            tvDniCustomer.setText(job.optString("dni_cliente", ""));

                            String base64Image = job.optString("imagen", null);
                            if (base64Image != null && base64Image.contains(",")) {
                                base64Image = base64Image.substring(base64Image.indexOf(",") + 1);
                            }

                            try {
                                byte[] decodedBytes = Base64.decode(base64Image, Base64.DEFAULT);
                                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                                ivJob.setImageBitmap(bitmap);
                            } catch (Exception e) {
                                ivJob.setImageResource(R.drawable.iconos_coche_peque_bicolor);
                            }
                        }
                    }
                },
                error -> Log.e("SUPABASE", "Error al recargar trabajo: " + error.toString())
        ) {
            @Override
            public Map<String, String> getHeaders() {
                SharedPreferences prefs = requireContext().getSharedPreferences("SupabasePrefs", Context.MODE_PRIVATE);
                String token = prefs.getString("access_token", "");

                Map<String, String> headers = new HashMap<>();
                headers.put("apikey", API_ANON_KEY);
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        queue.add(request);
    }*/

    //en este se muestra la imagen
    //pero no tareas ni matri ni dni
    //en el anterior ninguno de los 3
    private void reloadData() {
        String jobId = getArguments().getString("trabajo_id");
        if (jobId == null) {
            Log.e("RECARGAR", "No se encontró trabajo_id. No se puede recargar.");
            return;
        }

        String url = "https://gtiqlopkoiconeivobxa.supabase.co/rest/v1/trabajos?id=eq." + jobId + "&select=*";

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    if (response.length() > 0) {
                        JSONObject job = response.optJSONObject(0);
                        if (job != null) {
                            //actualiza la vista:
                            String description = job.optString("descripcion", "");
                            String startDate = job.optString("fecha_inicio", "");
                            String endDate = job.optString("fecha_fin", "");
                            String status = job.optString("estado", "");
                            String comments = job.optString("comentarios", "");
                            String licensePlate = job.optString("matricula", "");
                            String dni = job.optString("dni_cliente", "");
                            String base64Image = job.optString("imagen", null);

                            tvDescription.setText(description);
                            tvStartDate.setText(startDate);
                            tvEndDate.setText(endDate);
                            tvStatus.setText(status);
                            tvComments.setText(comments);
                            tvLicensePlate.setText(licensePlate);
                            tvDniCustomer.setText(dni);

                            Log.d("RECARGAR", "matricula: " + job.optString("matricula"));
                            Log.d("RECARGAR", "dni_cliente: " + job.optString("dni_cliente"));


                            if (base64Image != null && base64Image.contains(",")) {
                                base64Image = base64Image.substring(base64Image.indexOf(",") + 1);
                            }

                            try {
                                byte[] decodedBytes = Base64.decode(base64Image, Base64.DEFAULT);
                                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                                ivJob.setVisibility(View.VISIBLE);
                                ivJob.setImageBitmap(bitmap);
                            } catch (Exception e) {
                                ivJob.setVisibility(View.GONE);
                                Log.e("IMAGEN", "Error al decodificar imagen actualizada: " + e.getMessage());
                            }

                            //recarga las tareas asociadas:
                            try {
                                int idJob = Integer.parseInt(jobId);
                                loadTasksForJob(idJob);
                            } catch (NumberFormatException e) {
                                Log.e("TAREAS", "ID de trabajo inválido: " + e.getMessage());
                            }

                            //aqui se actualizan los argumentos por si se vuelven a usar:
                            Bundle updatedArgs = getArguments();
                            if (updatedArgs != null) {
                                updatedArgs.putString("descripcion", description);
                                updatedArgs.putString("fecha_inicio", startDate);
                                updatedArgs.putString("fecha_fin", endDate);
                                updatedArgs.putString("estado", status);
                                updatedArgs.putString("comentarios", comments);
                                updatedArgs.putString("matricula", licensePlate);
                                updatedArgs.putString("dni_cliente", dni);
                                updatedArgs.putString("imagen", job.optString("imagen", null));
                            }
                        }
                    }
                },
                error -> Log.e("SUPABASE", "Error al recargar trabajo: " + error.toString())
        ) {
            @Override
            public Map<String, String> getHeaders() {
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
     * Método que llama al repository, para poder mostrar las tareas asociadas al trabajo
     */
    private void loadTasksForJob(int jobId) {
        JobRepository jobRepository = new JobRepository(requireContext());

        jobRepository.getTasksByJobId(jobId, new JobRepository.TaskLoadCallback() {
            @Override
            public void onSuccess(List<String> tareas) {
                if (tareas.isEmpty()) {
                    tvTaskTitle.setVisibility(View.GONE);
                    tvTaskList.setVisibility(View.GONE);
                    return;
                }

                StringBuilder sb = new StringBuilder();
                for (String tarea : tareas) {
                    sb.append("• ").append(tarea).append("\n");
                }

                tvTaskTitle.setVisibility(View.VISIBLE);
                tvTaskList.setVisibility(View.VISIBLE);
                tvTaskList.setText(sb.toString());
            }

            @Override
            public void onError(String message) {
                tvTaskTitle.setText("Tareas: error al cargar");
                tvTaskList.setText("");
            }
        });
    }

}
