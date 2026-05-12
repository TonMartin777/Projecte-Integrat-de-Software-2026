package edu.ub.pis2526.projecte;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import edu.ub.pis2526.projecte.data.repositories.firestore.FirestoreEventRepository;

public class EncuestaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encuesta);

        String eventoId     = getIntent().getStringExtra("eventoId");
        String tituloEvento = getIntent().getStringExtra("tituloEvento");
        String nomUsuari    = getIntent().getStringExtra("NOM_USUARI");

        TextView tvTitulo     = findViewById(R.id.encuestaTitulo);
        LinearLayout layoutPaso1 = findViewById(R.id.layoutPaso1);
        LinearLayout layoutPaso2 = findViewById(R.id.layoutPaso2);
        RatingBar ratingBar   = findViewById(R.id.ratingBar);
        TextView tvPuntuacion = findViewById(R.id.tvPuntuacion);
        EditText editMissatge = findViewById(R.id.editMissatge);
        Button btnSiVaig      = findViewById(R.id.btnSiVaig);
        Button btnNoVaig      = findViewById(R.id.btnNoVaig);
        Button btnEnviar      = findViewById(R.id.btnEnviar);

        tvTitulo.setText(tituloEvento);

        // RatingBar de 5 estrellas → convertimos a puntuación sobre 10
        ratingBar.setOnRatingBarChangeListener((rb, rating, fromUser) -> {
            int puntuacion = (int) (rating * 2); // 0.5 estrellas = 1 punto
            tvPuntuacion.setText(puntuacion + " / 10");
        });

        // PASO 1 — No fui
        btnNoVaig.setOnClickListener(v -> {
            Toast.makeText(this, "Gracias por contestar", Toast.LENGTH_SHORT).show();
            finish();
        });

        // PASO 1 — Sí fui: mostrar paso 2
        btnSiVaig.setOnClickListener(v -> {
            layoutPaso1.setVisibility(View.GONE);
            layoutPaso2.setVisibility(View.VISIBLE);
        });

        // PASO 2 — Enviar reseña
        btnEnviar.setOnClickListener(v -> {
            int puntuacion = (int) (ratingBar.getRating() * 2);
            String missatge = editMissatge.getText().toString().trim();

            Resena resena = new Resena(nomUsuari, puntuacion, missatge);
            FirestoreEventRepository repo = new FirestoreEventRepository();
            repo.guardarResena(eventoId, resena,
                    () -> {
                        Toast.makeText(this, "Gracias por contestar", Toast.LENGTH_SHORT).show();
                        finish();
                    },
                    e -> Toast.makeText(this,
                            "Error al guardar: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show()
            );
        });
    }
}