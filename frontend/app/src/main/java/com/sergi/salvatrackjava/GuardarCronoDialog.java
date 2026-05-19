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

import java.util.List;

public class GuardarCronoDialog extends DialogFragment {

    private static final String ARG_TIEMPO_MS = "tiempo_ms";
    private static final String ARG_PARCIALES = "parciales"; // JSON array string
    private static final String ARG_MODO      = "modo";
    private static final String ARG_JUGADOR   = "jugador";

    private String piscinaSeleccionada = "25m";
    private TextView chip25m, chip50m;

    // ---- Factory ----

    public static GuardarCronoDialog newInstance(long tiempoMs, List<long[]> parciales,
                                                 String modo, String jugador) {
        GuardarCronoDialog d = new GuardarCronoDialog();
        Bundle args = new Bundle();
        args.putLong(ARG_TIEMPO_MS, tiempoMs);
        args.putString(ARG_MODO,    modo);
        args.putString(ARG_JUGADOR, jugador != null ? jugador : "");
        // Serializar parciales: cada elemento es [numero, tiempo_ms, split_ms]
        JSONArray arr = new JSONArray();
        if (parciales != null) {
            for (long[] p : parciales) {
                JSONArray item = new JSONArray();
                for (long v : p) item.put(v);
                arr.put(item);
            }
        }
        args.putString(ARG_PARCIALES, arr.toString());
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

        long tiempoMs = requireArguments().getLong(ARG_TIEMPO_MS);
        String modo   = requireArguments().getString(ARG_MODO, "individual");
        String jugador= requireArguments().getString(ARG_JUGADOR, "");

        // Mostrar tiempo
        TextView tvTiempo = view.findViewById(R.id.tvTiempoAGuardar);
        tvTiempo.setText(formatear(tiempoMs));

        EditText etNombre = view.findViewById(R.id.etNombreTiempo);
        EditText etPrueba = view.findViewById(R.id.etPrueba);

        // Chips de piscina
        chip25m  = view.findViewById(R.id.chip25m);
        chip50m  = view.findViewById(R.id.chip50m);
        view.findViewById(R.id.chipPlaya).setVisibility(View.GONE);
        seleccionarChip(chip25m);
        chip25m.setOnClickListener(v  -> seleccionarChip(chip25m));
        chip50m.setOnClickListener(v  -> seleccionarChip(chip50m));

        view.findViewById(R.id.btnCancelarGuardar).setOnClickListener(v -> dismiss());

        view.findViewById(R.id.btnConfirmarGuardar).setOnClickListener(v -> {
            String token = PrefsManager.getToken(requireContext());
            if (token == null) { dismiss(); return; }

            try {
                // Construir parciales desde JSON
                JSONArray parcialesRaw = new JSONArray(
                        requireArguments().getString(ARG_PARCIALES, "[]"));
                JSONArray parcialesObj = new JSONArray();
                for (int i = 0; i < parcialesRaw.length(); i++) {
                    JSONArray p = parcialesRaw.getJSONArray(i);
                    JSONObject obj = new JSONObject();
                    obj.put("numero",    p.getLong(0));
                    obj.put("tiempo_ms", p.getLong(1));
                    obj.put("split_ms",  p.getLong(2));
                    parcialesObj.put(obj);
                }

                JSONObject datos = new JSONObject();
                datos.put("nombre",    etNombre.getText().toString().trim());
                datos.put("prueba",    etPrueba.getText().toString().trim());
                datos.put("piscina",   piscinaSeleccionada);
                datos.put("modo",      modo);
                datos.put("jugador",   jugador);
                datos.put("tiempo_ms", tiempoMs);
                datos.put("parciales", parcialesObj);

                view.findViewById(R.id.btnConfirmarGuardar).setEnabled(false);

                ApiClient.guardarTiempo(token, datos, new ApiClient.Callback() {
                    @Override public void onSuccess(JSONObject r) {
                        if (!isAdded()) return;
                        Toast.makeText(requireContext(), "✓ Tiempo guardado", Toast.LENGTH_SHORT).show();
                        dismiss();
                    }
                    @Override public void onError(String msg) {
                        if (!isAdded()) return;
                        view.findViewById(R.id.btnConfirmarGuardar).setEnabled(true);
                        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                Toast.makeText(requireContext(), "Error al preparar datos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void seleccionarChip(TextView seleccionado) {
        piscinaSeleccionada = seleccionado.getText().toString();
        for (TextView chip : new TextView[]{chip25m, chip50m}) {
            boolean sel = chip == seleccionado;
            chip.setTextColor(sel ? Color.parseColor("#1976D2") : Color.parseColor("#64748B"));
            chip.setBackgroundTintList(ColorStateList.valueOf(
                    sel ? Color.parseColor("#DBEAFE") : Color.parseColor("#F1F5F9")));
        }
    }

    private String formatear(long ms) {
        long centis  = (ms % 1000) / 10;
        long seconds = (ms / 1000) % 60;
        long minutes = ms / 60000;
        return String.format("%02d:%02d.%02d", minutes, seconds, centis);
    }
}