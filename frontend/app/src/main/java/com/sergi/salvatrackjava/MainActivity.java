package com.sergi.salvatrackjava;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

public class MainActivity extends AppCompatActivity {

    private android.widget.TextView tvTime;
    private android.widget.TextView tvMillis;
    private ImageButton btnStartStop;
    private ImageButton btnReset;
    private LinearLayout btnParcial;
    private MaterialButton btnIndividual;
    private MaterialButton btnMulti;

    private final Handler handler = new Handler();
    private long startTime = 0L;
    private long elapsedTime = 0L;
    private boolean isRunning = false;
    private boolean isIndividual = true;

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

        btnStartStop.setOnClickListener(v -> {
            if (isRunning) stopCrono();
            else startCrono();
        });

        btnReset.setOnClickListener(v -> resetCrono());

        btnParcial.setOnClickListener(v -> {
            if (isRunning) {
                long current = elapsedTime + (System.currentTimeMillis() - startTime);
                Toast.makeText(this, "Parcial: " + formatTime(current), Toast.LENGTH_SHORT).show();
            }
        });

        btnIndividual.setOnClickListener(v -> setMode(true));
        btnMulti.setOnClickListener(v -> setMode(false));

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_crono);
        bottomNav.setOnItemSelectedListener(item -> true);
    }

    private void setMode(boolean individual) {
        isIndividual = individual;
        resetCrono();
        if (individual) {
            btnIndividual.setBackgroundTintList(ColorStateList.valueOf(0xFFFFFFFF));
            btnIndividual.setTextColor(getColor(R.color.primary));
            btnMulti.setBackgroundTintList(ColorStateList.valueOf(0x00000000));
            btnMulti.setTextColor(getColor(R.color.text_secondary));
        } else {
            btnMulti.setBackgroundTintList(ColorStateList.valueOf(0xFFFFFFFF));
            btnMulti.setTextColor(getColor(R.color.primary));
            btnIndividual.setBackgroundTintList(ColorStateList.valueOf(0x00000000));
            btnIndividual.setTextColor(getColor(R.color.text_secondary));
        }
    }

    private void startCrono() {
        startTime = System.currentTimeMillis();
        handler.post(updateRunnable);
        isRunning = true;
        btnStartStop.setImageResource(R.drawable.ic_pause);
    }

    private void stopCrono() {
        elapsedTime += System.currentTimeMillis() - startTime;
        handler.removeCallbacks(updateRunnable);
        isRunning = false;
        btnStartStop.setImageResource(R.drawable.ic_play);
    }

    private void resetCrono() {
        handler.removeCallbacks(updateRunnable);
        isRunning = false;
        elapsedTime = 0L;
        startTime = 0L;
        tvTime.setText("00:00");
        tvMillis.setText(".00");
        btnStartStop.setImageResource(R.drawable.ic_play);
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
