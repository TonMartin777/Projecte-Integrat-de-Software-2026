package edu.ub.pis2526.projecte;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton; // Afegit el import correcte
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private List<Event> eventList;
    private List<Event> listaCompleta;
    private OnEventDeleteListener deleteListener;
    private String nomUsuari;

    public interface OnEventDeleteListener {
        void onDeleteClick(Event event, int position);
    }

    public EventAdapter(List<Event> eventList, String nomUsuari, OnEventDeleteListener listener) {
        this.eventList = new ArrayList<>(eventList);
        this.listaCompleta = new ArrayList<>(eventList);
        this.nomUsuari = nomUsuari;
        this.deleteListener = listener;
    }

    public EventAdapter(List<Event> eventList, String nomUsuari) {
        this.eventList = new ArrayList<>(eventList);
        this.listaCompleta = new ArrayList<>(eventList);
        this.nomUsuari = nomUsuari;
        this.deleteListener = null;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.event_item, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = eventList.get(position);

        holder.nombre.setText(event.getTitulo());
        if (event.getFechaHora() != null) {
            holder.fecha.setText(event.getFechaHora().toLocalDate().toString());
        }

        Glide.with(holder.itemView.getContext())
                .load(event.getFoto())
                .placeholder(R.drawable.ic_launcher_background) // Imatge per defecte mentre carrega
                .into(holder.imagen);

        holder.btnOpciones.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(v.getContext(), v);
            popup.getMenu().add("Eliminar");
            popup.setOnMenuItemClickListener(item -> {
                if (item.getTitle().equals("Eliminar") && deleteListener != null) {
                    deleteListener.onDeleteClick(event, position);
                }
                return true;
            });
            popup.show();
        });

        holder.itemView.setOnClickListener(v -> {
            Context context = v.getContext();
            Intent intent = new Intent(context, EventDetailActivity.class);
            intent.putExtra("titulo", event.getTitulo());
            intent.putExtra("descripcion", event.getDescripcion());
            intent.putExtra("eventoId", event.getId());
            intent.putExtra("NOM_USUARI", nomUsuari);
            intent.putExtra("genero", event.getGenero() != null ? event.getGenero().name() : "");
            intent.putExtra("aforoMaximo", event.getAforoMaxim());
            if (event.getFechaHora() != null) {
                intent.putExtra("fecha", event.getFechaHora().toLocalDate().toString());
                intent.putExtra("hora", event.getFechaHora().toLocalTime().toString());
            }
            intent.putExtra("foto", event.getFoto());
            if (event.getCreador() != null) {
                intent.putExtra("creador", event.getCreador().getNom());
            }
            if (event.getCoordenadas() != null) {
                intent.putExtra("lat", event.getCoordenadas()[0]);
                intent.putExtra("lng", event.getCoordenadas()[1]);
            }
            // Pasar la URL de Google Maps si existe
            String mapsUrl = event.getLinkGoogleMapsString();
            if (mapsUrl != null) {
                intent.putExtra("maps_url", mapsUrl);
            }
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView nombre, fecha;
        ImageView imagen;
        ImageButton btnOpciones;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            nombre = itemView.findViewById(R.id.eventNombre);
            fecha = itemView.findViewById(R.id.eventFecha);
            imagen = itemView.findViewById(R.id.eventImagen);
            btnOpciones = itemView.findViewById(R.id.btnOpciones);
        }
    }

    public void actualizarLista(List<Event> nuevaLista) {
        listaCompleta.clear();
        listaCompleta.addAll(nuevaLista);
        eventList.clear();
        eventList.addAll(nuevaLista);
        notifyDataSetChanged();
    }

    public void filtrar(String texto) {
        eventList.clear();
        if (texto.isEmpty()) {
            eventList.addAll(listaCompleta);
        } else {
            String textoBusqueda = texto.toLowerCase();
            for (Event e : listaCompleta) {
                if (e.getTitulo().toLowerCase().contains(textoBusqueda)) {
                    eventList.add(e);
                }
            }
        }
        notifyDataSetChanged();
    }
}