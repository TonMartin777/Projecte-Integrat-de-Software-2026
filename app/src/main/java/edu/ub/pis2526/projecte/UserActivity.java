package edu.ub.pis2526.projecte;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
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
    private String correoUsuarioActual;
    private androidx.activity.result.ActivityResultLauncher<Intent> editProfileLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        // Rep les dades passades per Intent
        nomUsuarioActual = getIntent().getStringExtra("NOM_USUARI");
        correoUsuarioActual = getIntent().getStringExtra("CORREO_USUARI");

        TextView nomTxt = findViewById(R.id.nomTxt);
        TextView correuTxt = findViewById(R.id.correuTxt);

        // Preparem el receptor per quan tornem de la pantalla d'edició
        editProfileLauncher = registerForActivityResult(
                new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        // Actualitzem les variables amb les dades noves!
                        String nouNom = result.getData().getStringExtra("NOU_NOM");
                        String nouCorreu = result.getData().getStringExtra("NOU_CORREU");
                        String novaFoto = result.getData().getStringExtra("NOVA_FOTO");

                        if (nouNom != null) nomUsuarioActual = nouNom;
                        if (nouCorreu != null) correoUsuarioActual = nouCorreu;

                        // Actualitzem els textos de la pantalla perquè l'usuari ho vegi
                        nomTxt.setText("Nom: " + nomUsuarioActual);
                        correuTxt.setText("Correu: " + correoUsuarioActual);

                        if (novaFoto != null) {
                            ImageView fotoPerfil = findViewById(R.id.fotoPerfil);
                            com.bumptech.glide.Glide.with(this).load(novaFoto).into(fotoPerfil);
                        }
                    }
                }
        );

        if (nomUsuarioActual != null)    nomTxt.setText("Nom: " + nomUsuarioActual);
        if (correoUsuarioActual != null) correuTxt.setText("Correu: " + correoUsuarioActual);

        Button crearEventBtn = findViewById(R.id.crearEventBtn);
        crearEventBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateEventActivity.class);
            intent.putExtra("NOM_USUARI",    nomUsuarioActual);
            intent.putExtra("CORREO_USUARI", correoUsuarioActual);
            startActivity(intent);
        });

        Button editPerfilBtn = findViewById(R.id.editPerfilBtn);
        editPerfilBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditProfileActivity.class);
            // Passem les dades actuals perquè es mostrin a la pantalla d'edició
            intent.putExtra("NOM_USUARI", nomUsuarioActual);
            intent.putExtra("CORREO_USUARI", correoUsuarioActual);
            editProfileLauncher.launch(intent);
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