package edu.ub.pis2526.projecte;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class UserActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        // Rep les dades passades per Intent
        String nom    = getIntent().getStringExtra("NOM_USUARI");
        String correo = getIntent().getStringExtra("CORREO_USUARI");

        // Mostra les dades als TextViews
        TextView nomTxt    = findViewById(R.id.nomTxt);
        TextView correuTxt = findViewById(R.id.correuTxt);
        TextView telefonTxt = findViewById(R.id.telefonTxt);

        if (nom != null)    nomTxt.setText("Nom: " + nom);
        if (correo != null) correuTxt.setText("Correu: " + correo);
        telefonTxt.setText("");

        Button crearEventBtn = findViewById(R.id.crearEventBtn);
        crearEventBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateEventActivity.class);
            intent.putExtra("NOM_USUARI",    getIntent().getStringExtra("NOM_USUARI"));
            intent.putExtra("CORREO_USUARI", getIntent().getStringExtra("CORREO_USUARI"));
            startActivity(intent);
        });
    }
}