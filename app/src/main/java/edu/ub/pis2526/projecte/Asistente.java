package edu.ub.pis2526.projecte;

import java.util.ArrayList;
import java.util.List;

public class Asistente extends User {
    private List<String> bandasSeguides; // IDs de las bandas que sigue

    public Asistente() {
        super();
        this.bandasSeguides = new ArrayList<>();
    }

    public Asistente(String nom, String correo) {
        super(nom, correo);
        this.bandasSeguides = new ArrayList<>();
    }

    public List<String> getBandasSeguides() { return bandasSeguides; }
    public void setBandasSeguides(List<String> bandasSeguides) { this.bandasSeguides = bandasSeguides; }
}