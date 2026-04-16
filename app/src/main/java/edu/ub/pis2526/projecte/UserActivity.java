package edu.ub.pis2526.projecte;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import edu.ub.pis2526.projecte.data.repositories.firestore.FirestoreEventRepository;

public class UserActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EventAdapter eventAdapter;
    private List<Event> listaMisEventos;
    private FirestoreEventRepository eventRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        eventRepository = new FirestoreEventRepository(); // Inicialitzem el repo

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
            intent.putExtra("NOM_USUARI",    nom);
            intent.putExtra("CORREO_USUARI", correo);
            startActivity(intent);
        });

        recyclerView = findViewById(R.id.meusEventsList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        listaMisEventos = new ArrayList<>();

        eventAdapter = new EventAdapter(listaMisEventos, (event, position) -> {
            new AlertDialog.Builder(this)
                    .setTitle("Eliminar esdeveniment")
                    .setMessage("Segur que vols eliminar '" + event.getTitulo() + "'?")
                    .setPositiveButton("Sí, eliminar", (dialog, which) -> {
                        eventRepository.delete(event.getId(), new FirestoreEventRepository.OnDeleteListener() {
                            @Override
                            public void onSuccess() {
                                listaMisEventos.remove(position);
                                eventAdapter.notifyItemRemoved(position);
                                Toast.makeText(UserActivity.this, "Esdeveniment eliminat", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onFailure(Exception e) {
                                Toast.makeText(UserActivity.this, "Error al eliminar", Toast.LENGTH_SHORT).show();
                            }
                        });
                    })
                    .setNegativeButton("Cancel·lar", null)
                    .show();
        });

        recyclerView.setAdapter(eventAdapter);

        // Si tenim el nom de l'usuari, busquem els seus esdeveniments
        if (nom != null) {
            cargarEventosDelUsuario(nom);
        }
    }

    private void cargarEventosDelUsuario(String nomUsuario) {
        eventRepository.getEventsByCreador(nomUsuario, new FirestoreEventRepository.OnUserEventsListener() {
            @Override
            public void onSuccess(List<Event> events) {
                listaMisEventos.clear();
                listaMisEventos.addAll(events);
                eventAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(UserActivity.this, "Error carregant esdeveniments", Toast.LENGTH_SHORT).show();
            }
        });
    }
}