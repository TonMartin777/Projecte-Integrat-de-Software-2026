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
                    userMap.put("nom", nom);
                    userMap.put("correo", correo);
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

    public interface OnLoginListener {
        void onLoginSuccess(String nom, String correo);

        void onLoginError(Exception e);
    }

    public void login(String nom, String contrasenya, OnLoginListener listener) {
        db.collection(COLLECTION)
                .document(nom)
                .get()
                .addOnSuccessListener(document -> {
                    if (!document.exists()) {
                        listener.onLoginError(new Exception("Usuari no trobat"));
                        return;
                    }
                    String contrasenyaGuardada = document.getString("contrasenya");
                    if (!contrasenya.equals(contrasenyaGuardada)) {
                        listener.onLoginError(new Exception("Contrasenya incorrecta"));
                        return;
                    }
                    String correo = document.getString("correo");
                    listener.onLoginSuccess(nom, correo);
                })
                .addOnFailureListener(listener::onLoginError);
    }

    public interface OnUpdateListener {
        void onUpdateSuccess();
        void onUpdateError(Exception e);
    }

    public void updateUser(String oldNom, String newNom, String newCorreo, String newContrasenya, OnUpdateListener listener) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("nom", newNom);
        userMap.put("correo", newCorreo);
        userMap.put("contrasenya", newContrasenya);

        if (oldNom.equals(newNom)) {
            // Si el nom és el mateix, només actualitzem els camps
            db.collection(COLLECTION).document(oldNom).update(userMap)
                    .addOnSuccessListener(unused -> listener.onUpdateSuccess())
                    .addOnFailureListener(listener::onUpdateError);
        } else {
            // Si el nom canvia, hem de crear un nou document amb la nova ID (nom)
            db.collection(COLLECTION).document(newNom).set(userMap)
                    .addOnSuccessListener(unused -> {
                        // Esborrem el document antic
                        db.collection(COLLECTION).document(oldNom).delete();
                        listener.onUpdateSuccess();
                    })
                    .addOnFailureListener(listener::onUpdateError);
        }
    }
}