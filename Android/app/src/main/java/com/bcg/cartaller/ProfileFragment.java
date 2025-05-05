package com.bcg.cartaller;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.bcg.cartaller.Adapters.TrabajoAdapter;
import com.bcg.cartaller.Models.Cliente;
import com.bcg.cartaller.Models.Trabajo;
import com.bcg.cartaller.Models.Vehiculo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Este fragmento es el que se carga por defecto en el main al iniciar la app, ya que es el
 * perfil del mecánico que ha iniciado sesión. Se le da la bienvenida usando su username y se muestran
 * por defecto en un RV los trabajos que están sin finalizar (pendientes):
 */

// ProfileFragment.java actualizado con headers y control RLS (hay que activarlas en supabase)
public class ProfileFragment extends Fragment {
    private RecyclerView recyclerTrabajos;
    private TrabajoAdapter adapter;
    private RequestQueue queue;
    //Añado las url a una variable para no tener que estar copiando todo el codigo contantemente:
    private final String SUPABASE_URL = "https://gtiqlopkoiconeivobxa.supabase.co";
    private final String API_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imd0aXFsb3Brb2ljb25laXZvYnhhIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDYxMjMyMTAsImV4cCI6MjA2MTY5OTIxMH0.T5MFUR9KAWXQOnoeZChYXu-FQ9LGClPp1lrSX8q733o";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        recyclerTrabajos = view.findViewById(R.id.recyclerTrabajos);
        recyclerTrabajos.setLayoutManager(new LinearLayoutManager(getContext()));

        queue = Volley.newRequestQueue(getContext());
        verTrabajosActivos();

        return view;
    }

    private void verTrabajosActivos() {
        //lo primero es recuperar el id del usuario para poder ver solo sus trabajos
        SharedPreferences prefs = requireContext().getSharedPreferences("prefs", Context.MODE_PRIVATE);
        String mecanicoId = prefs.getString("mecanico_id", null);

        if (mecanicoId == null) {
            Toast.makeText(getContext(), "ID mecánico no encontrado", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("SUPABASE", "Usando mecanico_id: " + mecanicoId);

        //es como una consulta sql hecha desde la app conlas particularidades de suabase, como el eq
        //se mostrarán todos los trabajos cuyo estado sea pendiente o en curso
        String url = SUPABASE_URL + "/rest/v1/trabajos" +
                "?select=id,estado,descripcion,fecha_inicio,vehiculos(matricula,cliente:clientes(dni))" +
                "&mecanico_id=eq." + mecanicoId +
                "&estado=in.(pendiente,en%20curso)";


        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    List<Trabajo> trabajos = new ArrayList<>();
                    try {
                        Log.d("SUPABASE", "Respuesta completa: " + response.toString());
                        Log.d("SUPABASE", "Cantidad de trabajos: " + response.length());

                        for (int i = 0; i < response.length(); i++) {
                            JSONObject trabajoJson = response.getJSONObject(i);
                            Trabajo trabajo = new Trabajo();
                            trabajo.id = trabajoJson.getString("id");
                            trabajo.estado = trabajoJson.getString("estado");
                            trabajo.descripcion = trabajoJson.getString("descripcion");

                            JSONObject vehiculoJson = trabajoJson.getJSONObject("vehiculos");
                            Vehiculo vehiculo = new Vehiculo();
                            vehiculo.matricula = vehiculoJson.getString("matricula");

                            JSONObject clienteJson = vehiculoJson.getJSONObject("cliente");
                            Cliente cliente = new Cliente();
                            cliente.dni = clienteJson.getString("dni");

                            vehiculo.cliente = cliente;
                            trabajo.vehiculo = vehiculo;

                            trabajos.add(trabajo);
                        }
                        adapter = new TrabajoAdapter(trabajos);
                        recyclerTrabajos.setAdapter(adapter);
                    } catch (JSONException e) {
                        Toast.makeText(getContext(), "Error al procesar datos", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Toast.makeText(getContext(), "Error al cargar trabajos", Toast.LENGTH_SHORT).show();
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
}


