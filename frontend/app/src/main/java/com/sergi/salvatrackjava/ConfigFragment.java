package com.sergi.salvatrackjava;

import android.app.AlertDialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.button.MaterialButton;

public class ConfigFragment extends Fragment {

    private LinearLayout layoutEmpty;
    private LinearLayout layoutHistorial;
    private int limiteTiempos = 5;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_config, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        layoutEmpty = view.findViewById(R.id.layoutEmpty);
        layoutHistorial = view.findViewById(R.id.layoutHistorial);

        // Selector cantidad tiempos
        TextView chip5  = view.findViewById(R.id.chip5);
        TextView chip10 = view.findViewById(R.id.chip10);
        TextView chip20 = view.findViewById(R.id.chip20);
        TextView chip50 = view.findViewById(R.id.chip50);
        TextView[] chips = {chip5, chip10, chip20, chip50};
        int[] limites    = {5, 10, 20, 50};

        for (int i = 0; i < chips.length; i++) {
            final int lim = limites[i];
            chips[i].setOnClickListener(v -> {
                limiteTiempos = lim;
                for (TextView c : chips) {
                    c.setTextColor(android.graphics.Color.parseColor("#64748B"));
                    c.setBackgroundResource(0);
                }
                ((TextView) v).setTextColor(android.graphics.Color.WHITE);
                ((TextView) v).setBackgroundResource(R.drawable.bg_lap_number);
                ((TextView) v).setBackgroundTintList(
                        android.content.res.ColorStateList.valueOf(
                                android.graphics.Color.parseColor("#1976D2")));
                actualizarHistorial();
            });
        }

        // Nombre del usuario
        String nombre = PrefsManager.getNombreUsuario(requireContext());
        if (!nombre.isEmpty()) {
            TextView tvNombreUsuario = view.findViewById(R.id.tvNombreUsuario);
            if (tvNombreUsuario != null) tvNombreUsuario.setText(nombre);
        }

        // Cerrar sesión
        MaterialButton btnCerrarSesion = view.findViewById(R.id.btnCerrarSesion);
        if (btnCerrarSesion != null) {
            btnCerrarSesion.setOnClickListener(v ->
                    ((MainActivity) requireActivity()).cerrarSesionYVolverLogin());
        }

        // Borrar cuenta
        MaterialButton btnBorrarCuenta = view.findViewById(R.id.btnBorrarCuenta);
        if (btnBorrarCuenta != null) {
            btnBorrarCuenta.setOnClickListener(v -> mostrarConfirmacionBorrar());
        }

