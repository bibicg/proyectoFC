package com.bcg.cartaller;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.bcg.cartaller.Adapters.ThemeUtils;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //aplico el tema:
        ThemeUtils.applyTheme(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigationView);
        toolbar = findViewById(R.id.toolbar);

        // Configurar el Toolbar como ActionBar:
        setSupportActionBar(toolbar);

        // Configurar el botón de menú (hamburguesa):
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.open_drawer, R.string.close_drawer
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Listener de los elementos del menú lateral
        // para saber cuak se está pulsando:
        navigationView.setNavigationItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_customers) { // -> Clientes
                selectedFragment = new CustomersFragment();
            } else if (itemId == R.id.nav_jobs) { // -> Trabajos
                selectedFragment = new JobsFragment();
            } else if (itemId == R.id.nav_profile) { // -> Perfil
                selectedFragment = new ProfileFragment();
            } else if (itemId == R.id.nav_change_theme) { // -> Modo dia/noche
                //llamada al cambio de tema;
                ThemeUtils.toggleTheme(this);
                return true;
            } else if (itemId == R.id.nav_logout) { // -> Cerrar sesión
                logoutUser();
                return true;
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, selectedFragment)
                        .commit();
            }

            drawerLayout.closeDrawers(); // Cerrar el menú después de seleccolnar
            return true;
        });

        /** //Cargamos el ProfileFragment por defecto para que se vea  al cargar la app:
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, new ProfileFragment())
                    .commit();
            navigationView.setCheckedItem(R.id.nav_profile); // Marcar "Profile" como seleccionado
        }*/

        /**
         * Cargamos el ProfileFragment por defecto para que se vea  al cargar la app.
         * A no ser que haya un trabajo_id (esto sucede si se ha seleccionado un trabajo del RV),
         * que entronces se carga JobsNewFragment directa_ en el Main, para que se vean los detalles
         * del trabajo en el mismo formulario, pero sin los botones buscar y nuevo del JobsFragment:
         */
        if (savedInstanceState == null) {
            int jobId = getIntent().getIntExtra("trabajo_id", -1);

            Fragment defaultFragment;
            if (jobId != -1) {
                JobsNewFragment fragment = new JobsNewFragment();
                Bundle args = new Bundle();
                args.putInt("trabajo_id", jobId);
                fragment.setArguments(args);
                defaultFragment = fragment;
            } else {
                defaultFragment = new ProfileFragment();
            }
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, defaultFragment)
                    .commit();
            if (jobId == -1) {
                navigationView.setCheckedItem(R.id.nav_profile);
            }
        }
    }

    /**
     * Esto es heredado de la práctica de DI con Firebase, tengo que ver cómo hacerlo aquí
     *
     * POR AHORA, TAL CUAL, ESTÁ FUNCIONANDO :)
     */
    private void logoutUser() {
        //FirebaseAuth.getInstance().signOut();
        drawerLayout.closeDrawers(); // Cerrar el menú antes de salir
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

}
