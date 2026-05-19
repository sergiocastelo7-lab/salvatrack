package com.sergi.salvatrackjava;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ConfigFragment extends Fragment {

    private LinearLayout layoutEmpty;
    private LinearLayout layoutHistorial;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_config, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        layoutEmpty = view.findViewById(R.id.layoutEmpty);
        layoutHistorial = view.findViewById(R.id.layoutHistorial);

        layoutEmpty.setVisibility(View.VISIBLE);
        layoutHistorial.setVisibility(View.GONE);
    }
}