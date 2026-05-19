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

public class EditarTiempoDialog extends DialogFragment {

    private static final String ARG_ID      = "id";
    private static final String ARG_NOMBRE  = "nombre";
    private static final String ARG_PRUEBA  = "prueba";
    private static final String ARG_PISCINA = "piscina";

    private Runnable onGuardado;
    private String piscinaSeleccionada;
    private TextView chip25m, chip50m;

    public static EditarTiempoDialog newInstance(int id, String nombre,
                                                 String prueba, String piscina) {
        EditarTiempoDialog d = new EditarTiempoDialog();
        Bundle args = new Bundle();
        args.putInt(ARG_ID, id);
        args.putString(ARG_NOMBRE,  nombre  != null ? nombre  : "");
        args.putString(ARG_PRUEBA,  prueba  != null ? prueba  : "");
        args.putString(ARG_PISCINA, piscina != null ? piscina : "25m");
        d.setArguments(args);
        return d;
    }

    public void setOnGuardado(Runnable r) { this.onGuardado = r; }

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
            w.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
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

        Bundle args = requireArguments();
        int id           = args.getInt(ARG_ID);
        String nombre    = args.getString(ARG_NOMBRE, "");
        String prueba    = args.getString(ARG_PRUEBA, "");
        piscinaSeleccionada = args.getString(ARG_PISCINA, "25m");

        // Cambiar título
        ((TextView) view.findViewById(R.id.tvTiempoAGuardar)).setText("Editar tiempo");
        ((TextView) view.findViewById(R.id.tvTiempoAGuardar)).setTextSize(20f);

        EditText etNombre = view.findViewById(R.id.etNombreTiempo);
        EditText etPrueba = view.findViewById(R.id.etPrueba);
        etNombre.setText(nombre);
        etPrueba.setText(prueba);

        chip25m = view.findViewById(R.id.chip25m);
        chip50m = view.findViewById(R.id.chip50m);
        view.findViewById(R.id.chipPlaya).setVisibility(View.GONE);

        // Seleccionar piscina actual
        seleccionarChip(piscinaSeleccionada.equals("50m") ? chip50m : chip25m);
        chip25m.setOnClickListener(v -> seleccionarChip(chip25m));
        chip50m.setOnClickListener(v -> seleccionarChip(chip50m));

        view.findViewById(R.id.btnCancelarGuardar).setOnClickListener(v -> dismiss());

        view.findViewById(R.id.btnConfirmarGuardar).setOnClickListener(v -> {
            String token = PrefsManager.getToken(requireContext());
            if (token == null) { dismiss(); return; }

            String nuevoNombre  = etNombre.getText().toString().trim();
            String nuevaPrueba  = etPrueba.getText().toString().trim();

            view.findViewById(R.id.btnConfirmarGuardar).setEnabled(false);

            ApiClient.editarTiempo(token, id, nuevoNombre, nuevaPrueba,
                    piscinaSeleccionada, new ApiClient.Callback() {
                        @Override public void onSuccess(org.json.JSONObject r) {
                            if (!isAdded()) return;
                            Toast.makeText(requireContext(), "✓ Tiempo actualizado", Toast.LENGTH_SHORT).show();
                            if (onGuardado != null) onGuardado.run();
                            dismiss();
                        }
                        @Override public void onError(String msg) {
                            if (!isAdded()) return;
                            view.findViewById(R.id.btnConfirmarGuardar).setEnabled(true);
                            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                        }
                    });
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