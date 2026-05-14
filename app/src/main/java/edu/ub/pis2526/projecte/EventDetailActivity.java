package edu.ub.pis2526.projecte;

import com.bumptech.glide.Glide;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import edu.ub.pis2526.projecte.data.repositories.firestore.FirestoreEventRepository;
import edu.ub.pis2526.projecte.data.repositories.firestore.FirestoreNotificacioRepository;
import edu.ub.pis2526.projecte.data.repositories.firestore.FirestoreUserRepository;

public class EventDetailActivity extends AppCompatActivity {

    private FirestoreUserRepository userRepository;
    private boolean estaSubscrit = false;
    private int currentParticipantsCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        // Repositoris
        userRepository = new FirestoreUserRepository();
        FirestoreEventRepository repo = new FirestoreEventRepository();

        // Recuperar dades de l'Intent
        String titulo      = getIntent().getStringExtra("titulo");
        String descripcion = getIntent().getStringExtra("descripcion");
        String fecha       = getIntent().getStringExtra("fecha");
        String hora        = getIntent().getStringExtra("hora");
        String foto        = getIntent().getStringExtra("foto");
        int aforoMaximo    = getIntent().getIntExtra("aforoMaximo", 0);
        double lat         = getIntent().getDoubleExtra("lat", 0);
        double lng         = getIntent().getDoubleExtra("lng", 0);
        String creador     = getIntent().getStringExtra("creador"); // El nom de la banda/artista
        String genero      = getIntent().getStringExtra("genero");
        String eventoId    = getIntent().getStringExtra("eventoId");
        String nomUsuari   = getIntent().getStringExtra("NOM_USUARI"); // Usuari loguejat

        // Referències UI
        TextView tvCreador = findViewById(R.id.detailCreador);
        TextView tvNombre = findViewById(R.id.detailNombre);
        TextView tvFecha = findViewById(R.id.detailFecha);
        TextView tvDescripcion = findViewById(R.id.detailDescripcion);
        TextView tvHora        = findViewById(R.id.detailHora);
        TextView tvUbicacion   = findViewById(R.id.detailUbicacion);
        ImageView imgEvento    = findViewById(R.id.detailImagen);
        TextView tvGenero      = findViewById(R.id.detailGenero);
        TextView tvAforo       = findViewById(R.id.detailAforo);
        Button btnSub          = findViewById(R.id.btnSubscriure);

        // Assignar dades
        tvNombre.setText(titulo);
        tvFecha.setText(fecha);
        tvDescripcion.setText(descripcion);
        tvHora.setText(hora);
        tvUbicacion.setText("Lat: " + lat + ", Lng: " + lng);
        tvCreador.setText("Creado por: " + creador);
        tvGenero.setText("Género: " + (genero != null && !genero.isEmpty() ? genero : "No especificado"));

        // Gestió de la imatge
        Generos generoEnum = null;
        if (genero != null && !genero.isEmpty()) {
            try { generoEnum = Generos.valueOf(genero); } catch (IllegalArgumentException ignored) {}
        }

        if (foto == null || foto.isEmpty()) {
            imgEvento.setImageResource(Event.getImagenPorGenero(generoEnum));
        } else {
            Glide.with(this).load(foto).placeholder(R.drawable.evento_default).error(Event.getImagenPorGenero(generoEnum)).into(imgEvento);
        }

        // --- LÒGICA DE SUBSCRIPCIÓ ---
        if (creador != null && nomUsuari != null && !creador.equals(nomUsuari)) {
            btnSub.setVisibility(View.VISIBLE);

            // Comprovar si ja està subscrit
            userRepository.comprovarSubscripcio(nomUsuari, creador, esta -> {
                estaSubscrit = esta;
                actualitzarInterficieSub(btnSub);
            });

            btnSub.setOnClickListener(v -> {
                if (estaSubscrit) {
                    userRepository.desSubscriure(nomUsuari, creador, new FirestoreUserRepository.OnSubscripcioListener() {
                        @Override
                        public void onSuccess() {
                            estaSubscrit = false;
                            actualitzarInterficieSub(btnSub);
                            Toast.makeText(EventDetailActivity.this, "Has dejado de seguir a " + creador, Toast.LENGTH_SHORT).show();
                        }
                        @Override
                        public void onError(Exception e) { Toast.makeText(EventDetailActivity.this, "Error", Toast.LENGTH_SHORT).show(); }
                    });
                } else {
                    userRepository.subscriure(nomUsuari, creador, new FirestoreUserRepository.OnSubscripcioListener() {
                        @Override
                        public void onSuccess() {
                            estaSubscrit = true;
                            actualitzarInterficieSub(btnSub);
                            Toast.makeText(EventDetailActivity.this, "Ahora sigues a " + creador + "!", Toast.LENGTH_SHORT).show();
                        }
                        @Override
                        public void onError(Exception e) { Toast.makeText(EventDetailActivity.this, "Error", Toast.LENGTH_SHORT).show(); }
                    });
                }
            });
        } else {
            btnSub.setVisibility(View.GONE);
        }

