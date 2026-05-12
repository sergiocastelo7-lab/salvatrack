package com.sergi.salvatrackjava;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Calendar;

public class FederFragment extends Fragment {

    private TextView tvMesAnyo;
    private LinearLayout llCalendarGrid;
    private final Calendar calActual = Calendar.getInstance();

    private static final String[] MESES = {
            "Enero","Febrero","Marzo","Abril","Mayo","Junio",
            "Julio","Agosto","Septiembre","Octubre","Noviembre","Diciembre"
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_feder, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvMesAnyo = view.findViewById(R.id.tvMesAnyo);
        llCalendarGrid = view.findViewById(R.id.llCalendarGrid);

        view.findViewById(R.id.btnMesAnterior).setOnClickListener(v -> {
            calActual.add(Calendar.MONTH, -1);
            refreshCalendar();
        });
        view.findViewById(R.id.btnMesSiguiente).setOnClickListener(v -> {
            calActual.add(Calendar.MONTH, 1);
            refreshCalendar();
        });

        refreshCalendar();
    }

    private void refreshCalendar() {
        int year = calActual.get(Calendar.YEAR);
        int month = calActual.get(Calendar.MONTH);
        tvMesAnyo.setText(MESES[month] + " " + year);
        buildCalendar(year, month);
    }

    private void buildCalendar(int year, int month) {
        llCalendarGrid.removeAllViews();

        Calendar today = Calendar.getInstance();
        int todayDay = (today.get(Calendar.YEAR) == year && today.get(Calendar.MONTH) == month)
                ? today.get(Calendar.DAY_OF_MONTH) : -1;

        Calendar cal = Calendar.getInstance();
        cal.set(year, month, 1);
        int firstDow = cal.get(Calendar.DAY_OF_WEEK);
        int offset = (firstDow == Calendar.SUNDAY) ? 6 : firstDow - 2;
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        int day = 1;
        while (day <= daysInMonth) {
            LinearLayout row = new LinearLayout(requireContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            rowParams.setMargins(0, 2, 0, 2);
            row.setLayoutParams(rowParams);

            for (int col = 0; col < 7; col++) {
                boolean skip = (day == 1) && (col < offset);
                if (skip || day > daysInMonth) {
                    row.addView(makeEmptyCell());
                } else {
                    row.addView(makeDayCell(day, day == todayDay));
                    day++;
                }
            }
            llCalendarGrid.addView(row);
        }
    }

    private View makeEmptyCell() {
        View cell = new View(requireContext());
        cell.setLayoutParams(new LinearLayout.LayoutParams(0, dpToPx(44), 1f));
        return cell;
    }

    private View makeDayCell(int day, boolean isToday) {
        LinearLayout cell = new LinearLayout(requireContext());
        cell.setOrientation(LinearLayout.VERTICAL);
        cell.setGravity(Gravity.CENTER);
        cell.setLayoutParams(new LinearLayout.LayoutParams(0, dpToPx(44), 1f));

        TextView tvDay = new TextView(requireContext());
        tvDay.setText(String.valueOf(day));
        tvDay.setTextSize(14f);
        tvDay.setGravity(Gravity.CENTER);

        if (isToday) {
            tvDay.setTextColor(Color.WHITE);
            tvDay.setTypeface(null, Typeface.BOLD);
            tvDay.setBackgroundResource(R.drawable.bg_lap_number);
            tvDay.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#1976D2")));
            tvDay.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(34), dpToPx(34)));
        } else {
            tvDay.setTextColor(Color.parseColor("#1A1A2E"));
        }

        cell.addView(tvDay);
        return cell;
    }

    private int dpToPx(int dp) {
        return Math.round(dp * requireContext().getResources().getDisplayMetrics().density);
    }
}