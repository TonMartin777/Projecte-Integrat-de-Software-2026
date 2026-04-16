package edu.ub.pis2526.projecte;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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

    // --- VARIABLES GLOBALS RECUPERADES ---
    private String nomUsuarioActual;
    private String correoUsuarioActual;
    private ActivityResultLauncher<Intent> editProfileLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        eventRepository = new FirestoreEventRepository();

        // Recuperem les dades i les guardem a les variables globals
        nomUsuarioActual = getIntent().getStringExtra("NOM_USUARI");
        correoUsuarioActual = getIntent().getStringExtra("CORREO_USUARI");

        // Mostra les dades als TextViews
        TextView nomTxt = findViewById(R.id.nomTxt);
        TextView correuTxt = findViewById(R.id.correuTxt);
        TextView telefonTxt = findViewById(R.id.telefonTxt);

        if (nomUsuarioActual != null) nomTxt.setText("Nom: " + nomUsuarioActual);
        if (correoUsuarioActual != null) correuTxt.setText("Correu: " + correoUsuarioActual);
        telefonTxt.setText("");

        // --- RECUPEREM EL RECEPTOR PER L'EDICIÓ DEL PERFIL ---
        editProfileLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        String nouNom = result.getData().getStringExtra("NOU_NOM");
                        String nouCorreu = result.getData().getStringExtra("NOU_CORREU");
                        String novaFoto = result.getData().getStringExtra("NOVA_FOTO");

                        if (nouNom != null) nomUsuarioActual = nouNom;
                        if (nouCorreu != null) correoUsuarioActual = nouCorreu;

                        nomTxt.setText("Nom: " + nomUsuarioActual);
                        correuTxt.setText("Correu: " + correoUsuarioActual);

                        if (novaFoto != null) {
                            ImageView fotoPerfil = findViewById(R.id.fotoPerfil);
                            com.bumptech.glide.Glide.with(this).load(novaFoto).into(fotoPerfil);
                        }

                        // Recarreguem els events si el nom ha canviat
                        cargarEventosDelUsuario(nomUsuarioActual);
                    }
                }
        );

        // --- RECUPEREM EL BOTÓ D'EDITAR PERFIL ---
        Button editPerfilBtn = findViewById(R.id.editPerfilBtn);
        editPerfilBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditProfileActivity.class);
            intent.putExtra("NOM_USUARI", nomUsuarioActual);
            intent.putExtra("CORREO_USUARI", correoUsuarioActual);
            editProfileLauncher.launch(intent); // Fem servir el launcher aquí
        });

        Button crearEventBtn = findViewById(R.id.crearEventBtn);
        crearEventBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateEventActivity.class);
            intent.putExtra("NOM_USUARI", nomUsuarioActual);
            intent.putExtra("CORREO_USUARI", correoUsuarioActual);
            startActivity(intent);
        });

        recyclerView = findViewById(R.id.meusEventsList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        listaMisEventos = new ArrayList<>();

        // --- LA TEVA LÒGICA D'ESBORRAR EVENTS MANTINGUDA INTACTA ---
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
        if (nomUsuarioActual != null) {
            cargarEventosDelUsuario(nomUsuarioActual);
        }
    }

    // --- RECUPEREM EL MÈTODE ONRESUME ---
    @Override
    protected void onResume() {
        super.onResume();
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