package com.sergi.salvatrackjava;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class GuardarCronoMultiDialog extends DialogFragment {

    private static final String ARG_JUGADORES = "jugadores_json";

    private String piscinaSeleccionada = "25m";
    private TextView chip25m, chip50m;

    public static GuardarCronoMultiDialog newInstance(List<JugadorMulti> jugadores) {
        GuardarCronoMultiDialog d = new GuardarCronoMultiDialog();
        Bundle args = new Bundle();
        // Serializar jugadores a JSON
        JSONArray arr = new JSONArray();
        for (int i = 0; i < jugadores.size(); i++) {
            JugadorMulti j = jugadores.get(i);
            long tiempoActual = j.isRunning
                    ? j.elapsedTime + (System.currentTimeMillis() - j.startTime)
                    : j.elapsedTime;
            try {
                JSONObject obj = new JSONObject();
                obj.put("num", i + 1);
                obj.put("tiempo_ms", tiempoActual);
                JSONArray parc = new JSONArray();
                for (long[] p : j.parciales) {
                    JSONArray item = new JSONArray();
                    for (long v : p) item.put(v);
                    parc.put(item);
                }
                obj.put("parciales", parc);
                arr.put(obj);
            } catch (Exception ignored) {}
        }
        args.putString(ARG_JUGADORES, arr.toString());
        d.setArguments(args);
        return d;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_guardar_crono, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        Window w = getDialog() != null ? getDialog().getWindow() : null;
        if (w != null) {
            w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.92);
            w.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);
            WindowManager.LayoutParams lp = w.getAttributes();
            lp.gravity = Gravity.CENTER;
            w.setAttributes(lp);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Cambiar título del tiempo por "Multijugador"
        TextView tvTiempo = view.findViewById(R.id.tvTiempoAGuardar);
        tvTiempo.setText("Todos los jugadores");
        tvTiempo.setTextSize(20f);

        EditText etNombre = view.findViewById(R.id.etNombreTiempo);
        EditText etPrueba = view.findViewById(R.id.etPrueba);

        chip25m = view.findViewById(R.id.chip25m);
        chip50m = view.findViewById(R.id.chip50m);
        // Ocultar Playa
        view.findViewById(R.id.chipPlaya).setVisibility(View.GONE);
        seleccionarChip(chip25m);
        chip25m.setOnClickListener(v -> seleccionarChip(chip25m));
        chip50m.setOnClickListener(v -> seleccionarChip(chip50m));

        view.findViewById(R.id.btnCancelarGuardar).setOnClickListener(v -> dismiss());

        view.findViewById(R.id.btnConfirmarGuardar).setOnClickListener(v -> {
            String token = PrefsManager.getToken(requireContext());
            if (token == null) { dismiss(); return; }

            String nombre = etNombre.getText().toString().trim();
            String prueba = etPrueba.getText().toString().trim();

            try {
                JSONArray jugadores = new JSONArray(
                        requireArguments().getString(ARG_JUGADORES, "[]"));

                view.findViewById(R.id.btnConfirmarGuardar).setEnabled(false);

                // Guardar cada jugador
                final int[] pendientes = {jugadores.length()};
                final boolean[] hayError = {false};

                for (int i = 0; i < jugadores.length(); i++) {
                    JSONObject j = jugadores.getJSONObject(i);
                    int num = j.getInt("num");
                    long tiempoMs = j.getLong("tiempo_ms");
                    if (tiempoMs == 0L) { pendientes[0]--; continue; }

                    JSONArray parcialesRaw = j.getJSONArray("parciales");
                    JSONArray parcialesObj = new JSONArray();
                    for (int k = 0; k < parcialesRaw.length(); k++) {
                        JSONArray p = parcialesRaw.getJSONArray(k);
                        JSONObject po = new JSONObject();
                        po.put("numero", p.getLong(0));
                        po.put("tiempo_ms", p.getLong(1));
                        po.put("split_ms", p.getLong(2));
                        parcialesObj.put(po);
                    }

                    JSONObject datos = new JSONObject();
                    datos.put("nombre",    nombre.isEmpty() ? "" : nombre + " - J" + num);
                    datos.put("prueba",    prueba);
                    datos.put("piscina",   piscinaSeleccionada);
                    datos.put("modo",      "multi");
                    datos.put("jugador",   "Jugador " + num);
                    datos.put("tiempo_ms", tiempoMs);
                    datos.put("parciales", parcialesObj);

                    ApiClient.guardarTiempo(token, datos, new ApiClient.Callback() {
                        @Override public void onSuccess(JSONObject r) {
                            if (!isAdded()) return;
                            pendientes[0]--;
                            if (pendientes[0] <= 0 && !hayError[0]) {
                                Toast.makeText(requireContext(),
                                        "✓ Tiempos guardados", Toast.LENGTH_SHORT).show();
                                dismiss();
                            }
                        }
                        @Override public void onError(String msg) {
                            if (!isAdded()) return;
                            if (!hayError[0]) {
                                hayError[0] = true;
                                view.findViewById(R.id.btnConfirmarGuardar).setEnabled(true);
                                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }

                if (pendientes[0] <= 0) dismiss();

            } catch (Exception e) {
                Toast.makeText(requireContext(), "Error al preparar datos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void seleccionarChip(TextView sel) {
        piscinaSeleccionada = sel.getText().toString();
        for (TextView chip : new TextView[]{chip25m, chip50m}) {
            boolean s = chip == sel;
            chip.setTextColor(s ? Color.parseColor("#1976D2") : Color.parseColor("#64748B"));
            chip.setBackgroundTintList(ColorStateList.valueOf(
                    s ? Color.parseColor("#DBEAFE") : Color.parseColor("#F1F5F9")));
        }
    }
}