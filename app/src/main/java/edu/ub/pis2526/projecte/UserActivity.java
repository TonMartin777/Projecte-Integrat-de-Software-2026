package edu.ub.pis2526.projecte;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
    private String nomUsuarioActual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        // Rep les dades passades per Intent
        nomUsuarioActual = getIntent().getStringExtra("NOM_USUARI");
        String correo = getIntent().getStringExtra("CORREO_USUARI");

        // Mostra les dades als TextViews
        TextView nomTxt    = findViewById(R.id.nomTxt);
        TextView correuTxt = findViewById(R.id.correuTxt);
        TextView telefonTxt = findViewById(R.id.telefonTxt);

        if (nomUsuarioActual != null)    nomTxt.setText("Nom: " + nomUsuarioActual);
        if (correo != null) correuTxt.setText("Correu: " + correo);
        telefonTxt.setText("");

        Button crearEventBtn = findViewById(R.id.crearEventBtn);
        crearEventBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateEventActivity.class);
            intent.putExtra("NOM_USUARI",    nomUsuarioActual);
            intent.putExtra("CORREO_USUARI", correo);
            startActivity(intent);
        });

        recyclerView = findViewById(R.id.meusEventsList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        listaMisEventos = new ArrayList<>();
        eventAdapter = new EventAdapter(listaMisEventos);
        recyclerView.setAdapter(eventAdapter);

        eventRepository = new FirestoreEventRepository();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Cada vegada que l'activitat torna al primer pla, demanem els events actualitzats
        if (nomUsuarioActual != null) {
            cargarEventosDelUsuario(nomUsuarioActual);
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