package edu.ub.pis2526.projecte.data.repositories.firestore;

import android.util.Log;

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
import com.google.firebase.firestore.Query;


import edu.ub.pis2526.projecte.Event;
import edu.ub.pis2526.projecte.Generos;
import edu.ub.pis2526.projecte.User;
import edu.ub.pis2526.projecte.domain.repositories.EventRepository;

public class FirestoreEventRepository implements EventRepository {

    private final FirebaseFirestore db;

    public FirestoreEventRepository() {
        this.db = FirebaseFirestore.getInstance();
    }
    private static final int PAGE_SIZE = 20;

    public void getPage(DocumentSnapshot ultimoDoc, OnPageLoadedListener onLoaded, OnFailureListener onFailure) {
        com.google.firebase.firestore.Query query = db.collection("events")
                .whereEqualTo("activo", true)
                .orderBy("fechaHora")
                .limit(PAGE_SIZE);

        if (ultimoDoc != null) {
            query = query.startAfter(ultimoDoc);
        }

        query.get().addOnSuccessListener(querySnapshot -> {
            List<Event> eventos = new ArrayList<>();
            DocumentSnapshot nuevoUltimoDoc = null;

            if (!querySnapshot.isEmpty()) {
                nuevoUltimoDoc = querySnapshot.getDocuments()
                        .get(querySnapshot.size() - 1);
            }

            for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                Event evento = parsearEvento(doc);
                if (evento != null) eventos.add(evento);
            }

            boolean hayMas = querySnapshot.size() == PAGE_SIZE;
            onLoaded.onPageLoaded(eventos, nuevoUltimoDoc, hayMas);
        }).addOnFailureListener(onFailure::onFailure);
    }

    public interface OnPageLoadedListener {
        void onPageLoaded(List<Event> eventos, DocumentSnapshot ultimoDoc, boolean hayMas);
    }

    private Event parsearEvento(DocumentSnapshot doc) {
        try {
            String id          = doc.getString("id");
            String titulo      = doc.getString("titulo");
            String descripcion = doc.getString("descripcion");
            String foto        = doc.getString("foto");
            int aforo = doc.contains("aforoMaximo") ? doc.getLong("aforoMaximo").intValue() : 0;

            Timestamp ts = doc.getTimestamp("fechaHora");
            LocalDateTime fechaHora = null;
            if (ts != null) {
                fechaHora = ts.toDate().toInstant()
                        .atZone(ZoneOffset.UTC).toLocalDateTime();
            }

            Map<String, Object> creadorMap = (Map<String, Object>) doc.get("creador");
            User creador = new User();
            if (creadorMap != null) {
                creador.setNom((String) creadorMap.get("nom"));
                creador.setCorreo((String) creadorMap.get("correo"));
            }

            Event evento = Event.fromFirestore(id, titulo, descripcion, foto, fechaHora, creador, aforo);

            Double lat = doc.getDouble("lat");
            Double lng = doc.getDouble("lng");
            if (lat != null && lng != null) evento.setCoordenadas(new double[]{lat, lng});

            String mapsUrl = doc.getString("mapsUrl");
            if (mapsUrl != null) evento.setLinkGoogleMapsString(mapsUrl);

            String generoStr = doc.getString("genero");
            if (generoStr != null) {
                try { evento.setGenero(Generos.valueOf(generoStr)); }
                catch (IllegalArgumentException ignored) {}
            }

            return evento;
        } catch (Exception e) {
            Log.e("FIRESTORE", "Error parseando evento: " + doc.getId(), e);
            return null;
        }
    }

    @Override
    public void save(Event evento, OnSuccessListener onSuccess, OnFailureListener onFailure) {
        User creador = evento.getCreador();

        Map<String, Object> creadorMap = new HashMap<>();
        creadorMap.put("nom",    creador.getNom());
        creadorMap.put("correo", creador.getCorreo());

        Date fechaDate = Date.from(evento.getFechaHora().atZone(java.time.ZoneId.systemDefault()).toInstant());
        Timestamp fechaTimestamp = new Timestamp(fechaDate);

        Map<String, Object> eventoMap = new HashMap<>();
        eventoMap.put("id",            evento.getId());
        eventoMap.put("titulo",        evento.getTitulo());
        eventoMap.put("descripcion",   evento.getDescripcion());
        eventoMap.put("fechaHora",     fechaTimestamp);
        eventoMap.put("foto",          evento.getFoto());
        eventoMap.put("genero", evento.getGenero() != null ? evento.getGenero().name() : null);
        eventoMap.put("participantes", new ArrayList<>());
        eventoMap.put("aforoMaximo", evento.getAforoMaxim());
        eventoMap.put("creador",       creadorMap);
        eventoMap.put("activo",            evento.isActivo());
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
                .whereEqualTo("activo", true)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Event> eventos = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {

                        String id          = doc.getString("id");
                        String titulo      = doc.getString("titulo");
                        String descripcion = doc.getString("descripcion");
                        String foto        = doc.getString("foto");
                        int aforo = doc.contains("aforoMaximo") ? doc.getLong("aforoMaximo").intValue() : 0;
                        Timestamp ts = doc.getTimestamp("fechaHora");
                        LocalDateTime fechaHora = null;
                        if (ts != null) {
                            fechaHora = ts.toDate()
                                    .toInstant()
                                    .atZone(ZoneOffset.UTC)
                                    .toLocalDateTime();
                        }

                        Map<String, Object> creadorMap = (Map<String, Object>) doc.get("creador");
                        User creador = new User();
                        if (creadorMap != null) {
                            creador.setNom((String) creadorMap.get("nom"));
                            creador.setCorreo((String) creadorMap.get("correo"));
                        }

                        Event evento = Event.fromFirestore(id, titulo, descripcion, foto, fechaHora, creador, aforo);

                        Double lat = doc.getDouble("lat");
                        Double lng = doc.getDouble("lng");
                        if (lat != null && lng != null) {
                            evento.setCoordenadas(new double[]{lat, lng});
                        }
                        String mapsUrl = doc.getString("mapsUrl");
                        if (mapsUrl != null) {
                            evento.setLinkGoogleMapsString(mapsUrl);
                        }

                        String generoStr = doc.getString("genero");
                        if (generoStr != null) {
                            try {
                                evento.setGenero(Generos.valueOf(generoStr));
                            } catch (IllegalArgumentException ignored) {}
                        }

                        eventos.add(evento);
                    }
                    onLoaded.onEventsLoaded(eventos);
                })
                .addOnFailureListener(onFailure::onFailure); // Solo eventos activos
    }

    public interface OnUserEventsListener {
        void onSuccess(List<Event> events);
        void onFailure(Exception e);
    }

    public void getEventsByCreador(String nomCreador, OnUserEventsListener listener) {
        Log.d("FIRESTORE", "getEventsByCreador - buscando creador: '" + nomCreador + "'");
        db.collection("events")
                .whereEqualTo("creador.nom", nomCreador)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d("FIRESTORE", "Documentos encontrados: " + queryDocumentSnapshots.size());
                    List<Event> userEvents = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Log.d("FIRESTORE", "Evento: " + doc.getString("titulo") + " - creador.nom = " + doc.get("creador.nom"));
                        String id = doc.getString("id");
                        String titulo = doc.getString("titulo");
                        String descripcion = doc.getString("descripcion");
                        String foto = doc.getString("foto");
                        int aforo = doc.contains("aforoMaximo") ? doc.getLong("aforoMaximo").intValue() : 0;
                        Timestamp timestamp = doc.getTimestamp("fechaHora");
                        LocalDateTime fechaHora = null;
                        if (timestamp != null) {
                            fechaHora = LocalDateTime.ofInstant(
                                    timestamp.toDate().toInstant(),
                                    java.time.ZoneId.systemDefault()
                            );
                        }
                        Map<String, Object> creadorMap = (Map<String, Object>) doc.get("creador");
                        User creador = new User();
                        if (creadorMap != null) {
                            creador = new User(
                                    (String) creadorMap.get("nom"),
                                    (String) creadorMap.get("correo")
                            );
                        }
                        Event event = Event.fromFirestore(id, titulo, descripcion, foto, fechaHora, creador, aforo);
                        event.setId(id);
                        event.setFoto(foto);
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
                .addOnFailureListener(e -> {
                    Log.e("FIRESTORE", "Error en getEventsByCreador", e);
                    listener.onFailure(e);
                });
    }

    public void getEventsByParticipante(String nomParticipante, OnUserEventsListener listener) {
        db.collection("events")
                // LA MÀGIA ÉS AQUÍ: Busca dins de l'array "participantes"
                .whereArrayContains("participantes", nomParticipante)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Event> userEvents = new ArrayList<>();

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        try {
                            String id          = doc.getString("id");
                            String titulo      = doc.getString("titulo");
                            String descripcion = doc.getString("descripcion");
                            String foto        = doc.getString("foto");

                            int aforo = doc.contains("aforoMaximo") && doc.get("aforoMaximo") instanceof Number
                                    ? doc.getLong("aforoMaximo").intValue() : 0;

                            Timestamp timestamp = doc.getTimestamp("fechaHora");
                            LocalDateTime fechaHora = null;
                            if (timestamp != null) {
                                fechaHora = LocalDateTime.ofInstant(
                                        timestamp.toDate().toInstant(),
                                        java.time.ZoneId.systemDefault()
                                );
                            }

                            Map<String, Object> creadorMap = (Map<String, Object>) doc.get("creador");
                            User creador = new User();
                            if (creadorMap != null) {
                                creador = new User(
                                        (String) creadorMap.get("nom"),
                                        (String) creadorMap.get("correo")
                                );
                            }

                            Event event = Event.fromFirestore(id, titulo, descripcion, foto, fechaHora, creador, aforo);
                            event.setId(id);
                            event.setFoto(foto);

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

                        } catch (Exception e) {
                            android.util.Log.e("PROVA_PERFIL", "ERROR LLEGINT EVENT APUNTAT: " + doc.getId(), e);
                        }
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
                        doc.getReference().update("activo", false);
                    }
                    onSuccess.onSuccess();
                })
                .addOnFailureListener(onFailure::onFailure);
    }

    public interface OnParticipantesLoadedListener {
        void onParticipantesLoaded(List<String> participantes);
    }

    public void getParticipantesConCorreos(String eventoId, OnParticipantesConCorreosListener listener) {
        db.collection("events").document(eventoId).get()
                .addOnSuccessListener(doc -> {
                    List<String> nombres = (List<String>) doc.get("participantes");
                    if (nombres == null) nombres = new ArrayList<>();
                    if (nombres.isEmpty()) {
                        listener.onSuccess(new ArrayList<>());
                        return;
                    }
                    // Obtener los correos de la colección users
                    db.collection("users")
                            .whereIn("nom", nombres)
                            .get()
                            .addOnSuccessListener(querySnapshot -> {
                                List<Map<String, String>> participantes = new ArrayList<>();
                                for (DocumentSnapshot userDoc : querySnapshot.getDocuments()) {
                                    String nom = userDoc.getString("nom");
                                    String correo = userDoc.getString("correo");
                                    if (nom != null && correo != null) {
                                        Map<String, String> map = new HashMap<>();
                                        map.put("nom", nom);
                                        map.put("correo", correo);
                                        participantes.add(map);
                                    }
                                }
                                listener.onSuccess(participantes);
                            })
                            .addOnFailureListener(listener::onFailure);
                })
                .addOnFailureListener(listener::onFailure);
    }

    public interface OnParticipantesConCorreosListener {
        void onSuccess(List<Map<String, String>> participantes);
        void onFailure(Exception e);
    }

    public void desunirse(String eventoId, String nomUsuari, OnSuccessListener onSuccess, OnFailureListener onFailure) {
        db.collection("events")
                .document(eventoId)
                .update("participantes", com.google.firebase.firestore.FieldValue.arrayRemove(nomUsuari))
                .addOnSuccessListener(unused -> onSuccess.onSuccess())
                .addOnFailureListener(onFailure::onFailure);
    }
}