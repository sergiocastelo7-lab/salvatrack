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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class IndividualFragment extends Fragment {

    private TextView tvTime;
    private TextView tvMillis;
    private ImageButton btnStartStop;
    private ImageButton btnReset;
    private LinearLayout btnParcial;
    private LinearLayout llParciales;

    private int contadorParciales = 1;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private long startTime = 0L;
    private long elapsedTime = 0L;
    private long lastLapTime = 0L;
    private boolean isRunning = false;
    private final Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            long current = elapsedTime + (System.currentTimeMillis() - startTime);
            updateDisplay(current);
            handler.postDelayed(this, 10);
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_individual, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvTime       = view.findViewById(R.id.tvTime);
        tvMillis     = view.findViewById(R.id.tvMillis);
        btnStartStop = view.findViewById(R.id.btnStartStop);
        btnReset     = view.findViewById(R.id.btnReset);
        btnParcial   = view.findViewById(R.id.btnParcial);
        llParciales  = view.findViewById(R.id.llParciales);

        btnStartStop.setOnClickListener(v -> {
            if (isRunning) stopCrono();
            else startCrono();
        });

        btnReset.setOnClickListener(v -> resetCrono());


        btnParcial.setOnClickListener(v -> {
            if (!isRunning) return;

            long current = elapsedTime + (System.currentTimeMillis() - startTime);
            long split   = (lastLapTime == 0L) ? current : current - lastLapTime;
            lastLapTime  = current;

            View lapView = LayoutInflater.from(requireContext())
                    .inflate(R.layout.item_parcial, llParciales, false);

            ((TextView) lapView.findViewById(R.id.tvLapNumber)).setText(String.valueOf(contadorParciales));
            ((TextView) lapView.findViewById(R.id.tvLapTotalTime)).setText(formatTime(current));
            ((TextView) lapView.findViewById(R.id.tvLapSplitTime)).setText("+" + formatTime(split));

            llParciales.addView(lapView, 0);
            contadorParciales++;
        });
    }

    // ==========================================
    // LÓGICA DEL CRONÓMETRO
    // ==========================================

    private void startCrono() {
        startTime = System.currentTimeMillis();
        handler.post(updateRunnable);
        isRunning = true;
        btnStartStop.setImageResource(R.drawable.ic_pause);
        btnStartStop.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E53935")));
    }

    private void stopCrono() {
        elapsedTime += System.currentTimeMillis() - startTime;
        handler.removeCallbacks(updateRunnable);
        isRunning = false;
        btnStartStop.setImageResource(R.drawable.ic_play);
        btnStartStop.setBackgroundTintList(null);
    }

    private void resetCrono() {
        handler.removeCallbacks(updateRunnable);
        isRunning    = false;
        elapsedTime  = 0L;
        startTime    = 0L;
        lastLapTime  = 0L;
        contadorParciales = 1;
        tvTime.setText("00:00");
        tvMillis.setText(".00");
        btnStartStop.setImageResource(R.drawable.ic_play);
        btnStartStop.setBackgroundTintList(null);
        llParciales.removeAllViews();
    }

    // ==========================================
    // UTILIDADES DE FORMATO
    // ==========================================

    private void updateDisplay(long millis) {
        long centis  = (millis % 1000) / 10;
        long seconds = (millis / 1000) % 60;
        long minutes = millis / 60000;
        tvTime.setText(String.format("%02d:%02d", minutes, seconds));
        tvMillis.setText(String.format(".%02d", centis));
    }

    private String formatTime(long millis) {
        long centis  = (millis % 1000) / 10;
        long seconds = (millis / 1000) % 60;
        long minutes = millis / 60000;
        return String.format("%02d:%02d.%02d", minutes, seconds, centis);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacks(updateRunnable); // Evitar memory leaks
    }
}
