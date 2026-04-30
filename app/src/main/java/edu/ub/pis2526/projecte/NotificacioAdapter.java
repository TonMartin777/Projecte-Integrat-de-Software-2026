package edu.ub.pis2526.projecte;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class NotificacioAdapter extends RecyclerView.Adapter<NotificacioAdapter.NotiViewHolder> {

    private List<Notificacio> notificacions;

    public NotificacioAdapter(List<Notificacio> notificacions) {
        this.notificacions = notificacions;
    }

    @NonNull
    @Override
    public NotiViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notificacio, parent, false);
        return new NotiViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotiViewHolder holder, int position) {
        // Omplim les dades de cada fila
        Notificacio n = notificacions.get(position);
        holder.titol.setText(n.getTitol());
        holder.missatge.setText(n.getMissatge());
    }

    @Override
    public int getItemCount() {
        return notificacions.size();
    }

    // Aquesta classe interna busca els TextViews dins de cada fila
    public static class NotiViewHolder extends RecyclerView.ViewHolder {
        TextView titol, missatge;

        public NotiViewHolder(@NonNull View itemView) {
            super(itemView);
            titol = itemView.findViewById(R.id.notiTitol);
            missatge = itemView.findViewById(R.id.notiMissatge);
        }
    }
}