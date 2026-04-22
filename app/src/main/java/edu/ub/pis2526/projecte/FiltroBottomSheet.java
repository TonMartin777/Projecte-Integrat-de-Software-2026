package edu.ub.pis2526.projecte;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;

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

    private LocalDate fechaDesde = null;
    private LocalDate fechaHasta = null;

    public FiltroBottomSheet(List<Event> todosLosEventos, OnFiltroAplicadoListener listener) {
        this.todosLosEventos = todosLosEventos;
        this.listener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_filtro_bottom_sheet, container, false);

        RadioGroup radioGroupOrden = view.findViewById(R.id.radioGroupOrden);
        Button btnFechaDesde = view.findViewById(R.id.btnFechaDesde);
        Button btnFechaHasta = view.findViewById(R.id.btnFechaHasta);
        Button btnAplicar = view.findViewById(R.id.btnAplicar);
        Button btnLimpiar = view.findViewById(R.id.btnLimpiar);

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

            // Ordenar
            int selectedId = radioGroupOrden.getCheckedRadioButtonId();
            if (selectedId == R.id.radioFechaAsc) {
                resultado.sort(Comparator.comparing(
                        e -> e.getFechaHora() != null ? e.getFechaHora() : LocalDateTime.MAX
                ));
            }

            listener.onFiltroAplicado(resultado);
            dismiss();
        });

        btnLimpiar.setOnClickListener(v -> {
            fechaDesde = null;
            fechaHasta = null;
            radioGroupOrden.check(R.id.radioSinOrden);
            btnFechaDesde.setText("Desde: cualquier fecha");
            btnFechaHasta.setText("Hasta: cualquier fecha");
            listener.onFiltroAplicado(new ArrayList<>(todosLosEventos));
            dismiss();
        });

        return view;
    }
}