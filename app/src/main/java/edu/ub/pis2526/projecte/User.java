package edu.ub.pis2526.projecte;

public class User {
    private String id;          // añadir id para identificación única
    private String nom;
    private String correo;
    private String contrasenya;
    private String fotoPerfil;
    private String rol;         // "banda" o "asistente"

    public User() {}

    public User(String nom, String correo) {
        this.nom = nom;
        this.correo = correo;
    }

    public User(String nom, String correo, String rol) {
        this.nom = nom;
        this.correo = correo;
        this.rol = rol;
    }

    // Getters y setters (incluye id y contrasenya, fotoPerfil)
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getContrasenya() { return contrasenya; }
    public void setContrasenya(String contrasenya) { this.contrasenya = contrasenya; }

    public String getFotoPerfil() { return fotoPerfil; }
    public void setFotoPerfil(String fotoPerfil) { this.fotoPerfil = fotoPerfil; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    // Necesario para que participantes.contains(user) funcione correctamente en Event.java
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof User)) return false;
        User other = (User) obj;
        return nom != null && nom.equals(other.nom);
    }
}