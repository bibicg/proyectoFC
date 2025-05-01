package com.bcg.cartaller;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

/**
 * Desde este fragment el mecánico podrá crear un nuevo cliente cubriendo un formulario
 * con nombre, apellidos, dni, domicilio, teléfono y vehículos a su nombre.
 */
public class ClientsNewFragment extends Fragment {

    public ClientsNewFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_clients_new, container, false);
    }
}