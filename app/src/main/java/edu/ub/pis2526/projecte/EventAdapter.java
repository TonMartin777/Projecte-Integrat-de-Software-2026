package edu.ub.pis2526.projecte;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

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
        holder.nombre.setText(event.getNombre());
        holder.fecha.setText(event.getFecha());
    }

    @Override
    public int getItemCount() { return eventList.size(); }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView nombre, fecha;

        public EventViewHolder(View itemView) {
            super(itemView);
            nombre = itemView.findViewById(R.id.eventNombre);
            fecha = itemView.findViewById(R.id.eventFecha);
        }
    }
}
