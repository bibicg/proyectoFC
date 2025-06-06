package com.bcg.cartaller;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.bcg.cartaller.Adapters.JobAdapter;
import com.bcg.cartaller.Models.Customer;
import com.bcg.cartaller.Models.Job;
import com.bcg.cartaller.Models.Car;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Este fragmento es el que se carga por defecto en el main al iniciar la app, ya que es el
 * perfil del mecánico que ha iniciado sesión. Se le da la bienvenida usando su username y se muestran
 * por defecto en un RV los jobs que están sin finalizar (pendientes):
 */

// ProfileFragment.java actualizado con headers y control RLS (hay que activarlas en supabase)
public class ProfileFragment extends Fragment {
    private RecyclerView recyclerJobs;
    private JobAdapter adapter;
    private RequestQueue queue;
    //Añado las url a una variable para no tener que estar copiando todo el codigo contantemente:
    private final String SUPABASE_URL = "https://gtiqlopkoiconeivobxa.supabase.co";
    private final String API_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imd0aXFsb3Brb2ljb25laXZvYnhhIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDYxMjMyMTAsImV4cCI6MjA2MTY5OTIxMH0.T5MFUR9KAWXQOnoeZChYXu-FQ9LGClPp1lrSX8q733o";
    //atributos de clase para mostrar datos del mecánico logueado:
    private TextView tvUserName;
    private TextView tvUserStatus;
    private List<Job> jobs = new ArrayList<>();


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        tvUserName = view.findViewById(R.id.userName);
        tvUserStatus = view.findViewById(R.id.userUsername);

