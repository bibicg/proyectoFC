package com.bcg.cartaller.Models;

public class Cliente {
    public String dni;

    //Constructores (vacio y con atributos) - Getters y Setters - toString
    public Cliente() {
    }

    public Cliente(String dni) {
        this.dni = dni;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    @Override
    public String toString() {
        return "Cliente{" +
                "dni='" + dni + '\'' +
                '}';
    }
}
