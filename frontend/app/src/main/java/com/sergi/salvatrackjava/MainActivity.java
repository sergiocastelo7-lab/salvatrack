package com.sergi.salvatrackjava;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottomNav);

        if (savedInstanceState == null) {
            if (PrefsManager.haySesion(this)) {
                mostrarNavegacion();
                loadFragment(new CronoFragment());
            } else {
                ocultarNavegacion();
                loadFragment(new LoginFragment());
            }
        }

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_crono) {
                loadFragment(new CronoFragment());
                return true;
            } else if (id == R.id.nav_feder) {
                loadFragment(new FederFragment());
                return true;
            } else if (id == R.id.nav_buscar) {
                loadFragment(new BuscarFragment());
                return true;
            } else if (id == R.id.nav_config) {
                loadFragment(new ConfigFragment());
                return true;
            }
            return false;
        });
    }

    public void navegarAppPrincipal() {
        mostrarNavegacion();
        bottomNav.setSelectedItemId(R.id.nav_crono);
        loadFragment(new CronoFragment());
    }

    public void cerrarSesionYVolverLogin() {
        PrefsManager.cerrarSesion(this);
        ocultarNavegacion();
        getSupportFragmentManager().popBackStack(null,
                androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
        loadFragment(new LoginFragment());
    }

    private void mostrarNavegacion()  { bottomNav.setVisibility(View.VISIBLE); }
    private void ocultarNavegacion()  { bottomNav.setVisibility(View.GONE); }

    public void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }
}