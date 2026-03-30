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

import edu.ub.pis2526.projecte.data.repositories.firestore.FirestoreEventRepository;
import edu.ub.pis2526.projecte.domain.repositories.EventRepository;

public class CreateEventActivity extends AppCompatActivity {

    private EditText editTitulo, editDescripcion, editDireccion;
    private Button btnFecha, btnCrear;
    private LocalDateTime fechaHoraSeleccionada;

    private EventRepository eventRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        eventRepository = new FirestoreEventRepository();

        editTitulo      = findViewById(R.id.editTitulo);
        editDescripcion = findViewById(R.id.editDescripcion);
        editDireccion   = findViewById(R.id.editDireccion);
        btnFecha        = findViewById(R.id.btnFecha);
        btnCrear        = findViewById(R.id.btnCrear);

        btnFecha.setOnClickListener(v -> mostrarSelectorFechaHora());
        btnCrear.setOnClickListener(v -> crearEvento());
    }

    private void mostrarSelectorFechaHora() {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) ->
                new TimePickerDialog(this, (tview, hour, minute) -> {
                    fechaHoraSeleccionada = LocalDateTime.of(year, month + 1, day, hour, minute);
                    btnFecha.setText(day + "/" + (month + 1) + "/" + year
                            + " " + hour + ":" + String.format("%02d", minute));
                }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show(),
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void crearEvento() {
        String titulo      = editTitulo.getText().toString().trim();
        String descripcion = editDescripcion.getText().toString().trim();
        String direccion   = editDireccion.getText().toString().trim();

        if (titulo.isEmpty() || descripcion.isEmpty() ||
                direccion.isEmpty() || fechaHoraSeleccionada == null) {
            Toast.makeText(this, "Rellena todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        String nomUsuari    = getIntent().getStringExtra("NOM_USUARI");
        String correoUsuari = getIntent().getStringExtra("CORREO_USUARI");
        User creador = new User(
                nomUsuari    != null ? nomUsuari    : "usuari_desconegut",
                correoUsuari != null ? correoUsuari : ""
        );
        Event evento = new Event(titulo, descripcion, fechaHoraSeleccionada, direccion, creador, this);

        eventRepository.save(evento,
                () -> {
                    Toast.makeText(this, "Evento creado!", Toast.LENGTH_SHORT).show();
                    finish();
                },
                e -> Toast.makeText(this,
                        "Error al guardar: " + e.getMessage(),
                        Toast.LENGTH_LONG).show()
        );
    }
}