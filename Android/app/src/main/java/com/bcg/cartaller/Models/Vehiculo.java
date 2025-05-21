package com.bcg.cartaller.Models;

public class Vehiculo {
    public String matricula;
    public Cliente cliente;
    public String marca;
    public String modelo;
    public int anio;
    public int cliente_id; //es lo que usa la BD para relacionar ambas tablas

    //Constructores (vacio y con atributos) - Getters y Setters - toString
    public Vehiculo() {
    }

    public Vehiculo(String matricula, Cliente cliente) {
        this.matricula = matricula;
        this.cliente = cliente;
    }

    public Vehiculo(String matricula, String marca, String modelo, int anio, int cliente_id) {
        this.matricula = matricula;
        this.marca = marca;
        this.modelo = modelo;
        this.anio = anio;
        this.cliente_id = cliente_id;
    }


    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public int getAnio() {
        return anio;
    }

    public void setAnio(int anio) {
        this.anio = anio;
    }

    public int getCliente_id() {
        return cliente_id;
    }

    public void setCliente_id(int cliente_id) {
        this.cliente_id = cliente_id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    @Override
    public String toString() {
        return "Vehiculo{" +
                "matricula='" + matricula + '\'' +
                ", cliente=" + cliente +
                '}';
    }
}