        // Panel admin
        androidx.cardview.widget.CardView cardAdmin = view.findViewById(R.id.cardAdmin);
        if (cardAdmin != null) {
            boolean esAdmin = PrefsManager.isAdmin(requireContext());
            cardAdmin.setVisibility(esAdmin ? View.VISIBLE : View.GONE);
            if (esAdmin) {
                view.findViewById(R.id.btnAbrirAdmin).setOnClickListener(v ->
                        requireActivity().getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragmentContainer, new AdminLiveEventFragment())
                                .addToBackStack(null)
                                .commit());
            }
        }

        actualizarHistorial();
    }

    private void mostrarConfirmacionBorrar() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Borrar cuenta")
                .setMessage("¿Seguro que quieres borrar tu cuenta? Esta acción no se puede deshacer.")
                .setPositiveButton("Borrar", (dialog, which) -> borrarCuenta())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void borrarCuenta() {
        String token = PrefsManager.getToken(requireContext());
        if (token == null) return;

        // Mostrar loading
        MaterialButton btn = requireView().findViewById(R.id.btnBorrarCuenta);
        if (btn != null) { btn.setEnabled(false); btn.setText("Borrando..."); }

        ApiClient.deleteAccount(token, new ApiClient.Callback() {
            @Override
            public void onSuccess(org.json.JSONObject response) {
                if (!isAdded()) return;
                ((MainActivity) requireActivity()).cerrarSesionYVolverLogin();
            }

            @Override
            public void onError(String mensaje) {
                if (!isAdded()) return;
                if (btn != null) { btn.setEnabled(true); btn.setText("Borrar cuenta"); }
                new AlertDialog.Builder(requireContext())
                        .setTitle("Error")
                        .setMessage(mensaje)
                        .setPositiveButton("OK", null)
                        .show();
            }
        });
    }

    private void actualizarHistorial() {
        String token = PrefsManager.getToken(requireContext());
        if (token == null) return;

        ApiClient.getTiempos(token, limiteTiempos, new ApiClient.Callback() {
            @Override
            public void onSuccess(org.json.JSONObject response) {
                if (!isAdded()) return;
                try {
                    org.json.JSONArray tiempos = response.getJSONArray("tiempos");
                    if (tiempos.length() == 0) {
                        layoutEmpty.setVisibility(View.VISIBLE);
                        layoutHistorial.setVisibility(View.GONE);
                        return;
                    }
                    layoutEmpty.setVisibility(View.GONE);
                    layoutHistorial.setVisibility(View.VISIBLE);
                    layoutHistorial.removeAllViews();

                    int mostrar = Math.min(tiempos.length(), limiteTiempos);
                    for (int i = 0; i < mostrar; i++) {
                        org.json.JSONObject t = tiempos.getJSONObject(i);
                        layoutHistorial.addView(crearFilaTiempo(t));
                    }
                } catch (Exception ignored) {}
            }

            @Override
            public void onError(String msg) { /* silencioso */ }
        });
    }

    private View crearFilaTiempo(org.json.JSONObject t) {
        int id = t.optInt("id", -1);

        // Contenedor principal (card)
        androidx.cardview.widget.CardView card = new androidx.cardview.widget.CardView(requireContext());
        androidx.cardview.widget.CardView.LayoutParams cp =
                new androidx.cardview.widget.CardView.LayoutParams(
                        androidx.cardview.widget.CardView.LayoutParams.MATCH_PARENT,
                        androidx.cardview.widget.CardView.LayoutParams.WRAP_CONTENT);
        cp.bottomMargin = dpToPx(10);
        card.setLayoutParams(cp);
        card.setRadius(dpToPx(12));
        card.setCardElevation(dpToPx(2));
        card.setCardBackgroundColor(android.graphics.Color.WHITE);

        android.widget.LinearLayout inner = new android.widget.LinearLayout(requireContext());
        inner.setOrientation(android.widget.LinearLayout.VERTICAL);
        inner.setPadding(dpToPx(14), dpToPx(12), dpToPx(14), dpToPx(12));

        // ---- Fila superior: info + tiempo + botón eliminar ----
        android.widget.LinearLayout filaSup = new android.widget.LinearLayout(requireContext());
        filaSup.setOrientation(android.widget.LinearLayout.HORIZONTAL);
        filaSup.setGravity(android.view.Gravity.CENTER_VERTICAL);
        filaSup.setLayoutParams(new android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT));

        // Info (nombre + subtítulo)
        android.widget.LinearLayout info = new android.widget.LinearLayout(requireContext());
        info.setOrientation(android.widget.LinearLayout.VERTICAL);
        info.setLayoutParams(new android.widget.LinearLayout.LayoutParams(
                0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        String nombre = t.optString("nombre");
        String prueba = t.optString("prueba");
        TextView tvNombre = new TextView(requireContext());
        tvNombre.setText(nombre.isEmpty() ? (prueba.isEmpty() ? "Sin nombre" : prueba) : nombre);
        tvNombre.setTextSize(13f);
        tvNombre.setTypeface(null, android.graphics.Typeface.BOLD);
        tvNombre.setTextColor(android.graphics.Color.parseColor("#1A1A2E"));
        info.addView(tvNombre);

        String sub = "";
        if (!prueba.isEmpty() && !prueba.equals(nombre)) sub += prueba;
        String piscina = t.optString("piscina");
        if (!piscina.isEmpty()) sub += (sub.isEmpty() ? "" : " · ") + piscina;
        String modo = t.optString("modo");
        String jugador = t.optString("jugador");
        if (!jugador.isEmpty()) sub += (sub.isEmpty() ? "" : " · ") + jugador;
        else if (!modo.isEmpty()) sub += (sub.isEmpty() ? "" : " · ") + modo;

        if (!sub.isEmpty()) {
            TextView tvSub = new TextView(requireContext());
            tvSub.setText(sub);
            tvSub.setTextSize(11f);
            tvSub.setTextColor(android.graphics.Color.parseColor("#A0AEC0"));
            info.addView(tvSub);
        }
        filaSup.addView(info);

        // Tiempo
        TextView tvTiempo = new TextView(requireContext());
        long ms = t.optLong("tiempo_ms", 0);
        tvTiempo.setText(formatMs(ms));
        tvTiempo.setTextSize(15f);
        tvTiempo.setTypeface(android.graphics.Typeface.MONOSPACE, android.graphics.Typeface.BOLD);
        tvTiempo.setTextColor(android.graphics.Color.parseColor("#1976D2"));
        android.widget.LinearLayout.LayoutParams tp = new android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
        tp.setMargins(dpToPx(8), 0, dpToPx(8), 0);
        tvTiempo.setLayoutParams(tp);
        filaSup.addView(tvTiempo);

        // Botón editar ✏️
        TextView tvEditar = new TextView(requireContext());
        tvEditar.setText("✏️");
        tvEditar.setTextSize(15f);
        tvEditar.setClickable(true);
        tvEditar.setFocusable(true);
        tvEditar.setPadding(dpToPx(4), 0, dpToPx(4), 0);
        tvEditar.setOnClickListener(v -> {
            if (id < 0) return;
            EditarTiempoDialog dialog = EditarTiempoDialog.newInstance(
                    id,
                    t.optString("nombre"),
                    t.optString("prueba"),
                    t.optString("piscina"));
            dialog.setOnGuardado(() -> actualizarHistorial());
            dialog.show(getChildFragmentManager(), "editar_tiempo");
        });
        filaSup.addView(tvEditar);

        // Botón eliminar 🗑
        TextView tvEliminar = new TextView(requireContext());
        tvEliminar.setText("🗑");
        tvEliminar.setTextSize(16f);
        tvEliminar.setClickable(true);
        tvEliminar.setFocusable(true);
        tvEliminar.setPadding(dpToPx(4), 0, 0, 0);
        tvEliminar.setOnClickListener(v -> {
            if (id < 0) return;
            new android.app.AlertDialog.Builder(requireContext())
                    .setTitle("Eliminar tiempo")
                    .setMessage("¿Seguro que quieres eliminar este tiempo?")
                    .setPositiveButton("Eliminar", (d, w) -> {
                        String token = PrefsManager.getToken(requireContext());
                        if (token == null) return;
                        ApiClient.eliminarTiempo(token, id, new ApiClient.Callback() {
                            @Override public void onSuccess(org.json.JSONObject r) {
                                if (!isAdded()) return;
                                actualizarHistorial(); // refrescar lista
                            }
                            @Override public void onError(String msg) {}
                        });
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        });
        filaSup.addView(tvEliminar);
        inner.addView(filaSup);

        // ---- Parciales ----
        try {
            org.json.JSONArray parciales = t.optJSONArray("parciales");
            if (parciales != null && parciales.length() > 0) {
                // Separador
                View sep = new View(requireContext());
                android.widget.LinearLayout.LayoutParams sp = new android.widget.LinearLayout.LayoutParams(
                        android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 1);
                sp.setMargins(0, dpToPx(8), 0, dpToPx(8));
                sep.setLayoutParams(sp);
                sep.setBackgroundColor(android.graphics.Color.parseColor("#F1F5F9"));
                inner.addView(sep);

                for (int k = 0; k < parciales.length(); k++) {
                    org.json.JSONObject parc;
                    // Puede venir como array [num, tiempo_ms, split_ms] o como objeto
                    if (parciales.get(k) instanceof org.json.JSONArray) {
                        org.json.JSONArray pa = parciales.getJSONArray(k);
                        parc = new org.json.JSONObject();
                        parc.put("numero", pa.getLong(0));
                        parc.put("tiempo_ms", pa.getLong(1));
                        parc.put("split_ms", pa.getLong(2));
                    } else {
                        parc = parciales.getJSONObject(k);
                    }

                    android.widget.LinearLayout filaParcial = new android.widget.LinearLayout(requireContext());
                    filaParcial.setOrientation(android.widget.LinearLayout.HORIZONTAL);
                    filaParcial.setGravity(android.view.Gravity.CENTER_VERTICAL);
                    android.widget.LinearLayout.LayoutParams pp = new android.widget.LinearLayout.LayoutParams(
                            android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
                    pp.setMargins(0, 0, 0, dpToPx(3));
                    filaParcial.setLayoutParams(pp);

                    // Número del parcial
                    TextView tvNum = new TextView(requireContext());
                    tvNum.setText("#" + parc.optLong("numero"));
                    tvNum.setTextSize(11f);
                    tvNum.setTypeface(null, android.graphics.Typeface.BOLD);
                    tvNum.setTextColor(android.graphics.Color.parseColor("#A0AEC0"));
                    android.widget.LinearLayout.LayoutParams np = new android.widget.LinearLayout.LayoutParams(
                            dpToPx(32), android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
                    tvNum.setLayoutParams(np);
                    filaParcial.addView(tvNum);

                    // Tiempo total del parcial
                    TextView tvTotalParcial = new TextView(requireContext());
                    tvTotalParcial.setText(formatMs(parc.optLong("tiempo_ms")));
                    tvTotalParcial.setTextSize(12f);
                    tvTotalParcial.setTypeface(android.graphics.Typeface.MONOSPACE);
                    tvTotalParcial.setTextColor(android.graphics.Color.parseColor("#1A1A2E"));
                    tvTotalParcial.setLayoutParams(new android.widget.LinearLayout.LayoutParams(
                            0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
                    filaParcial.addView(tvTotalParcial);

                    // Split
                    TextView tvSplit = new TextView(requireContext());
                    tvSplit.setText("+" + formatMs(parc.optLong("split_ms")));
                    tvSplit.setTextSize(12f);
                    tvSplit.setTypeface(android.graphics.Typeface.MONOSPACE);
                    tvSplit.setTextColor(android.graphics.Color.parseColor("#64748B"));
                    filaParcial.addView(tvSplit);

                    inner.addView(filaParcial);
                }
            }
        } catch (Exception ignored) {}

        card.addView(inner);
        return card;
    }

    private String formatMs(long ms) {
        long centis  = (ms % 1000) / 10;
        long seconds = (ms / 1000) % 60;
        long minutes = ms / 60000;
        return String.format("%02d:%02d.%02d", minutes, seconds, centis);
    }

    private int dpToPx(int dp) {
        return Math.round(dp * requireContext().getResources().getDisplayMetrics().density);
    }
}