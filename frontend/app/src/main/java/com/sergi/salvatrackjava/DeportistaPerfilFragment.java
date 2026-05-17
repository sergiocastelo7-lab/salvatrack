package com.sergi.salvatrackjava;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class DeportistaPerfilFragment extends Fragment {

    private static final String ARG_ATHLETE = "athlete";

    // Cache estático para no recargar en cada perfil
    private static List<MinRecord> cacheMinimas = null;
    private static List<MinRecord> cacheRecords = null;

    private Athlete athlete;
    private FirebaseFirestore db;

    // Vistas
    private TextView tvAvatar, tvNombrePerfil, tvEdadAno, tvMisMarcasTab, tvHistorialTab;
    private View indicadorMarcas, indicadorHistorial;
    private LinearLayout llTags, layoutMarcasContent, layoutHistorialContent;
    private ProgressBar pbPerfil;

    private boolean esPestanaMarcas = true;
    private boolean historialCargado = false;

    // ==========================================
    // CREACIÓN
    // ==========================================

    public static DeportistaPerfilFragment newInstance(Athlete athlete) {
        DeportistaPerfilFragment f = new DeportistaPerfilFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_ATHLETE, athlete);
        f.setArguments(args);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_deportista_perfil, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        athlete = (Athlete) requireArguments().getSerializable(ARG_ATHLETE);

        tvAvatar = view.findViewById(R.id.tvAvatar);
        tvNombrePerfil = view.findViewById(R.id.tvNombrePerfil);
        tvEdadAno = view.findViewById(R.id.tvEdadAno);
        llTags = view.findViewById(R.id.llTags);
        tvMisMarcasTab = view.findViewById(R.id.tabMisMarcas);
        tvHistorialTab = view.findViewById(R.id.tabHistorial);
        indicadorMarcas = view.findViewById(R.id.indicadorMarcas);
        indicadorHistorial = view.findViewById(R.id.indicadorHistorial);
        layoutMarcasContent = view.findViewById(R.id.layoutMarcasContent);
        layoutHistorialContent = view.findViewById(R.id.layoutHistorialContent);
        pbPerfil = view.findViewById(R.id.pbPerfil);

        view.findViewById(R.id.btnVolverPerfil).setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack());

        // Estrella: toggle favorito
        TextView btnFavorito = view.findViewById(R.id.btnFavorito);
        actualizarEstrella(btnFavorito);
        btnFavorito.setOnClickListener(v -> {
            PrefsManager.toggleFavorito(requireContext(), athlete);
            actualizarEstrella(btnFavorito);
        });

        // Tabs
        tvMisMarcasTab.setOnClickListener(v -> cambiarPestana(true));
        tvHistorialTab.setOnClickListener(v -> cambiarPestana(false));

        // Rellenar cabecera
        mostrarCabecera();

        // Cargar mínimas y récords
        cargarMinRecord();
    }

    // ==========================================
    // CABECERA
    // ==========================================

    private void actualizarEstrella(TextView btn) {
        boolean esFav = PrefsManager.esFavorito(requireContext(), athlete.getNombre());
        btn.setText(esFav ? "★" : "☆");
        btn.setTextColor(esFav ? Color.parseColor("#D97706") : Color.parseColor("#A0AEC0"));
    }

    private void mostrarCabecera() {
        tvAvatar.setText(athlete.getIniciales());
        tvNombrePerfil.setText(athlete.getNombre() != null ? athlete.getNombre() : "—");

        // Edad y año
        String edadStr = "—";
        if (athlete.getAnoNacimiento() > 0) {
            int ano = (int) athlete.getAnoNacimiento();
            int edad = Calendar.getInstance().get(Calendar.YEAR) - ano;
            edadStr = edad + " años · " + ano;
        }
        tvEdadAno.setText(edadStr);

        // Tags
        llTags.removeAllViews();
        if (athlete.getClub() != null && !athlete.getClub().isEmpty())
            llTags.addView(crearTag(athlete.getClub(), "#DBEAFE", "#1976D2"));

        if ((athlete.getCategoria() != null && !athlete.getCategoria().isEmpty())
                || (athlete.getGenero() != null && !athlete.getGenero().isEmpty())) {

            LinearLayout filaCatGen = new LinearLayout(requireContext());
            filaCatGen.setOrientation(LinearLayout.HORIZONTAL);
            filaCatGen.setGravity(android.view.Gravity.CENTER);
            LinearLayout.LayoutParams fp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            fp.setMargins(0, dpToPx(6), 0, 0);
            filaCatGen.setLayoutParams(fp);

            if (athlete.getCategoria() != null && !athlete.getCategoria().isEmpty())
                filaCatGen.addView(crearTag(athlete.getCategoria(), "#F5F3FF", "#7C3AED"));

            if (athlete.getGenero() != null && !athlete.getGenero().isEmpty()) {
                boolean fem = normalizarGenero(athlete.getGenero()).equals("F");
                filaCatGen.addView(crearTag(
                        fem ? "Femenino" : "Masculino",
                        fem ? "#FDF2F8" : "#EFF6FF",
                        fem ? "#BE185D" : "#1D4ED8"));
            }
            llTags.addView(filaCatGen);
        }
    }

    private View crearTag(String texto, String bgHex, String textHex) {
        TextView tag = new TextView(requireContext());
        tag.setText(texto);
        tag.setTextSize(12f);
        try {
            tag.setTextColor(Color.parseColor(textHex));
            tag.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(bgHex)));
        } catch (Exception ignored) {}
        tag.setBackgroundResource(R.drawable.bg_lap_number);
        tag.setPadding(dpToPx(10), dpToPx(5), dpToPx(10), dpToPx(5));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        p.setMargins(0, 0, dpToPx(8), 0);
        tag.setLayoutParams(p);
        return tag;
    }
    private void cambiarPestana(boolean marcas) {
        esPestanaMarcas = marcas;
        if (marcas) {
            tvMisMarcasTab.setTextColor(Color.parseColor("#1976D2"));
            tvMisMarcasTab.setTypeface(null, Typeface.BOLD);
            indicadorMarcas.setBackgroundColor(Color.parseColor("#1976D2"));
            tvHistorialTab.setTextColor(Color.parseColor("#A0AEC0"));
            tvHistorialTab.setTypeface(null, Typeface.NORMAL);
            indicadorHistorial.setBackgroundColor(Color.TRANSPARENT);
            layoutMarcasContent.setVisibility(View.VISIBLE);
            layoutHistorialContent.setVisibility(View.GONE);
        } else {
            tvHistorialTab.setTextColor(Color.parseColor("#1976D2"));
            tvHistorialTab.setTypeface(null, Typeface.BOLD);
            indicadorHistorial.setBackgroundColor(Color.parseColor("#1976D2"));
            tvMisMarcasTab.setTextColor(Color.parseColor("#A0AEC0"));
            tvMisMarcasTab.setTypeface(null, Typeface.NORMAL);
            indicadorMarcas.setBackgroundColor(Color.TRANSPARENT);
            layoutMarcasContent.setVisibility(View.GONE);
            layoutHistorialContent.setVisibility(View.VISIBLE);
            if (!historialCargado) cargarHistorial();
        }
    }

    // ==========================================
    // CARGA MÍNIMAS Y RÉCORDS
    // ==========================================

    private void cargarMinRecord() {
        if (cacheMinimas != null && cacheRecords != null) {
            buildMisMarcas();
            return;
        }

        pbPerfil.setVisibility(View.VISIBLE);
        final boolean[] minimasOk = {cacheMinimas != null};
        final boolean[] recordsOk = {cacheRecords != null};

        Runnable checkDone = () -> {
            if (minimasOk[0] && recordsOk[0] && isAdded()) buildMisMarcas();
        };

        if (cacheMinimas == null) {
            db.collection("minimas").get().addOnSuccessListener(snap -> {
                cacheMinimas = new ArrayList<>();
                for (QueryDocumentSnapshot doc : snap)
                    cacheMinimas.add(doc.toObject(MinRecord.class));
                minimasOk[0] = true;
                checkDone.run();
            });
        }

        if (cacheRecords == null) {
            db.collection("records").get().addOnSuccessListener(snap -> {
                cacheRecords = new ArrayList<>();
                for (QueryDocumentSnapshot doc : snap)
                    cacheRecords.add(doc.toObject(MinRecord.class));
                recordsOk[0] = true;
                checkDone.run();
            });
        }
    }

    // ==========================================
    // MIS MARCAS
    // ==========================================

    private void buildMisMarcas() {
        pbPerfil.setVisibility(View.GONE);
        layoutMarcasContent.removeAllViews();
        layoutMarcasContent.setVisibility(View.VISIBLE);

        if (athlete.getMarcas() == null || athlete.getMarcas().isEmpty()) {
            TextView tv = new TextView(requireContext());
            tv.setText("Sin marcas registradas");
            tv.setTextColor(Color.parseColor("#A0AEC0"));
            tv.setGravity(Gravity.CENTER);
            tv.setPadding(0, dpToPx(32), 0, dpToPx(32));
            layoutMarcasContent.addView(tv);
            return;
        }

        for (Marca marca : athlete.getMarcas()) {
            layoutMarcasContent.addView(crearTarjetaMarca(marca));
            View sep = new View(requireContext());
            sep.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(12)));
            layoutMarcasContent.addView(sep);
        }
    }

    private View crearTarjetaMarca(Marca marca) {
        // Buscar mínimas primero para determinar el color del tiempo
        MinRecord minimaRFESS  = buscarEnLista(cacheMinimas, marca.getPrueba(),
                athlete.getCategoria(), athlete.getGenero(), "RFESS");
        MinRecord minimaFESSGA = buscarEnLista(cacheMinimas, marca.getPrueba(),
                athlete.getCategoria(), athlete.getGenero(), "FESSGA");
        MinRecord recordRFESS  = buscarEnLista(cacheRecords, marca.getPrueba(),
                athlete.getCategoria(), athlete.getGenero(), "RFESS");
        MinRecord recordFESSGA = buscarEnLista(cacheRecords, marca.getPrueba(),
                athlete.getCategoria(), athlete.getGenero(), "FESSGA");

        // Color: verde si cumple alguna mínima, rojo si no la cumple, azul si no hay datos
        MinRecord minimaRef = minimaRFESS != null ? minimaRFESS : minimaFESSGA;
        int colorTiempo;
        if (minimaRef != null) {
            colorTiempo = marca.getTiempoMs() <= minimaRef.getTiempoMs()
                    ? Color.parseColor("#2E7D32")   // verde: cumple mínima
                    : Color.parseColor("#C62828");  // rojo: no cumple mínima
        } else {
            colorTiempo = Color.parseColor("#1A1A2E"); // gris oscuro: sin datos
        }
        CardView card = new CardView(requireContext());
        CardView.LayoutParams cardParams = new CardView.LayoutParams(
                CardView.LayoutParams.MATCH_PARENT, CardView.LayoutParams.WRAP_CONTENT);
        card.setLayoutParams(cardParams);
        card.setRadius(dpToPx(16));
        card.setCardElevation(dpToPx(2));
        card.setCardBackgroundColor(Color.WHITE);

        LinearLayout inner = new LinearLayout(requireContext());
        inner.setOrientation(LinearLayout.VERTICAL);
        inner.setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16));

        // Badge de prueba
        TextView tvPrueba = new TextView(requireContext());
        String pruebaDisplay = marca.getPrueba() != null ? marca.getPrueba().toUpperCase() : "—";
        tvPrueba.setText(pruebaDisplay);
        tvPrueba.setTextSize(11f);
        tvPrueba.setTextColor(Color.parseColor("#1976D2"));
        tvPrueba.setTypeface(null, Typeface.BOLD);
        tvPrueba.setBackgroundResource(R.drawable.bg_lap_number);
        tvPrueba.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#DBEAFE")));
        tvPrueba.setPadding(dpToPx(10), dpToPx(4), dpToPx(10), dpToPx(4));
        LinearLayout.LayoutParams pruebap = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        pruebap.setMargins(0, 0, 0, dpToPx(8));
        tvPrueba.setLayoutParams(pruebap);
        inner.addView(tvPrueba);

        // Piscina
        if (marca.getPiscina() != null) {
            TextView tvPiscina = new TextView(requireContext());
            tvPiscina.setText("🏊 Piscina " + marca.getPiscina());
            tvPiscina.setTextSize(13f);
            tvPiscina.setTextColor(Color.parseColor("#64748B"));
            LinearLayout.LayoutParams pp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            pp.setMargins(0, 0, 0, dpToPx(8));
            tvPiscina.setLayoutParams(pp);
            inner.addView(tvPiscina);
        }

        // Tiempo grande
        TextView tvTiempo = new TextView(requireContext());
        tvTiempo.setText(marca.getTiempoDisplay());
        tvTiempo.setTextSize(48f);
        tvTiempo.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        tvTiempo.setTextColor(colorTiempo);
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        tp.setMargins(0, dpToPx(4), 0, dpToPx(12));
        tvTiempo.setLayoutParams(tp);
        inner.addView(tvTiempo);

        // Sección RFESS
        if (minimaRFESS != null || recordRFESS != null) {
            inner.addView(crearSeparadorSeccion("🇪🇸", "RFESS — Nacional"));
            inner.addView(crearFilaMinRecord(
                    minimaRFESS, "Mínima España", marca.getTiempoMs(),
                    recordRFESS, "Récord España"));
        }

        // Sección FESSGA
        inner.addView(crearSeparadorSeccion("GAL", "FESSGA — Galicia"));
        inner.addView(crearFilaMinRecord(
                minimaFESSGA, "Mínima Galicia", marca.getTiempoMs(),
                recordFESSGA, "Récord Galicia"));

        if (minimaFESSGA == null && recordFESSGA == null) {
            TextView tvAviso = new TextView(requireContext());
            tvAviso.setText("Los datos autonómicos FESSGA se mostrarán cuando estén disponibles.");
            tvAviso.setTextSize(11f);
            tvAviso.setTextColor(Color.parseColor("#A0AEC0"));
            LinearLayout.LayoutParams ap = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            ap.setMargins(0, dpToPx(6), 0, 0);
            tvAviso.setLayoutParams(ap);
            inner.addView(tvAviso);
        }

        card.addView(inner);
        return card;
    }

    private View crearSeparadorSeccion(String badge, String titulo) {
        LinearLayout fila = new LinearLayout(requireContext());
        fila.setOrientation(LinearLayout.HORIZONTAL);
        fila.setGravity(android.view.Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        p.setMargins(0, dpToPx(12), 0, dpToPx(8));
        fila.setLayoutParams(p);

        TextView tvBadge = new TextView(requireContext());
        tvBadge.setText(badge);
        tvBadge.setTextSize(10f);
        tvBadge.setTypeface(null, Typeface.BOLD);
        tvBadge.setTextColor(Color.parseColor("#1976D2"));
        tvBadge.setBackgroundResource(R.drawable.bg_lap_number);
        tvBadge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#DBEAFE")));
        tvBadge.setPadding(dpToPx(6), dpToPx(2), dpToPx(6), dpToPx(2));
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        bp.setMargins(0, 0, dpToPx(8), 0);
        tvBadge.setLayoutParams(bp);
        fila.addView(tvBadge);

        TextView tvTitulo = new TextView(requireContext());
        tvTitulo.setText(titulo);
        tvTitulo.setTextSize(12f);
        tvTitulo.setTextColor(Color.parseColor("#64748B"));

        View linea = new View(requireContext());
        linea.setBackgroundColor(Color.parseColor("#E2E8F0"));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, 1, 1f);
        lp.setMargins(dpToPx(8), 0, 0, 0);
        lp.gravity = android.view.Gravity.CENTER_VERTICAL;
        linea.setLayoutParams(lp);

        fila.addView(tvTitulo);
        fila.addView(linea);
        return fila;
    }

    private View crearFilaMinRecord(MinRecord minima, String labelMin, long atletaMs,
                                    MinRecord record, String labelRec) {
        LinearLayout fila = new LinearLayout(requireContext());
        fila.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        fila.setLayoutParams(p);

        // Caja mínima
        fila.addView(crearCajaMinima(minima, labelMin, atletaMs));
        View spacer = new View(requireContext());
        spacer.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(8), 0));
        fila.addView(spacer);
        // Caja récord
        fila.addView(crearCajaRecord(record, labelRec));

        return fila;
    }

    private View crearCajaMinima(MinRecord minima, String label, long atletaMs) {
        LinearLayout caja = new LinearLayout(requireContext());
        caja.setOrientation(LinearLayout.VERTICAL);
        caja.setPadding(dpToPx(12), dpToPx(10), dpToPx(12), dpToPx(10));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        caja.setLayoutParams(p);
        caja.setBackgroundResource(R.drawable.bg_lap_number);

        if (minima == null) {
            caja.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F1F5F9")));
            caja.addView(labelCaja(label, "#A0AEC0", "—"));
            return caja;
        }

        boolean cumple = atletaMs <= minima.getTiempoMs();
        if (cumple) {
            caja.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F0FDF4")));
            caja.addView(labelCajaConIcono("✅ " + label, "#15803D", minima.getTiempoDisplay(), "#15803D"));
        } else {
            caja.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFF1F2")));
            caja.addView(labelCajaConIcono("❌ " + label, "#BE123C", minima.getTiempoDisplay(), "#BE123C"));
        }
        return caja;
    }

    private View crearCajaRecord(MinRecord record, String label) {
        LinearLayout caja = new LinearLayout(requireContext());
        caja.setOrientation(LinearLayout.VERTICAL);
        caja.setPadding(dpToPx(12), dpToPx(10), dpToPx(12), dpToPx(10));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        caja.setLayoutParams(p);
        caja.setBackgroundResource(R.drawable.bg_lap_number);

        if (record == null) {
            caja.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F1F5F9")));
            caja.addView(labelCaja(label, "#A0AEC0", "—"));
        } else {
            caja.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#EFF6FF")));
            caja.addView(labelCajaConIcono("🏅 " + label, "#1D4ED8", record.getTiempoDisplay(), "#1D4ED8"));
        }
        return caja;
    }

    private View labelCaja(String label, String labelColor, String valor) {
        LinearLayout col = new LinearLayout(requireContext());
        col.setOrientation(LinearLayout.VERTICAL);
        col.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        TextView tvLabel = new TextView(requireContext()); tvLabel.setText(label);
        tvLabel.setTextSize(11f); tvLabel.setTextColor(Color.parseColor(labelColor));
        col.addView(tvLabel);

        TextView tvValor = new TextView(requireContext()); tvValor.setText(valor);
        tvValor.setTextSize(14f); tvValor.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        tvValor.setTextColor(Color.parseColor(labelColor));
        col.addView(tvValor);
        return col;
    }

    private View labelCajaConIcono(String label, String labelColor, String valor, String valorColor) {
        LinearLayout col = new LinearLayout(requireContext());
        col.setOrientation(LinearLayout.VERTICAL);
        col.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        TextView tvLabel = new TextView(requireContext()); tvLabel.setText(label);
        tvLabel.setTextSize(11f); tvLabel.setTextColor(Color.parseColor(labelColor));
        col.addView(tvLabel);

        TextView tvValor = new TextView(requireContext()); tvValor.setText(valor);
        tvValor.setTextSize(14f); tvValor.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        tvValor.setTextColor(Color.parseColor(valorColor));
        col.addView(tvValor);
        return col;
    }

    // ==========================================
    // HISTORIAL
    // ==========================================

    private void cargarHistorial() {
        historialCargado = true;
        pbPerfil.setVisibility(View.VISIBLE);
        layoutHistorialContent.removeAllViews();

        // ID del documento atleta = nombre con espacios → guiones bajos
        String athleteId = athlete.getNombre().replace(" ", "_");

        db.collection("athletes").document(athleteId)
                .collection("temporadas")
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!isAdded()) return;
                    pbPerfil.setVisibility(View.GONE);
                    if (snapshot.isEmpty()) {
                        mostrarHistorialVacio();
                        return;
                    }
                    // Mostrar todas las temporadas
                    for (QueryDocumentSnapshot doc : snapshot) {
                        buildTemporadaCard(doc);
                    }
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    pbPerfil.setVisibility(View.GONE);
                    mostrarHistorialVacio();
                });
    }

    @SuppressWarnings("unchecked")
    private void buildTemporadaCard(QueryDocumentSnapshot doc) {
        String temporada = doc.getString("temporada");
        Map<String, Object> mejoresTiempos =
                (Map<String, Object>) doc.get("mejores_tiempos_temporada");
        List<Map<String, Object>> competiciones =
                (List<Map<String, Object>>) doc.get("competiciones");

        // Cabecera de temporada
        TextView tvTemp = new TextView(requireContext());
        tvTemp.setText("Temporada " + (temporada != null ? temporada : ""));
        tvTemp.setTextSize(16f);
        tvTemp.setTypeface(null, Typeface.BOLD);
        tvTemp.setTextColor(Color.parseColor("#1A1A2E"));
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        tp.setMargins(0, dpToPx(8), 0, dpToPx(10));
        tvTemp.setLayoutParams(tp);
        layoutHistorialContent.addView(tvTemp);

        // MEJORES MARCAS
        if (mejoresTiempos != null && !mejoresTiempos.isEmpty()) {
            layoutHistorialContent.addView(seccionLabel("MEJORES MARCAS"));

            CardView cardMejores = new CardView(requireContext());
            cardMejores.setRadius(dpToPx(12));
            cardMejores.setCardElevation(dpToPx(2));
            cardMejores.setCardBackgroundColor(Color.WHITE);
            LinearLayout.LayoutParams cmp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            cmp.setMargins(0, 0, 0, dpToPx(16));
            cardMejores.setLayoutParams(cmp);

            LinearLayout innerMejores = new LinearLayout(requireContext());
            innerMejores.setOrientation(LinearLayout.VERTICAL);
            innerMejores.setPadding(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4));

            boolean primera = true;
            for (Map.Entry<String, Object> entry : mejoresTiempos.entrySet()) {
                if (!primera) {
                    View div = new View(requireContext());
                    LinearLayout.LayoutParams dp = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, 1);
                    dp.setMargins(dpToPx(16), 0, dpToPx(16), 0);
                    div.setLayoutParams(dp);
                    div.setBackgroundColor(Color.parseColor("#F1F5F9"));
                    innerMejores.addView(div);
                }
                primera = false;

                String prueba = entry.getKey();
                Map<String, Object> data = (Map<String, Object>) entry.getValue();
                String tiempo = data != null ? (String) data.get("tiempo") : null;
                String piscina = data != null ? (String) data.get("piscina") : null;
                String crono = data != null ? (String) data.get("crono") : null;

                String tiempoDisplay = tiempo != null
                        ? (tiempo.contains(":")
                        ? (tiempo.lastIndexOf(':') >= 0
                        ? tiempo.substring(0, tiempo.lastIndexOf(':')) + "." + tiempo.substring(tiempo.lastIndexOf(':') + 1)
                        : tiempo)
                        : tiempo)
                        : "—";

                LinearLayout fila = new LinearLayout(requireContext());
                fila.setOrientation(LinearLayout.HORIZONTAL);
                fila.setGravity(android.view.Gravity.CENTER_VERTICAL);
                fila.setPadding(dpToPx(16), dpToPx(12), dpToPx(16), dpToPx(12));

                LinearLayout textos = new LinearLayout(requireContext());
                textos.setOrientation(LinearLayout.VERTICAL);
                textos.setLayoutParams(new LinearLayout.LayoutParams(0,
                        LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

                TextView tvPrueba = new TextView(requireContext());
                tvPrueba.setText(prueba);
                tvPrueba.setTextSize(13f);
                tvPrueba.setTextColor(Color.parseColor("#1A1A2E"));
                textos.addView(tvPrueba);

                if (piscina != null || crono != null) {
                    TextView tvSub = new TextView(requireContext());
                    tvSub.setText((piscina != null ? piscina : "") +
                            (crono != null ? " · " + (crono.equals("E") ? "Electrónico" : "Manual") : ""));
                    tvSub.setTextSize(11f);
                    tvSub.setTextColor(Color.parseColor("#A0AEC0"));
                    textos.addView(tvSub);
                }

                fila.addView(textos);

                TextView tvT = new TextView(requireContext());
                tvT.setText(tiempoDisplay);
                tvT.setTextSize(15f);
                tvT.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
                tvT.setTextColor(Color.parseColor("#1976D2"));
                fila.addView(tvT);

                innerMejores.addView(fila);
            }
            cardMejores.addView(innerMejores);
            layoutHistorialContent.addView(cardMejores);
        }

        // COMPETICIONES
        if (competiciones != null && !competiciones.isEmpty()) {
            layoutHistorialContent.addView(seccionLabel("COMPETICIONES"));
            for (Map<String, Object> comp : competiciones) {
                layoutHistorialContent.addView(buildCompeticionCard(comp));
            }
        }

        // Separador entre temporadas
        View sep = new View(requireContext());
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(1));
        sp.setMargins(0, dpToPx(16), 0, dpToPx(8));
        sep.setLayoutParams(sp);
        sep.setBackgroundColor(Color.parseColor("#E2E8F0"));
        layoutHistorialContent.addView(sep);
    }

    @SuppressWarnings("unchecked")
    private View buildCompeticionCard(Map<String, Object> comp) {
        String nombre   = (String) comp.get("nombre");
        String entorno  = (String) comp.get("entorno");
        List<Map<String, Object>> pruebas = (List<Map<String, Object>>) comp.get("pruebas");

        CardView card = new CardView(requireContext());
        card.setRadius(dpToPx(12));
        card.setCardElevation(dpToPx(2));
        card.setCardBackgroundColor(Color.WHITE);
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        cp.setMargins(0, 0, 0, dpToPx(10));
        card.setLayoutParams(cp);

        LinearLayout inner = new LinearLayout(requireContext());
        inner.setOrientation(LinearLayout.VERTICAL);

        // Cabecera expandible
        LinearLayout header = new LinearLayout(requireContext());
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(android.view.Gravity.CENTER_VERTICAL);
        header.setPadding(dpToPx(16), dpToPx(14), dpToPx(16), dpToPx(14));
        header.setClickable(true);
        header.setFocusable(true);

        TextView tvTrophy = new TextView(requireContext());
        tvTrophy.setText("🏆");
        tvTrophy.setTextSize(18f);
        tvTrophy.setBackgroundResource(R.drawable.bg_lap_number);
        tvTrophy.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#DBEAFE")));
        tvTrophy.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams trp = new LinearLayout.LayoutParams(dpToPx(40), dpToPx(40));
        trp.setMargins(0, 0, dpToPx(14), 0);
        tvTrophy.setLayoutParams(trp);
        header.addView(tvTrophy);

        LinearLayout textos = new LinearLayout(requireContext());
        textos.setOrientation(LinearLayout.VERTICAL);
        textos.setLayoutParams(new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView tvNombre = new TextView(requireContext());
        tvNombre.setText(nombre != null ? nombre : "—");
        tvNombre.setTextSize(14f);
        tvNombre.setTypeface(null, Typeface.BOLD);
        tvNombre.setTextColor(Color.parseColor("#1A1A2E"));
        textos.addView(tvNombre);

        if (entorno != null) {
            TextView tvEnt = new TextView(requireContext());
            tvEnt.setText(entorno);
            tvEnt.setTextSize(12f);
            tvEnt.setTextColor(Color.parseColor("#A0AEC0"));
            textos.addView(tvEnt);
        }
        header.addView(textos);

        TextView tvArrow = new TextView(requireContext());
        tvArrow.setText("∨");
        tvArrow.setTextSize(16f);
        tvArrow.setTextColor(Color.parseColor("#1976D2"));
        header.addView(tvArrow);

        // Contenedor de pruebas (inicialmente visible)
        LinearLayout layoutPruebas = new LinearLayout(requireContext());
        layoutPruebas.setOrientation(LinearLayout.VERTICAL);
        layoutPruebas.setPadding(dpToPx(16), 0, dpToPx(16), dpToPx(12));
        layoutPruebas.setVisibility(View.VISIBLE);

        if (pruebas != null) {
            for (Map<String, Object> prueba : pruebas) {
                String p    = (String) prueba.get("prueba");
                String t    = (String) prueba.get("tiempo");
                Object posObj = prueba.get("posicion");
                int pos = posObj instanceof Long ? ((Long) posObj).intValue() : 0;

                String tDisplay = t != null
                        ? (t.lastIndexOf(':') >= 0
                        ? t.substring(0, t.lastIndexOf(':')) + "." + t.substring(t.lastIndexOf(':') + 1)
                        : t)
                        : "—";

                LinearLayout fila = new LinearLayout(requireContext());
                fila.setOrientation(LinearLayout.HORIZONTAL);
                fila.setGravity(android.view.Gravity.CENTER_VERTICAL);
                LinearLayout.LayoutParams fp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                fp.setMargins(0, dpToPx(4), 0, dpToPx(4));
                fila.setLayoutParams(fp);

                // Posición
                if (pos > 0) {
                    TextView tvPos = new TextView(requireContext());
                    tvPos.setText(pos + "º");
                    tvPos.setTextSize(11f);
                    tvPos.setTypeface(null, Typeface.BOLD);
                    tvPos.setTextColor(Color.WHITE);
                    tvPos.setGravity(Gravity.CENTER);
                    tvPos.setBackgroundResource(R.drawable.bg_lap_number);
                    tvPos.setBackgroundTintList(ColorStateList.valueOf(
                            pos == 1 ? Color.parseColor("#D97706") :
                                    pos == 2 ? Color.parseColor("#6B7280") :
                                            Color.parseColor("#92400E")));
                    tvPos.setPadding(dpToPx(8), dpToPx(3), dpToPx(8), dpToPx(3));
                    LinearLayout.LayoutParams pp2 = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    pp2.setMargins(0, 0, dpToPx(10), 0);
                    tvPos.setLayoutParams(pp2);
                    fila.addView(tvPos);
                }

                TextView tvP = new TextView(requireContext());
                tvP.setText(p != null ? p : "—");
                tvP.setTextSize(13f);
                tvP.setTextColor(Color.parseColor("#1A1A2E"));
                tvP.setLayoutParams(new LinearLayout.LayoutParams(0,
                        LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
                fila.addView(tvP);

                TextView tvT = new TextView(requireContext());
                tvT.setText(tDisplay);
                tvT.setTextSize(14f);
                tvT.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
                tvT.setTextColor(Color.parseColor("#1976D2"));
                fila.addView(tvT);

                layoutPruebas.addView(fila);
            }
        }

        // Toggle expand/collapse
        final boolean[] expandido = {true};
        header.setOnClickListener(v -> {
            expandido[0] = !expandido[0];
            layoutPruebas.setVisibility(expandido[0] ? View.VISIBLE : View.GONE);
            tvArrow.setText(expandido[0] ? "∨" : "›");
        });

        inner.addView(header);
        inner.addView(layoutPruebas);
        card.addView(inner);
        return card;
    }

    private View seccionLabel(String texto) {
        TextView tv = new TextView(requireContext());
        tv.setText(texto);
        tv.setTextSize(11f);
        tv.setTypeface(null, Typeface.BOLD);
        tv.setTextColor(Color.parseColor("#A0AEC0"));
        tv.setLetterSpacing(0.08f);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        p.setMargins(0, dpToPx(8), 0, dpToPx(6));
        tv.setLayoutParams(p);
        return tv;
    }

    private void mostrarHistorialVacio() {
        TextView tv = new TextView(requireContext());
        tv.setText("Sin historial disponible");
        tv.setTextColor(Color.parseColor("#A0AEC0"));
        tv.setGravity(Gravity.CENTER);
        tv.setPadding(0, dpToPx(32), 0, dpToPx(32));
        layoutHistorialContent.addView(tv);
    }

    // ==========================================
    // BÚSQUEDA EN MÍNIMAS/RÉCORDS
    // ==========================================

    private MinRecord buscarEnLista(List<MinRecord> lista, String prueba,
                                    String categoria, String genero, String origen) {
        if (lista == null || prueba == null) return null;
        String pruebaN = BuscarFragment.normalizar(prueba);
        String catN    = BuscarFragment.normalizar(categoria != null ? categoria : "");
        String genN    = normalizarGenero(genero);

        for (MinRecord m : lista) {
            if (BuscarFragment.normalizar(m.getPrueba()).equals(pruebaN)
                    && BuscarFragment.normalizar(m.getCategoria() != null ? m.getCategoria() : "").equals(catN)
                    && normalizarGenero(m.getGenero()).equals(genN)
                    && origen.equalsIgnoreCase(m.getOrigen())) {
                return m;
            }
        }
        for (MinRecord m : lista) {
            String mCatN = BuscarFragment.normalizar(m.getCategoria() != null ? m.getCategoria() : "");
            if (BuscarFragment.normalizar(m.getPrueba()).equals(pruebaN)
                    && (mCatN.contains(catN) || catN.contains(mCatN))
                    && normalizarGenero(m.getGenero()).equals(genN)
                    && origen.equalsIgnoreCase(m.getOrigen())) {
                return m;
            }
        }
        return null;
    }

    // ==========================================
    // UTILIDADES
    // ==========================================

    private String normalizarGenero(String g) {
        if (g == null) return "";
        String gl = g.toLowerCase(java.util.Locale.getDefault()).trim();
        if (gl.startsWith("f")) return "F";
        if (gl.startsWith("m")) return "M";
        return g;
    }

    private int dpToPx(int dp) {
        return Math.round(dp * requireContext().getResources().getDisplayMetrics().density);
    }
}