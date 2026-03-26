package edu.ub.pis2526.projecte.data.repositories.firestore;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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
}