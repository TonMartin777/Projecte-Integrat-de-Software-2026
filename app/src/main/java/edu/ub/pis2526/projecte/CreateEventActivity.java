package edu.ub.pis2526.projecte;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.time.LocalDateTime;
import java.util.Calendar;

import edu.ub.pis2526.projecte.data.repositories.firestore.FirestoreEventRepository;
import edu.ub.pis2526.projecte.data.repositories.firestore.FirestoreNotificacioRepository;
import edu.ub.pis2526.projecte.data.repositories.firestore.FirestoreUserRepository;
import edu.ub.pis2526.projecte.domain.repositories.EventRepository;

public class CreateEventActivity extends AppCompatActivity {

    private EditText editTitulo, editDescripcion, editLatitud, editLongitud, editFoto, editAforo;
    private Button btnFecha, btnCrear;
    private Spinner spinnerGenero;
    private LocalDateTime fechaHoraSeleccionada;
    private EventRepository eventRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        eventRepository = new FirestoreEventRepository();

        editTitulo      = findViewById(R.id.editTitulo);
        editDescripcion = findViewById(R.id.editDescripcion);
        editLatitud     = findViewById(R.id.editLatitud);
        editLongitud    = findViewById(R.id.editLongitud);
        editFoto        = findViewById(R.id.editFoto);
        editAforo       = findViewById(R.id.editAforo);
        btnFecha        = findViewById(R.id.btnFecha);
        btnCrear        = findViewById(R.id.btnCrear);
        spinnerGenero   = findViewById(R.id.spinnerGenero);

        // Rellenar el Spinner con los valores del enum
        ArrayAdapter<Generos> generoAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                Generos.values()
        );
        generoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGenero.setAdapter(generoAdapter);

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
        String titulo = editTitulo.getText().toString().trim();
        String descripcion = editDescripcion.getText().toString().trim();
        String latStr = editLatitud.getText().toString().trim();
        String lngStr = editLongitud.getText().toString().trim();
        Generos genero = (Generos) spinnerGenero.getSelectedItem();
        String aforoStr = editAforo.getText().toString().trim();

        if (titulo.isEmpty() || descripcion.isEmpty() ||
                latStr.isEmpty() || lngStr.isEmpty() || aforoStr.isEmpty() || fechaHoraSeleccionada == null) {
            Toast.makeText(this, "Rellena todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }
        int aforoMaximo;
        try {
            aforoMaximo = Integer.parseInt(aforoStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "El aforo debe ser un número entero", Toast.LENGTH_SHORT).show();
            return;
        }
        double latitud, longitud;
        try {
            latitud = Double.parseDouble(latStr);
            longitud = Double.parseDouble(lngStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Las coordenadas deben ser números (ej: 41.3851)", Toast.LENGTH_LONG).show();
            return;
        }

        String nomUsuari = getIntent().getStringExtra("NOM_USUARI");
        String correoUsuari = getIntent().getStringExtra("CORREO_USUARI");
        User creador = new User(
                nomUsuari != null ? nomUsuari : "usuari_desconegut",
                correoUsuari != null ? correoUsuari : ""
        );

        Event evento = new Event(titulo, descripcion, fechaHoraSeleccionada,
                latitud, longitud, creador, genero, aforoMaximo, true);
        evento.setGenero(genero);

        String foto = editFoto.getText().toString().trim();
        if (!foto.isEmpty()) {
            evento.setFoto(foto);
        }
        btnCrear.setEnabled(false);
        android.util.Log.d("CREATE_EVENT", "Dades validades. Cridant a save...");

        eventRepository.save(evento,
                () -> {
                    android.util.Log.d("CREATE_EVENT", "Callback exitós. Tancant pantalla.");
                    Toast.makeText(this, "¡Evento creado!", Toast.LENGTH_SHORT).show();

                    // Ir directamente a la pantalla del usuario
                    Intent intent = new Intent(CreateEventActivity.this, UserActivity.class);
                    intent.putExtra("NOM_USUARI", getIntent().getStringExtra("NOM_USUARI"));
                    intent.putExtra("CORREO_USUARI", getIntent().getStringExtra("CORREO_USUARI"));
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish(); // Esto cierra la actividad actual
                },
                e -> {
                    btnCrear.setEnabled(true);
                    android.util.Log.e("CREATE_EVENT", "Callback fallit: " + e.getMessage());
                    Toast.makeText(this, "Error al guardar: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    btnCrear.setEnabled(true); // Solo si quieres reactivar el botón, pero ya no haría falta porque sales de la actividad
                }
        );
        eventRepository.save(evento,
                () -> {
                    // Notifiquem als subscrits
                    FirestoreUserRepository userRepo = new FirestoreUserRepository();
                    FirestoreNotificacioRepository notiRepo = new FirestoreNotificacioRepository();

                    userRepo.getSeguidors(nomUsuari, seguidors -> {
                        for (String seguidor : seguidors) {
                            notiRepo.enviarNotificacio(
                                    seguidor,
                                    "Nou event de " + nomUsuari + "!",
                                    nomUsuari + " ha creat un nou event: " + titulo
                            );
                        }
                    });

                    Toast.makeText(this, "¡Evento creado!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(CreateEventActivity.this, UserActivity.class);
                    intent.putExtra("NOM_USUARI", getIntent().getStringExtra("NOM_USUARI"));
                    intent.putExtra("CORREO_USUARI", getIntent().getStringExtra("CORREO_USUARI"));
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                },
                e -> {
                    btnCrear.setEnabled(true);
                    Toast.makeText(this, "Error al guardar: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
        );



    }}