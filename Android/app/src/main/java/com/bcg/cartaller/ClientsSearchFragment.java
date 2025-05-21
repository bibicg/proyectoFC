package com.bcg.cartaller;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.bcg.cartaller.Adapters.ClienteAdapter;
import com.bcg.cartaller.Models.Cliente;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.android.volley.AuthFailureError;
import java.util.HashMap;
import java.util.Map;

/**
 * Desde este fragment el mecánico podrá buscar clientes, bien por su dni o por la matrícula de
 * uno de sus vehículos.
 *
 * SIGUE LA  MISMA ESTRUCTURA QUE JobsSearchFragment.
 */
public class ClientsSearchFragment extends Fragment {
    private RequestQueue queue;
    private final String SUPABASE_URL = "https://gtiqlopkoiconeivobxa.supabase.co";
    private final String API_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imd0aXFsb3Brb2ljb25laXZvYnhhIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDYxMjMyMTAsImV4cCI6MjA2MTY5OTIxMH0.T5MFUR9KAWXQOnoeZChYXu-FQ9LGClPp1lrSX8q733o";
    private List<Cliente> clientes = new ArrayList<>();
    private ClienteAdapter adapter;

    public ClientsSearchFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_clients_search, container, false);

        queue = Volley.newRequestQueue(requireContext());

        //Recicler view (el mismo que usé en Profile Fragment) para cargar los trabajos:
        RecyclerView recyclerView = view.findViewById(R.id.recyclerClientes);
        adapter = new ClienteAdapter(clientes, new ClienteAdapter.OnClienteClickListener() {
            @Override
            public void onDetalleClienteClick(Cliente cliente) {
                ClientDetailFragment fragment = new ClientDetailFragment();
                Bundle args = new Bundle();
                //args.putString("cliente_dni", cliente.dni);
                args.putInt("cliente_id", cliente.getId());
                fragment.setArguments(args);
                getParentFragmentManager()
                        .beginTransaction()
                        .replace(R.id.clientsGeneralContainer, fragment)
                        .addToBackStack(null)
                        .commit();
            }

            @Override
            public void onAgregarVehiculoClick(Cliente cliente) {
                CarNewFragment fragment = new CarNewFragment();
                Bundle args = new Bundle();
                //args.putString("cliente_dni", cliente.dni);
                args.putInt("cliente_id", cliente.getId());
                fragment.setArguments(args);
                getParentFragmentManager()
                        .beginTransaction()
                        .replace(R.id.clientsGeneralContainer, fragment)
                        .addToBackStack(null)
                        .commit();
            }

            @Override
            public void onModificarClienteClick(Cliente cliente) {
                ClientsNewFragment fragment = new ClientsNewFragment();

                Bundle args = new Bundle();
                args.putString("dni", cliente.getDni());
                args.putString("nombre", cliente.getNombre());
                args.putString("apellidos", cliente.getApellidos());
                args.putString("telefono", cliente.getTelefono());
                args.putString("email", cliente.getEmail());
                args.putString("direccion", cliente.getDireccion());
                fragment.setArguments(args);

                getParentFragmentManager()
                        .beginTransaction()
                        .replace(R.id.clientsGeneralContainer, fragment)
                        .addToBackStack(null)
                        .commit();
            }

        });
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        return view;
    }

    //añadir el método con las opciones del Dialog, como en la búsqueda de tareas:
    //antes mostrarDialogBusqueda
    public void showSearchDialog() {
        CharSequence[] opciones = {"Por DNI", "Por matrícula"};

        new AlertDialog.Builder(getContext())
                .setTitle("Buscar cliente por:")
                .setItems(opciones, (dialog, which) -> {
                    switch (which) {
                        case 0: searchByDni(); break;
                        case 1: searchByMatricula(); break;
                    }
                }).show();
    }

    //Para matricula y DNI creo un dialog personalizado (como en la práctica de Cocktails).
    //No uso el metodo showDialog para los elementos del xml porque son distintos campos.
    //si me da tiempo modifico la estetica en paso final:

    //antes buscarPorDni
    private void searchByDni() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_search, null);
        TextView titleDialog = dialogView.findViewById(R.id.titleDialog);
        EditText inputField = dialogView.findViewById(R.id.dialogInputText);
        titleDialog.setText("Buscar por DNI");
        inputField.setHint("Introduce el DNI del cliente");

        new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .setPositiveButton("Buscar", (dialog, which) -> {
                    String dni = inputField.getText().toString();
                    if (!dni.isEmpty()) searchClients("dni", dni);

                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    //antes buscarPorMatricula
    private void searchByMatricula() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_search, null);
        TextView titleDialog = dialogView.findViewById(R.id.titleDialog);
        EditText inputField = dialogView.findViewById(R.id.dialogInputText);
        titleDialog.setText("Buscar por matrícula");
        inputField.setHint("Introduce la matrícula del vehículo");

        new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .setPositiveButton("Buscar", (dialog, which) -> {
                    String matricula = inputField.getText().toString();
                    if (!matricula.isEmpty()) searchClients("matricula", matricula);


                })
                .setNegativeButton("Cancelar", null)
                .show();
    }


    /**
     * Funcion para ver los clientes (buscarClientes al comienzo)
     */
    private void searchClients(String tipo, String valor) {
        String url;
        if (tipo.equals("dni")) {
            //url = SUPABASE_URL + "/rest/v1/clientes?dni=eq." + valor; //asi no consigo todoslos datos del cliente
            url = SUPABASE_URL + "/rest/v1/clientes?dni=eq." + valor + "&select=id,dni,nombre,apellidos,telefono,email,direccion";
        } else {
            url = SUPABASE_URL + "/rest/v1/vehiculos?matricula=eq." + valor + "&select=cliente(*)";
        }
        Log.d("CLIENTS_SEARCH", "URL de búsqueda: " + url);

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    clientes.clear();
                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject clienteJson = tipo.equals("dni") ? response.getJSONObject(i)
                                    : response.getJSONObject(i).getJSONObject("cliente");

                            /*Cliente cliente = new Cliente(
                                    clienteJson.getInt("id"),
                                    clienteJson.getString("dni"),
                                    clienteJson.optString("nombre", ""),
                                    clienteJson.optString("telefono", "")
                            );*/
                            Cliente cliente = new Cliente(
                                    clienteJson.getInt("id"),
                                    clienteJson.getString("dni"),
                                    clienteJson.optString("nombre", ""),
                                    clienteJson.optString("apellidos", ""),
                                    clienteJson.optString("telefono", ""),
                                    clienteJson.optString("email", ""),
                                    clienteJson.optString("direccion", "")
                            );

                            clientes.add(cliente);
                        }
                    } catch (JSONException e) {
                        Log.e("CLIENTS_SEARCH", "Error parseando JSON", e);
                    }
                    adapter.notifyDataSetChanged();
                },
                error -> {
                    Log.e("CLIENTS_SEARCH", "Error al cargar clientes", error);
                    if (error.networkResponse != null) {
                        int statusCode = error.networkResponse.statusCode;
                        byte[] errorBytes = error.networkResponse.data;
                        String errorMessage = new String(errorBytes);
                        Log.e("CLIENTS_SEARCH", "Código de estado del error: " + statusCode);
                        Log.e("CLIENTS_SEARCH", "Cuerpo del error: " + errorMessage);
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                SharedPreferences prefs = requireContext().getSharedPreferences("SupabasePrefs", Context.MODE_PRIVATE);
                String token = prefs.getString("access_token", "");
                Log.d("CLIENTS_SEARCH", "Token para buscar clientes: " + token);
                Map<String, String> headers = new HashMap<>();
                headers.put("apikey", API_ANON_KEY);
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };
        queue.add(request);
    }
}
