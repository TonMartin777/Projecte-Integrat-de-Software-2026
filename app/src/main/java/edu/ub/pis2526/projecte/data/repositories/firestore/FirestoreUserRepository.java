package edu.ub.pis2526.projecte.data.repositories.firestore;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import edu.ub.pis2526.projecte.User;

public class FirestoreUserRepository {

    private final FirebaseFirestore db;
    private static final String COLLECTION = "users";

    // Interfície d'escoltadors
    public interface OnSignUpListener {
        void onSignUpSuccess();
        void onSignUpError(Exception e);
    }

    public FirestoreUserRepository() {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Comprova si el nom d'usuari ja existeix i, si no, registra l'usuari.
     */
    public void signUp(String nom, String correo, String contrasenya,
                       OnSignUpListener listener) {

        // Primer comprovem que el nom no estigui ja agafat
        db.collection(COLLECTION)
                .whereEqualTo("nom", nom)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        listener.onSignUpError(
                                new Exception("El nom d'usuari ja existeix")
                        );
                        return;
                    }

                    //  si el nom esta lliure, guardem l'usuari
                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("nom",        nom);
                    userMap.put("correo",     correo);
                    userMap.put("contrasenya", contrasenya);

                    db.collection(COLLECTION)
                            .document(nom) // usem el nom com a ID del document
                            .set(userMap)
                            .addOnSuccessListener(unused ->
                                    listener.onSignUpSuccess()
                            )
                            .addOnFailureListener(listener::onSignUpError);
                })
                .addOnFailureListener(listener::onSignUpError);
    }
}