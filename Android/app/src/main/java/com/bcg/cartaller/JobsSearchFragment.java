package com.bcg.cartaller;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.content.SharedPreferences;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.bcg.cartaller.Adapters.TrabajoAdapter;
import com.bcg.cartaller.Models.Cliente;
import com.bcg.cartaller.Models.Trabajo;
import com.bcg.cartaller.Models.Vehiculo;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Desde este fragment el mecánico podrá buscar trabajos, bien por estado del trabajo,
 * la matrícula del vehículo implicado en el trabajo o el dni del cliente.
 * Busquedas(son tipo.  sql):
 * TODOS LOS TRABAJOS: GET /trabajos?select=*,vehiculos(*,clientes(*))&mecanico_id
 * TRABAJOS POR ESTADO: GET /trabajos?select=*,vehiculos(*,clientes(*))&estado=eq.pendiente&mecanico_id
 * TRABAJOS POR MATRICULA: GET /trabajos?select=*,vehiculos(*,clientes(*))&vehiculos.matricula
 * TRABAJOS POR CLIENTE: como el cliente no está directamente en la tabla trabajos, sino que se llega a él
 * a través de la matri. del vehículo, lo mejor es hacer una view en supabase, que es como una "consulta" pre-guardada
 */
public class JobsSearchFragment extends Fragment {
    private RequestQueue queue;
    private final String SUPABASE_URL = "https://gtiqlopkoiconeivobxa.supabase.co";
    private final String API_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imd0aXFsb3Brb2ljb25laXZvYnhhIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDYxMjMyMTAsImV4cCI6MjA2MTY5OTIxMH0.T5MFUR9KAWXQOnoeZChYXu-FQ9LGClPp1lrSX8q733o";
    private List<Trabajo> trabajos = new ArrayList<>();
    private TrabajoAdapter adapter;