        // Mapa
        Button btnAbrirMapa = findViewById(R.id.btnAbrirMapa);
        btnAbrirMapa.setOnClickListener(v -> {
            if (lat != 0 && lng != 0) {
                Uri gmmIntentUri = Uri.parse("geo:" + lat + "," + lng + "?q=" + lat + "," + lng + "(" + titulo + ")");
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
            }
        });

        // --- BOTÓ UNIR-SE / DESAPUNTAR-SE ---
        Button btnUnirse = findViewById(R.id.btnUnirse);
        final boolean[] estaApuntado = {false};

        repo.getParticipantes(eventoId, participantes -> {
            currentParticipantsCount = participantes.size();
            tvAforo.setText("Aforo: " + currentParticipantsCount + " / " + aforoMaximo);

            if (creador != null && creador.equals(nomUsuari)) {
                btnUnirse.setVisibility(View.GONE);
                return;
            }
            if (participantes.contains(nomUsuari)) {
                estaApuntado[0] = true;
                btnUnirse.setText("Desapuntarse");
            } else if (currentParticipantsCount >= aforoMaximo) {
                btnUnirse.setText("Concierto lleno");
                btnUnirse.setEnabled(false);
            } else {
                estaApuntado[0] = false;
                btnUnirse.setText("Unirse al evento");
            }
        }, e -> Log.e("EventDetail", "Error", e));

        btnUnirse.setOnClickListener(v -> {
            if (estaApuntado[0]) {
                repo.desunirse(eventoId, nomUsuari, () -> {
                    estaApuntado[0] = false;
                    btnUnirse.setText("Unirse al evento");
                    currentParticipantsCount--;
                    tvAforo.setText("Aforo: " + currentParticipantsCount + " / " + aforoMaximo);
                    if (currentParticipantsCount < aforoMaximo) {
                        btnUnirse.setEnabled(true);
                    }
                }, e -> {});
            } else {
                repo.unirse(eventoId, nomUsuari, () -> {
                    estaApuntado[0] = true;
                    btnUnirse.setText("Desapuntarse");
                    currentParticipantsCount++;
                    tvAforo.setText("Aforo: " + currentParticipantsCount + " / " + aforoMaximo);
                    if (currentParticipantsCount >= aforoMaximo) {
                        btnUnirse.setEnabled(false);
                        btnUnirse.setText("Concierto lleno");
                    }
                    new FirestoreNotificacioRepository().enviarNotificacio(creador, "Nuevo assistente!", nomUsuari + " se ha unido a: " + titulo);
                }, e -> {});
            }
        });

        // Botons extres (Participants i Ressenyes)
        Button btnParticipantes = findViewById(R.id.btnParticipantes);
        btnParticipantes.setVisibility((creador != null && creador.equals(nomUsuari)) ? View.VISIBLE : View.GONE);
        btnParticipantes.setOnClickListener(v -> {
            Intent i = new Intent(this, EventParticipantsActivity.class);
            i.putExtra("EVENT_ID", eventoId);
            startActivity(i);
        });

        Button btnResenas = findViewById(R.id.btnResenas);
        btnResenas.setVisibility(View.VISIBLE); // Ho poso visible per a tothom per poder-les llegir
        btnResenas.setOnClickListener(v -> {
            Intent i = new Intent(this, ResenasActivity.class);
            i.putExtra("eventoId", eventoId);
            i.putExtra("titulo", titulo);
            startActivity(i);
        });
    }

    // Mètode per actualitzar visualment el botó de subscripció
    private void actualitzarInterficieSub(Button btn) {
        if (estaSubscrit) {
            btn.setText("Suscrito");
            btn.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.btn_star_big_on, 0, 0, 0);
        } else {
            btn.setText("Suscribirse");
            btn.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.btn_star_big_off, 0, 0, 0);
        }
    }
}