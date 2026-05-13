package edu.ub.pis2526.projecte;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import edu.ub.pis2526.projecte.data.repositories.firestore.FirestoreNotificacioRepository;

public class NotificacionsActivity extends AppCompatActivity {

    private RecyclerView rvNotificacions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notificacions);

        rvNotificacions = findViewById(R.id.rvNotificacions);
        rvNotificacions.setLayoutManager(new LinearLayoutManager(this));

        // Agafem el nom de l'usuari que ha entrat a la pantalla
        String usuariNom = getIntent().getStringExtra("NOM_USUARI");

        if (usuariNom != null) {
            carregarNotificacions(usuariNom);
        } else {
            Toast.makeText(this, "Error: No se ha encontrado el usuario", Toast.LENGTH_SHORT).show();
        }
    }

    private void carregarNotificacions(String destinatari) {
        FirestoreNotificacioRepository repo = new FirestoreNotificacioRepository();

        repo.getNotificacionsUsuari(destinatari, new FirestoreNotificacioRepository.OnNotificacionsListener() {
            @Override
            public void onSuccess(List<Notificacio> notificacions) {
                // Quan arriben les dades, les posem a l'adaptador
                NotificacioAdapter adapter = new NotificacioAdapter(notificacions, notificacio -> {
                    Intent intent = new Intent(NotificacionsActivity.this, EncuestaActivity.class);
                    intent.putExtra("eventoId", notificacio.getEventoId());
                    intent.putExtra("tituloEvento", notificacio.getTituloEvento());
                    intent.putExtra("NOM_USUARI", getIntent().getStringExtra("NOM_USUARI"));
                    startActivity(intent);
                });
                rvNotificacions.setAdapter(adapter);
            }

            @Override
            public void onFailure(Exception e) {
                // 1. Imprimim l'error al Logcat amb una etiqueta nostra
                android.util.Log.e("PROVA_ERROR", "Motiu de la fallada:", e);

                // 2. Mostrem l'error exacte per pantalla perquè el puguis llegir al mòbil
                Toast.makeText(NotificacionsActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}