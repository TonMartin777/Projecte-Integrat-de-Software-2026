package edu.ub.pis2526.projecte;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class Event {

    private String titulo;
    private String descripcion;
    private LocalDateTime fechaHora;
    private double[] coordenadas;   // [0] = lat, [1] = lng
    private Uri linkGoogleMaps;
    private String foto;
    private List<Categorias> categorias;
    private List<User> participantes;
    private User creador;

    private String id;

    // ─── CONSTRUCTOR ────────────────────────────────────────────────────────────

    // Aviso: para cuando se use este constructor, en el COntext solo hay que escribir "this". Se usa para sacar la ubicación
// Constructor vacío (requerido por Firestore)
// Constructor vacío (para Firestore)
    public Event() {
        this.categorias = new ArrayList<>();
        this.participantes = new ArrayList<>();
    }

    // Constructor original — NO tocar
    public Event(String titulo, String descripcion, LocalDateTime fechaHora, String direccion, User creador, Context context) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.categorias = new ArrayList<>();
        this.participantes = new ArrayList<>();
        this.fechaHora = fechaHora;
        this.creador = creador;
        this.foto = null;
        this.id = UUID.randomUUID().toString();
        setUbicacionPorDireccion(direccion, context);
    }

    public static Event fromFirestore(String id, String titulo, String descripcion, String foto, LocalDateTime fechaHora, User creador) {
        Event e = new Event();
        e.id = id;
        e.titulo = titulo;
        e.descripcion = descripcion;
        e.fechaHora = fechaHora;
        e.foto = foto;
        e.creador = creador;
        return e;
    }

    // ─── UBICACIÓN ──────────────────────────────────────────────────────────────

    /**
     * El usuario introduce una dirección en texto.
     * Ejemplo: "Carrer de Mallorca 401, Barcelona"
     * Usa el Geocoder para obtener las coordenadas.
     * Devuelve true si se han podido obtener las coordenadas.
     */

    /*
    public boolean setUbicacionPorDireccion(String direccion, Context context) {
        try {
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            List<Address> resultados = geocoder.getFromLocationName(direccion, 1);

            if (resultados != null && !resultados.isEmpty()) {
                double lat = resultados.get(0).getLatitude();
                double lng = resultados.get(0).getLongitude();
                this.coordenadas = new double[]{lat, lng};
                this.linkGoogleMaps = Uri.parse(
                        "https://maps.google.com/?q=" + lat + "," + lng
                );
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
     */
    public boolean setUbicacionPorDireccion(String direccion, Context context) {
        return false; // Desactivado temporalmente
    }
    /**
     * Indica si el evento tiene coordenadas asignadas.
     * Útil para validar antes de guardar el evento.
     */
    public boolean tieneCoordenadas() {
        return coordenadas != null;
    }

    // ─── PARTICIPANTES ──────────────────────────────────────────────────────────

    public void addParticipante(User user) {
        if (!participantes.contains(user)) {
            participantes.add(user);
        }
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

    public void addCategoria(Categorias categoria) {
        if (!categorias.contains(categoria)) {
            categorias.add(categoria);
        }
    }

    public void removeCategoria(Categorias categoria) {
        categorias.remove(categoria);
    }

    public boolean tieneCategoria(Categorias categoria) {
        return categorias.contains(categoria);
    }


    // ─── GETTERS Y SETTERS ───────────────────────────────────────────────────────

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fecha) { this.fechaHora = fecha; }

    public double[] getCoordenadas() { return coordenadas; }
    // No hay setter directo de coordenadas, usar setUbicacionPorDireccion()

    public Uri getLinkGoogleMaps() { return linkGoogleMaps; }

    public String getFoto() { return foto; }
    public void setFoto(String foto) { this.foto = foto; }

    public List<Categorias> getCategorias() { return categorias; }

    public List<User> getParticipantes() { return participantes; }

    public User getCreador() { return creador; }
    public void setCreador(User creador) { this.creador = creador; }

    // ID para el evento
    public String getId() { return id; }
}