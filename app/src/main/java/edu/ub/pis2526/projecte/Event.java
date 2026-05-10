package edu.ub.pis2526.projecte;

import android.net.Uri;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Event {

    private String titulo;
    private String descripcion;
    private LocalDateTime fechaHora;
    private double[] coordenadas;   // [0] = lat, [1] = lng
    private String linkGoogleMaps;  // URL generada a partir de las coordenadas
    private String foto;
    private Generos genero;
    private List<User> participantes;
    private User creador;
    private String id;
    private int aforoMaxim;
    private boolean activo;
    private boolean recordatoriEnviat;

    // ─── CONSTRUCTORES ───────────────────────────────────────────────────────────

    // Constructor vacío requerido por Firestore
    public Event() {
        this.participantes = new ArrayList<>();
        this.recordatoriEnviat = false;
    }

    /**
     * Constructor principal.
     * Recibe lat y lng directamente (el usuario las introduce en el formulario).
     * El link de Google Maps se genera automáticamente a partir de ellas.
     */
    public Event(String titulo, String descripcion, LocalDateTime fechaHora,
                 double latitud, double longitud, User creador, Generos genero, int aforoMaxim, boolean activo) {
        this.id = UUID.randomUUID().toString();
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.fechaHora = fechaHora;
        this.creador = creador;
        this.genero = genero;
        this.aforoMaxim = aforoMaxim;
        this.participantes = new ArrayList<>();
        this.foto = null;
        setCoordenadas(new double[]{latitud, longitud}); // genera el link automáticamente

        this.activo = true;
    }

    // Factory method para reconstruir desde Firestore
    public static Event fromFirestore(String id, String titulo, String descripcion,
                                      String foto, LocalDateTime fechaHora, User creador, int aforoMaxim) {
        Event e = new Event();
        e.id = id;
        e.titulo = titulo;
        e.descripcion = descripcion;
        e.fechaHora = fechaHora;
        e.foto = foto;
        e.creador = creador;
        e.aforoMaxim = aforoMaxim;
        return e;
    }

    // ─── COORDENADAS Y MAPS ──────────────────────────────────────────────────────

    /**
     * Asigna las coordenadas y genera automáticamente el link de Google Maps.
     * Este link es el que usa el botón "Abrir Mapa" en EventDetailActivity.
     */
    public void setCoordenadas(double[] coords) {
        this.coordenadas = coords;
        if (coords != null) {
            this.linkGoogleMaps = "https://maps.google.com/?q=" + coords[0] + "," + coords[1];
        }
    }

    public boolean tieneCoordenadas() {
        return coordenadas != null;
    }

    // ─── PARTICIPANTES ──────────────────────────────────────────────────────────

    public void addParticipante(User user) {
        if (!participantes.contains(user)) participantes.add(user);
    }

    public void removeParticipante(User user) {
        participantes.remove(user);
    }

    public boolean esParticipante(User user) {
        return participantes.contains(user);
    }

    public int getNumParticipantes() {
        return participantes.size();
    }

    // ─── CATEGORIAS ─────────────────────────────────────────────────────────────

    public Generos getGenero() { return genero; }
    public void setGenero(Generos genero) { this.genero = genero; }

    // ─── GETTERS Y SETTERS ───────────────────────────────────────────────────────

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fecha) { this.fechaHora = fecha; }

    public double[] getCoordenadas() { return coordenadas; }

    // El link se genera desde setCoordenadas(), no se asigna directamente
    public String getLinkGoogleMapsString() { return linkGoogleMaps; }
    public void setLinkGoogleMapsString(String url) { this.linkGoogleMaps = url; }

    // Compatibilidad con FirestoreEventRepository que usa Uri
    public void setLinkGoogleMaps(Uri uri) {
        this.linkGoogleMaps = uri != null ? uri.toString() : null;
    }

    public String getFoto() { return foto; }
    public void setFoto(String foto) { this.foto = foto; }

    public List<User> getParticipantes() { return participantes; }

    public User getCreador() { return creador; }
    public void setCreador(User creador) { this.creador = creador; }
    public void setAforoMaxim(int aforoMaxim) {this.aforoMaxim = aforoMaxim;}
    public int getAforoMaxim() {return this.aforoMaxim;}
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public boolean isRecordatoriEnviat() { return recordatoriEnviat; }
    public void setRecordatoriEnviat(boolean recordatoriEnviat) { this.recordatoriEnviat = recordatoriEnviat; }
}