package edu.ub.pis2526.projecte;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;

import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private List<Event> eventList;

    public EventAdapter(List<Event> eventList) {
        this.eventList = eventList;
    }

    @Override
    public EventViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.event_item, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(EventViewHolder holder, int position) {
        Event event = eventList.get(position);
        holder.nombre.setText(event.getTitulo());
        holder.fecha.setText(event.getFechaHora().toLocalDate().toString());

        Glide.with(holder.itemView.getContext())
                .load(event.getFoto())
                .into(holder.imagen);

        holder.itemView.setOnClickListener(v -> {
            Context context = v.getContext();
            Intent intent = new Intent(context, EventDetailActivity.class);
            intent.putExtra("titulo", event.getTitulo());
            intent.putExtra("descripcion", event.getDescripcion());
            intent.putExtra("fecha", event.getFechaHora().toLocalDate().toString());
            intent.putExtra("hora", event.getFechaHora().toLocalTime().toString());
            intent.putExtra("foto", event.getFoto());
            if (event.getCoordenadas() != null) {
                intent.putExtra("lat", event.getCoordenadas()[0]);
                intent.putExtra("lng", event.getCoordenadas()[1]);
            }
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() { return eventList.size(); }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView nombre, fecha;
        ImageView imagen;

        public EventViewHolder(View itemView) {
            super(itemView);
            nombre = itemView.findViewById(R.id.eventNombre);
            fecha = itemView.findViewById(R.id.eventFecha);
            imagen = itemView.findViewById(R.id.eventImagen);
        }
    }
}
