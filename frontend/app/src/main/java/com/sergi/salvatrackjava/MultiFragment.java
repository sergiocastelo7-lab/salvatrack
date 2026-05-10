package com.sergi.salvatrackjava;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class MultiFragment extends Fragment {

    private LinearLayout llJugadoresMulti;
    private TextView btnNum2, btnNum3, btnNum4;
    private MaterialButton btnMultiIniciar;
    private ImageButton btnMultiReset;
    private ImageButton btnMultiSave;

    private int numJugadoresActual = 4;
    private boolean isMultiRunningGlobal = false;
    private final List<JugadorMulti> listaJugadores = new ArrayList<>();

    private final Handler handler = new Handler(Looper.getMainLooper());

    private final Runnable multiUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            boolean algunCorriendo = false;
            for (JugadorMulti j : listaJugadores) {
                if (j.isRunning) {
                    long current = j.elapsedTime + (System.currentTimeMillis() - j.startTime);
                    j.tvTime.setText(formatTime(current));
                    algunCorriendo = true;
                }
            }
            if (algunCorriendo) {
                handler.postDelayed(this, 10);
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_multi, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        llJugadoresMulti = view.findViewById(R.id.llJugadoresMulti);
        btnNum2          = view.findViewById(R.id.btnNum2);
        btnNum3          = view.findViewById(R.id.btnNum3);
        btnNum4          = view.findViewById(R.id.btnNum4);
        btnMultiIniciar  = view.findViewById(R.id.btnMultiIniciar);
        btnMultiReset    = view.findViewById(R.id.btnMultiReset);
        btnMultiSave     = view.findViewById(R.id.btnMultiSave);

        btnMultiIniciar.setOnClickListener(v -> toggleGlobalMulti());
        btnMultiReset.setOnClickListener(v -> resetGlobalMulti());
        view.findViewById(R.id.btnMultiSave).setOnClickListener(v ->
                android.widget.Toast.makeText(requireContext(),
                        "Guardar — Próximamente", android.widget.Toast.LENGTH_SHORT).show());

        configurarSelectorMulti();
        generarTarjetasMulti(4);
    }

    // ==========================================
    // LÓGICA MULTIJUGADOR
    // ==========================================

    private void configurarSelectorMulti() {
        btnNum2.setOnClickListener(v -> { cambiarSeleccionNum(2); generarTarjetasMulti(2); });
        btnNum3.setOnClickListener(v -> { cambiarSeleccionNum(3); generarTarjetasMulti(3); });
        btnNum4.setOnClickListener(v -> { cambiarSeleccionNum(4); generarTarjetasMulti(4); });
    }

    private void cambiarSeleccionNum(int num) {
        numJugadoresActual = num;
        TextView[] botones = {btnNum2, btnNum3, btnNum4};
        for (TextView btn : botones) {
            btn.setTextColor(Color.parseColor("#64748B"));
            btn.setBackgroundResource(0);
        }
        TextView seleccionado = (num == 2) ? btnNum2 : (num == 3) ? btnNum3 : btnNum4;
        seleccionado.setTextColor(Color.parseColor("#FFFFFF"));
        seleccionado.setBackgroundResource(R.drawable.bg_lap_number);
        seleccionado.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#1976D2")));
    }

    private void generarTarjetasMulti(int cantidad) {
        if (llJugadoresMulti == null) return;
        llJugadoresMulti.removeAllViews();
        listaJugadores.clear();

        // Reiniciar estado global al cambiar número de jugadores
        isMultiRunningGlobal = false;
        btnMultiIniciar.setText("Iniciar");
        btnMultiIniciar.setIconResource(R.drawable.ic_play);
        btnMultiIniciar.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#1976D2")));

        String[] colorTexto = {"#1976D2", "#7C3AED", "#059669", "#EA580C"};
        String[] colorFondo = {"#E3F2FD", "#F5F3FF", "#ECFDF5", "#FFF7ED"};

        for (int i = 0; i < cantidad; i++) {
            View lapView = LayoutInflater.from(requireContext())
                    .inflate(R.layout.item_jugador, llJugadoresMulti, false);

            TextView tvPlayerNum      = lapView.findViewById(R.id.tvPlayerNum);
            TextView tvPlayerTime     = lapView.findViewById(R.id.tvPlayerTime);
            ImageButton btnPlayerPlay = lapView.findViewById(R.id.btnPlayerPlay);
            ImageButton btnPlayerFlag = lapView.findViewById(R.id.btnPlayerFlag);

            tvPlayerNum.setText(String.valueOf(i + 1));
            tvPlayerNum.setTextColor(Color.parseColor(colorTexto[i]));
            tvPlayerNum.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(colorFondo[i])));

            btnPlayerPlay.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(colorFondo[i])));
            btnPlayerPlay.setColorFilter(Color.parseColor(colorTexto[i]));

            JugadorMulti jugador = new JugadorMulti();
            jugador.tvTime  = tvPlayerTime;
            jugador.btnPlay = btnPlayerPlay;
            jugador.llLaps  = lapView.findViewById(R.id.llPlayerLaps);

            final int indexColor = i;
            final int numJugador = i + 1;

            btnPlayerPlay.setOnClickListener(v -> toggleJugadorIndividual(jugador));

            btnPlayerFlag.setOnClickListener(v -> {
                if (!jugador.isRunning) return;

                long current = jugador.elapsedTime + (System.currentTimeMillis() - jugador.startTime);
                long split   = (jugador.lastLapTime == 0L) ? current : current - jugador.lastLapTime;
                jugador.lastLapTime = current;

                TextView tvLap = new TextView(requireContext());
                tvLap.setText(jugador.lapCount + ". " + formatTime(current) + "   +" + formatTime(split));
                tvLap.setTextSize(11f);
                tvLap.setTypeface(android.graphics.Typeface.MONOSPACE, android.graphics.Typeface.BOLD);
                tvLap.setTextColor(Color.parseColor(colorTexto[indexColor]));
                tvLap.setBackgroundResource(R.drawable.bg_lap_number);
                tvLap.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(colorFondo[indexColor])));
                tvLap.setPadding(24, 8, 24, 8);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                params.setMargins(0, 0, 0, 8);
                tvLap.setLayoutParams(params);

                jugador.llLaps.addView(tvLap, 0);
                jugador.lapCount++;
            });

            listaJugadores.add(jugador);
            llJugadoresMulti.addView(lapView);
        }
    }

    private void toggleJugadorIndividual(JugadorMulti j) {
        if (j.isRunning) {
            j.elapsedTime += System.currentTimeMillis() - j.startTime;
            j.isRunning = false;
            j.btnPlay.setImageResource(R.drawable.ic_play);
        } else {
            j.startTime = System.currentTimeMillis();
            j.isRunning = true;
            j.btnPlay.setImageResource(R.drawable.ic_pause);
            handler.post(multiUpdateRunnable);
        }
        actualizarEstadoBotonGlobal();
    }

    private void toggleGlobalMulti() {
        if (isMultiRunningGlobal) {
            for (JugadorMulti j : listaJugadores) {
                if (j.isRunning) {
                    j.elapsedTime += System.currentTimeMillis() - j.startTime;
                    j.isRunning = false;
                    j.btnPlay.setImageResource(R.drawable.ic_play);
                }
            }
            isMultiRunningGlobal = false;
            btnMultiIniciar.setText("Iniciar");
            btnMultiIniciar.setIconResource(R.drawable.ic_play);
            btnMultiIniciar.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#1976D2")));
        } else {
            for (JugadorMulti j : listaJugadores) {
                if (!j.isRunning) {
                    j.startTime = System.currentTimeMillis();
                    j.isRunning = true;
                    j.btnPlay.setImageResource(R.drawable.ic_pause);
                }
            }
            isMultiRunningGlobal = true;
            btnMultiIniciar.setText("Parar");
            btnMultiIniciar.setIconResource(R.drawable.ic_pause);
            btnMultiIniciar.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E53935")));
            handler.post(multiUpdateRunnable);
        }
    }

    private void resetGlobalMulti() {
        for (JugadorMulti j : listaJugadores) {
            j.isRunning  = false;
            j.elapsedTime = 0L;
            j.startTime   = 0L;
            j.lastLapTime = 0L;
            j.lapCount    = 1;
            j.tvTime.setText("00:00.00");
            j.btnPlay.setImageResource(R.drawable.ic_play);
            j.llLaps.removeAllViews();
        }
        isMultiRunningGlobal = false;
        btnMultiIniciar.setText("Iniciar");
        btnMultiIniciar.setIconResource(R.drawable.ic_play);
        btnMultiIniciar.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#1976D2")));
    }

    private void actualizarEstadoBotonGlobal() {
        boolean algunCorriendo = false;
        for (JugadorMulti j : listaJugadores) {
            if (j.isRunning) { algunCorriendo = true; break; }
        }
        if (algunCorriendo && !isMultiRunningGlobal) {
            isMultiRunningGlobal = true;
            btnMultiIniciar.setText("Parar");
            btnMultiIniciar.setIconResource(R.drawable.ic_pause);
            btnMultiIniciar.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E53935")));
        } else if (!algunCorriendo && isMultiRunningGlobal) {
            isMultiRunningGlobal = false;
            btnMultiIniciar.setText("Iniciar");
            btnMultiIniciar.setIconResource(R.drawable.ic_play);
            btnMultiIniciar.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#1976D2")));
        }
    }

    // ==========================================
    // UTILIDADES
    // ==========================================

    private String formatTime(long millis) {
        long centis  = (millis % 1000) / 10;
        long seconds = (millis / 1000) % 60;
        long minutes = millis / 60000;
        return String.format("%02d:%02d.%02d", minutes, seconds, centis);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacks(multiUpdateRunnable); // Evitar memory leaks
    }
}
