package com.sergi.salvatrackjava;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class FederBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_TITULO    = "titulo";
    private static final String ARG_KEY_GAL   = "key_gal";   // campo en links_estaticos para FESSGA
    private static final String ARG_KEY_ESP   = "key_esp";   // campo en links_estaticos para RFESS

    /**
     * Uso: FederBottomSheet.newInstance("Mínimas", "minimas_gal", "minimas_esp")
     * Las keys deben coincidir exactamente con los campos del doc config/links_estaticos
     */
    public static FederBottomSheet newInstance(String titulo, String keyGal, String keyEsp) {
        FederBottomSheet sheet = new FederBottomSheet();
        Bundle args = new Bundle();
        args.putString(ARG_TITULO,  titulo);
        args.putString(ARG_KEY_GAL, keyGal);
        args.putString(ARG_KEY_ESP, keyEsp);
        sheet.setArguments(args);
        return sheet;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bottom_sheet_feder, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args    = requireArguments();
        String titulo  = args.getString(ARG_TITULO, "");
        String keyGal  = args.getString(ARG_KEY_GAL, "");
        String keyEsp  = args.getString(ARG_KEY_ESP, "");

        ((TextView) view.findViewById(R.id.tvBottomSheetTitulo)).setText(titulo);

        // Deshabilitar botones mientras carga
        view.findViewById(R.id.cardFessga).setEnabled(false);
        view.findViewById(R.id.cardRfess).setEnabled(false);

        // Cargar links desde Firebase
        FirebaseFirestore.getInstance()
                .collection("config")
                .document("links_estaticos")
                .get()
                .addOnSuccessListener(doc -> {
                    if (!isAdded()) return;

                    String urlGal = doc.getString(keyGal);
                    String urlEsp = doc.getString(keyEsp);

                    configurarBoton(view.findViewById(R.id.cardFessga), urlGal);
                    configurarBoton(view.findViewById(R.id.cardRfess),  urlEsp);
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    Toast.makeText(requireContext(),
                            "Error cargando enlaces", Toast.LENGTH_SHORT).show();
                    dismiss();
                });
    }

    private void configurarBoton(View card, String url) {
        if (url != null && !url.isEmpty()) {
            card.setEnabled(true);
            card.setAlpha(1f);
            card.setOnClickListener(v -> {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                dismiss();
            });
        } else {
            card.setEnabled(false);
            card.setAlpha(0.4f);
        }
    }
}