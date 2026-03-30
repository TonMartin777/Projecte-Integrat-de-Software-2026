package edu.ub.pis2526.projecte;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import edu.ub.pis2526.projecte.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

  private ActivityMainBinding binding;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = ActivityMainBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());
    RecyclerView recyclerView = findViewById(R.id.recyclerEvents);
    recyclerView.setLayoutManager(new LinearLayoutManager(this));

    List<Event> eventos = new ArrayList<>();
    User pruebas = new User("pruebas");

    eventos.add(new Event(
            "Concierto de Rock",
            "Concierto de rock en vivo en el Palau Sant Jordi.",
            LocalDateTime.of(2026, 6, 12, 21, 0),
            "Carrer de Mallorca 401, Barcelona",
            pruebas, this
    ));
    eventos.get(0).setFoto("https://picsum.photos/seed/rock/400/300");

    eventos.add(new Event(
            "Festival de Jazz",
            "Festival de jazz internacional en el centro de Madrid.",
            LocalDateTime.of(2026, 7, 5, 19, 30),
            "Plaza Mayor, Madrid",
            pruebas, this
    ));
    eventos.get(1).setFoto("https://picsum.photos/seed/jazz/400/300");

    eventos.add(new Event(
            "Partido de Volley",
            "Partido de voleibol amateur abierto a todos.",
            LocalDateTime.of(2026, 5, 21, 18, 0),
            "Carrer de Pallars 100, Barcelona",
            pruebas, this
    ));
    eventos.get(2).setFoto("https://picsum.photos/seed/volley/400/300");

    eventos.add(new Event(
            "Ruta de Senderismo",
            "Ruta de senderismo por la montaña de Montserrat.",
            LocalDateTime.of(2026, 4, 18, 9, 0),
            "Montserrat, Barcelona",
            pruebas, this
    ));
    eventos.get(3).setFoto("https://picsum.photos/seed/hiking/400/300");

    eventos.add(new Event(
            "Exposición de Arte",
            "Exposición de arte contemporáneo en el MACBA.",
            LocalDateTime.of(2026, 6, 1, 11, 0),
            "Plaça dels Àngels 1, Barcelona",
            pruebas, this
    ));
    eventos.get(4).setFoto("https://picsum.photos/seed/art/400/300");

    eventos.add(new Event(
            "Torneo de Ajedrez",
            "Torneo de ajedrez abierto para todos los niveles.",
            LocalDateTime.of(2026, 5, 10, 16, 0),
            "Carrer de Balmes 50, Barcelona",
            pruebas, this
    ));
    eventos.get(5).setFoto("https://picsum.photos/seed/chess/400/300");

    eventos.add(new Event(
            "Concierto de Flamenco",
            "Espectáculo de flamenco en directo en el Tablao.",
            LocalDateTime.of(2026, 7, 20, 22, 0),
            "Carrer dels Flassaders 40, Barcelona",
            pruebas, this
    ));
    eventos.get(6).setFoto("https://picsum.photos/seed/flamenco/400/300");

    eventos.add(new Event(
            "Maratón Popular",
            "Carrera popular de 10km por el centro de la ciudad.",
            LocalDateTime.of(2026, 4, 26, 8, 30),
            "Passeig de Gràcia, Barcelona",
            pruebas, this
    ));
    eventos.get(7).setFoto("https://picsum.photos/seed/marathon/400/300");

    eventos.add(new Event(
            "Taller de Cocina",
            "Aprende a cocinar platos mediterráneos con chefs locales.",
            LocalDateTime.of(2026, 5, 15, 17, 0),
            "Mercat de la Boqueria, Barcelona",
            pruebas, this
    ));
    eventos.get(8).setFoto("https://picsum.photos/seed/cooking/400/300");

    eventos.add(new Event(
            "Noche de Cine",
            "Proyección de películas clásicas al aire libre.",
            LocalDateTime.of(2026, 8, 3, 21, 30),
            "Parc de la Ciutadella, Barcelona",
            pruebas, this
    ));
    eventos.get(9).setFoto("https://picsum.photos/seed/cinema/400/300");

    // Boton User
    ImageButton userButton = findViewById(R.id.userButton);
    userButton.setOnClickListener(v -> {
      Intent intent = new Intent(this, UserActivity.class);
      intent.putExtra("NOM_USUARI",    getIntent().getStringExtra("NOM_USUARI"));
      intent.putExtra("CORREO_USUARI", getIntent().getStringExtra("CORREO_USUARI"));
      startActivity(intent);
    });

    EventAdapter adapter = new EventAdapter(eventos);
    recyclerView.setAdapter(adapter);
  }
}