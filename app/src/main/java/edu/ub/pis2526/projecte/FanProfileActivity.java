package edu.ub.pis2526.projecte;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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

public class FanProfileActivity extends AppCompatActivity {

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

        Button crearEventBtn = findViewById(R.id.crearEventBtn);
        crearEventBtn.setVisibility(View.GONE);

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

        Button editPerfilBtn = findViewById(R.id.editPerfilBtn);
        editPerfilBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditProfileActivity.class);
            intent.putExtra("NOM_USUARI", nomUsuarioActual);
            intent.putExtra("CORREO_USUARI", correoUsuarioActual);
            editProfileLauncher.launch(intent); // Fem servir el launcher aquí
        });

        recyclerView = findViewById(R.id.meusEventsList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        listaMisEventos = new ArrayList<>();
        String rol = getIntent().getStringExtra("ROL");
        eventAdapter = new EventAdapter(listaMisEventos, nomUsuarioActual, rol);
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
        eventRepository.getEventsByParticipante(nomUsuario, new FirestoreEventRepository.OnUserEventsListener() {
            @Override
            public void onSuccess(List<Event> events) {
                eventAdapter.actualizarLista(events);
            }
            @Override
            public void onFailure(Exception e) {
                Toast.makeText(FanProfileActivity.this, "Error carregant esdeveniments", Toast.LENGTH_SHORT).show();
            }
        });
    }
}