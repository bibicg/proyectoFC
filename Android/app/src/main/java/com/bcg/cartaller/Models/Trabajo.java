package com.bcg.cartaller.Models;

public class Trabajo {
    public String id;
    public String estado;
    public String descripcion;
    public Vehiculo vehiculo;

    //Constructores (vacio y con atributos) - Getters y Setters - toString
    public Trabajo() {
    }

    public Trabajo(String id, String estado, String descripcion, Vehiculo vehiculo) {
        this.id = id;
        this.estado = estado;
        this.descripcion = descripcion;
        this.vehiculo = vehiculo;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Vehiculo getVehiculo() {
        return vehiculo;
    }

    public void setVehiculo(Vehiculo vehiculo) {
        this.vehiculo = vehiculo;
    }

    @Override
    public String toString() {
        return "Trabajo{" +
                "id='" + id + '\'' +
                ", estado='" + estado + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", vehiculo=" + vehiculo +
                '}';
    }
}
