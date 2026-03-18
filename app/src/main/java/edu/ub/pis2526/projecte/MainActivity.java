package edu.ub.pis2526.projecte;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
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
    eventos.add(new Event("Concierto de Rock", "15/04/2026", "Barcelona", "Un gran concierto de rock en vivo", "Música", "20:00", "https://picsum.photos/400/300"));
    eventos.add(new Event("Partido Futbol", "23/08/2026", "Madrid", "Partido de futbol 11 al aire libre", "Deporte", "20:00", "https://fastly.picsum.photos/id/10/400/300.jpg"));
    eventos.add(new Event("Partida de Rol", "10/05/2026", "Barcelona", "Partida de D&D hasta 4 personas", "Rol", "17:00", "https://m.media-amazon.com/images/I/81RFxSAOe2L._AC_UF1000,1000_QL80_.jpg"));

    EventAdapter adapter = new EventAdapter(eventos);
    recyclerView.setAdapter(adapter);
  }
}