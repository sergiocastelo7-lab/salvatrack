package com.sergi.salvatrackjava;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.button.MaterialButton;

public class LoginFragment extends Fragment {

    private boolean modoRegistro = false;

    private EditText etNombre, etPassword, etConfirmPassword;
    private TextView tvSubtitulo, tvLoginError, tvToggleTexto, btnToggle, tvConfirmLabel;
    private MaterialButton btnAccion;
    private ProgressBar pbLogin;
    private androidx.cardview.widget.CardView cardConfirmPassword;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etNombre          = view.findViewById(R.id.etNombre);
        etPassword        = view.findViewById(R.id.etPassword);
        etConfirmPassword = view.findViewById(R.id.etConfirmPassword);
        tvConfirmLabel    = view.findViewById(R.id.tvConfirmLabel);
        cardConfirmPassword = view.findViewById(R.id.cardConfirmPassword);
        tvSubtitulo       = view.findViewById(R.id.tvLoginSubtitulo);
        tvLoginError      = view.findViewById(R.id.tvLoginError);
        tvToggleTexto     = view.findViewById(R.id.tvToggleTexto);
        btnToggle         = view.findViewById(R.id.btnToggle);
        btnAccion         = view.findViewById(R.id.btnAccion);
        pbLogin           = view.findViewById(R.id.pbLogin);

        btnAccion.setOnClickListener(v -> ejecutarAccion());

        // Acción al pulsar "Listo" en el teclado
        etPassword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                ejecutarAccion();
                return true;
            }
            return false;
        });

        btnToggle.setOnClickListener(v -> {
            modoRegistro = !modoRegistro;
            actualizarUI();
        });
    }

    private void actualizarUI() {
        tvLoginError.setVisibility(View.GONE);
        etConfirmPassword.setText("");
        if (modoRegistro) {
            tvSubtitulo.setText("Crea tu cuenta");
            btnAccion.setText("Crear cuenta");
            tvToggleTexto.setText("¿Ya tienes cuenta? ");
            btnToggle.setText("Iniciar sesión");
            tvConfirmLabel.setVisibility(View.VISIBLE);
            cardConfirmPassword.setVisibility(View.VISIBLE);
        } else {
            tvSubtitulo.setText("Inicia sesión para continuar");
            btnAccion.setText("Iniciar sesión");
            tvToggleTexto.setText("¿No tienes cuenta? ");
            btnToggle.setText("Crear cuenta");
            tvConfirmLabel.setVisibility(View.GONE);
            cardConfirmPassword.setVisibility(View.GONE);
        }
    }

    private void ejecutarAccion() {
        String nombre   = etNombre.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (nombre.isEmpty() || password.isEmpty()) {
            mostrarError("Rellena todos los campos.");
            return;
        }

        if (modoRegistro) {
            String confirm = etConfirmPassword.getText().toString().trim();
            if (confirm.isEmpty()) {
                mostrarError("Confirma tu contraseña.");
                return;
            }
            if (!password.equals(confirm)) {
                mostrarError("Las contraseñas no coinciden.");
                return;
            }
        }

        setLoading(true);

        ApiClient.Callback callback = new ApiClient.Callback() {
            @Override
            public void onSuccess(org.json.JSONObject response) {
                if (!isAdded()) return;
                setLoading(false);
                String token         = response.optString("token");
                String nombreGuardado= response.optString("nombre");
                boolean isAdmin      = response.optBoolean("is_admin", false);

                PrefsManager.setToken(requireContext(), token);
                PrefsManager.setNombreUsuario(requireContext(), nombreGuardado);
                PrefsManager.setAdmin(requireContext(), isAdmin);

                // Sincronizar favoritos e historial del servidor
                try {
                    org.json.JSONArray favs = response.optJSONArray("favoritos");
                    org.json.JSONArray hist = response.optJSONArray("historial");
                    if (favs != null) PrefsManager.setFavoritosFromServer(requireContext(), favs);
                    if (hist != null) PrefsManager.setHistorialFromServer(requireContext(), hist);
                } catch (Exception ignored) {}

                ((MainActivity) requireActivity()).navegarAppPrincipal();
            }

            @Override
            public void onError(String mensaje) {
                if (!isAdded()) return;
                setLoading(false);
                mostrarError(mensaje);
            }
        };

        if (modoRegistro) {
            ApiClient.register(nombre, password, callback);
        } else {
            ApiClient.login(nombre, password, callback);
        }
    }

    private void mostrarError(String msg) {
        tvLoginError.setText(msg);
        tvLoginError.setVisibility(View.VISIBLE);
    }

    private void setLoading(boolean loading) {
        pbLogin.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnAccion.setEnabled(!loading);
        etNombre.setEnabled(!loading);
        etPassword.setEnabled(!loading);
    }
}