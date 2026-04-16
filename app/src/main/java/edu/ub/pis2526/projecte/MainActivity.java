package edu.ub.pis2526.projecte;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import edu.ub.pis2526.projecte.data.repositories.firestore.FirestoreEventRepository;
import edu.ub.pis2526.projecte.databinding.ActivityMainBinding;
import androidx.appcompat.widget.SearchView;

public class MainActivity extends AppCompatActivity {

  private ActivityMainBinding binding;
  private EventAdapter adapter;
  private FirestoreEventRepository repo;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = ActivityMainBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());
    RecyclerView recyclerView = findViewById(R.id.recyclerEvents);
    recyclerView.setLayoutManager(new LinearLayoutManager(this));

// Adaptador con lista vacía inicial
    repo = new FirestoreEventRepository();

    List<Event> eventos = new ArrayList<>();
    adapter = new EventAdapter(eventos);
    recyclerView.setAdapter(adapter);

// Cargar desde Firestore
    FirestoreEventRepository repo = new FirestoreEventRepository();
    repo.getAll(
            eventosFirestore -> {
              adapter.actualizarLista(eventosFirestore); // NUEVO — reemplaza las dos líneas anteriores
            },
            e -> Log.e("MainActivity", "Error cargando eventos", e)
    );

    SearchView searchView = findViewById(R.id.searchEvent);
    searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
      @Override
      public boolean onQueryTextSubmit(String query) {
        return false;
      }

      @Override
      public boolean onQueryTextChange(String newText) {
        adapter.filtrar(newText);
        return true;
      }
    });


    // Button User
    ImageButton userButton = findViewById(R.id.userButton);
    userButton.setOnClickListener(v -> {
      Intent intent = new Intent(this, UserActivity.class);
      intent.putExtra("NOM_USUARI",    getIntent().getStringExtra("NOM_USUARI"));
      intent.putExtra("CORREO_USUARI", getIntent().getStringExtra("CORREO_USUARI"));
      startActivity(intent);
    });

    // Button map
    ImageButton mapButton = findViewById(R.id.mapButton);
    mapButton.setOnClickListener(v -> {
      startActivity(new Intent(this, MapActivity.class));
    });
  }

  @Override
  protected void onResume() {
    super.onResume();
    repo.getAll(
            eventosFirestore -> adapter.actualizarLista(eventosFirestore),
            e -> Log.e("MainActivity", "Error cargando eventos", e)
    );
  }
}