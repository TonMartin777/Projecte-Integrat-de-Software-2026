package edu.ub.pis2526.projecte;

import android.app.DatePickerDialog;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;

public class FiltroBottomSheet extends BottomSheetDialogFragment {

    public interface OnFiltroAplicadoListener {
        void onFiltroAplicado(List<Event> eventosFiltrados);
    }

    private List<Event> todosLosEventos;
    private OnFiltroAplicadoListener listener;
    private double userLat;
    private double userLng;
    private boolean tieneUbicacion;

    private LocalDate fechaDesde = null;
    private LocalDate fechaHasta = null;
    private int distanciaMaxKm = 100; // valor por defecto

    public FiltroBottomSheet(List<Event> todosLosEventos, double userLat, double userLng, OnFiltroAplicadoListener listener) {
        this.todosLosEventos = todosLosEventos;
        this.userLat = userLat;
        this.userLng = userLng;
        this.tieneUbicacion = (userLat != 0 || userLng != 0);
        this.listener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_filtro_bottom_sheet, container, false);

        RadioGroup radioGroupOrden   = view.findViewById(R.id.radioGroupOrden);
        Button btnFechaDesde         = view.findViewById(R.id.btnFechaDesde);
        Button btnFechaHasta         = view.findViewById(R.id.btnFechaHasta);
        Button btnAplicar            = view.findViewById(R.id.btnAplicar);
        Button btnLimpiar            = view.findViewById(R.id.btnLimpiar);
        SeekBar seekBarDistancia     = view.findViewById(R.id.seekBarDistancia);
        TextView txtDistancia        = view.findViewById(R.id.txtDistancia);
        RadioGroup radioLejaniaAsc   = view.findViewById(R.id.radioGroupOrden);

        seekBarDistancia.setMax(100);
        seekBarDistancia.setProgress(100);
        txtDistancia.setText("Distancia máxima: sin límite");

        seekBarDistancia.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                distanciaMaxKm = progress;
                if (progress >= 100) {
                    txtDistancia.setText("Distancia máxima: sin límite");
                } else {
                    txtDistancia.setText("Distancia máxima: " + progress + " km");
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        btnFechaDesde.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new DatePickerDialog(requireContext(), (datePicker, year, month, day) -> {
                fechaDesde = LocalDate.of(year, month + 1, day);
                btnFechaDesde.setText("Desde: " + day + "/" + (month + 1) + "/" + year);
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
        });

        btnFechaHasta.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new DatePickerDialog(requireContext(), (datePicker, year, month, day) -> {
                fechaHasta = LocalDate.of(year, month + 1, day);
                btnFechaHasta.setText("Hasta: " + day + "/" + (month + 1) + "/" + year);
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
        });

        btnAplicar.setOnClickListener(v -> {
            List<Event> resultado = new ArrayList<>(todosLosEventos);

            // Filtrar por fecha
            if (fechaDesde != null) {
                resultado.removeIf(e -> e.getFechaHora() != null &&
                        e.getFechaHora().toLocalDate().isBefore(fechaDesde));
            }
            if (fechaHasta != null) {
                resultado.removeIf(e -> e.getFechaHora() != null &&
                        e.getFechaHora().toLocalDate().isAfter(fechaHasta));
            }

            // Filtrar por distancia
            if (distanciaMaxKm < 100) {
                if (!tieneUbicacion) {
                    Toast.makeText(requireContext(),
                            "Ubicación no disponible, no se puede filtrar por distancia",
                            Toast.LENGTH_SHORT).show();
                } else {
                    resultado.removeIf(e -> {
                        double[] coords = e.getCoordenadas();
                        if (coords == null) return true;
                        float[] distResult = new float[1];
                        Location.distanceBetween(userLat, userLng, coords[0], coords[1], distResult);
                        float distKm = distResult[0] / 1000f;
                        return distKm > distanciaMaxKm;
                    });
                }
            }

            // Ordenar
            int selectedId = radioGroupOrden.getCheckedRadioButtonId();
            if (selectedId == R.id.radioFechaAsc) {
                resultado.sort(Comparator.comparing(
                        e -> e.getFechaHora() != null ? e.getFechaHora() : LocalDateTime.MAX
                ));
            } else if (selectedId == R.id.radioLejaniaAsc && tieneUbicacion) {
                resultado.sort((a, b) -> {
                    float distA = getDistanciaKm(a);
                    float distB = getDistanciaKm(b);
                    return Float.compare(distA, distB);
                });
            }

            listener.onFiltroAplicado(resultado);
            dismiss();
        });

        btnLimpiar.setOnClickListener(v -> {
            fechaDesde = null;
            fechaHasta = null;
            distanciaMaxKm = 100;
            radioGroupOrden.check(R.id.radioSinOrden);
            btnFechaDesde.setText("Desde: cualquier fecha");
            btnFechaHasta.setText("Hasta: cualquier fecha");
            seekBarDistancia.setProgress(100);
            txtDistancia.setText("Distancia máxima: sin límite");
            listener.onFiltroAplicado(new ArrayList<>(todosLosEventos));
            dismiss();
        });

        return view;
    }

    private float getDistanciaKm(Event e) {
        double[] coords = e.getCoordenadas();
        if (coords == null) return Float.MAX_VALUE;
        float[] result = new float[1];
        Location.distanceBetween(userLat, userLng, coords[0], coords[1], result);
        return result[0] / 1000f;
    }
}