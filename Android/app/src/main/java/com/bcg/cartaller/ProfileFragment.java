package com.bcg.cartaller;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

/**
 * Este fragmento es el que se carga por defecto en el main al iniciar la app, ya que es el
 * perfil del mecánico que ha iniciado sesión. Se le da la bienvenida usando su username y se muestran
 * por defecto en un RV los trabajos que están sin finalizar (pendientes):
 */
public class ProfileFragment extends Fragment {

    public ProfileFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        return view;
    }
}
