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

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

/**
 * PASO INTERMEDIO ENTRE EL CLIC DE UN TRABAJO EN EL RV Y VISUALIZARLO EN EL FORMULARIO PARA EDITARLO.
 * SIRVE PARA VISUALIZAR LA INDFORMACIÓN DETALLADA DE UN TRABAJO (el item del RV muestra un resumen).
 * Se viene aqui desde el RV de JobsSearchFragment y el RV de ProfileFragment.
 * Desde aqui se da la posibilidad de eliminar un trabajo (único objeto que se permite borrar desde la app)
 * y de modificarlo, abriendo los detalles del trabajo en el formulario de trabajos (el mismo que se usa para crear)
 */
public class JobsDetailFragment extends Fragment {
    private RequestQueue queue;
    private final String API_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imd0aXFsb3Brb2ljb25laXZvYnhhIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDYxMjMyMTAsImV4cCI6MjA2MTY5OTIxMH0.T5MFUR9KAWXQOnoeZChYXu-FQ9LGClPp1lrSX8q733o";
    private TextView tvDescripcion, tvFechaInicio, tvFechaFin, tvEstado, tvComentarios, tvMatricula, tvDniCliente;
    private ImageView ivTrabajo;
    private Button btnEditar, btnEliminar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_jobs_detail, container, false);

        tvDescripcion = view.findViewById(R.id.textViewDescrippcion);
        tvFechaInicio = view.findViewById(R.id.textViewFechaInicio);
        tvFechaFin = view.findViewById(R.id.textViewFechaFin);
        tvEstado = view.findViewById(R.id.textViewEstado);
        tvComentarios = view.findViewById(R.id.textViewComentarios);
        tvMatricula = view.findViewById(R.id.textViewMatricula);
        tvDniCliente = view.findViewById(R.id.textViewDni);
        ivTrabajo = view.findViewById(R.id.imageViewTrabajo);
        btnEditar = view.findViewById(R.id.editarTrabajoButton);
        btnEliminar = view.findViewById(R.id.eliminarTrabajoButton);

        queue = Volley.newRequestQueue(getContext());

        Bundle args = getArguments();
        if (args != null) {
            tvDescripcion.setText(args.getString("descripcion", ""));
            tvFechaInicio.setText(args.getString("fecha_inicio", ""));
            tvFechaFin.setText(args.getString("fecha_fin", ""));
            tvEstado.setText(args.getString("estado", ""));
            tvComentarios.setText(args.getString("comentarios", ""));
            tvMatricula.setText(args.getString("matricula", ""));
            tvDniCliente.setText(args.getString("dni_cliente", ""));

            String base64Image = args.getString("imagen", null);
            if (base64Image != null && !base64Image.isEmpty()) {
                byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                ivTrabajo.setImageBitmap(decodedByte);
            }

            /**
             * ESTE BOTÓN ABRE EL FORMULARIO, DENTRO DE JOBS NEW FRAGMENT, CON TODA LA INFO DEL TRABAJO CARGADA
             * EN LOS CAMPOS CORRESPONDIENTES. DESDE AHÍ SERÁ DESDE DONDE SE PERMITA EDITAR EL TRABAJO Y ENVIAR
             * EL RESULTADO DE DICHA EDICIÓN A SUPABASE A TRAVÉS DE UN PATCH.
             */
            btnEditar.setOnClickListener(v -> {
                JobsNewFragment editFragment = new JobsNewFragment();
                editFragment.setArguments(args);
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragmentContainer, editFragment) //container de fragments de MainActivity
                        .addToBackStack(null)
                        .commit();
            });

            Log.d("SUPABASE", "ID TRABAJO = " + args.getString("trabajo_id"));
            Log.d("SUPABASE", "ID MECANICO (Bundle) = " + args.getString("mecanico_id"));


            /**
             * ESTE BOTÓN ABRE UNA CONFIRMACIÓN PARA ELIMINAR EL TRABAJO
             */
            btnEliminar.setOnClickListener(v ->
                    showConfirmationDelete(args.getString("trabajo_id")));
        }

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
        String mecanicoId = prefs.getString("mecanico_id", null);
        Log.d("SUPABASE", "Verificando mecanico_id actual (SharedPreferences): " + mecanicoId);

        StringRequest request = new StringRequest(
                Request.Method.DELETE,
                url,
                response -> {
                    Log.d("SUPABASE", "Respuesta DELETE (vacía esperada): '" + response + "'");
                    Toast.makeText(getContext(), "Trabajo eliminado correctamente", Toast.LENGTH_SHORT).show();
                    requireActivity().getSupportFragmentManager().popBackStack();
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



}
