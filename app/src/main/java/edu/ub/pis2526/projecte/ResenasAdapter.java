package edu.ub.pis2526.projecte;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ResenasAdapter extends RecyclerView.Adapter<ResenasAdapter.ResenaViewHolder> {

    private List<Resena> resenas;

    public ResenasAdapter(List<Resena> resenas) {
        this.resenas = resenas;
    }

    @NonNull
    @Override
    public ResenaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_resena, parent, false);
        return new ResenaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ResenaViewHolder holder, int position) {
        Resena r = resenas.get(position);
        holder.tvUsuari.setText(r.getNomUsuari());
        holder.tvPuntuacion.setText("Puntuación: " + r.getPuntuacion() + "/10");
        if (r.getMissatge() != null && !r.getMissatge().isEmpty()) {
            holder.tvMissatge.setVisibility(View.VISIBLE);
            holder.tvMissatge.setText(r.getMissatge());
        } else {
            holder.tvMissatge.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() { return resenas.size(); }

    public static class ResenaViewHolder extends RecyclerView.ViewHolder {
        TextView tvUsuari, tvPuntuacion, tvMissatge;

        public ResenaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsuari     = itemView.findViewById(R.id.resenaUsuari);
            tvPuntuacion = itemView.findViewById(R.id.resenaPuntuacion);
            tvMissatge   = itemView.findViewById(R.id.resenaMissatge);
        }
    }
}