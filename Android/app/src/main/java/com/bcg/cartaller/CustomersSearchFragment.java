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
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.bcg.cartaller.Adapters.CustomerAdapter;
import com.bcg.cartaller.Models.Customer;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.android.volley.AuthFailureError;
import com.bcg.cartaller.Repositories.CustomerRepository;

import java.util.HashMap;
import java.util.Map;

/**
 * Desde este fragment el mecánico podrá buscar clientes, bien por su dni o por la matrícula de
 * uno de sus vehículos.
 *
 * SIGUE LA  MISMA ESTRUCTURA QUE JobsSearchFragment.
 *
 * Modificado para implementar repository, que será quien contenga los métodos HTTP.
 */
public class CustomersSearchFragment extends Fragment {
    private RequestQueue queue;
    //private final String SUPABASE_URL = "https://gtiqlopkoiconeivobxa.supabase.co";
    //private final String API_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imd0aXFsb3Brb2ljb25laXZvYnhhIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDYxMjMyMTAsImV4cCI6MjA2MTY5OTIxMH0.T5MFUR9KAWXQOnoeZChYXu-FQ9LGClPp1lrSX8q733o";
    private List<Customer> customers = new ArrayList<>();
    private CustomerAdapter adapter;

    //Creo una instancia del repo para poder separar la lógica de la UI:
    private CustomerRepository customerRepository;

    public CustomersSearchFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_customers_search, container, false);

        queue = Volley.newRequestQueue(requireContext());

        //inicializo el repo:
        customerRepository = new CustomerRepository(requireContext());

        //Recicler view (el mismo que usé en Profile Fragment) para cargar los trabajos:
        RecyclerView recyclerView = view.findViewById(R.id.recyclerCustomers);
        adapter = new CustomerAdapter(customers, new CustomerAdapter.OnCustomerClickListener() {
            @Override
            public void onDetailCustomerClick(Customer customer) {
                CustomerDetailFragment fragment = new CustomerDetailFragment();
                Bundle args = new Bundle();
                //args.putString("cliente_dni", customer.dni);
                args.putInt("cliente_id", customer.getId());
                fragment.setArguments(args);
                getParentFragmentManager()
                        .beginTransaction()
                        .replace(R.id.customersGeneralContainer, fragment)
                        .addToBackStack(null)
                        .commit();
            }

            //BOTÓN PARA IR A DONDE AÑADIR UN VEHÍCULO:
            @Override
            public void onAddCarClick(Customer customer) {
                CarNewFragment fragment = new CarNewFragment();
                Bundle args = new Bundle();
                //args.putString("cliente_dni", customer.dni);
                args.putInt("cliente_id", customer.getId());
                fragment.setArguments(args);
                getParentFragmentManager()
                        .beginTransaction()
                        .replace(R.id.customersGeneralContainer, fragment)
                        .addToBackStack(null)
                        .commit();
            }

            //BOTÓN PARA IR A DONDE MODIFICAR DATOS DE UN CLIENTE:
            @Override
            public void onModifyCustomerClick(Customer customer) {
                CustomersNewFragment fragment = new CustomersNewFragment();

                Bundle args = new Bundle();
                args.putString("dni", customer.getDni());
                args.putString("nombre", customer.getName());
                args.putString("apellidos", customer.getSurname());
                args.putString("telefono", customer.getPhone());
                args.putString("email", customer.getEmail());
                args.putString("direccion", customer.getAddress());
                fragment.setArguments(args);

                getParentFragmentManager()
                        .beginTransaction()
                        .replace(R.id.customersGeneralContainer, fragment)
                        .addToBackStack(null)
                        .commit();
            }

        });
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        return view;
    }

    /**
     * Desde un Dialog, permite la búsqueda de clientes, mostrando 2 opciones de búsqueda: por dni y por matrícula
     * Mismo sistema que en en la búsqueda de trabajos.
     * (antes mostrarDialogBusqueda)
     */
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

    //(antes buscarPorDni)
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
                    //if (!dni.isEmpty()) searchClients("dni", dni);

                    //ahora llamamos al repositorio pq es el que maneja la lógica de la búsqueda (HTTP - GET):
                    if (!dni.isEmpty()) {
                        customerRepository.searchCustomers("dni", dni, new CustomerRepository.CustomerCallback() {
                            @Override
                            public void onSuccess(List<Customer> result) {
                                customers.clear();
                                customers.addAll(result);
                                adapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onError(String message) {
                                Toast.makeText(getContext(), "Error: " + message, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    //(antes buscarPorMatricula)
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
                    //if (!matricula.isEmpty()) searchClients("matricula", matricula);

                    //ahora llamamos al repositorio pq es el que maneja la lógica de la búsqueda (HTTP - GET):
                    if (!matricula.isEmpty()) {
                        customerRepository.searchCustomers("matricula", matricula, new CustomerRepository.CustomerCallback() {
                            @Override
                            public void onSuccess(List<Customer> result) {
                                customers.clear();
                                customers.addAll(result);
                                adapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onError(String message) {
                                Toast.makeText(getContext(), "Error: " + message, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }




                })
                .setNegativeButton("Cancelar", null)
                .show();
    }


    /**
     * Funcion para ver los customers (buscarClientes al comienzo)
     */
    /** VOY A MOVERLA AL REPOSITORY
    private void searchClients(String tipo, String valor) {
        String url;
        if (tipo.equals("dni")) {
            //url = SUPABASE_URL + "/rest/v1/customers?dni=eq." + valor; //asi no consigo todoslos datos del customer
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
                    customers.clear();
                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject clienteJson = tipo.equals("dni") ? response.getJSONObject(i)
                                    : response.getJSONObject(i).getJSONObject("cliente");

                            /*Customer customer = new Customer(
                                    clienteJson.getInt("id"),
                                    clienteJson.getString("dni"),
                                    clienteJson.optString("nombre", ""),
                                    clienteJson.optString("telefono", "")
                            );*/
 /**                           Customer customer = new Customer(
                                    clienteJson.getInt("id"),
                                    clienteJson.getString("dni"),
                                    clienteJson.optString("nombre", ""),
                                    clienteJson.optString("apellidos", ""),
                                    clienteJson.optString("telefono", ""),
                                    clienteJson.optString("email", ""),
                                    clienteJson.optString("direccion", "")
                            );

                            customers.add(customer);
                        }
                    } catch (JSONException e) {
                        Log.e("CLIENTS_SEARCH", "Error parseando JSON", e);
                    }
                    adapter.notifyDataSetChanged();
                },
                error -> {
                    Log.e("CLIENTS_SEARCH", "Error al cargar customers", error);
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
                Log.d("CLIENTS_SEARCH", "Token para buscar customers: " + token);
                Map<String, String> headers = new HashMap<>();
                headers.put("apikey", API_ANON_KEY);
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };
        queue.add(request);
    }*/
}
