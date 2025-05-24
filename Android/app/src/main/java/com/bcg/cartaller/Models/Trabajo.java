package com.bcg.cartaller.Models;

public class Trabajo {
    public String id;
    public String estado;
    public String descripcion;
    public Vehiculo vehiculo;
    public String fecha_inicio;
    public String fecha_fin;
    public String comentarios;
    public String imagen;
    public String mecanico_id;

    //Constructores (vacio y con atributos) - Getters y Setters - toString
    public Trabajo() {
    }

    public Trabajo(String id, String estado, String descripcion, Vehiculo vehiculo) {
        this.id = id;
        this.estado = estado;
        this.descripcion = descripcion;
        this.vehiculo = vehiculo;
    }

    public Trabajo(String id, String descripcion, String fecha_inicio, String fecha_fin, String estado, String comentarios, String imagen, Vehiculo vehiculo, String mecanico_id) {
        this.id = id;
        this.descripcion = descripcion;
        this.fecha_inicio = fecha_inicio;
        this.fecha_fin = fecha_fin;
        this.estado = estado;
        this.comentarios = comentarios;
        this.imagen = imagen;
        this.vehiculo = vehiculo;
        this.mecanico_id = mecanico_id;
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

    public String getFecha_inicio() {
        return fecha_inicio;
    }

    public void setFecha_inicio(String fecha_inicio) {
        this.fecha_inicio = fecha_inicio;
    }

    public String getFecha_fin() {
        return fecha_fin;
    }

    public void setFecha_fin(String fecha_fin) {
        this.fecha_fin = fecha_fin;
    }

    public String getComentarios() {
        return comentarios;
    }

    public void setComentarios(String comentarios) {
        this.comentarios = comentarios;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public String getMecanico_id() {
        return mecanico_id;
    }

    public void setMecanico_id(String mecanico_id) {
        this.mecanico_id = mecanico_id;
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
