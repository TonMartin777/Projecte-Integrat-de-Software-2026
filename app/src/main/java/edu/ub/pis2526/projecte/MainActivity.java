package edu.ub.pis2526.projecte;

import android.os.Bundle;

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
            "Concierto de rock en barcelona",
            LocalDateTime.of(2026, 6, 12, 21, 0),   // año mes dia hora minuto
            "Carrer de Mallorca 401, Barcelona",
            pruebas,
            this
    ));
    /*
    eventos.add(new Event("Festival de Jazz", "20/05/2026", "Madrid"));
    eventos.add(new Event("Partido de Volley", "21/03/2026", "Barcelona"));
    eventos.add(new Event("Practicar Escalada", "15/03/2026", "Girona"));
    eventos.add(new Event("Concierto Metal", "09/06/2026", "Barcelona"));
    eventos.add(new Event("Evento Cultural", "28/07/2026", "Igualada"));
    eventos.add(new Event("Partida de Rol", "01/08/2026", "Madrid"));
    eventos.add(new Event("Club de Lectura", "16/03/2026", "Tarragona"));
    eventos.add(new Event("Partido de Basquet", "30/05/2026", "Madrid"));
     */

    EventAdapter adapter = new EventAdapter(eventos);
    recyclerView.setAdapter(adapter);
  }
}