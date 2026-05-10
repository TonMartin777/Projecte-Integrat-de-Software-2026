package edu.ub.pis2526.projecte;

public class Resena {
    private String nomUsuari;
    private int puntuacion;
    private String missatge;
    private long timestamp;

    public Resena() {}

    public Resena(String nomUsuari, int puntuacion, String missatge) {
        this.nomUsuari = nomUsuari;
        this.puntuacion = puntuacion;
        this.missatge = missatge;
        this.timestamp = System.currentTimeMillis();
    }

    public String getNomUsuari() { return nomUsuari; }
    public void setNomUsuari(String nomUsuari) { this.nomUsuari = nomUsuari; }
    public int getPuntuacion() { return puntuacion; }
    public void setPuntuacion(int puntuacion) { this.puntuacion = puntuacion; }
    public String getMissatge() { return missatge; }
    public void setMissatge(String missatge) { this.missatge = missatge; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}