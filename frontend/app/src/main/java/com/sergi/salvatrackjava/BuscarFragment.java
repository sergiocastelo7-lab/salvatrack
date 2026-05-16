package com.sergi.salvatrackjava;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class BuscarFragment extends Fragment {

    private EditText etBuscar;
    private TextView btnLimpiar;
    private CardView cardResultados;
    private RecyclerView rvResultados;
    private LinearLayout layoutIdle;
    private LinearLayout layoutSinResultados;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_buscar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etBuscar = view.findViewById(R.id.etBuscar);
        btnLimpiar = view.findViewById(R.id.btnLimpiar);
        cardResultados = view.findViewById(R.id.cardResultados);
        rvResultados = view.findViewById(R.id.rvResultados);
        layoutIdle = view.findViewById(R.id.layoutIdle);
        layoutSinResultados = view.findViewById(R.id.layoutSinResultados);

        btnLimpiar.setOnClickListener(v -> etBuscar.setText(""));
    }
}