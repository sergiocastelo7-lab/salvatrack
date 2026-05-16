package com.sergi.salvatrackjava;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BuscarFragment extends Fragment {

    private static List<Athlete> cacheAtletas = null;
    private EditText etBuscar;
    private TextView btnLimpiar;
    private ProgressBar pbBuscar;
    private ScrollView layoutIdle;
    private LinearLayout sectionFavoritos, llFavoritos;
    private LinearLayout sectionHistorial, llHistorial;
    private TextView btnBorrarTodo;
    private CardView cardResultados;
    private RecyclerView rvResultados;
    private LinearLayout layoutSinResultados;
    private TextView tvMensajeSinResultados;
    private AtletaAdapter adapter;
    private FirebaseFirestore db;
    private final Handler debounceHandler = new Handler(Looper.getMainLooper());
    private Runnable debounceRunnable;
    private static final int DEBOUNCE_MS = 400;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_buscar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = FirebaseFirestore.getInstance();

        etBuscar             = view.findViewById(R.id.etBuscar);
        btnLimpiar           = view.findViewById(R.id.btnLimpiar);
        pbBuscar             = view.findViewById(R.id.pbBuscar);
        layoutIdle           = view.findViewById(R.id.layoutIdle);
        sectionFavoritos     = view.findViewById(R.id.sectionFavoritos);
        llFavoritos          = view.findViewById(R.id.llFavoritos);
        sectionHistorial     = view.findViewById(R.id.sectionHistorial);
        llHistorial          = view.findViewById(R.id.llHistorial);
        btnBorrarTodo        = view.findViewById(R.id.btnBorrarTodo);
        cardResultados       = view.findViewById(R.id.cardResultados);
        rvResultados         = view.findViewById(R.id.rvResultados);
        layoutSinResultados  = view.findViewById(R.id.layoutSinResultados);
        tvMensajeSinResultados = view.findViewById(R.id.tvMensajeSinResultados);

        adapter = new AtletaAdapter(this::abrirPerfil);
        rvResultados.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvResultados.setAdapter(adapter);

        btnLimpiar.setOnClickListener(v -> etBuscar.setText(""));

        btnBorrarTodo.setOnClickListener(v -> {
            PrefsManager.borrarHistorial(requireContext());
            refrescarIdle();
        });

        etBuscar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().trim();
                btnLimpiar.setVisibility(query.isEmpty() ? View.GONE : View.VISIBLE);

                if (debounceRunnable != null) debounceHandler.removeCallbacks(debounceRunnable);

                if (query.length() < 2) {
                    mostrarIdle();
                    return;
                }
                debounceRunnable = () -> buscar(query);
                debounceHandler.postDelayed(debounceRunnable, DEBOUNCE_MS);
            }
        });

        mostrarIdle();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refrescar favoritos al volver de un perfil (por si cambió la estrella)
        if (etBuscar.getText().toString().trim().length() < 2) refrescarIdle();
    }

    // ==========================================
    // ESTADO IDLE
    // ==========================================

    private void mostrarIdle() {
        cardResultados.setVisibility(View.GONE);
        layoutSinResultados.setVisibility(View.GONE);
        layoutIdle.setVisibility(View.VISIBLE);
        refrescarIdle();
    }

    private void refrescarIdle() {
        // Favoritos
        List<Athlete> favs = PrefsManager.getFavoritos(requireContext());
        if (favs.isEmpty()) {
            sectionFavoritos.setVisibility(View.GONE);
        } else {
            sectionFavoritos.setVisibility(View.VISIBLE);
            llFavoritos.removeAllViews();
            for (Athlete a : favs) llFavoritos.addView(crearTarjetaFavorito(a));
        }

        // Historial
        List<String> hist = PrefsManager.getHistorial(requireContext());
        if (hist.isEmpty()) {
            sectionHistorial.setVisibility(View.GONE);
        } else {
            sectionHistorial.setVisibility(View.VISIBLE);
            llHistorial.removeAllViews();
            for (String q : hist) llHistorial.addView(crearItemHistorial(q));
        }
    }

    // ==========================================
    // TARJETA FAVORITO
    // ==========================================

    private View crearTarjetaFavorito(Athlete a) {
        CardView card = new CardView(requireContext());
        CardView.LayoutParams cp = new CardView.LayoutParams(
                CardView.LayoutParams.MATCH_PARENT, CardView.LayoutParams.WRAP_CONTENT);
        cp.bottomMargin = dpToPx(10);
        card.setLayoutParams(cp);
        card.setRadius(dpToPx(16));
        card.setCardElevation(dpToPx(2));
        card.setCardBackgroundColor(Color.WHITE);
        card.setClickable(true);
        card.setFocusable(true);
        card.setOnClickListener(v -> abrirPerfil(a));

        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);
        row.setPadding(dpToPx(14), dpToPx(14), dpToPx(14), dpToPx(14));

        // Avatar
        TextView tvAvatar = new TextView(requireContext());
        tvAvatar.setText(a.getIniciales());
        tvAvatar.setTextSize(15f);
        tvAvatar.setTypeface(null, Typeface.BOLD);
        tvAvatar.setTextColor(Color.WHITE);
        tvAvatar.setGravity(android.view.Gravity.CENTER);
        tvAvatar.setBackground(requireContext().getDrawable(R.drawable.bg_avatar_circulo));
        LinearLayout.LayoutParams avp = new LinearLayout.LayoutParams(dpToPx(48), dpToPx(48));
        avp.setMargins(0, 0, dpToPx(14), 0);
        tvAvatar.setLayoutParams(avp);
        row.addView(tvAvatar);

        // Info centro
        LinearLayout info = new LinearLayout(requireContext());
        info.setOrientation(LinearLayout.VERTICAL);
        info.setLayoutParams(new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView tvNombre = new TextView(requireContext());
        tvNombre.setText(a.getNombre());
        tvNombre.setTextSize(14f);
        tvNombre.setTypeface(null, Typeface.BOLD);
        tvNombre.setTextColor(Color.parseColor("#1A1A2E"));
        info.addView(tvNombre);

        String sub = (a.getAnoNacimiento() > 0 ? a.getAnoNacimiento() : "")
                + (a.getClub() != null && !a.getClub().isEmpty()
                ? (a.getAnoNacimiento() > 0 ? " • " : "") + a.getClub() : "");
        if (!sub.isEmpty()) {
            TextView tvSub = new TextView(requireContext());
            tvSub.setText(sub);
            tvSub.setTextSize(12f);
            tvSub.setTextColor(Color.parseColor("#A0AEC0"));
            info.addView(tvSub);
        }

        if (a.getCategoria() != null && !a.getCategoria().isEmpty()) {
            TextView tvCat = new TextView(requireContext());
            tvCat.setText(a.getCategoria());
            tvCat.setTextSize(12f);
            tvCat.setTextColor(Color.parseColor("#1976D2"));
            tvCat.setTypeface(null, Typeface.BOLD);
            info.addView(tvCat);
        }
        row.addView(info);

        // Estrella naranja
        TextView tvStar = new TextView(requireContext());
        tvStar.setText("★");
        tvStar.setTextSize(20f);
        tvStar.setTextColor(Color.parseColor("#D97706"));
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        sp.setMargins(dpToPx(8), 0, dpToPx(4), 0);
        tvStar.setLayoutParams(sp);
        row.addView(tvStar);

        // Flecha
        TextView tvArrow = new TextView(requireContext());
        tvArrow.setText("›");
        tvArrow.setTextSize(22f);
        tvArrow.setTextColor(Color.parseColor("#CBD5E0"));
        row.addView(tvArrow);

        card.addView(row);
        return card;
    }

    // ==========================================
    // ITEM HISTORIAL
    // ==========================================

    private View crearItemHistorial(String query) {
        LinearLayout fila = new LinearLayout(requireContext());
        fila.setOrientation(LinearLayout.HORIZONTAL);
        fila.setGravity(android.view.Gravity.CENTER_VERTICAL);
        fila.setPadding(dpToPx(4), dpToPx(14), dpToPx(4), dpToPx(14));
        fila.setClickable(true);
        fila.setFocusable(true);
        // Al pulsar el historial → poner en el buscador
        fila.setOnClickListener(v -> {
            etBuscar.setText(query);
            etBuscar.setSelection(query.length());
        });

        // Icono reloj
        TextView tvClock = new TextView(requireContext());
        tvClock.setText("🕐");
        tvClock.setTextSize(16f);
        tvClock.setAlpha(0.5f);
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(
                dpToPx(36), LinearLayout.LayoutParams.WRAP_CONTENT);
        tvClock.setLayoutParams(cp);
        fila.addView(tvClock);

        // Texto
        TextView tvQuery = new TextView(requireContext());
        tvQuery.setText(query);
        tvQuery.setTextSize(14f);
        tvQuery.setTextColor(Color.parseColor("#1A1A2E"));
        tvQuery.setLayoutParams(new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        fila.addView(tvQuery);

        // X para eliminar
        TextView tvX = new TextView(requireContext());
        tvX.setText("✕");
        tvX.setTextSize(14f);
        tvX.setTextColor(Color.parseColor("#A0AEC0"));
        tvX.setPadding(dpToPx(8), 0, 0, 0);
        tvX.setClickable(true);
        tvX.setFocusable(true);
        tvX.setOnClickListener(v -> {
            PrefsManager.eliminarBusqueda(requireContext(), query);
            refrescarIdle();
        });
        fila.addView(tvX);

        // Separador
        LinearLayout wrapper = new LinearLayout(requireContext());
        wrapper.setOrientation(LinearLayout.VERTICAL);
        wrapper.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        wrapper.addView(fila);

        View div = new View(requireContext());
        LinearLayout.LayoutParams dp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 1);
        dp.setMargins(dpToPx(36), 0, 0, 0);
        div.setLayoutParams(dp);
        div.setBackgroundColor(Color.parseColor("#F1F5F9"));
        wrapper.addView(div);

        return wrapper;
    }

    // ==========================================
    // BÚSQUEDA
    // ==========================================

    private void buscar(String query) {
        if (cacheAtletas != null) {
            filtrarYMostrar(query);
        } else {
            pbBuscar.setVisibility(View.VISIBLE);
            db.collection("athletes").get()
                    .addOnSuccessListener(snapshot -> {
                        if (!isAdded()) return;
                        cacheAtletas = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : snapshot) {
                            Athlete a = parsearAtleta(doc);
                            if (a != null && a.getNombre() != null) cacheAtletas.add(a);
                        }
                        pbBuscar.setVisibility(View.GONE);
                        filtrarYMostrar(query);
                    })
                    .addOnFailureListener(e -> {
                        if (!isAdded()) return;
                        pbBuscar.setVisibility(View.GONE);
                        mostrarSinResultados("Error al cargar datos.\nInténtalo de nuevo.");
                    });
        }
    }

    private void filtrarYMostrar(String query) {
        if (cacheAtletas == null) return;
        String qn = normalizar(query);
        List<Athlete> res = new ArrayList<>();
        for (Athlete a : cacheAtletas) {
            if (normalizar(a.getNombre()).contains(qn)) res.add(a);
        }

        // Guardar en historial
        PrefsManager.addBusqueda(requireContext(), query);

        if (res.isEmpty()) {
            mostrarSinResultados("Sin resultados para\n\"" + query + "\"");
        } else {
            adapter.setData(res);
            layoutIdle.setVisibility(View.GONE);
            layoutSinResultados.setVisibility(View.GONE);
            cardResultados.setVisibility(View.VISIBLE);
        }
    }

    private void mostrarSinResultados(String msg) {
        cardResultados.setVisibility(View.GONE);
        layoutIdle.setVisibility(View.GONE);
        layoutSinResultados.setVisibility(View.VISIBLE);
        tvMensajeSinResultados.setText(msg);
    }

    // ==========================================
    // NAVEGACIÓN
    // ==========================================

    private void abrirPerfil(Athlete athlete) {
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, DeportistaPerfilFragment.newInstance(athlete))
                .addToBackStack(null)
                .commit();
    }

    // ==========================================
    // PARSER MANUAL FIRESTORE
    // ==========================================

    @SuppressWarnings("unchecked")
    private Athlete parsearAtleta(QueryDocumentSnapshot doc) {
        try {
            Athlete a = new Athlete();
            a.setNombre((String) doc.get("nombre"));
            a.setClub((String) doc.get("club"));
            a.setCategoria((String) doc.get("categoria"));
            a.setGenero((String) doc.get("genero"));
            Object ano = doc.get("anoNacimiento");
            if (ano instanceof Long)   a.setAnoNacimiento((Long) ano);
            else if (ano instanceof String) {
                try { a.setAnoNacimiento(Long.parseLong((String) ano)); } catch (Exception ignored) {}
            }
            List<Map<String, Object>> rawMarcas = (List<Map<String, Object>>) doc.get("marcas");
            if (rawMarcas != null) {
                List<Marca> marcas = new ArrayList<>();
                for (Map<String, Object> m : rawMarcas) {
                    Marca marca = new Marca();
                    marca.setPrueba((String) m.get("prueba"));
                    marca.setTiempo((String) m.get("tiempo"));
                    marca.setPiscina((String) m.get("piscina"));
                    marca.setCrono((String) m.get("crono"));
                    marcas.add(marca);
                }
                a.setMarcas(marcas);
            }
            return a;
        } catch (Exception e) { return null; }
    }

    // ==========================================
    // UTILIDADES
    // ==========================================

    public static String normalizar(String texto) {
        if (texto == null) return "";
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "").toLowerCase(Locale.getDefault());
    }

    private int dpToPx(int dp) {
        return Math.round(dp * requireContext().getResources().getDisplayMetrics().density);
    }

    // Workaround for 14sp literal in code
    private static final float textSize14sp = 14f;

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (debounceRunnable != null) debounceHandler.removeCallbacks(debounceRunnable);
    }
}