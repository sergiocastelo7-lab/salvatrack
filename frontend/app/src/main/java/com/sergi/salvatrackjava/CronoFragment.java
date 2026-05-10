package com.sergi.salvatrackjava;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.button.MaterialButton;

public class CronoFragment extends Fragment {

    private MaterialButton btnIndividual;
    private MaterialButton btnMulti;
    private boolean isIndividual = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_crono, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnIndividual = view.findViewById(R.id.btnIndividual);
        btnMulti = view.findViewById(R.id.btnMulti);

        if (savedInstanceState == null) {
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.cronoContainer, new IndividualFragment())
                    .commit();
        }

        btnIndividual.setOnClickListener(v -> switchTab(true));
        btnMulti.setOnClickListener(v -> switchTab(false));
    }

    private void switchTab(boolean individual) {
        if (isIndividual == individual) return;
        isIndividual = individual;

        Fragment target = individual ? new IndividualFragment() : new MultiFragment();
        getChildFragmentManager().beginTransaction()
                .replace(R.id.cronoContainer, target)
                .commit();

        updateTabUI(individual);
    }

    private void updateTabUI(boolean individual) {
        if (individual) {
            btnIndividual.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFFFFF")));
            btnIndividual.setTextColor(Color.parseColor("#1976D2"));
            btnMulti.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
            btnMulti.setTextColor(Color.parseColor("#A0AEC0"));
        } else {
            btnMulti.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFFFFF")));
            btnMulti.setTextColor(Color.parseColor("#1976D2"));
            btnIndividual.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
            btnIndividual.setTextColor(Color.parseColor("#A0AEC0"));
        }
    }
}
