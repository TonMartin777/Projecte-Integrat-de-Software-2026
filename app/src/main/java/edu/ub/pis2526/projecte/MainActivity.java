package edu.ub.pis2526.projecte;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.firestore.DocumentSnapshot;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
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
  private double userLat = 0;
  private double userLng = 0;
  private DocumentSnapshot ultimoDocumento = null;
  private boolean cargando = false;
  private boolean hayMasPaginas = true;

  // Para los filtros
  private String lastSearchText = "";
  private boolean filtrosAplicados = false;
  private List<Event> lastFilteredList = null;

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
    String rol = getIntent().getStringExtra("ROL");
    adapter = new EventAdapter(listaEventos, nomUsuari, rol);
    recyclerView.setAdapter(adapter);
    recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
      @Override
      public void onScrolled(RecyclerView rv, int dx, int dy) {
        LinearLayoutManager lm = (LinearLayoutManager) rv.getLayoutManager();
        int totalItems = lm.getItemCount();
        int ultimoVisible = lm.findLastVisibleItemPosition();
        // Si quedan 5 o menos items para el final, carga la siguiente página
        if (!cargando && hayMasPaginas && ultimoVisible >= totalItems - 5) {
          cargarSiguientePagina();
        }
      }
    });

    recyclerView.getLayoutManager().setMeasurementCacheEnabled(false); // opcional
    recyclerView.setVisibility(View.VISIBLE);
    recyclerView.post(() -> {
      Log.d("UserActivity", "RecyclerView height: " + recyclerView.getHeight());
      Log.d("UserActivity", "RecyclerView visibility: " + recyclerView.getVisibility());
    });

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
              userLat,
              userLng,
              eventosFiltrados -> {
                if (eventosFiltrados.size() == todosLosEventos.size()) {
                  filtrosAplicados = false;
                  lastFilteredList = null;
                } else {
                  lastFilteredList = new ArrayList<>(eventosFiltrados);
                  filtrosAplicados = true;
                }
                adapter.actualizarLista(eventosFiltrados);
              }
      );
      bottomSheet.show(getSupportFragmentManager(), "filtro");
    });

    ImageButton btnCampaneta = findViewById(R.id.campaneta);
    btnCampaneta.setOnClickListener(v -> {
      Intent intent = new Intent(this, NotificacionsActivity.class);
      // Passem el nom de l'usuari perquè NotificacionsActivity sàpiga quins avisos buscar a Firestore
      intent.putExtra("NOM_USUARI", getIntent().getStringExtra("NOM_USUARI"));
      startActivity(intent);
    });

    //  Botó d'usuari
    ImageButton userButton = findViewById(R.id.userButton);
    userButton.setOnClickListener(v -> {
      String rol1 = getIntent().getStringExtra("ROL");
      Intent intent;
      intent = new Intent(this, FanProfileActivity.class);

      intent.putExtra("NOM_USUARI", getIntent().getStringExtra("NOM_USUARI"));
      intent.putExtra("CORREO_USUARI", getIntent().getStringExtra("CORREO_USUARI"));
      intent.putExtra("ROL", rol1);
      startActivity(intent);
    });

    // Button map

    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(this,
              new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
    } else {
      obtenerUbicacionUsuario();
    }
    obtenerUbicacionUsuario();
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (requestCode == 100 && grantResults.length > 0
            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
      obtenerUbicacionUsuario();
    } else {
      Log.e("MainActivity", "Permiso denegado por el usuario");
    }
  }

  // Mètode separat per carregar/actualitzar
  private void cargarEventos() {
    repo.eliminarEventosCaducados(
            () -> {
              // Resetear paginación
              ultimoDocumento = null;
              hayMasPaginas = true;
              todosLosEventos.clear();
              filtrosAplicados = false;
              lastFilteredList = null;
              cargarSiguientePagina();
            },
            e -> Log.e("MainActivity", "Error eliminando eventos caducados", e)
    );
  }

  @Override
  protected void onResume() {
    super.onResume();
    cargarEventos();
  }
  private void obtenerUbicacionForzada(FusedLocationProviderClient fusedClient) {
    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) return;

    com.google.android.gms.location.LocationRequest locationRequest =
            com.google.android.gms.location.LocationRequest.create()
                    .setPriority(com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setNumUpdates(1)
                    .setInterval(0);

    fusedClient.requestLocationUpdates(
            locationRequest,
            new com.google.android.gms.location.LocationCallback() {
              @Override
              public void onLocationResult(com.google.android.gms.location.LocationResult result) {
                if (result != null && result.getLastLocation() != null) {
                  userLat = result.getLastLocation().getLatitude();
                  userLng = result.getLastLocation().getLongitude();
                  Log.d("MainActivity", "Ubicación forzada: " + userLat + ", " + userLng);
                  fusedClient.removeLocationUpdates(this);
                }
              }
            },
            getMainLooper()
    );
  }

  private void obtenerUbicacionUsuario() {
    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
      FusedLocationProviderClient fusedClient =
              LocationServices.getFusedLocationProviderClient(this);
      fusedClient.getLastLocation().addOnSuccessListener(location -> {
        if (location != null) {
          userLat = location.getLatitude();
          userLng = location.getLongitude();
          Log.d("MainActivity", "Ubicación obtenida: " + userLat + ", " + userLng);
        } else {
          Log.e("MainActivity", "getLastLocation devolvió null");
          // Forzar una petición de ubicación actual
          obtenerUbicacionForzada(fusedClient);
        }
      });
    } else {
      Log.e("MainActivity", "Permiso de ubicación no concedido");
    }
  }

  private void cargarSiguientePagina() {
    if (cargando || !hayMasPaginas) return;
    cargando = true;

    repo.getPage(ultimoDocumento,
            (eventos, nuevoUltimoDoc, hayMas) -> {
              ultimoDocumento = nuevoUltimoDoc;
              hayMasPaginas = hayMas;
              cargando = false;
              todosLosEventos.addAll(eventos);
              if (!filtrosAplicados) {
                adapter.actualizarLista(todosLosEventos);
              }
            },
            e -> {
              cargando = false;
              Log.e("MainActivity", "Error cargando página", e);
            }
    );
  }
}