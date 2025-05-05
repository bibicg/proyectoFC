package com.bcg.cartaller.Models;

public class Vehiculo {
    public String matricula;
    public Cliente cliente;

    //Constructores (vacio y con atributos) - Getters y Setters - toString
    public Vehiculo() {
    }

    public Vehiculo(String matricula, Cliente cliente) {
        this.matricula = matricula;
        this.cliente = cliente;
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
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
