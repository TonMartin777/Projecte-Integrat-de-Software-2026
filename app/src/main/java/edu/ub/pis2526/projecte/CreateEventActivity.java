package edu.ub.pis2526.projecte;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.time.LocalDateTime;
import java.util.Calendar;

public class CreateEventActivity extends AppCompatActivity {

    private EditText editTitulo, editDescripcion, editDireccion;
    private Button btnFecha, btnCrear;
    private LocalDateTime fechaHoraSeleccionada;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        editTitulo     = findViewById(R.id.editTitulo);
        editDescripcion = findViewById(R.id.editDescripcion);
        editDireccion  = findViewById(R.id.editDireccion);
        btnFecha       = findViewById(R.id.btnFecha);
        btnCrear       = findViewById(R.id.btnCrear);

        // Selector de fecha primero, y luego de la hora
        btnFecha.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new DatePickerDialog(this, (view, year, month, day) -> {
                new TimePickerDialog(this, (tview, hour, minute) -> {
                    fechaHoraSeleccionada = LocalDateTime.of(year, month + 1, day, hour, minute);
                    btnFecha.setText(day + "/" + (month+1) + "/" + year + " " + hour + ":" + String.format("%02d", minute));
                }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show();
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
        });

        // Botón crear
        btnCrear.setOnClickListener(v -> {
            String titulo     = editTitulo.getText().toString().trim();
            String descripcion = editDescripcion.getText().toString().trim();
            String direccion  = editDireccion.getText().toString().trim();

            // Validación básica
            if (titulo.isEmpty() || descripcion.isEmpty() ||
                    direccion.isEmpty() || fechaHoraSeleccionada == null) {
                Toast.makeText(this, "Rellena todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            // Aquí el creador debería salir de la sesión activa,
            // de momento usamos uno de prueba
            User creador = new User("usuario_activo");

            Event evento = new Event(titulo, descripcion, fechaHoraSeleccionada,
                    direccion, creador, this);

            if (!evento.tieneCoordenadas()) {
                Toast.makeText(this, "Dirección no encontrada, prueba con otro formato",
                        Toast.LENGTH_LONG).show();
                return;
            }

            // Aquí guardarías el evento (base de datos, lista global, etc.)
            Toast.makeText(this, "Evento creado!", Toast.LENGTH_SHORT).show();
            finish(); // vuelve a la pantalla anterior
        });
    }
}