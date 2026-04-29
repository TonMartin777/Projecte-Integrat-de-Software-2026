package edu.ub.pis2526.projecte;

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
            Toast.makeText(this, "Error: No s'ha trobat l'usuari", Toast.LENGTH_SHORT).show();
        }
    }

    private void carregarNotificacions(String destinatari) {
        FirestoreNotificacioRepository repo = new FirestoreNotificacioRepository();

        repo.getNotificacionsUsuari(destinatari, new FirestoreNotificacioRepository.OnNotificacionsListener() {
            @Override
            public void onSuccess(List<Notificacio> notificacions) {
                // Quan arriben les dades, les posem a l'adaptador
                NotificacioAdapter adapter = new NotificacioAdapter(notificacions);
                rvNotificacions.setAdapter(adapter);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(NotificacionsActivity.this, "Error carregant avisos", Toast.LENGTH_SHORT).show();
            }
        });
    }
}