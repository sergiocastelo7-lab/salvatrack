package com.sergi.salvatrackjava;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AdminLiveEventFragment extends Fragment {

    private EditText etResultados, etSalidas, etStreaming;
    private ProgressBar pbAdmin;
    private LinearLayout llForm;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_live_event, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();

        etResultados = view.findViewById(R.id.etCurrentResultados);
        etSalidas    = view.findViewById(R.id.etCurrentSalidas);
        etStreaming  = view.findViewById(R.id.etCurrentStreaming);
        pbAdmin      = view.findViewById(R.id.pbAdmin);
        llForm       = view.findViewById(R.id.llAdminForm);

        view.findViewById(R.id.btnVolverAdmin).setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack());

        view.findViewById(R.id.btnGuardarAdmin).setOnClickListener(v -> guardarCambios());

        cargarValoresActuales();
    }

    // ==========================================
    // CARGAR VALORES ACTUALES DESDE FIREBASE
    // ==========================================

    private void cargarValoresActuales() {
        pbAdmin.setVisibility(View.VISIBLE);
        llForm.setVisibility(View.GONE);

        db.collection("config").document("live_event")
                .get()
                .addOnSuccessListener(doc -> {
                    if (!isAdded()) return;
                    pbAdmin.setVisibility(View.GONE);
                    llForm.setVisibility(View.VISIBLE);

                    String res    = doc.getString("current_resultados");
                    String sal    = doc.getString("current_salidas");
                    String stream = doc.getString("current_streaming");

                    if (res    != null) etResultados.setText(res);
                    if (sal    != null) etSalidas.setText(sal);
                    if (stream != null) etStreaming.setText(stream);
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    pbAdmin.setVisibility(View.GONE);
                    llForm.setVisibility(View.VISIBLE);
                    Toast.makeText(requireContext(),
                            "Error cargando datos de Firebase", Toast.LENGTH_SHORT).show();
                });
    }

    // ==========================================
    // GUARDAR EN FIREBASE
    // ==========================================

    private void guardarCambios() {
        MaterialButton btn = requireView().findViewById(R.id.btnGuardarAdmin);
        btn.setEnabled(false);
        btn.setText("Guardando...");

        Map<String, Object> datos = new HashMap<>();
        datos.put("current_resultados", etResultados.getText().toString().trim());
        datos.put("current_salidas",    etSalidas.getText().toString().trim());
        datos.put("current_streaming",  etStreaming.getText().toString().trim());

        db.collection("config").document("live_event")
                .update(datos)
                .addOnSuccessListener(unused -> {
                    if (!isAdded()) return;
                    btn.setEnabled(true);
                    btn.setText("Guardar cambios");
                    Toast.makeText(requireContext(),
                            "✓ Cambios guardados en Firebase", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    btn.setEnabled(true);
                    btn.setText("Guardar cambios");
                    Toast.makeText(requireContext(),
                            "Error al guardar: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}