        recyclerJobs = view.findViewById(R.id.recyclerJobs);
        recyclerJobs.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new JobAdapter(jobs, job -> {
            JobsDetailFragment fragment = new JobsDetailFragment();
            Bundle bundle = new Bundle();
            bundle.putString("trabajo_id", job.getId());
            bundle.putString("estado", job.getStatus());
            bundle.putString("descripcion", job.getDescription());
            bundle.putString("fecha_inicio", job.getStartDate());
            bundle.putString("fecha_fin", job.getEndDate());
            bundle.putString("comentarios", job.getComment());
            bundle.putString("imagen", job.getImage());
            bundle.putString("matricula", job.getCar().getLicensePlate());
            bundle.putString("dni_cliente", job.getCar().getCustomer().getDni());
            bundle.putString("mecanico_id", job.getMechanicId());
            fragment.setArguments(bundle);

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        recyclerJobs.setAdapter(adapter);

        queue = Volley.newRequestQueue(getContext());
        showActiveJobs();

        showUserInformation();

        return view;
    }

    
    private void showActiveJobs() {
        //lo primero es recuperar el id del usuario para poder ver solo sus jobs
        SharedPreferences prefs = requireContext().getSharedPreferences("prefs", Context.MODE_PRIVATE);
        String mechanicId = prefs.getString("mecanico_id", null);

        if (mechanicId == null) {
            Toast.makeText(getContext(), "ID mecánico no encontrado", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("SUPABASE", "Usando mecanico_id: " + mechanicId);

        //es como una consulta sql hecha desde la app con las particularidades de supabase, como el "eq"
        //se mostrarán todos los jobs cuyo estado sea pendiente o en curso
        String url = SUPABASE_URL + "/rest/v1/trabajos" +
                "?select=id,estado,descripcion,fecha_inicio,fecha_fin,comentarios,imagen,vehiculos(matricula,cliente:clientes(dni))" +
                "&mecanico_id=eq." + mechanicId +
                "&estado=in.(pendiente,en%20curso)";





        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    List<Job> jobs = new ArrayList<>();
                    try {
                        Log.d("SUPABASE", "Respuesta completa: " + response.toString());
                        Log.d("SUPABASE", "Cantidad de trabajos: " + response.length());

                        if (response.length() == 0) {
                            Toast.makeText(getContext(), "No hay trabajos activos asignados.", Toast.LENGTH_LONG).show();
                        } else {
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject trabajoJson = response.getJSONObject(i);
                                Job job = new Job();
                                job.id = trabajoJson.getString("id");
                                job.status = trabajoJson.getString("estado");
                                job.description = trabajoJson.getString("descripcion");
                                job.image = trabajoJson.optString("imagen", null);

                                job.startDate = trabajoJson.optString("fecha_inicio", null);
                                job.endDate = trabajoJson.optString("fecha_fin", null);
                                job.comment = trabajoJson.optString("comentarios", null);

                                JSONObject vehiculoJson = trabajoJson.getJSONObject("vehiculos");
                                Car car = new Car();
                                car.licensePlate = vehiculoJson.getString("matricula");

                                JSONObject clienteJson = vehiculoJson.getJSONObject("cliente");

                                Customer customer = new Customer();
                                customer.dni = clienteJson.getString("dni");

                                car.customer = customer;
                                job.car = car;

                                jobs.add(job);
                            }
                            //adapter = new JobAdapter(jobs);
                            adapter = new JobAdapter(jobs, job -> {
                                JobsDetailFragment fragment = new JobsDetailFragment();//Se abre otro fragment con info más detallada del job clicado

                                Bundle bundle = new Bundle();
                                bundle.putString("trabajo_id", job.getId());
                                bundle.putString("estado", job.getStatus());
                                bundle.putString("descripcion", job.getDescription());
                                bundle.putString("fecha_inicio", job.getStartDate());
                                bundle.putString("fecha_fin", job.getEndDate());
                                bundle.putString("comentarios", job.getComment());

                                Log.d("IMAGEN", "Imagen del job: " + job.getImage());
                                bundle.putString("imagen", job.getImage());

                                bundle.putString("matricula", job.getCar().getLicensePlate());
                                bundle.putString("dni_cliente", job.getCar().getCustomer().getDni());
                                bundle.putString("mecanico_id", job.getMechanicId());

                                fragment.setArguments(bundle);

                                getParentFragmentManager()
                                        .beginTransaction()
                                        .replace(R.id.fragmentContainer, fragment) //ese fragment detalle se carga en el container de fragments de MainActivity
                                        .addToBackStack(null)
                                        .commit();
                            });

                            recyclerJobs.setAdapter(adapter);
                        }
                    } catch (JSONException e) {
                        Toast.makeText(getContext(), "Error al procesar datos", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Toast.makeText(getContext(), "Error al cargar jobs", Toast.LENGTH_SHORT).show();
                    Log.e("SUPABASE", "Error: " + error.toString());
                    if (error.networkResponse != null) {
                        Log.e("SUPABASE", "Código HTTP: " + error.networkResponse.statusCode);
                        Log.e("SUPABASE", "Respuesta: " + new String(error.networkResponse.data));
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                SharedPreferences prefs = requireContext().getSharedPreferences("SupabasePrefs", Context.MODE_PRIVATE);
                String token = prefs.getString("access_token", null);

                Map<String, String> headers = new HashMap<>();
                headers.put("apikey", API_ANON_KEY);
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        queue.add(request);
    }

    private void showUserInformation() {
        //lo primero es recuperar el id del usuario para poder ver sus datos
        SharedPreferences prefs = requireContext().getSharedPreferences("prefs", Context.MODE_PRIVATE);
        String mecanicoId = prefs.getString("mecanico_id", null);

        /**
         * url del endpoint + parámetros de consulta tipo select sql:
         * LA EQUIVALENCIA ES:
         *      SELECT nombre, apellidos, cargo
         *      FROM mecanicos
         *      WHERE id = '461e176c-821a-4b19-b0ec-591c6ed895c7';
         */
        String url = SUPABASE_URL + "/rest/v1/mecanicos" +
                "?select=nombre,apellidos,cargo" +
                "&id=eq." + mecanicoId;

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        if (response.length() > 0) {
                            JSONObject mechanic = response.getJSONObject(0);
                            String name = mechanic.getString("nombre");
                            String surname = mechanic.getString("apellidos");
                            String status = mechanic.optString("cargo", "Sin cargo");

                            tvUserName.setText(name + " " + surname);
                            tvUserStatus.setText(status != null ? status : "Sin cargo");
                        } else {
                            Toast.makeText(getContext(), "Datos del usuario no encontrados", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(getContext(), "Error al procesar datos del usuario", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Toast.makeText(getContext(), "Error al cargar datos del usuario", Toast.LENGTH_SHORT).show();
                    Log.e("SUPABASE", "Error: " + error.toString());
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                SharedPreferences prefs = requireContext().getSharedPreferences("SupabasePrefs", Context.MODE_PRIVATE);
                String token = prefs.getString("access_token", null);

                Map<String, String> headers = new HashMap<>();
                headers.put("apikey", API_ANON_KEY);
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        queue.add(request);
    }

}


