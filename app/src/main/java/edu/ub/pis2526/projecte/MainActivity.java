package edu.ub.pis2526.projecte;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;

import java.util.ArrayList;
import java.util.List;

import edu.ub.pis2526.projecte.data.repositories.firestore.FirestoreEventRepository;
import edu.ub.pis2526.projecte.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

  private ActivityMainBinding binding;
  private EventAdapter adapter;
  private FirestoreEventRepository repo;
  private List<Event> todosLosEventos = new ArrayList<>();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    binding = ActivityMainBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());

    repo = new FirestoreEventRepository();

    //  Configuració del RecyclerView
    RecyclerView recyclerView = findViewById(R.id.recyclerEvents);
    recyclerView.setLayoutManager(new LinearLayoutManager(this));

    List<Event> listaEventos = new ArrayList<>();
    String nomUsuari = getIntent().getStringExtra("NOM_USUARI");
    adapter = new EventAdapter(listaEventos, nomUsuari);
    recyclerView.setAdapter(adapter);


    cargarEventos();

    //  Configuració del Cercador (SearchView)
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

    // Boton de filtrar
    ImageButton btnFiltrar = findViewById(R.id.btnFiltrar);
    btnFiltrar.setOnClickListener(v -> {
      FiltroBottomSheet bottomSheet = new FiltroBottomSheet(
              todosLosEventos,
              eventosFiltrados -> adapter.actualizarLista(eventosFiltrados)
      );
      bottomSheet.show(getSupportFragmentManager(), "filtro");
    });

    //  Botó d'usuari
    ImageButton userButton = findViewById(R.id.userButton);
    userButton.setOnClickListener(v -> {
      Intent intent = new Intent(this, UserActivity.class);
      // Passem els strings que van arribar del Login
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

  // Mètode separat per carregar/actualitzar
  private void cargarEventos() {
    repo.eliminarEventosCaducados(
            () -> repo.getAll(
                    eventosFirestore -> {
                      todosLosEventos = new ArrayList<>(eventosFirestore);
                      adapter.actualizarLista(eventosFirestore);
                    },
                    e -> Log.e("MainActivity", "Error cargando eventos", e)
            ),
            e -> Log.e("MainActivity", "Error eliminando eventos caducados", e)
    );
  }

  @Override
  protected void onResume() {
    super.onResume();
    // Quan tornem a la pantalla (ex: després de crear un event), refresquem la llista
    cargarEventos();
  }
}