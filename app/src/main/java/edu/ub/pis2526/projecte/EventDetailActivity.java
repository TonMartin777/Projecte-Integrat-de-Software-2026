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

public class EventDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        String titulo = getIntent().getStringExtra("titulo");
        String descripcion = getIntent().getStringExtra("descripcion");
        String fecha = getIntent().getStringExtra("fecha");
        String hora = getIntent().getStringExtra("hora");
        String foto = getIntent().getStringExtra("foto");
        int aforoMaximo = getIntent().getIntExtra("aforoMaximo", 0);
        double lat = getIntent().getDoubleExtra("lat", 0);
        double lng = getIntent().getDoubleExtra("lng", 0);
        String creador = getIntent().getStringExtra("creador");
        String genero = getIntent().getStringExtra("genero");


        TextView tvCreador = findViewById(R.id.detailCreador);
        TextView tvNombre = findViewById(R.id.detailNombre);
        TextView tvFecha = findViewById(R.id.detailFecha);
        TextView tvDescripcion = findViewById(R.id.detailDescripcion);
        TextView tvHora = findViewById(R.id.detailHora);
        TextView tvUbicacion = findViewById(R.id.detailUbicacion);
        ImageView imgEvento = findViewById(R.id.detailImagen);
        TextView tvGenero = findViewById(R.id.detailGenero);
        TextView tvAforo = findViewById(R.id.detailAforo);

        tvNombre.setText(titulo);
        tvFecha.setText(fecha);
        tvDescripcion.setText(descripcion);
        tvHora.setText(hora);
        tvUbicacion.setText("Lat: " + lat + ", Lng: " + lng);
        tvCreador.setText("Creado por: " + creador);
        tvGenero.setText("Género: " + (genero != null && !genero.isEmpty() ? genero : "No especificado"));

        Glide.with(this)
                .load(foto)
                .into(imgEvento);

        Button btnAbrirMapa = findViewById(R.id.btnAbrirMapa);
        btnAbrirMapa.setOnClickListener(v -> {
            String mapsUrl = getIntent().getStringExtra("maps_url");

            if (mapsUrl != null && !mapsUrl.isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mapsUrl));
                startActivity(intent);
            } else {
                if (lat != 0 && lng != 0) {
                    Uri gmmIntentUri = Uri.parse("geo:" + lat + "," + lng + "?q=" + lat + "," + lng + "(" + titulo + ")");
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    startActivity(mapIntent);
                } else {
                    Toast.makeText(this, "Ubicación no disponible", Toast.LENGTH_SHORT).show();
                }
            }
        });

        String eventoId = getIntent().getStringExtra("eventoId");
        String nomUsuari = getIntent().getStringExtra("NOM_USUARI");

        Button btnUnirse = findViewById(R.id.btnUnirse);
        FirestoreEventRepository repo = new FirestoreEventRepository();

        // Cargar participantes para mostrar estado inicial
        repo.getParticipantes(eventoId,
                participantes -> {
                    int numActual = participantes.size();
                    tvAforo.setText("Aforo: " + numActual + " / " + aforoMaximo);

                    if (participantes.contains(nomUsuari)) {
                        btnUnirse.setText("Ya te has unido");
                        btnUnirse.setEnabled(false);
                    } else if (numActual >= aforoMaximo) {
                        btnUnirse.setText("Concierto lleno");
                        btnUnirse.setEnabled(false);
                    }
                },
                e -> Log.e("EventDetail", "Error cargando participantes", e)
        );

        // BOTÓN UNIRSE - UNICA llamada a unirse
        btnUnirse.setOnClickListener(v -> {
            repo.unirse(eventoId, nomUsuari,
                    () -> {
                        // Recargar participantes para actualizar UI
                        repo.getParticipantes(eventoId, participantes -> {
                            int numActual = participantes.size();
                            tvAforo.setText("Aforo: " + numActual + " / " + aforoMaximo);
                            btnUnirse.setText("Ya te has unido");
                            btnUnirse.setEnabled(false);
                        }, e -> Log.e("EventDetail", "Error recargando participantes", e));

                        // Enviar notificación
                        FirestoreNotificacioRepository notiRepo = new FirestoreNotificacioRepository();
                        notiRepo.enviarNotificacio(creador, "Nou assistent!", nomUsuari + " s'ha unit a: " + titulo);
                    },
                    e -> Toast.makeText(this, "Error al unirse: " + e.getMessage(), Toast.LENGTH_SHORT).show()
            );
        });

        // Mostrar botón de participantes solo si el usuario actual es el creador
        Button btnParticipantes = findViewById(R.id.btnParticipantes);
        if (creador != null && creador.equals(nomUsuari)) {
            btnParticipantes.setVisibility(View.VISIBLE);
            btnParticipantes.setOnClickListener(v -> {
                Intent intent = new Intent(EventDetailActivity.this, EventParticipantsActivity.class);
                intent.putExtra("EVENT_ID", eventoId);
                startActivity(intent);
            });
        } else {
            btnParticipantes.setVisibility(View.GONE);
        }
    }
}