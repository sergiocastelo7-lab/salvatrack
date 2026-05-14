package com.sergi.salvatrackjava;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;

public class EventosDiaDialog extends DialogFragment {

    private static final String ARG_EVENTOS   = "eventos";
    private static final String ARG_FECHA_DIA = "fecha_dia";

    public static EventosDiaDialog newInstance(ArrayList<Evento> eventos, String fechaDia) {
        EventosDiaDialog dialog = new EventosDiaDialog();
        Bundle args = new Bundle();
        args.putSerializable(ARG_EVENTOS, eventos);
        args.putString(ARG_FECHA_DIA, fechaDia);
        dialog.setArguments(args);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_eventos_dia, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Fondo transparente para que se vean los bordes redondeados
        Window window = getDialog() != null ? getDialog().getWindow() : null;
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            // Anchura casi completa con margen lateral
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.90);
            window.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);
            WindowManager.LayoutParams wlp = window.getAttributes();
            wlp.gravity = Gravity.CENTER;
            window.setAttributes(wlp);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ArrayList<Evento> eventos = (ArrayList<Evento>) requireArguments().getSerializable(ARG_EVENTOS);
        String fechaDia = requireArguments().getString(ARG_FECHA_DIA, "");

        TextView tvNumEventos    = view.findViewById(R.id.tvNumEventos);
        LinearLayout llContainer = view.findViewById(R.id.llEventosDialog);

        if (eventos == null || eventos.isEmpty()) {
            dismiss();
            return;
        }

        // "1 evento" / "2 eventos"
        tvNumEventos.setText(eventos.size() == 1 ? "1 evento" : eventos.size() + " eventos");

        for (int i = 0; i < eventos.size(); i++) {
            llContainer.addView(crearTarjetaEvento(eventos.get(i)));
            if (i < eventos.size() - 1) {
                View sep = new View(requireContext());
                LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(10));
                sep.setLayoutParams(p);
                llContainer.addView(sep);
            }
        }

        view.findViewById(R.id.btnCerrarDialog).setOnClickListener(v -> dismiss());
    }

    private View crearTarjetaEvento(Evento evento) {
        LinearLayout card = new LinearLayout(requireContext());
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundResource(R.drawable.bg_lap_number);
        card.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFEBF5FB));
        card.setPadding(dpToPx(16), dpToPx(14), dpToPx(16), dpToPx(14));
        card.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        card.setClickable(true);
        card.setFocusable(true);

        // Navegar a Evento en Directo al pulsar la tarjeta
        card.setOnClickListener(v -> {
            dismiss();
        });

        // Nombre del evento
        TextView tvNombre = new TextView(requireContext());
        tvNombre.setText(evento.getEvento() != null ? evento.getEvento() : "—");
        tvNombre.setTextSize(15f);
        tvNombre.setTypeface(null, android.graphics.Typeface.BOLD);
        tvNombre.setTextColor(0xFF1A1A2E);
        LinearLayout.LayoutParams nombreParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        nombreParams.setMargins(0, 0, 0, dpToPx(10));
        tvNombre.setLayoutParams(nombreParams);
        card.addView(tvNombre);

        // Filas de info
        String fecha = evento.getFecha_inicio();
        if (fecha != null && evento.getFecha_fin() != null && !fecha.equals(evento.getFecha_fin())) {
            fecha = fecha + " → " + evento.getFecha_fin();
        }
        if (fecha != null) card.addView(filaInfo("📅", fecha));
        if (evento.getSede()    != null && !evento.getSede().isEmpty())
            card.addView(filaInfo("📍", evento.getSede()));
        if (evento.getHorario() != null && !evento.getHorario().isEmpty())
            card.addView(filaInfo("🕐", evento.getHorario()));
        if (evento.getOrigen()  != null && !evento.getOrigen().isEmpty())
            card.addView(filaInfo("🛡️", evento.getOrigen()));

        return card;
    }

    private View filaInfo(String emoji, String texto) {
        LinearLayout fila = new LinearLayout(requireContext());
        fila.setOrientation(LinearLayout.HORIZONTAL);
        fila.setGravity(android.view.Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        p.setMargins(0, 0, 0, dpToPx(4));
        fila.setLayoutParams(p);

        TextView tvEmoji = new TextView(requireContext());
        tvEmoji.setText(emoji);
        tvEmoji.setTextSize(13f);
        tvEmoji.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(24), LinearLayout.LayoutParams.WRAP_CONTENT));
        fila.addView(tvEmoji);

        TextView tvTexto = new TextView(requireContext());
        tvTexto.setText(texto);
        tvTexto.setTextSize(13f);
        tvTexto.setTextColor(0xFF4A5568);
        fila.addView(tvTexto);

        return fila;
    }

    private int dpToPx(int dp) {
        return Math.round(dp * requireContext().getResources().getDisplayMetrics().density);
    }
}