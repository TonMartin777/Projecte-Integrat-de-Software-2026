package edu.ub.pis2526.projecte;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CreateEventActivity extends AppCompatActivity {

    private EditText editTitulo, editDescripcion, editDireccion;
    private Button btnFecha, btnCrear;
    private LocalDateTime fechaHoraSeleccionada;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        db = FirebaseFirestore.getInstance();

        editTitulo      = findViewById(R.id.editTitulo);
        editDescripcion = findViewById(R.id.editDescripcion);
        editDireccion   = findViewById(R.id.editDireccion);
        btnFecha        = findViewById(R.id.btnFecha);
        btnCrear        = findViewById(R.id.btnCrear);

        btnFecha.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new DatePickerDialog(this, (view, year, month, day) -> {
                new TimePickerDialog(this, (tview, hour, minute) -> {
                    fechaHoraSeleccionada = LocalDateTime.of(year, month + 1, day, hour, minute);
                    btnFecha.setText(day + "/" + (month+1) + "/" + year
                            + " " + hour + ":" + String.format("%02d", minute));
                }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show();
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
        });

        btnCrear.setOnClickListener(v -> {
            String titulo      = editTitulo.getText().toString().trim();
            String descripcion = editDescripcion.getText().toString().trim();
            String direccion   = editDireccion.getText().toString().trim();

            if (titulo.isEmpty() || descripcion.isEmpty() ||
                    direccion.isEmpty() || fechaHoraSeleccionada == null) {
                Toast.makeText(this, "Rellena todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            //  SE CANVIA POR USER USER CUANDO LO TENGAMOS
            User creador = new User("usuario_activo");
            Event evento = new Event(titulo, descripcion, fechaHoraSeleccionada,
                    direccion, creador, this);


            if (!evento.tieneCoordenadas()) {
                // Toast.makeText(this, "Dirección no encontrada...", Toast.LENGTH_LONG).show();
                // return;   <-- comentat fins que reactivem  el Geocoder
            }

            guardarEventoEnFirestore(evento);
        });
    }

    private void guardarEventoEnFirestore(Event evento) {
        User creador = evento.getCreador();

        // Sub-document del creador dins l'event
        Map<String, Object> creadorMap = new HashMap<>();
        creadorMap.put("nom",    creador.getNom());
        creadorMap.put("correo", creador.getCorreo()); // pot ser null, Firestore ho accepta

        // Convertim LocalDateTime a Timestamp de Firestore
        Date fechaDate = Date.from(evento.getFechaHora().toInstant(ZoneOffset.UTC));
        Timestamp fechaTimestamp = new Timestamp(fechaDate);

        // Document principal de l'event
        Map<String, Object> eventoMap = new HashMap<>();
        eventoMap.put("id",           evento.getId());
        eventoMap.put("titulo",       evento.getTitulo());
        eventoMap.put("descripcion",  evento.getDescripcion());
        eventoMap.put("fechaHora",    fechaTimestamp);
        eventoMap.put("foto",         evento.getFoto());         // null si no s'ha posat
        eventoMap.put("categorias",   new ArrayList<>());        // buida de moment
        eventoMap.put("participantes", new ArrayList<>());       // buida de moment
        eventoMap.put("creador",      creadorMap);
        // coordenades desactivades fins que reactivem el Geocoder:
        // eventoMap.put("latitud",  evento.getCoordenadas()[0]);
        // eventoMap.put("longitud", evento.getCoordenadas()[1]);

        // Usem l'ID de l'event (UUID) com a ID del document a Firestore
        db.collection("events")
                .document(evento.getId())
                .set(eventoMap)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Evento creado!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this,
                            "Error al guardar: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }
}