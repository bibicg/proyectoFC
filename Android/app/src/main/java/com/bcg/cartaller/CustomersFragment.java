package com.bcg.cartaller;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

/**
 * Acceso a este fragment desde el menú lateral "Clientes"
 * Desde aquí, el mecánico podrá buscar clientes y añadir nuevos clientes
 * Ambas informaciones se mostrarán en un contenedor en esta misma página, a través de fragments
 * Dependiendo del botón que se clique, se cargará un fragment u otro
 */
public class CustomersFragment extends Fragment {

    public CustomersFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_clients, container, false);

        // Botones para buscar clientes y para añadir un nuevo customer
        Button btnBuscar = view.findViewById(R.id.btnBuscarCliente);
        Button btnNuevo = view.findViewById(R.id.btnNuevoCliente);

        // Este es el contenedor donde se van a cargar los fragments (search y new)
        getChildFragmentManager().beginTransaction()
                .replace(R.id.clientsGeneralContainer, new CustomersSearchFragment())
                .commit();

        // Según se clique un botón u otro, se cargará el fragmento correspondiente en el contenedor
        // creado en el xml:
        /**
        btnBuscar.setOnClickListener(v -> {
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.clientsGeneralContainer, new CustomersSearchFragment())
                    .commit();
        });*/

        //Hacer lo mismo que en la busqueda de trabajos, mostrando las opciones en un Dialog:
        btnBuscar.setOnClickListener(v -> {
            CustomersSearchFragment fragment = new CustomersSearchFragment();
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.clientsGeneralContainer, fragment)
                    .commit();

            new Handler().postDelayed(() -> {
                Fragment currentFragment = getChildFragmentManager().findFragmentById(R.id.clientsGeneralContainer);
                if (currentFragment instanceof CustomersSearchFragment) {
                    ((CustomersSearchFragment) currentFragment).showSearchDialog();
                }
            }, 300);
        });


        btnNuevo.setOnClickListener(v -> {
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.clientsGeneralContainer, new CustomersNewFragment())
                    .commit();
        });

        return view;
    }
}
