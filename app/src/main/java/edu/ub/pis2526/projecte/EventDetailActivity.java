package edu.ub.pis2526.projecte;
import com.bumptech.glide.Glide;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import edu.ub.pis2526.projecte.data.repositories.firestore.FirestoreEventRepository;

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
        double lat = getIntent().getDoubleExtra("lat", 0);
        double lng = getIntent().getDoubleExtra("lng", 0);
        String creador = getIntent().getStringExtra("creador");

        TextView tvCreador = findViewById(R.id.detailCreador);
        TextView tvNombre = findViewById(R.id.detailNombre);
        TextView tvFecha = findViewById(R.id.detailFecha);
        TextView tvDescripcion = findViewById(R.id.detailDescripcion);
        TextView tvHora = findViewById(R.id.detailHora);
        TextView tvUbicacion = findViewById(R.id.detailUbicacion);
        ImageView imgEvento = findViewById(R.id.detailImagen);

        tvNombre.setText(titulo);
        tvFecha.setText(fecha);
        tvDescripcion.setText(descripcion);
        tvHora.setText(hora);
        tvUbicacion.setText("Lat: " + lat + ", Lng: " + lng);
        tvCreador.setText("Creado por: " + creador);

        Glide.with(this)
                .load(foto)
                .into(imgEvento);


        Button btnAbrirMapa = findViewById(R.id.btnAbrirMapa);
        btnAbrirMapa.setOnClickListener(v -> {
            String mapsUrl = getIntent().getStringExtra("maps_url");

            // Si tenemos una URL guardada (hardcodeada o generada), la usamos primero
            if (mapsUrl != null && !mapsUrl.isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mapsUrl));
                startActivity(intent);
            } else {
                // Si no, intentamos con coordenadas
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


        String eventoId  = getIntent().getStringExtra("eventoId");
        String nomUsuari = getIntent().getStringExtra("NOM_USUARI");

        TextView tvParticipantes = findViewById(R.id.detailParticipantes);
        Button btnUnirse = findViewById(R.id.btnUnirse);

        FirestoreEventRepository repo = new FirestoreEventRepository();

        repo.getParticipantes(eventoId,
                participantes -> {
                    tvParticipantes.setText("Participantes: " + participantes.size());
                    if (participantes.contains(nomUsuari)) {
                        btnUnirse.setText("Ya te has unido");
                        btnUnirse.setEnabled(false);
                    }
                },
                e -> Log.e("EventDetail", "Error cargando participantes", e)
        );

        btnUnirse.setOnClickListener(v -> {
            repo.unirse(eventoId, nomUsuari,
                    () -> {
                        int actual = Integer.parseInt(
                                tvParticipantes.getText().toString().replaceAll("[^0-9]", "")
                        );
                        tvParticipantes.setText("Participantes: " + (actual + 1));
                        btnUnirse.setText("Ya te has unido");
                        btnUnirse.setEnabled(false);
                    },
                    e -> Toast.makeText(this, "Error al unirse", Toast.LENGTH_SHORT).show()
            );
        });
    }
}