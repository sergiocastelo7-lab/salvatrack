package com.sergi.salvatrackjava;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.content.res.ColorStateList;
import android.graphics.Color;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private TextView tvTime;
    private TextView tvMillis;
    private ImageButton btnStartStop;
    private ImageButton btnReset;
    private LinearLayout btnParcial;
    private LinearLayout llParciales;
    private int contadorParciales = 1;
    private final Handler handler = new Handler();
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvTime       = findViewById(R.id.tvTime);
        tvMillis     = findViewById(R.id.tvMillis);
        btnStartStop = findViewById(R.id.btnStartStop);
        btnReset     = findViewById(R.id.btnReset);
        btnParcial   = findViewById(R.id.btnParcial);

        llParciales  = findViewById(R.id.llParciales);

        btnStartStop.setOnClickListener(v -> {
            if (isRunning) stopCrono();
            else startCrono();
        });

        btnReset.setOnClickListener(v -> resetCrono());

        btnParcial.setOnClickListener(v -> {
            if (isRunning) {
                long current = elapsedTime + (System.currentTimeMillis() - startTime);

                long split = current - lastLapTime;
                if (lastLapTime == 0L) {
                    split = current;
                }
                lastLapTime = current;

                View lapView = getLayoutInflater().inflate(R.layout.item_parcial, llParciales, false);

                TextView tvLapNumber = lapView.findViewById(R.id.tvLapNumber);
                TextView tvLapTotalTime = lapView.findViewById(R.id.tvLapTotalTime);
                TextView tvLapSplitTime = lapView.findViewById(R.id.tvLapSplitTime);

                tvLapNumber.setText(String.valueOf(contadorParciales));
                tvLapTotalTime.setText(formatTime(current));
                tvLapSplitTime.setText("+" + formatTime(split));

                if (llParciales != null) {
                    llParciales.addView(lapView);
                }

                contadorParciales++;
            }
        });

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_crono);
        bottomNav.setOnItemSelectedListener(item -> true);
    }

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
        isRunning = false;
        elapsedTime = 0L;
        startTime = 0L;
        tvTime.setText("00:00");
        tvMillis.setText(".00");
        btnStartStop.setImageResource(R.drawable.ic_play);

        btnStartStop.setBackgroundTintList(null);

        if (llParciales != null) {
            llParciales.removeAllViews();
        }
        contadorParciales = 1;
        lastLapTime = 0L;
    }

    private void updateDisplay(long millis) {
        long centis  = (millis % 1000) / 10;
        long seconds = (millis / 1000) % 60;
        long minutes = (millis / 60000);
        tvTime.setText(String.format("%02d:%02d", minutes, seconds));
        tvMillis.setText(String.format(".%02d", centis));
    }

    private String formatTime(long millis) {
        long centis  = (millis % 1000) / 10;
        long seconds = (millis / 1000) % 60;
        long minutes = (millis / 60000);
        return String.format("%02d:%02d.%02d", minutes, seconds, centis);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(updateRunnable);
    }
}