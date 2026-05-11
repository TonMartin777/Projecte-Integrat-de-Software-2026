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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        String titulo      = getIntent().getStringExtra("titulo");
        String descripcion = getIntent().getStringExtra("descripcion");
        String fecha       = getIntent().getStringExtra("fecha");
        String hora        = getIntent().getStringExtra("hora");
        String foto        = getIntent().getStringExtra("foto");
        int aforoMaximo    = getIntent().getIntExtra("aforoMaximo", 0);
        double lat         = getIntent().getDoubleExtra("lat", 0);
        double lng         = getIntent().getDoubleExtra("lng", 0);
        String creador     = getIntent().getStringExtra("creador");
        String genero      = getIntent().getStringExtra("genero");
        String rol         = getIntent().getStringExtra("ROL");
        String eventoId    = getIntent().getStringExtra("eventoId");
        String nomUsuari   = getIntent().getStringExtra("NOM_USUARI");

        // ── VINCULAR VISTES ─────────────────────────────────────────────
        TextView tvCreador     = findViewById(R.id.detailCreador);
        TextView tvNombre      = findViewById(R.id.detailNombre);
        TextView tvFecha       = findViewById(R.id.detailFecha);
        TextView tvDescripcion = findViewById(R.id.detailDescripcion);
        TextView tvHora        = findViewById(R.id.detailHora);
        TextView tvUbicacion   = findViewById(R.id.detailUbicacion);
        ImageView imgEvento    = findViewById(R.id.detailImagen);
        TextView tvGenero      = findViewById(R.id.detailGenero);
        TextView tvAforo       = findViewById(R.id.detailAforo);

        tvNombre.setText(titulo);
        tvFecha.setText(fecha);
        tvDescripcion.setText(descripcion);
        tvHora.setText(hora);
        tvUbicacion.setText("Lat: " + lat + ", Lng: " + lng);
        tvCreador.setText("Creado por: " + creador);
        tvGenero.setText("Género: " + (genero != null && !genero.isEmpty() ? genero : "No especificado"));

        Glide.with(this).load(foto).into(imgEvento);

        // ── MAPA ─────────────────────────────────────────────────────────
        Button btnAbrirMapa = findViewById(R.id.btnAbrirMapa);
        btnAbrirMapa.setOnClickListener(v -> {
            String mapsUrl = getIntent().getStringExtra("maps_url");
            if (mapsUrl != null && !mapsUrl.isEmpty()) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(mapsUrl)));
            } else if (lat != 0 && lng != 0) {
                Uri gmmIntentUri = Uri.parse("geo:" + lat + "," + lng + "?q=" + lat + "," + lng + "(" + titulo + ")");
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
            } else {
                Toast.makeText(this, "Ubicación no disponible", Toast.LENGTH_SHORT).show();
            }
        });

        // ── UNIRSE / DESAPUNTARSE ────────────────────────────────────────
        Button btnUnirse = findViewById(R.id.btnUnirse);
        FirestoreEventRepository repo = new FirestoreEventRepository();
        final boolean[] estaApuntado = {false};

        repo.getParticipantes(eventoId, participantes -> {
            int numActual = participantes.size();
            tvAforo.setText("Aforo: " + numActual + " / " + aforoMaximo);

            if (creador != null && creador.equals(nomUsuari)) {
                btnUnirse.setVisibility(View.GONE);
                return;
            }
            if (participantes.contains(nomUsuari)) {
                estaApuntado[0] = true;
                btnUnirse.setText("Desapuntarse");
                btnUnirse.setEnabled(true);
            } else if (numActual >= aforoMaximo) {
                btnUnirse.setText("Concierto lleno");
                btnUnirse.setEnabled(false);
            } else {
                estaApuntado[0] = false;
                btnUnirse.setText("Unirse al evento");
                btnUnirse.setEnabled(true);
            }
        }, e -> Log.e("EventDetail", "Error cargando participantes", e));

        btnUnirse.setOnClickListener(v -> {
            if (estaApuntado[0]) {
                repo.desunirse(eventoId, nomUsuari,
                        () -> repo.getParticipantes(eventoId, nous -> {
                            tvAforo.setText("Aforo: " + nous.size() + " / " + aforoMaximo);
                            estaApuntado[0] = false;
                            btnUnirse.setText("Unirse al evento");
                            btnUnirse.setEnabled(true);
                            Toast.makeText(this, "Te has desapuntado", Toast.LENGTH_SHORT).show();
                        }, e2 -> Log.e("EventDetail", "Error recargando", e2)),
                        e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            } else {
                repo.unirse(eventoId, nomUsuari,
                        () -> repo.getParticipantes(eventoId, nous -> {
                            tvAforo.setText("Aforo: " + nous.size() + " / " + aforoMaximo);
                            estaApuntado[0] = true;
                            btnUnirse.setText("Desapuntarse");
                            FirestoreNotificacioRepository notiRepo = new FirestoreNotificacioRepository();
                            notiRepo.enviarNotificacio(creador, "Nou assistent!", nomUsuari + " s'ha unit a: " + titulo);
                            notiRepo.enviarNotificacio(nomUsuari, "Recordatori ⏰", "Recorda que demà és l'esdeveniment: " + titulo);
                        }, e2 -> Log.e("EventDetail", "Error recargando", e2)),
                        e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }
        });

// ── SUBSCRIPCIÓ AL CREADOR ───────────────────────────────────────
        Button btnSubscriure = findViewById(R.id.btnSubscriure);
        FirestoreUserRepository userRepo = new FirestoreUserRepository();

        if ("asistente".equals(rol) && nomUsuari != null
                && creador != null && !creador.equals(nomUsuari)) {

            btnSubscriure.setVisibility(View.VISIBLE);
            final boolean[] estaSubscrit = {false};

            userRepo.comprovarSubscripcio(nomUsuari, creador, subscrit -> {
                estaSubscrit[0] = subscrit;
                runOnUiThread(() -> btnSubscriure.setText(subscrit
                        ? "Cancel·lar subscripció"
                        : "Subscriure's a " + creador));
            });

            btnSubscriure.setOnClickListener(v -> {
                if (estaSubscrit[0]) {
                    userRepo.desSubscriure(nomUsuari, creador,
                            new FirestoreUserRepository.OnSubscripcioListener() {
                                @Override
                                public void onSuccess() {
                                    estaSubscrit[0] = false;
                                    btnSubscriure.setText("Subscriure's a " + creador);
                                    Toast.makeText(EventDetailActivity.this,
                                            "Subscripció cancel·lada", Toast.LENGTH_SHORT).show();
                                }
                                @Override
                                public void onError(Exception e) {
                                    Toast.makeText(EventDetailActivity.this,
                                            "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                    );
                } else {
                    userRepo.subscriure(nomUsuari, creador,
                            new FirestoreUserRepository.OnSubscripcioListener() {
                                @Override
                                public void onSuccess() {
                                    estaSubscrit[0] = true;
                                    btnSubscriure.setText("Cancel·lar subscripció");
                                    Toast.makeText(EventDetailActivity.this,
                                            "Subscrit a " + creador + "!", Toast.LENGTH_SHORT).show();
                                }
                                @Override
                                public void onError(Exception e) {
                                    Toast.makeText(EventDetailActivity.this,
                                            "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                    );
                }
            });
        }

        // PARTICIPANTES (només creador)
        Button btnParticipantes = findViewById(R.id.btnParticipantes);
        if (creador != null && creador.equals(nomUsuari)) {
            btnParticipantes.setVisibility(View.VISIBLE);
            btnParticipantes.setOnClickListener(v -> {
                Intent intent = new Intent(this, EventParticipantsActivity.class);
                intent.putExtra("EVENT_ID", eventoId);
                startActivity(intent);
            });
        } else {
            btnParticipantes.setVisibility(View.GONE);
        }

        // Amaguem btnUnirse si és banda
        if ("banda".equals(rol) || (creador != null && creador.equals(nomUsuari))) {
            btnUnirse.setVisibility(View.GONE);
        }

        // RESSENYES (nomes creador)
        Button btnResenas = findViewById(R.id.btnResenas);
        if (creador != null && creador.equals(nomUsuari)) {
            btnResenas.setVisibility(View.VISIBLE);
            btnResenas.setOnClickListener(v -> {
                Intent intent = new Intent(this, ResenasActivity.class);
                intent.putExtra("eventoId", eventoId);
                intent.putExtra("titulo", titulo);
                startActivity(intent);
            });
        } else {
            btnResenas.setVisibility(View.GONE);
        }
    }
}