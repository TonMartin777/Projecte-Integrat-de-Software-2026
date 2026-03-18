package edu.ub.pis2526.projecte;

public class User {
    private String nom;
    private String correo;

    public User(String nom, String correo) {
        this.nom = nom;
        this.correo = correo;
    }

    // Constructor sin correo por si no se tiene todavía
    public User(String nom) {
        this.nom = nom;
        this.correo = null;
    }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    // Necesario para que participantes.contains(user) funcione correctamente en Event.java
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof User)) return false;
        User other = (User) obj;
        return nom != null && nom.equals(other.nom);
    }
}