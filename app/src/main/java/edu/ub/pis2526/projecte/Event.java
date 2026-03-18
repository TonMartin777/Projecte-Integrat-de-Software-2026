package edu.ub.pis2526.projecte;

public class Event {
    private String nombre;
    private String fecha;
    private String ubicacion;
    private String descripcion;
    private String categoria;
    private String hora;
    private String imagenUrl;


    public Event(String nombre, String fecha, String ubicacion,
                 String descripcion, String categoria, String hora, String imagenUrl) {
        this.nombre = nombre;
        this.fecha = fecha;
        this.ubicacion = ubicacion;
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.hora = hora;
        this.imagenUrl = imagenUrl;
    }

    public String getNombre() { return nombre; }
    public String getFecha() { return fecha; }
    public String getUbicacion() { return ubicacion; }
    public String getDescripcion() { return descripcion; }
    public String getCategoria() { return categoria; }
    public String getHora() { return hora; }
    public String getImagenUrl() { return imagenUrl; }

}