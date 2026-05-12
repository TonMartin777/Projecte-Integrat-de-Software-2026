package edu.ub.pis2526.projecte;

public class Banda extends User {
    private String descripcion;   // biografía de la banda
    private String generoMusical; // estilo principal
    // otros campos que quieras (año formación, etc.)

    public Banda() {
        super(); // constructor vacío para Firestore
    }

    public Banda(String nom, String correo, String descripcion, String generoMusical) {
        super(nom, correo);
        this.descripcion = descripcion;
        this.generoMusical = generoMusical;
    }

    // Getters y setters
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getGeneroMusical() { return generoMusical; }
    public void setGeneroMusical(String generoMusical) { this.generoMusical = generoMusical; }
}