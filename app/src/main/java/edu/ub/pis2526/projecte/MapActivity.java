package edu.ub.pis2526.projecte;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.List;

import edu.ub.pis2526.projecte.data.repositories.firestore.FirestoreEventRepository;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private FirestoreEventRepository eventRepository;
    private List<Event> allEvents;
    private int currentRangeKm = 10; // valor por defecto

    private TextView txtRange;
    private SeekBar rangeSeekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        txtRange = findViewById(R.id.txtRange);
        rangeSeekBar = findViewById(R.id.rangeSeekBar);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        eventRepository = new FirestoreEventRepository();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);

        rangeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentRangeKm = progress;
                txtRange.setText(progress + " km");
                if (mMap != null) {
                    updateMapMarkers();
                }
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Pedir permisos si no los tenemos
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            getCurrentLocationAndLoadEvents();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
                getCurrentLocationAndLoadEvents();
            }
        }
    }

    private void getCurrentLocationAndLoadEvents() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) return;

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 12f));
                        loadEventsAndShowMarkers(location);
                    } else {
                        Toast.makeText(this, "No se pudo obtener la ubicación", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadEventsAndShowMarkers(Location userLocation) {
        eventRepository.getAll(
                events -> {
                    allEvents = events;
                    updateMapMarkers();
                },
                e -> Toast.makeText(this, "Error cargando eventos", Toast.LENGTH_SHORT).show()
        );
    }

    private void updateMapMarkers() {
        if (mMap == null || allEvents == null) return;
        mMap.clear();

        // Obtener ubicación actual para cálculos
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) return;

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location == null) return;

            LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());

            for (Event event : allEvents) {
                double[] coords = event.getCoordenadas();
                if (coords == null) continue;

                LatLng eventLatLng = new LatLng(coords[0], coords[1]);
                float distance = distanceBetween(userLatLng, eventLatLng);

                if (distance <= currentRangeKm) {
                    Marker marker = mMap.addMarker(new MarkerOptions()
                            .position(eventLatLng)
                            .title(event.getTitulo())
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                    marker.setTag(event); // Para recuperar el evento al hacer clic
                }
            }

            mMap.setOnMarkerClickListener(marker -> {
                Event event = (Event) marker.getTag();
                if (event != null) {
                    Intent intent = new Intent(MapActivity.this, EventDetailActivity.class);
                    intent.putExtra("titulo", event.getTitulo());
                    intent.putExtra("descripcion", event.getDescripcion());
                    intent.putExtra("fecha", event.getFechaHora().toLocalDate().toString());
                    intent.putExtra("hora", event.getFechaHora().toLocalTime().toString());
                    intent.putExtra("foto", event.getFoto());
                    intent.putExtra("creador", event.getCreador().getNom());
                    if (event.getCoordenadas() != null) {
                        intent.putExtra("lat", event.getCoordenadas()[0]);
                        intent.putExtra("lng", event.getCoordenadas()[1]);
                    }
                    String mapsUrl = event.getLinkGoogleMapsString();
                    if (mapsUrl != null) {
                        intent.putExtra("maps_url", mapsUrl);
                    }
                    startActivity(intent);
                }
                return true;
            });
        });
    }

    // Método para calcular distancia en km entre dos LatLng
    private float distanceBetween(LatLng start, LatLng end) {
        float[] results = new float[1];
        Location.distanceBetween(start.latitude, start.longitude,
                end.latitude, end.longitude, results);
        return results[0] / 1000; // a km
    }
}