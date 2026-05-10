package edu.ub.pis2526.projecte;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import edu.ub.pis2526.projecte.data.repositories.firestore.FirestoreEventRepository;

public class ResenasActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resenas);

        String eventoId = getIntent().getStringExtra("eventoId");
        String titulo   = getIntent().getStringExtra("titulo");

        TextView tvTitulo  = findViewById(R.id.resenasTitulo);
        TextView tvMedia   = findViewById(R.id.resenasMedia);
        RecyclerView rv    = findViewById(R.id.resenasRecycler);

        tvTitulo.setText("Reseñas: " + titulo);
        rv.setLayoutManager(new LinearLayoutManager(this));

        FirestoreEventRepository repo = new FirestoreEventRepository();
        repo.getResenas(eventoId,
                resenas -> {
                    if (resenas.isEmpty()) {
                        tvMedia.setText("Sin reseñas todavía");
                    } else {
                        double media = resenas.stream()
                                .mapToInt(Resena::getPuntuacion)
                                .average()
                                .orElse(0);
                        tvMedia.setText(String.format("Puntuación media: %.1f / 10", media));
                    }
                    ResenasAdapter adapter = new ResenasAdapter(resenas);
                    rv.setAdapter(adapter);
                },
                e -> Toast.makeText(this, "Error cargando reseñas", Toast.LENGTH_SHORT).show()
        );
    }
}
