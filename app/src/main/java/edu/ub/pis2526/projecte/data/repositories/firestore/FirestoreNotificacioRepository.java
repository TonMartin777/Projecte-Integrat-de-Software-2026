package edu.ub.pis2526.projecte.data.repositories.firestore;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.List;

import edu.ub.pis2526.projecte.Notificacio;

public class FirestoreNotificacioRepository {

    private final FirebaseFirestore db;
    private final String COLLECTION = "notificacions";

    public FirestoreNotificacioRepository() {
        db = FirebaseFirestore.getInstance();
    }

    // Mètode màgic per enviar una notificació a qui vulguis
    public void enviarNotificacio(String destinatari, String titol, String missatge) {
        Notificacio novaNotificacio = new Notificacio(destinatari, titol, missatge);

        // Deixem que Firestore generi una ID aleatòria
        db.collection(COLLECTION)
                .add(novaNotificacio)
                .addOnSuccessListener(documentReference -> {
                    // Actualitzem l'ID de l'objecte amb la que ha creat Firestore
                    documentReference.update("id", documentReference.getId());
                });
    }

    public interface OnNotificacionsListener {
        void onSuccess(List<Notificacio> notificacions);
        void onFailure(Exception e);
    }

    public void getNotificacionsUsuari(String destinatari, OnNotificacionsListener listener) {
        db.collection(COLLECTION)
                .whereEqualTo("destinatari", destinatari)
                .orderBy("timestamp", Query.Direction.DESCENDING) // ORDRE: Mes noves a dalt
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Notificacio> llista = queryDocumentSnapshots.toObjects(Notificacio.class);
                    listener.onSuccess(llista);
                })
                .addOnFailureListener(listener::onFailure);
    }
}