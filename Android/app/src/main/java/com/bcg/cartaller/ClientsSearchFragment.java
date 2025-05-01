package com.bcg.cartaller;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

/**
 * Desde este fragment el mecánico podrá buscar clientes, bien por su dni o por la matrícula de
 * uno de sus vehículos.
 */
public class ClientsSearchFragment extends Fragment {

    public ClientsSearchFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_clients_search, container, false);
    }
}
