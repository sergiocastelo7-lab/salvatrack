package com.sergi.salvatrackjava;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import com.google.firebase.firestore.FirebaseFirestore;

public class EventoDirectoFragment extends Fragment {

    private static final String ARG_EVENTO = "evento";

    // Vistas del banner
    private TextView tvDirectoNombre;
    private TextView tvDirectoFederacion;
    private TextView tvDirectoSede;

    // Accesos
    private ProgressBar pbDirecto;
    private LinearLayout llAccesos;
    private CardView cardResultados;
    private CardView cardSalidas;
    private CardView cardStreaming;
    private TextView tvSalidasSubtitle;

    // Firebase
    private FirebaseFirestore db;

    // Evento actual
    private Evento evento;

    // URLs resueltas
    private String urlResultados  = null;
    private String urlSalidas     = null;
    private String urlStreaming   = null;

    public static EventoDirectoFragment newInstance(Evento evento) {
        EventoDirectoFragment f = new EventoDirectoFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_EVENTO, evento);
        f.setArguments(args);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_evento_directo, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        evento = (Evento) requireArguments().getSerializable(ARG_EVENTO);

        // Cabecera
        view.findViewById(R.id.btnVolver).setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack());

                // Vistas
        tvDirectoNombre      = view.findViewById(R.id.tvDirectoNombre);
        tvDirectoFederacion  = view.findViewById(R.id.tvDirectoFederacion);
        tvDirectoSede        = view.findViewById(R.id.tvDirectoSede);
        pbDirecto            = view.findViewById(R.id.pbDirecto);
        llAccesos            = view.findViewById(R.id.llAccesos);
        cardResultados       = view.findViewById(R.id.cardResultadosDirecto);
        cardSalidas          = view.findViewById(R.id.cardSalidas);
        cardStreaming         = view.findViewById(R.id.cardStreaming);
        tvSalidasSubtitle    = view.findViewById(R.id.tvSalidasSubtitle);

        // Rellenar banner con el evento
        mostrarBanner();

        // Cargar links desde Firebase
        cargarConfigFirebase();
    }

    // ==========================================
    // BANNER
    // ==========================================

    private void mostrarBanner() {
        if (evento == null) return;
        tvDirectoNombre.setText(evento.getEvento() != null ? evento.getEvento() : "—");
        tvDirectoFederacion.setText(evento.getOrigen() != null ? evento.getOrigen() : "—");
        tvDirectoSede.setText(evento.getSede() != null ? evento.getSede() : "—");
    }

    // ==========================================
    // CARGA DE LINKS DESDE FIREBASE
    // ==========================================

    private void cargarConfigFirebase() {
        pbDirecto.setVisibility(View.VISIBLE);
        llAccesos.setVisibility(View.GONE);

        db.collection("config").document("live_event")
                .get()
                .addOnSuccessListener(doc -> {
                    if (!isAdded()) return;

                    String currentResultados = doc.getString("current_resultados");
                    String currentSalidas    = doc.getString("current_salidas");
                    String currentStreaming  = doc.getString("current_streaming");
                    String defaultResGal     = doc.getString("default_resultados_fessga");
                    String defaultResEsp     = doc.getString("default_resultados_rfess");
                    String defaultStreamGal  = doc.getString("default_streaming_fessga");
                    String defaultStreamEsp  = doc.getString("default_streaming_rfess");

                    // Resolver URL de Resultados
                    if (noVacio(currentResultados)) {
                        urlResultados = currentResultados;
                    } else {
                        boolean esFessga = evento != null && "FESSGA".equalsIgnoreCase(evento.getOrigen());
                        urlResultados = esFessga ? defaultResGal : defaultResEsp;
                    }

                    // Resolver URL de Salidas
                    urlSalidas = noVacio(currentSalidas) ? currentSalidas : null;

                    // Resolver URL de Streaming
                    if (noVacio(currentStreaming)) {
                        urlStreaming = currentStreaming;
                    } else {
                        boolean esFessga = evento != null && "FESSGA".equalsIgnoreCase(evento.getOrigen());
                        urlStreaming = esFessga ? defaultStreamGal : defaultStreamEsp;
                    }

                    configurarAccesos();

                    pbDirecto.setVisibility(View.GONE);
                    llAccesos.setVisibility(View.VISIBLE);
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    pbDirecto.setVisibility(View.GONE);
                    Toast.makeText(requireContext(),
                            "Error cargando datos: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // ==========================================
    // CONFIGURAR BOTONES DE ACCESO
    // ==========================================

    private void configurarAccesos() {
        // Resultados
        if (noVacio(urlResultados)) {
            cardResultados.setClickable(true);
            cardResultados.setOnClickListener(v -> abrirUrl(urlResultados));
        }

        // Lista de Salida
        if (noVacio(urlSalidas)) {
            // Disponible
            cardSalidas.setClickable(true);
            cardSalidas.setFocusable(true);
            cardSalidas.setCardElevation(4f);
            cardSalidas.setCardBackgroundColor(0xFFFFFFFF);
            tvSalidasSubtitle.setText("Ver lista de salida");
            cardSalidas.setOnClickListener(v -> abrirUrl(urlSalidas));
        }
        // Si urlSalidas es null → queda deshabilitado por defecto (como está en el XML)

        // Streaming
        if (noVacio(urlStreaming)) {
            cardStreaming.setClickable(true);
            cardStreaming.setOnClickListener(v -> abrirUrl(urlStreaming));
        }
    }

    // ==========================================
    // UTILIDADES
    // ==========================================

    private void abrirUrl(String url) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (Exception e) {
            Toast.makeText(requireContext(), "No se puede abrir el enlace", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean noVacio(String s) {
        return s != null && !s.trim().isEmpty();
    }
}