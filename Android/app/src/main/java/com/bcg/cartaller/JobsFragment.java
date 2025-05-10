package com.bcg.cartaller;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

/**
 * Acceso a este fragment desde el menú lateral "Trabajos"
 * Desde esta página el mecánico podrá realizar todo lo relacionado con los trabajos:
 * - ver sus trabajos
 * - buscar trabajo por estado (pendiente, en curso, finalizado)
 * - buscar trabajo por matricula
 * - buscar trabajo por cliente
 * - añadir nuevos trabajos
 * - modificar los existentes
 */
public class JobsFragment extends Fragment {

    public JobsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_jobs, container, false);

        // Botones para buscar trabajos y para añadir un nuevo trabajo
        Button btnBuscar = view.findViewById(R.id.btnBuscarTrabajo);
        Button btnNuevo = view.findViewById(R.id.btnNuevoTrabajo);

        // Este es el contenedor donde se van a cargar los fragments (search y new)
        getChildFragmentManager().beginTransaction()
                .replace(R.id.jobsGeneralContainer, new ClientsSearchFragment())
                .commit();

        // Según se clique un botón u otro, se cargará el fragmento correspondiente en el contenedor
        // creado en el xml:
        btnBuscar.setOnClickListener(v -> {
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.jobsGeneralContainer, new ClientsSearchFragment())
                    .commit();
        });

        btnNuevo.setOnClickListener(v -> {
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.jobsGeneralContainer, new ClientsNewFragment())
                    .commit();
        });

        return view;
    }
}