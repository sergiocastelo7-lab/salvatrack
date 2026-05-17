package com.sergi.salvatrackjava;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Calendar;

public class DeportistaPerfilFragment extends Fragment {

    private static final String ARG_ATHLETE = "athlete";

    private Athlete athlete;

    private TextView tvAvatar;
    private TextView tvNombrePerfil;
    private TextView tvEdadAno;
    private LinearLayout llTags;
    private TextView tvMisMarcasTab;
    private TextView tvHistorialTab;
    private View indicadorMarcas;
    private View indicadorHistorial;
    private LinearLayout layoutMarcasContent;
    private LinearLayout layoutHistorialContent;

    private boolean esPestanaMarcas = true;

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

        view.findViewById(R.id.btnVolverPerfil).setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack());

        TextView btnFavorito = view.findViewById(R.id.btnFavorito);
        actualizarEstrella(btnFavorito);
        btnFavorito.setOnClickListener(v -> {
            PrefsManager.toggleFavorito(requireContext(), athlete);
            actualizarEstrella(btnFavorito);
        });

        tvMisMarcasTab.setOnClickListener(v -> cambiarPestana(true));
        tvHistorialTab.setOnClickListener(v -> cambiarPestana(false));

        mostrarCabecera();
        layoutMarcasContent.setVisibility(View.VISIBLE);
    }

    private void actualizarEstrella(TextView btn) {
        boolean esFav = PrefsManager.esFavorito(requireContext(), athlete.getNombre());
        btn.setText(esFav ? "★" : "☆");
        btn.setTextColor(esFav ? Color.parseColor("#D97706") : Color.parseColor("#A0AEC0"));
    }

    private void mostrarCabecera() {
        tvAvatar.setText(athlete.getIniciales());
        tvNombrePerfil.setText(athlete.getNombre() != null ? athlete.getNombre() : "—");

        String edadStr = "—";
        if (athlete.getAnoNacimiento() > 0) {
            int ano = (int) athlete.getAnoNacimiento();
            int edad = Calendar.getInstance().get(Calendar.YEAR) - ano;
            edadStr = edad + " años · " + ano;
        }
        tvEdadAno.setText(edadStr);

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
        }
    }

    private View crearTag(String texto, String bgHex, String textHex) {
        TextView tag = new TextView(requireContext());
        tag.setText(texto);
        tag.setTextSize(12f);
        try {
            tag.setTextColor(Color.parseColor(textHex));
            tag.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor(bgHex)));
        } catch (Exception ignored) {}
        tag.setBackgroundResource(R.drawable.bg_lap_number);
        tag.setPadding(dpToPx(10), dpToPx(5), dpToPx(10), dpToPx(5));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        p.setMargins(0, 0, dpToPx(8), 0);
        tag.setLayoutParams(p);
        return tag;
    }

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