package edu.ub.pis2526.projecte;
import com.bumptech.glide.Glide;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class EventDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        String nombre = getIntent().getStringExtra("nombre");
        String fecha = getIntent().getStringExtra("fecha");
        String ubicacion = getIntent().getStringExtra("ubicacion");
        String descripcion = getIntent().getStringExtra("descripcion");
        String categoria = getIntent().getStringExtra("categoria");
        String hora = getIntent().getStringExtra("hora");
        String imagenUrl = getIntent().getStringExtra("imagenUrl");

        TextView tvNombre = findViewById(R.id.detailNombre);
        TextView tvFecha = findViewById(R.id.detailFecha);
        TextView tvUbicacion = findViewById(R.id.detailUbicacion);
        TextView tvDescripcion = findViewById(R.id.detailDescripcion);
        TextView tvCategoria = findViewById(R.id.detailCategoria);
        TextView tvHora = findViewById(R.id.detailHora);
        ImageView imgEvento = findViewById(R.id.detailImagen);

        tvNombre.setText(nombre);
        tvFecha.setText(fecha);
        tvUbicacion.setText(ubicacion);
        tvDescripcion.setText(descripcion);
        tvCategoria.setText(categoria);
        tvHora.setText(hora);

        Log.d("DEBUG", "URL de imagen: " + imagenUrl);

        Glide.with(this)
                .load(imagenUrl)
                .into(imgEvento);
    }
}