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

    // VARIABLE GLOBAL PER GUARDAR EL NOM ORIGINAL
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

        // Recuperem les dades i les guardem a la VARIABLE GLOBAL
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

    // --- NOU MÈTODE PER PUJAR LA FOTO AL STORAGE ---
    private void pujarFotoISalvar(String nomUsuari) {
        // Creem una referència al Storage (es guardarà com a "perfils/nomDusuari.jpg")
        StorageReference ref = FirebaseStorage.getInstance().getReference()
                .child("perfils/" + nomUsuari + ".jpg");

        ref.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
            // Un cop pujada, demanem la URL pública
            ref.getDownloadUrl().addOnSuccessListener(uri -> {
                // Cridem a executarActualitzacio passant-li la URL de la foto nova
                executarActualitzacio(uri.toString());
            });
        }).addOnFailureListener(e -> Toast.makeText(this, "Error pujant foto", Toast.LENGTH_SHORT).show());
    }

    private void executarActualitzacio(String urlFoto) {
        FirestoreUserRepository repo = new FirestoreUserRepository();
        repo.updateUser(nomOriginal, inputNom.getText().toString(),
                inputCorreu.getText().toString(),
                inputContrasenya.getText().toString(),
                urlFoto, new FirestoreUserRepository.OnUpdateListener() {

                    @Override
                    public void onUpdateSuccess() {
                        Toast.makeText(EditProfileActivity.this, "Perfil actualitzat!", Toast.LENGTH_SHORT).show();

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