package edu.ub.pis2526.projecte.data.repositories.firestore;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

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
    public void signUp(String nom, String correo, String contrasenya, String rol, OnSignUpListener listener) {
        // Comprobar si el nom existe (igual)
        db.collection(COLLECTION)
                .whereEqualTo("nom", nom)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        listener.onSignUpError(new Exception("El nom d'usuari ja existeix"));
                        return;
                    }
                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("nom", nom);
                    userMap.put("correo", correo);
                    userMap.put("contrasenya", contrasenya);
                    userMap.put("rol", rol);
                    // Si es banda, podrías inicializar campos extra, pero no es necesario ahora
                    db.collection(COLLECTION).document(nom).set(userMap)
                            .addOnSuccessListener(unused -> listener.onSignUpSuccess())
                            .addOnFailureListener(listener::onSignUpError);
                })
                .addOnFailureListener(listener::onSignUpError);
    }

    public interface OnLoginListener {
        void onLoginSuccess(String nom, String correo, String rol);
        void onLoginError(Exception e);
    }

    public void login(String nom, String contrasenya, OnLoginListener listener) {
        db.collection(COLLECTION).document(nom).get()
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
                    String rol = document.getString("rol");
                    listener.onLoginSuccess(nom, correo, rol);
                })
                .addOnFailureListener(listener::onLoginError);
    }

    public interface OnUpdateListener {
        void onUpdateSuccess();
        void onUpdateError(Exception e);
    }

    public void updateUser(String oldNom, String newNom, String newCorreo, String newContrasenya, String fotoUrl, OnUpdateListener listener) {

        // Primer anem a buscar les dades actuals de l'usuari a la base de dades
        db.collection(COLLECTION).document(oldNom).get().addOnSuccessListener(documentSnapshot -> {
            if (!documentSnapshot.exists()) {
                listener.onUpdateError(new Exception("L'usuari no existeix"));
                return;
            }

            // Recuperem la contrasenya actual que hi ha a Firebase
            String contrasenyaActual = documentSnapshot.getString("contrasenya");

            // Decidim quina contrasenya farem servir:
            // Si la 'newContrasenya' està buida, usem la 'contrasenyaActual'
            String contrasenyaFinal = (newContrasenya == null || newContrasenya.isEmpty())
                    ? contrasenyaActual
                    : newContrasenya;

            // Preparem el mapa de dades amb la contrasenya correcta
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("nom", newNom);
            userMap.put("correo", newCorreo);
            userMap.put("contrasenya", contrasenyaFinal); // Ara ja no es perdrà mai
            if (fotoUrl != null) userMap.put("foto", fotoUrl);

            // 5. Ara procedim amb la lògica que ja tenies (update o batch)
            if (oldNom.equals(newNom)) {
                // El nom no canvia, actualitzem el document existent
                db.collection(COLLECTION).document(oldNom).update(userMap)
                        .addOnSuccessListener(unused -> listener.onUpdateSuccess())
                        .addOnFailureListener(listener::onUpdateError);
            } else {
                // El nom ha canviat, hem de fer el procés de copiar i esborrar
                WriteBatch batch = db.batch();
                batch.set(db.collection(COLLECTION).document(newNom), userMap);

                db.collection("events").whereEqualTo("creador.nom", oldNom).get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                                batch.update(doc.getReference(), "creador.nom", newNom);
                            }
                            batch.delete(db.collection(COLLECTION).document(oldNom));
                            batch.commit()
                                    .addOnSuccessListener(unused -> listener.onUpdateSuccess())
                                    .addOnFailureListener(listener::onUpdateError);
                        })
                        .addOnFailureListener(listener::onUpdateError);
            }
        }).addOnFailureListener(listener::onUpdateError);
    }




    // subscripcions :
    public interface OnSubscripcioListener {
        void onSuccess();
        void onError(Exception e);
    }

    public interface OnComprovarSubscripcioListener {
        void onResult(boolean estaSubscrit);
    }

    public void subscriure(String nomAssistent, String nomCreador,
                           OnSubscripcioListener listener) {
        // Afegim el creador a la llista de subscripcions de l'assistent
        db.collection(COLLECTION).document(nomAssistent)
                .update("subscripcions",
                        com.google.firebase.firestore.FieldValue.arrayUnion(nomCreador))
                .addOnSuccessListener(unused -> {
                    // Afegim l'assistent a la llista de seguidors del creador
                    db.collection(COLLECTION).document(nomCreador)
                            .update("seguidors",
                                    com.google.firebase.firestore.FieldValue.arrayUnion(nomAssistent))
                            .addOnSuccessListener(u -> listener.onSuccess())
                            .addOnFailureListener(listener::onError);
                })
                .addOnFailureListener(listener::onError);
    }

    public void desSubscriure(String nomAssistent, String nomCreador,
                              OnSubscripcioListener listener) {
        db.collection(COLLECTION).document(nomAssistent)
                .update("subscripcions",
                        com.google.firebase.firestore.FieldValue.arrayRemove(nomCreador))
                .addOnSuccessListener(unused -> {
                    db.collection(COLLECTION).document(nomCreador)
                            .update("seguidors",
                                    com.google.firebase.firestore.FieldValue.arrayRemove(nomAssistent))
                            .addOnSuccessListener(u -> listener.onSuccess())
                            .addOnFailureListener(listener::onError);
                })
                .addOnFailureListener(listener::onError);
    }

    public void comprovarSubscripcio(String nomAssistent, String nomCreador,
                                     OnComprovarSubscripcioListener listener) {
        db.collection(COLLECTION).document(nomAssistent).get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) { listener.onResult(false); return; }
                    java.util.List<String> subscripcions =
                            (java.util.List<String>) doc.get("subscripcions");
                    listener.onResult(subscripcions != null &&
                            subscripcions.contains(nomCreador));
                })
                .addOnFailureListener(e -> listener.onResult(false));
    }
    public interface OnSeguidorsListener {
        void onResult(int numSeguidors);
    }

    public void getNumSeguidors(String nomCreador, OnSeguidorsListener listener) {
        db.collection(COLLECTION).document(nomCreador).get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) { listener.onResult(0); return; }
                    java.util.List<String> seguidors =
                            (java.util.List<String>) doc.get("seguidors");
                    listener.onResult(seguidors != null ? seguidors.size() : 0);
                })
                .addOnFailureListener(e -> listener.onResult(0));
    }
    public interface OnSeguidorsListListener {
        void onResult(java.util.List<String> seguidors);
    }

    public void getSeguidors(String nomCreador, OnSeguidorsListListener listener) {
        db.collection(COLLECTION).document(nomCreador).get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) { listener.onResult(new java.util.ArrayList<>()); return; }
                    java.util.List<String> seguidors =
                            (java.util.List<String>) doc.get("seguidors");
                    listener.onResult(seguidors != null ? seguidors : new java.util.ArrayList<>());
                })
                .addOnFailureListener(e -> listener.onResult(new java.util.ArrayList<>()));
    }



}