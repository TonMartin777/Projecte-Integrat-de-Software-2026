package edu.ub.pis2526.projecte;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import edu.ub.pis2526.projecte.data.repositories.firestore.FirestoreUserRepository;

public class EditProfileActivity extends AppCompatActivity {

    private EditText inputNom, inputCorreu, inputContrasenya;
    private ImageView fotoPerfil;
    private Button guardarBtn;
    private Uri imageUri;
    private String nomOriginal;

    private final ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    imageUri = uri;
                    fotoPerfil.setImageURI(uri); // Mostrem la imatge seleccionada
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        inputNom = findViewById(R.id.editNom);
        inputCorreu = findViewById(R.id.editCorreu);
        inputContrasenya = findViewById(R.id.editContrasenya);
        fotoPerfil = findViewById(R.id.editFotoPerfil);
        guardarBtn = findViewById(R.id.guardarPerfilBtn);

        // Recuperem les dades i les guardem a la variable global
        nomOriginal = getIntent().getStringExtra("NOM_USUARI");
        String correuActual = getIntent().getStringExtra("CORREO_USUARI");

        // Omplim els camps perquè l'usuari vegi les seves dades actuals
        if (nomOriginal != null) inputNom.setText(nomOriginal);
        if (correuActual != null) inputCorreu.setText(correuActual);

        // --- CANVIAR FOTO ---
        fotoPerfil.setOnClickListener(v -> {
            pickMedia.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build());
        });

        // --- GUARDAR ---
        guardarBtn.setOnClickListener(v -> {
            String nouNom = inputNom.getText().toString();

            // Si hem triat una imatge nova, primer la pugem
            if (imageUri != null) {
                pujarFotoISalvar(nouNom);
            } else {
                // Si no hi ha imatge nova, actualitzem la resta de dades directament
                executarActualitzacio(null);
            }
        });
    }

    private void pujarFotoISalvar(String nomUsuari) {
        // Comprovació de seguretat: que el nom no estigui buit
        if (nomUsuari == null || nomUsuari.trim().isEmpty()) {
            Toast.makeText(this, "Error: El nombre de usuario no puede estar vacio", Toast.LENGTH_LONG).show();
            return;
        }

        Toast.makeText(this, "Iniciando la subida de la foto...", Toast.LENGTH_SHORT).show();

        StorageReference ref = FirebaseStorage.getInstance().getReference()
                .child("perfils/" + nomUsuari + ".jpg");

        // Intentem pujar l'arxiu físic
        ref.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {

                    // Si arribem aquí, la foto JA ESTÀ al núvol. Ara en demanem l'enllaç públic.
                    ref.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                // Tot perfecte, guardem les dades a Firestore
                                executarActualitzacio(uri.toString());
                            })
                            .addOnFailureListener(e -> {
                                // Ha fallat al demanar l'enllaç
                                Toast.makeText(this, "Error al generar URL: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            });

                })
                .addOnFailureListener(e -> {
                    // Ha fallat la pujada de l'arxiu a Firebase
                    Toast.makeText(this, "Error al guardar el arxivo a la nube: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void executarActualitzacio(String urlFoto) {
        FirestoreUserRepository repo = new FirestoreUserRepository();
        repo.updateUser(nomOriginal, inputNom.getText().toString(),
                inputCorreu.getText().toString(),
                inputContrasenya.getText().toString(),
                urlFoto, new FirestoreUserRepository.OnUpdateListener() {

                    @Override
                    public void onUpdateSuccess() {
                        Toast.makeText(EditProfileActivity.this, "Perfil actualizado!", Toast.LENGTH_SHORT).show();

                        // --- PREPAREM EL PAQUET DE TORNADA ---
                        Intent intentResultat = new Intent();
                        intentResultat.putExtra("NOU_NOM", inputNom.getText().toString());
                        intentResultat.putExtra("NOU_CORREU", inputCorreu.getText().toString());
                        if (urlFoto != null) {
                            intentResultat.putExtra("NOVA_FOTO", urlFoto);
                        }
                        // Avisem a l'Activity anterior que tot ha anat bé (RESULT_OK) i li passem les dades
                        setResult(RESULT_OK, intentResultat);
                        // -----------------------------------------------

                        finish(); // Tanquem la pantalla
                    }

                    @Override
                    public void onUpdateError(Exception e) {
                        Toast.makeText(EditProfileActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}