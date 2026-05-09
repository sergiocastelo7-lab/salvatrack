package com.sergi.salvatrackjava;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // --- Variables Individual ---
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

    // --- Variables Pestañas ---
    private MaterialButton btnIndividual;
    private MaterialButton btnMulti;
    private boolean isIndividual = true;

    // --- Variables Contenedores y Multijugador ---
    private LinearLayout layoutIndividual;
    private LinearLayout layoutMulti;
    private LinearLayout llJugadoresMulti;
    private TextView btnNum2, btnNum3, btnNum4;
    private MaterialButton btnMultiIniciar;
    private ImageButton btnMultiReset;
    private ImageButton btnMultiSave;

    private int numJugadoresActual = 4;
    private boolean isMultiRunningGlobal = false;
    private final List<JugadorMulti> listaJugadores = new ArrayList<>();

    // --- Runnable (Bucle) para el Crono Individual ---
    private final Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            long current = elapsedTime + (System.currentTimeMillis() - startTime);
            updateDisplay(current);
            handler.postDelayed(this, 10);
        }
    };

    // Runnable (Bucle) para los Cronos Multijugador
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
            // Si al menos uno está corriendo, seguimos el bucle
            if (algunCorriendo) {
                handler.postDelayed(this, 10);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar vistas Individual
        tvTime       = findViewById(R.id.tvTime);
        tvMillis     = findViewById(R.id.tvMillis);
        btnStartStop = findViewById(R.id.btnStartStop);
        btnReset     = findViewById(R.id.btnReset);
        btnParcial   = findViewById(R.id.btnParcial);
        llParciales  = findViewById(R.id.llParciales);

        // Inicializar Pestañas y Contenedores
        btnIndividual    = findViewById(R.id.btnIndividual);
        btnMulti         = findViewById(R.id.btnMulti);
        layoutIndividual = findViewById(R.id.layoutIndividual);
        layoutMulti      = findViewById(R.id.layoutMulti);

        // Inicializar vistas Multijugador
        llJugadoresMulti = findViewById(R.id.llJugadoresMulti);
        btnNum2 = findViewById(R.id.btnNum2);
        btnNum3 = findViewById(R.id.btnNum3);
        btnNum4 = findViewById(R.id.btnNum4);
        btnMultiIniciar = findViewById(R.id.btnMultiIniciar);
        btnMultiReset   = findViewById(R.id.btnMultiReset);
        btnMultiSave    = findViewById(R.id.btnMultiSave);

        // Eventos Pestañas
        btnIndividual.setOnClickListener(v -> setMode(true));
        btnMulti.setOnClickListener(v -> setMode(false));

        // Eventos Individual
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
                    llParciales.addView(lapView, 0);
                }
                contadorParciales++;
            }
        });

        // Eventos Globales Multijugador
        btnMultiIniciar.setOnClickListener(v -> toggleGlobalMulti());
        btnMultiReset.setOnClickListener(v -> resetGlobalMulti());
        btnMultiSave.setOnClickListener(v -> Toast.makeText(this, "Guardar datos... (Pronto)", Toast.LENGTH_SHORT).show());

        // Navegación Inferior
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_crono);
        bottomNav.setOnItemSelectedListener(item -> true);

        // Configurar botones 2, 3, 4 y generar iniciales
        configurarSelectorMulti();
        generarTarjetasMulti(4);
    }

    // ==========================================
    // LOGICA MULTIJUGADOR
    // ==========================================

    private void configurarSelectorMulti() {
        btnNum2.setOnClickListener(v -> { cambiarSeleccionNum(2); generarTarjetasMulti(2); });
        btnNum3.setOnClickListener(v -> { cambiarSeleccionNum(3); generarTarjetasMulti(3); });
        btnNum4.setOnClickListener(v -> { cambiarSeleccionNum(4); generarTarjetasMulti(4); });
    }

    private void cambiarSeleccionNum(int num) {
        numJugadoresActual = num;
        TextView[] botones = {btnNum2, btnNum3, btnNum4};

        // 1. Apagamos todos
        for (TextView btn : botones) {
            btn.setTextColor(Color.parseColor("#64748B"));
            btn.setBackgroundResource(0); // <-- Esto le quita el cuadradito a los que no están seleccionados
        }

        // 2. Encendemos solo el que has pulsado
        TextView seleccionado = (num == 2) ? btnNum2 : (num == 3) ? btnNum3 : btnNum4;
        seleccionado.setTextColor(Color.parseColor("#FFFFFF"));
        seleccionado.setBackgroundResource(R.drawable.bg_lap_number); // <-- Esto le pone la forma del cuadradito
        seleccionado.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#1976D2"))); // <-- Esto lo pinta de azul fuerte
    }

    private void generarTarjetasMulti(int cantidad) {
        if (llJugadoresMulti == null) return;
        llJugadoresMulti.removeAllViews();
        listaJugadores.clear();

        // Reiniciamos estado global al cambiar de número de jugadores
        isMultiRunningGlobal = false;
        btnMultiIniciar.setText("Iniciar");
        btnMultiIniciar.setIconResource(R.drawable.ic_play);
        btnMultiIniciar.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#1976D2")));

        String[] colorTexto = {"#1976D2", "#7C3AED", "#059669", "#EA580C"};
        String[] colorFondo = {"#E3F2FD", "#F5F3FF", "#ECFDF5", "#FFF7ED"};

        for (int i = 0; i < cantidad; i++) {
            View lapView = getLayoutInflater().inflate(R.layout.item_jugador, llJugadoresMulti, false);

            TextView tvPlayerNum = lapView.findViewById(R.id.tvPlayerNum);
            TextView tvPlayerTime = lapView.findViewById(R.id.tvPlayerTime);
            ImageButton btnPlayerPlay = lapView.findViewById(R.id.btnPlayerPlay);
            ImageButton btnPlayerFlag = lapView.findViewById(R.id.btnPlayerFlag);

            int numJugador = i + 1;
            tvPlayerNum.setText(String.valueOf(numJugador));
            tvPlayerNum.setTextColor(Color.parseColor(colorTexto[i]));
            tvPlayerNum.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(colorFondo[i])));

            btnPlayerPlay.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(colorFondo[i])));
            btnPlayerPlay.setColorFilter(Color.parseColor(colorTexto[i]));

            // Creamos el objeto lógico para controlar su tiempo
            JugadorMulti jugador = new JugadorMulti();
            jugador.tvTime = tvPlayerTime;
            jugador.btnPlay = btnPlayerPlay;
            jugador.llLaps = lapView.findViewById(R.id.llPlayerLaps); // Enlazamos el contenedor

            // Evento individual: Iniciar/Pausar su tiempo
            btnPlayerPlay.setOnClickListener(v -> toggleJugadorIndividual(jugador));

            // Evento individual: Parcial (Ahora visual como en la foto)
            final int indexColor = i; // Guardamos el índice para usar sus colores
            btnPlayerFlag.setOnClickListener(v -> {
                if (jugador.isRunning) {
                    long current = jugador.elapsedTime + (System.currentTimeMillis() - jugador.startTime);
                    long split = current - jugador.lastLapTime;
                    if (jugador.lastLapTime == 0L) {
                        split = current;
                    }
                    jugador.lastLapTime = current;

                    // Crear el diseño del parcial dinámicamente
                    TextView tvLap = new TextView(this);
                    tvLap.setText(jugador.lapCount + ". " + formatTime(current) + "   +" + formatTime(split));
                    tvLap.setTextSize(11f);
                    tvLap.setTypeface(android.graphics.Typeface.MONOSPACE, android.graphics.Typeface.BOLD);

                    // Colorear con los tonos propios del jugador
                    tvLap.setTextColor(Color.parseColor(colorTexto[indexColor]));
                    tvLap.setBackgroundResource(R.drawable.bg_lap_number);
                    tvLap.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(colorFondo[indexColor])));
                    tvLap.setPadding(24, 8, 24, 8);

                    // Márgenes para separarlos un poco
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    params.setMargins(0, 0, 0, 8);
                    tvLap.setLayoutParams(params);

                    // Añadimos a la lista (0 = arriba) y sumamos vuelta
                    jugador.llLaps.addView(tvLap, 0);
                    jugador.lapCount++;
                }
            });

            listaJugadores.add(jugador);
            llJugadoresMulti.addView(lapView);
        }
    }

    private void toggleJugadorIndividual(JugadorMulti j) {
        if (j.isRunning) {
            // Pausar
            j.elapsedTime += System.currentTimeMillis() - j.startTime;
            j.isRunning = false;
            j.btnPlay.setImageResource(R.drawable.ic_play);
        } else {
            // Iniciar
            j.startTime = System.currentTimeMillis();
            j.isRunning = true;
            j.btnPlay.setImageResource(R.drawable.ic_pause);
            handler.post(multiUpdateRunnable);
        }
        actualizarEstadoBotonGlobal();
    }

    private void toggleGlobalMulti() {
        if (isMultiRunningGlobal) {
            // Pausar a todos
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
            // Iniciar a todos
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
            j.isRunning = false;
            j.elapsedTime = 0L;
            j.startTime = 0L;
            j.lastLapTime = 0L;
            j.lapCount = 1;
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
            if (j.isRunning) algunCorriendo = true;
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
    // LOGICA INDIVIDUAL Y UTILIDADES
    // ==========================================

    private void setMode(boolean individual) {
        isIndividual = individual;
        if (individual) {
            btnIndividual.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFFFFF")));
            btnIndividual.setTextColor(Color.parseColor("#1976D2"));
            btnMulti.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
            btnMulti.setTextColor(Color.parseColor("#A0AEC0"));

            layoutIndividual.setVisibility(View.VISIBLE);
            layoutMulti.setVisibility(View.GONE);
        } else {
            btnMulti.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFFFFF")));
            btnMulti.setTextColor(Color.parseColor("#1976D2"));
            btnIndividual.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
            btnIndividual.setTextColor(Color.parseColor("#A0AEC0"));

            layoutIndividual.setVisibility(View.GONE);
            layoutMulti.setVisibility(View.VISIBLE);
        }
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
        handler.removeCallbacks(multiUpdateRunnable);
    }

    private class JugadorMulti {
        TextView tvTime;
        ImageButton btnPlay;
        LinearLayout llLaps;
        long startTime = 0L;
        long elapsedTime = 0L;
        boolean isRunning = false;
        long lastLapTime = 0L;
        int lapCount = 1;
    }
}