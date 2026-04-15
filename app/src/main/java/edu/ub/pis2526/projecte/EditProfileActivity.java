package edu.ub.pis2526.projecte;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class EditProfileActivity extends AppCompatActivity {

    private EditText inputNom, inputCorreu, inputContrasenya;
    private ImageView fotoPerfil;
    private Button guardarBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        inputNom = findViewById(R.id.editNom);
        inputCorreu = findViewById(R.id.editCorreu);
        inputContrasenya = findViewById(R.id.editContrasenya);
        fotoPerfil = findViewById(R.id.editFotoPerfil);
        guardarBtn = findViewById(R.id.guardarPerfilBtn);

        // Recuperem les dades que ens passa la UserActivity
        String nomActual = getIntent().getStringExtra("NOM_USUARI");
        String correuActual = getIntent().getStringExtra("CORREO_USUARI");

        // Omplim els camps perquè l'usuari vegi les seves dades actuals
        if (nomActual != null) inputNom.setText(nomActual);
        if (correuActual != null) inputCorreu.setText(correuActual);

        // --- CANVIAR FOTO ---
        fotoPerfil.setOnClickListener(v -> {
            Toast.makeText(this, "Obrint galeria...", Toast.LENGTH_SHORT).show();
        });

        // --- GUARDAR ---
        guardarBtn.setOnClickListener(v -> {
            String nouNom = inputNom.getText().toString();
            String nouCorreu = inputCorreu.getText().toString();
            String novaContrasenya = inputContrasenya.getText().toString();

            Toast.makeText(this, "Dades preparades per guardar!", Toast.LENGTH_SHORT).show();

            // Tancar aquesta pantalla i tornar al perfil
            finish();
        });
    }
}