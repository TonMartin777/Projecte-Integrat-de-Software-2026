package edu.ub.pis2526.projecte;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
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

        TextView tvTitulo    = findViewById(R.id.encuestaTitulo);
        SeekBar seekPuntuacion = findViewById(R.id.seekPuntuacion);
        TextView tvPuntuacion  = findViewById(R.id.tvPuntuacion);
        EditText editMissatge  = findViewById(R.id.editMissatge);
        Button btnSiVaig      = findViewById(R.id.btnSiVaig);
        Button btnNoVaig      = findViewById(R.id.btnNoVaig);

        tvTitulo.setText("¿Cómo fue: " + tituloEvento + "?");
        tvPuntuacion.setText("Puntuación: 5");
        seekPuntuacion.setMax(10);
        seekPuntuacion.setProgress(5);

        seekPuntuacion.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvPuntuacion.setText("Puntuación: " + progress);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        btnNoVaig.setOnClickListener(v -> {
            Toast.makeText(this, "Gracias por contestar", Toast.LENGTH_SHORT).show();
            finish();
        });

        btnSiVaig.setOnClickListener(v -> {
            int puntuacion = seekPuntuacion.getProgress();
            String missatge = editMissatge.getText().toString().trim();

            Resena resena = new Resena(nomUsuari, puntuacion, missatge);
            FirestoreEventRepository repo = new FirestoreEventRepository();
            repo.guardarResena(eventoId, resena,
                    () -> {
                        Toast.makeText(this, "Gracias por contestar", Toast.LENGTH_SHORT).show();
                        finish();
                    },
                    e -> Toast.makeText(this, "Error al guardar: " + e.getMessage(), Toast.LENGTH_SHORT).show()
            );
        });
    }
}
