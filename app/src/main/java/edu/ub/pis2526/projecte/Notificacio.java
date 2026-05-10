package edu.ub.pis2526.projecte;

public class Notificacio {
    private String id;
    private String destinatari; // El nom o correu de l'usuari que ha de rebre l'avís
    private String titol;
    private String missatge;
    private long timestamp;     // Per poder-les ordenar de més nova a més vella
    private boolean llegida;    // Per saber si hem de posar el text en negreta o no
    private String eventoId;
    private String tituloEvento;
    private String tipus; // "encuesta" o null

    // Constructor buit obligatori per a Firestore
    public Notificacio() {}

    public Notificacio(String destinatari, String titol, String missatge) {
        this.destinatari = destinatari;
        this.titol = titol;
        this.missatge = missatge;
        this.timestamp = System.currentTimeMillis();
        this.llegida = false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitol() {
        return titol;
    }

    public void setTitol(String titol) {
        this.titol = titol;
    }

    public String getDestinatari() {
        return destinatari;
    }

    public void setDestinatari(String destinatari) {
        this.destinatari = destinatari;
    }

    public String getMissatge() {
        return missatge;
    }

    public void setMissatge(String missatge) {
        this.missatge = missatge;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isLlegida() {
        return llegida;
    }

    public void setLlegida(boolean llegida) {
        this.llegida = llegida;
    }

    public String getEventoId() { return eventoId; }
    public void setEventoId(String eventoId) { this.eventoId = eventoId; }
    public String getTituloEvento() { return tituloEvento; }
    public void setTituloEvento(String tituloEvento) { this.tituloEvento = tituloEvento; }
    public String getTipus() { return tipus; }
    public void setTipus(String tipus) { this.tipus = tipus; }
}