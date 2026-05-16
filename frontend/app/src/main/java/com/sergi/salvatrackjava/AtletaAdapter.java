package com.sergi.salvatrackjava;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class AtletaAdapter extends RecyclerView.Adapter<AtletaAdapter.ViewHolder> {

    public interface OnAtletaClick {
        void onClick(Athlete athlete);
    }

    private List<Athlete> lista = new ArrayList<>();
    private final OnAtletaClick listener;

    public AtletaAdapter(OnAtletaClick listener) {
        this.listener = listener;
    }

    public void setData(List<Athlete> nuevaLista) {
        lista = nuevaLista != null ? nuevaLista : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_atleta_resultado, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Athlete a = lista.get(position);
        holder.tvIniciales.setText(a.getIniciales());
        holder.tvNombre.setText(a.getNombre() != null ? a.getNombre() : "—");

        String info = "";
        if (a.getClub() != null && !a.getClub().isEmpty()) info += a.getClub();
        if (a.getCategoria() != null && !a.getCategoria().isEmpty()) {
            info += (info.isEmpty() ? "" : " · ") + a.getCategoria();
        }
        holder.tvClubCategoria.setText(info);

        holder.itemView.setOnClickListener(v -> listener.onClick(a));

        holder.divider.setVisibility(position < lista.size() - 1 ? View.VISIBLE : View.GONE);
    }

    @Override
    public int getItemCount() { return lista.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvIniciales;
        TextView tvNombre;
        TextView tvClubCategoria;
        View divider;

        ViewHolder(View v) {
            super(v);
            tvIniciales = v.findViewById(R.id.tvIniciales);
            tvNombre = v.findViewById(R.id.tvNombreAtleta);
            tvClubCategoria = v.findViewById(R.id.tvClubCategoria);
            divider = v.findViewById(R.id.viewDivider);
        }
    }
}