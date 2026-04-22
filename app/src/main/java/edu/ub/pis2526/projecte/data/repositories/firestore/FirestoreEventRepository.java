package edu.ub.pis2526.projecte.data.repositories.firestore;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.ub.pis2526.projecte.Event;
import edu.ub.pis2526.projecte.User;
import edu.ub.pis2526.projecte.domain.repositories.EventRepository;

public class FirestoreEventRepository implements EventRepository {

    private final FirebaseFirestore db;

    public FirestoreEventRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    @Override
    public void save(Event evento, OnSuccessListener onSuccess, OnFailureListener onFailure) {
        User creador = evento.getCreador();

        Map<String, Object> creadorMap = new HashMap<>();
        creadorMap.put("nom",    creador.getNom());
        creadorMap.put("correo", creador.getCorreo());

        Date fechaDate = Date.from(evento.getFechaHora().toInstant(ZoneOffset.UTC));
        Timestamp fechaTimestamp = new Timestamp(fechaDate);

        Map<String, Object> eventoMap = new HashMap<>();
        eventoMap.put("id",            evento.getId());
        eventoMap.put("titulo",        evento.getTitulo());
        eventoMap.put("descripcion",   evento.getDescripcion());
        eventoMap.put("fechaHora",     fechaTimestamp);
        eventoMap.put("foto",          evento.getFoto());
        eventoMap.put("categorias",    new ArrayList<>());
        eventoMap.put("participantes", new ArrayList<>());
        eventoMap.put("creador",       creadorMap);
        if (evento.getLinkGoogleMapsString() != null) {
            eventoMap.put("mapsUrl", evento.getLinkGoogleMapsString());
        }

        // Guardar coordenadas si existen
        if (evento.getCoordenadas() != null) {
            eventoMap.put("lat", evento.getCoordenadas()[0]);
            eventoMap.put("lng", evento.getCoordenadas()[1]);
        }

        db.collection("events")
                .document(evento.getId())
                .set(eventoMap)
                .addOnSuccessListener(unused -> onSuccess.onSuccess())
                .addOnFailureListener(onFailure::onFailure);
    }
    @Override
    public void delete(String eventId, OnDeleteListener listener) {
        db.collection("events")
                .document(eventId)
                .delete()
                .addOnSuccessListener(unused -> listener.onSuccess())
                .addOnFailureListener(listener::onFailure);
    }

    @Override
    public void getAll(OnEventsLoadedListener onLoaded, OnFailureListener onFailure) {
        db.collection("events")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Event> eventos = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {

                        String id          = doc.getString("id");
                        String titulo      = doc.getString("titulo");
                        String descripcion = doc.getString("descripcion");
                        String foto        = doc.getString("foto");

                        Timestamp ts = doc.getTimestamp("fechaHora");
                        LocalDateTime fechaHora = null;
                        if (ts != null) {
                            fechaHora = ts.toDate()
                                    .toInstant()
                                    .atZone(ZoneOffset.UTC)
                                    .toLocalDateTime();
                        }

                        Map<String, Object> creadorMap = (Map<String, Object>) doc.get("creador");
                        User creador = new User(
                                creadorMap != null ? (String) creadorMap.get("nom") : ""
                        );

                        Event evento = Event.fromFirestore(id, titulo, descripcion, foto, fechaHora, creador);

                        Double lat = doc.getDouble("lat");
                        Double lng = doc.getDouble("lng");
                        if (lat != null && lng != null) {
                            evento.setCoordenadas(new double[]{lat, lng});
                        }
                        String mapsUrl = doc.getString("mapsUrl");
                        if (mapsUrl != null) {
                            evento.setLinkGoogleMapsString(mapsUrl);
                        }

                        eventos.add(evento);
                    }
                    onLoaded.onEventsLoaded(eventos);
                })
                .addOnFailureListener(onFailure::onFailure);
    }

    public interface OnUserEventsListener {
        void onSuccess(List<Event> events);
        void onFailure(Exception e);
    }

    public void getEventsByCreador(String nomCreador, OnUserEventsListener listener) {
        db.collection("events")
                .whereEqualTo("creador.nom", nomCreador)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Event> userEvents = new ArrayList<>();

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String id          = doc.getString("id");
                        String titulo      = doc.getString("titulo");
                        String descripcion = doc.getString("descripcion");
                        String foto        = doc.getString("foto");

                        Timestamp timestamp = doc.getTimestamp("fechaHora");
                        LocalDateTime fechaHora = null;
                        if (timestamp != null) {
                            fechaHora = LocalDateTime.ofInstant(
                                    timestamp.toDate().toInstant(),
                                    java.time.ZoneId.systemDefault()
                            );
                        }

                        Map<String, Object> creadorMap = (Map<String, Object>) doc.get("creador");
                        User creador = new User("");
                        if (creadorMap != null) {
                            creador = new User(
                                    (String) creadorMap.get("nom"),
                                    (String) creadorMap.get("correo")
                            );
                        }

                        // Crear evento (necesita un constructor que acepte estos parámetros)
                        Event event = new Event(titulo, descripcion, fechaHora, "", creador, null);
                        event.setId(id);
                        event.setFoto(foto);

                        // Recuperar coordenadas
                        Double lat = doc.getDouble("lat");
                        Double lng = doc.getDouble("lng");
                        if (lat != null && lng != null) {
                            event.setCoordenadas(new double[]{lat, lng});
                        }
                        String mapsUrl = doc.getString("mapsUrl");
                        if (mapsUrl != null) {
                            event.setLinkGoogleMapsString(mapsUrl);
                        }

                        userEvents.add(event);
                    }
                    listener.onSuccess(userEvents);
                })
                .addOnFailureListener(e -> listener.onFailure(e));
    }

    public void unirse(String eventoId, String nomUsuari, OnSuccessListener onSuccess, OnFailureListener onFailure) {
        db.collection("events")
                .document(eventoId)
                .update("participantes", com.google.firebase.firestore.FieldValue.arrayUnion(nomUsuari))
                .addOnSuccessListener(unused -> onSuccess.onSuccess())
                .addOnFailureListener(onFailure::onFailure);
    }

    public void getParticipantes(String eventoId, OnParticipantesLoadedListener onLoaded, OnFailureListener onFailure) {
        db.collection("events")
                .document(eventoId)
                .get()
                .addOnSuccessListener(doc -> {
                    List<String> participantes = (List<String>) doc.get("participantes");
                    if (participantes == null) participantes = new ArrayList<>();
                    onLoaded.onParticipantesLoaded(participantes);
                })
                .addOnFailureListener(onFailure::onFailure);
    }

    public void eliminarEventosCaducados(OnSuccessListener onSuccess, OnFailureListener onFailure) {
        Timestamp ahora = new Timestamp(new Date());
        db.collection("events")
                .whereLessThan("fechaHora", ahora)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        doc.getReference().delete();
                    }
                    onSuccess.onSuccess();
                })
                .addOnFailureListener(onFailure::onFailure);
    }

    public interface OnParticipantesLoadedListener {
        void onParticipantesLoaded(List<String> participantes);
    }
}