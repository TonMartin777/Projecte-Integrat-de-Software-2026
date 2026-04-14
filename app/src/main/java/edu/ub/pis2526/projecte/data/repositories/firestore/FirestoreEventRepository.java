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

        db.collection("events")
                .document(evento.getId())
                .set(eventoMap)
                .addOnSuccessListener(unused -> onSuccess.onSuccess())
                .addOnFailureListener(onFailure::onFailure);
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

                        // Convertir Timestamp → LocalDateTime
                        Timestamp ts = doc.getTimestamp("fechaHora");
                        LocalDateTime fechaHora = ts.toDate()
                                .toInstant()
                                .atZone(ZoneOffset.UTC)
                                .toLocalDateTime();

                        // Reconstruir el creador
                        Map<String, Object> creadorMap = (Map<String, Object>) doc.get("creador");
                        User creador = new User(
                                creadorMap != null ? (String) creadorMap.get("nom") : ""
                        );

                        eventos.add(Event.fromFirestore(id, titulo, descripcion, foto, fechaHora, creador));

                    }
                    onLoaded.onEventsLoaded(eventos);
                })
                .addOnFailureListener(onFailure::onFailure);
    }

    // Interfície per avisar quan les dades estiguin llestes
    public interface OnEventsLoadedListener {
        void onSuccess(List<Event> events);
        void onFailure(Exception e);
    }

    // Mètode per buscar els events d'un usuari
    public void getEventsByCreador(String nomCreador, OnEventsLoadedListener listener) {
        db.collection("events")
                .whereEqualTo("creador.nom", nomCreador) // Filtrem per nom del creador
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Event> userEvents = new ArrayList<>();

                    for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                        // Reconstruïm l'event des de la base de dades
                        String id = doc.getString("id");
                        String titulo = doc.getString("titulo");
                        String descripcion = doc.getString("descripcion");
                        String foto = doc.getString("foto");

                        // Convertir el Timestamp de Firebase a LocalDateTime
                        com.google.firebase.Timestamp timestamp = doc.getTimestamp("fechaHora");
                        java.time.LocalDateTime fechaHora = null;
                        if (timestamp != null) {
                            fechaHora = java.time.LocalDateTime.ofInstant(
                                    timestamp.toDate().toInstant(),
                                    java.time.ZoneId.systemDefault()
                            );
                        }

                        // Reconstruir el creador
                        Map<String, Object> creadorMap = (Map<String, Object>) doc.get("creador");
                        User creador = new User("");
                        if (creadorMap != null) {
                            creador = new User((String) creadorMap.get("nom"), (String) creadorMap.get("correo"));
                        }

                        // Creem l'objecte (passem un string buit a l'adreça i null al context perquè ja el tenim creat)
                        Event event = new Event(titulo, descripcion, fechaHora, "", creador, null);
                        event.setId(id); // Assignem l'ID real de Firebase
                        event.setFoto(foto);

                        userEvents.add(event);
                    }
                    listener.onSuccess(userEvents);
                })
                .addOnFailureListener(listener::onFailure);
    }
}