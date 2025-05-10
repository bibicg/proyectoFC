package com.bcg.cartaller;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

/**
 * Desde este fragment el mecánico podrá buscar trabajos, bien por estado del trabajo,
 * la matrícula del vehículo implicado en el trabajo o el dni del cliente.
 * Busquedas(son tipo.  sql):
 * TODOS LOS TRABAJOS: GET /trabajos?select=*,vehiculos(*,clientes(*))&mecanico_id=eq.e622f108-7c92-4b87-b646-18545647d170
 * TRABAJOS POR ESTADO: GET /trabajos?select=*,vehiculos(*,clientes(*))&estado=eq.pendiente&mecanico_id=eq.e622f108-7c92-4b87-b646-18545647d170
 * TRABAJOS POR MATRICULA: GET /trabajos?select=*,vehiculos(*,clientes(*))&vehiculos.matricula=eq.1234ABC
 * TRABAJOS POR CLIENTE: como el cliente no está directamente en la tabla trabajos, sino que se llega a él
 * a través de la matri. del vehículo, lo mejor es hacer una view en supabase, que es como una "consulta" pre-guardada
 */
public class JobsSearchFragment extends Fragment {

    public JobsSearchFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_jobs_search, container, false);
    }
}