    public JobsSearchFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_jobs_search, container, false);

        /**
         * Finalmente lllamo a este metodo desde JobsFragment, pprque sino no consigo que se vea
         * Lo tengo que hacer publico para poder llamarlo desde la clase padre
         */
        // Al entrar en este fragment, se carga un Dialog que muestra las opciones de búsqueda:
        // mostrarDialogBusqueda();

        queue = Volley.newRequestQueue(requireContext());


        //Recicler view (el mismo que usé en Profile Fragment) para cargar los trabajos:
        RecyclerView recyclerView = view.findViewById(R.id.recyclerTrabajos);
        //adapter = new TrabajoAdapter(trabajos); //este era antes de poder pasar la info al formulario para modificar
        adapter = new TrabajoAdapter(trabajos, new TrabajoAdapter.OnTrabajoClickListener() {
            @Override
            public void onModificarTrabajoClick(Trabajo trabajo) {
                //JobsNewFragment fragment = new JobsNewFragment();
                JobsDetailFragment fragment = new JobsDetailFragment();

                Bundle bundle = new Bundle();
                bundle.putString("trabajo_id", trabajo.getId());
                bundle.putString("estado", trabajo.getEstado());
                bundle.putString("descripcion", trabajo.getDescripcion());
                bundle.putString("fecha_inicio", trabajo.getFecha_inicio());
                bundle.putString("fecha_fin", trabajo.getFecha_fin());
                bundle.putString("comentarios", trabajo.getComentarios());
                bundle.putString("imagen", trabajo.getImagen());
                bundle.putString("matricula", trabajo.getVehiculo().getMatricula());
                bundle.putString("dni_cliente", trabajo.getVehiculo().getCliente().getDni());
                bundle.putString("mecanico_id", trabajo.getMecanico_id());
                fragment.setArguments(bundle);

                /**getParentFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragmentContainer, fragment)
                        .addToBackStack(null)
                        .commit();*/
                /**getParentFragmentManager() //se carga, pero dentro de JobsFragment, y se ven los btn superiores
                        .beginTransaction()
                        .replace(R.id.jobsGeneralContainer, fragment)
                        .addToBackStack(null)
                        .commit();*/
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragmentContainer, fragment) //se abre el detalle en el contenedor de fragments del main para evitar los btn superiores de JobsFragment
                        .addToBackStack(null)
                        .commit();

            }
        });


        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        return view;
    }

    //Este metodo es el que muestra el dialog y permite elegir la opción de busqueda:
   //Lopodría hacer como el de newFragment de las tareas, que es de opción múltiple
    public void mostrarDialogBusqueda() {
        CharSequence[] opciones = {"Por estado", "Por DNI", "Por matrícula"};

        new AlertDialog.Builder(getContext())
                .setTitle("Filtro de búsqueda:")
                .setItems(opciones, (dialog, which) -> {
                    switch (which) {
                        case 0: buscarPorEstado(); break;
                        case 1: buscarPorDni(); break;
                        case 2: buscarPorMatricula(); break;
                    }
                }).show();
    }

    //Las 3 opciones dentro del DIALOG: que son las busquedas por los 3 estados existentes en la tabla: PENDIENTE - EN CURSO - ACABADO
    private void buscarPorEstado() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Selecciona estado:")
                .setItems(new CharSequence[]{"Pendiente", "En curso", "Acabado"}, (dialog, which) -> {
                    switch (which) {
                        case 0: mostrarTrabajosSegunFiltro("pendiente", null, null); break;
                        case 1: mostrarTrabajosSegunFiltro("en curso", null, null); break;
                        case 2: mostrarTrabajosSegunFiltro("acabado", null, null); break;
                    }
                }).show();
    }

    /**
    private void buscarPorDni() {
        EditText input = new EditText(getContext());
        input.setHint("Introduce el DNI");

        new AlertDialog.Builder(getContext())
                .setTitle("Buscar por DNI")
                .setView(input)
                .setPositiveButton("Buscar", (dialog, which) -> {
                    String dni = input.getText().toString();
                    if (!dni.isEmpty()) mostrarTrabajosSegunFiltro(null, dni, null);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void buscarPorMatricula() {
        EditText input = new EditText(getContext());
        input.setHint("Introduce la matrícula");

        new AlertDialog.Builder(getContext())
                .setTitle("Buscar por matrícula")
                .setView(input)
                .setPositiveButton("Buscar", (dialog, which) -> {
                    String matricula = input.getText().toString();
                    if (!matricula.isEmpty()) mostrarTrabajosSegunFiltro(null, null, matricula);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }*/
    //Para matricula y DNI creo un dialog personalizado (como en la práctica de Cocktails).
    //No uso el metodo showDialog para los elementos del xml porque son distintos campos.
    //si me da tiempo modifico la estetica en paso final:
    private void buscarPorDni() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_search, null);
        TextView titleDialog = dialogView.findViewById(R.id.titleDialog);
        EditText inputField = dialogView.findViewById(R.id.dialogInputText);
        titleDialog.setText("Buscar por DNI");
        inputField.setHint("Introduce el DNI del cliente");

        new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .setPositiveButton("Buscar", (dialog, which) -> {
                    String dni = inputField.getText().toString();
                    if (!dni.isEmpty()) mostrarTrabajosSegunFiltro(null, dni, null);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void buscarPorMatricula() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_search, null);
        TextView titleDialog = dialogView.findViewById(R.id.titleDialog);
        EditText inputField = dialogView.findViewById(R.id.dialogInputText);
        titleDialog.setText("Buscar por matrícula");
        inputField.setHint("Introduce la matrícula del vehículo");

        new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .setPositiveButton("Buscar", (dialog, which) -> {
                    String matricula = inputField.getText().toString();
                    if (!matricula.isEmpty()) mostrarTrabajosSegunFiltro(null, null, matricula);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    /**
     * Funcion generica para hacer la busqueda, dentro de ella hay un if, y dependiendo de
     * por donde entra, buscará por un campo opor otro
     */
    private void mostrarTrabajosSegunFiltro(String estado, String dniCliente, String matriculaVehiculo) {
        //primero se recupera el id del mecanico a traves de SP:
        SharedPreferences prefs = requireContext().getSharedPreferences("prefs", Context.MODE_PRIVATE);
        String mecanicoId = prefs.getString("mecanico_id", null);

        if (mecanicoId == null) {
            Toast.makeText(getContext(), "ID mecánico no encontrado", Toast.LENGTH_SHORT).show();
            return;
        }

        String baseUrl = SUPABASE_URL + "/rest/v1/trabajos?select=*,vehiculos(*,clientes(*))";

        // Filtros dinámicos
        List<String> filtros = new ArrayList<>();
        filtros.add("mecanico_id=eq." + mecanicoId);

        if (estado != null && !estado.equals("todos")) {
            filtros.add("estado=eq." + estado);
        }
        if (dniCliente != null && !dniCliente.isEmpty()) {
            filtros.add("vehiculos.clientes.dni=eq." + dniCliente);
        }
        if (matriculaVehiculo != null && !matriculaVehiculo.isEmpty()) {
            filtros.add("vehiculos.matricula=eq." + matriculaVehiculo);
        }

        String url = baseUrl + "&" + TextUtils.join("&", filtros);

        Log.d("Trabajos", "Cargando trabajos con: estado=" + estado + ", dni=" + dniCliente + ", matricula=" + matriculaVehiculo + ", mecanicoId=" + mecanicoId);

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    trabajos.clear();
                    if (response.length() == 0) {
                        String mensaje = "No se encontraron trabajos";
                        if (estado != null && !estado.equals("todos")) {
                            mensaje += " con estado '" + estado + "'";
                        } else if (dniCliente != null && !dniCliente.isEmpty()) {
                            mensaje += " con DNI '" + dniCliente + "'";
                        } else if (matriculaVehiculo != null && !matriculaVehiculo.isEmpty()) {
                            mensaje += " con matrícula '" + matriculaVehiculo + "'";
                        }
                        Toast.makeText(getContext(), mensaje, Toast.LENGTH_LONG).show();
                    } else {
                        for (int i = 0; i < response.length(); i++) {
                            try {
                                JSONObject trabajoJson = response.getJSONObject(i);
                                JSONObject vehiculoJson = trabajoJson.getJSONObject("vehiculos");
                                JSONObject clienteJson = vehiculoJson.getJSONObject("clientes");

                                Log.d("JSON_DEBUG", "clienteJson = " + clienteJson.toString());

                                Cliente cliente = new Cliente(clienteJson.getString("dni"));
                                /**Cliente cliente = new Cliente(
                                        clienteJson.optInt("id", -1),
                                        clienteJson.optString("dni", "")
                                );*/


                                Vehiculo vehiculo = new Vehiculo(vehiculoJson.getString("matricula"), cliente);
                                Trabajo trabajo = new Trabajo(
                                        String.valueOf(trabajoJson.getInt("id")),
                                        trabajoJson.getString("estado"),
                                        trabajoJson.getString("descripcion"),
                                        vehiculo
                                );
                                Log.d("Trabajos", "Respuesta recibida: " + response.toString());

                                trabajos.add(trabajo);
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Log.e("Trabajos", "Error al parsear JSON", e);
                            }
                        }
                    }
                    adapter.notifyDataSetChanged();
                },
                error -> Log.e("Volley", "Error al cargar trabajos", error)
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


}