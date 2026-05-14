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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FederFragment extends Fragment {

    private FirebaseFirestore db;
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    // Banner
    private LinearLayout llEventoContent;
    private ProgressBar pbEvento;
    private TextView tvDias;
    private TextView tvNombreEvento;
    private TextView tvFechaEvento;
    private TextView tvFederacion;
    private Evento proximoEvento; // para navegación al pulsar el banner

    // Calendario
    private TextView tvMesAnyo;
    private LinearLayout llCalendarGrid;
    private final Calendar calActual = Calendar.getInstance();
    private final List<Evento> listaEventos = new ArrayList<>();

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

        db = FirebaseFirestore.getInstance();

        // Banner
        llEventoContent = view.findViewById(R.id.llEventoContent);
        pbEvento        = view.findViewById(R.id.pbEvento);
        tvDias          = view.findViewById(R.id.tvDias);
        tvNombreEvento  = view.findViewById(R.id.tvNombreEvento);
        tvFechaEvento   = view.findViewById(R.id.tvFechaEvento);
        tvFederacion    = view.findViewById(R.id.tvFederacion);

        // Pulsar el banner → abrir Evento en Directo
        view.findViewById(R.id.btnVerEvento).setOnClickListener(v -> {});

        // Calendario
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

        // Accesos rápidos → links desde Firebase config/links_estaticos
        view.findViewById(R.id.cardMinimas).setOnClickListener(v -> {});
        view.findViewById(R.id.cardRecords).setOnClickListener(v -> {});
        view.findViewById(R.id.cardResultados).setOnClickListener(v -> {});
        view.findViewById(R.id.cardRanking).setOnClickListener(v -> {});

        cargarEventos();
    }


    // ==========================================
    // CARGA DE DATOS DESDE FIRESTORE
    // ==========================================

    private void cargarEventos() {
        pbEvento.setVisibility(View.VISIBLE);
        llEventoContent.setVisibility(View.GONE);

        db.collection("calendario")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    listaEventos.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Evento e = doc.toObject(Evento.class);
                        if (e.getEvento() != null) {
                            listaEventos.add(e);
                        }
                    }
                    actualizarBanner();
                    refreshCalendar();
                    pbEvento.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    pbEvento.setVisibility(View.GONE);
                    Toast.makeText(requireContext(),
                            "Error al cargar eventos: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    // ==========================================
    // BANNER PRÓXIMO EVENTO
    // ==========================================

    private void actualizarBanner() {
        // Resetear hora de hoy a medianoche para comparar solo fechas
        Calendar hoyCalendar = Calendar.getInstance();
        hoyCalendar.set(Calendar.HOUR_OF_DAY, 0);
        hoyCalendar.set(Calendar.MINUTE, 0);
        hoyCalendar.set(Calendar.SECOND, 0);
        hoyCalendar.set(Calendar.MILLISECOND, 0);
        Date hoy = hoyCalendar.getTime();

        Evento localProximo = null;
        long menorDiff = Long.MAX_VALUE;

        for (Evento e : listaEventos) {
            try {
                Date fechaInicio = sdf.parse(e.getFecha_inicio());
                if (fechaInicio == null) continue;
                long diff = fechaInicio.getTime() - hoy.getTime();
                if (diff >= 0 && diff < menorDiff) {
                    menorDiff = diff;
                    localProximo = e;
                }
            } catch (ParseException ex) {
                // fecha con formato inesperado, ignorar
            }
        }

        proximoEvento = localProximo; // guardar como campo para el click del banner

        if (proximoEvento != null) {
            long dias = menorDiff / (1000L * 60 * 60 * 24);
            tvDias.setText(String.valueOf(dias));
            tvNombreEvento.setText(proximoEvento.getEvento());
            tvFechaEvento.setText(proximoEvento.getFecha_inicio());
            tvFederacion.setText(proximoEvento.getOrigen() != null
                    ? proximoEvento.getOrigen() : "—");
            llEventoContent.setVisibility(View.VISIBLE);
        } else {
            tvDias.setText("—");
            tvNombreEvento.setText("Sin eventos próximos");
            tvFechaEvento.setText("");
            tvFederacion.setText("");
            llEventoContent.setVisibility(View.VISIBLE);
        }
    }

    // ==========================================
    // CALENDARIO
    // ==========================================

    private void refreshCalendar() {
        int year = calActual.get(Calendar.YEAR);
        int month = calActual.get(Calendar.MONTH);
        tvMesAnyo.setText(MESES[month] + " " + year);

        // Mapa: día del mes → lista de eventos que caen ese día
        Map<Integer, List<Evento>> eventosPorDia = new HashMap<>();
        for (Evento e : listaEventos) {
            try {
                Date inicio = sdf.parse(e.getFecha_inicio());
                Date fin    = sdf.parse(e.getFecha_fin());
                if (inicio == null || fin == null) continue;

                Calendar c = Calendar.getInstance();
                c.setTime(inicio);
                c.set(Calendar.HOUR_OF_DAY, 0);
                c.set(Calendar.MINUTE, 0);
                c.set(Calendar.SECOND, 0);
                c.set(Calendar.MILLISECOND, 0);

                while (!c.getTime().after(fin)) {
                    if (c.get(Calendar.YEAR) == year && c.get(Calendar.MONTH) == month) {
                        int dia = c.get(Calendar.DAY_OF_MONTH);
                        if (!eventosPorDia.containsKey(dia)) {
                            eventosPorDia.put(dia, new ArrayList<>());
                        }
                        eventosPorDia.get(dia).add(e);
                    }
                    c.add(Calendar.DAY_OF_MONTH, 1);
                }
            } catch (ParseException ex) {
                // ignorar
            }
        }

        buildCalendar(year, month, eventosPorDia);
    }

    private void buildCalendar(int year, int month, Map<Integer, List<Evento>> eventosPorDia) {
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
                    List<Evento> eventosDelDia = eventosPorDia.get(day);
                    boolean hasEvent = eventosDelDia != null && !eventosDelDia.isEmpty();
                    row.addView(makeDayCell(day, day == todayDay, hasEvent, eventosDelDia, year, month));
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

    private View makeDayCell(int day, boolean isToday, boolean hasEvent,
                             List<Evento> eventos, int year, int month) {
        LinearLayout cell = new LinearLayout(requireContext());
        cell.setOrientation(LinearLayout.VERTICAL);
        cell.setGravity(Gravity.CENTER);
        cell.setLayoutParams(new LinearLayout.LayoutParams(0, dpToPx(44), 1f));

        if (hasEvent) {
            cell.setClickable(true);
            cell.setFocusable(true);
            String fechaFormateada = String.format(Locale.getDefault(),
                    "%02d/%02d/%04d", day, month + 1, year);
            cell.setOnClickListener(v ->
                    EventosDiaDialog
                            .newInstance(new ArrayList<>(eventos), fechaFormateada)
                            .show(getChildFragmentManager(), "evento_dia"));
        }

        // Número del día
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
        } else if (hasEvent) {
            tvDay.setTextColor(Color.parseColor("#1976D2"));
            tvDay.setTypeface(null, Typeface.BOLD);
            tvDay.setBackgroundResource(R.drawable.bg_lap_number);
            tvDay.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E3F2FD")));
            tvDay.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(34), dpToPx(34)));
        } else {
            tvDay.setTextColor(Color.parseColor("#1A1A2E"));
        }

        cell.addView(tvDay);

        // Punto debajo si hay evento
        if (hasEvent) {
            View dot = new View(requireContext());
            LinearLayout.LayoutParams dotParams = new LinearLayout.LayoutParams(dpToPx(5), dpToPx(5));
            dotParams.topMargin = dpToPx(2);
            dotParams.gravity = Gravity.CENTER_HORIZONTAL;
            dot.setLayoutParams(dotParams);
            dot.setBackgroundResource(R.drawable.bg_lap_number);
            dot.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#1976D2")));
            cell.addView(dot);
        }

        return cell;
    }

    // ==========================================
    // UTILIDADES
    // ==========================================

    private int dpToPx(int dp) {
        return Math.round(dp * requireContext().getResources().getDisplayMetrics().density);
    }
}