package com.bcg.cartaller.Models;

public class Cliente {
    public int id;
    public String dni;
    public String nombre;
    public String telefono;
    public int numVehiculos = 0; //no me está funcionando

    public Cliente() {}

    //Condstructor que uso en ClientSearcFragment:

    public Cliente(String dni, String nombre, String telefono) {
        this.dni = dni;
        this.nombre = nombre;
        this.telefono = telefono;
    }

    public Cliente(int id, String nombre, String telefono) {
        this.id = id;
        this.nombre = nombre;
        this.telefono = telefono;
    }

    public Cliente(int id, String dni, String nombre, String telefono) {
        this.id = id;
        this.dni = dni;
        this.nombre = nombre;
        this.telefono = telefono;
    }


    //Constructor que uso en JobsSearchFrament:
    public Cliente(String dni) {
        this.dni = dni;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDni() { return dni; }
    public String getNombre() { return nombre; }
    public String getTelefono() { return telefono; }
    public int getNumVehiculos() { return numVehiculos; }

    public void setDni(String dni) { this.dni = dni; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public void setNumVehiculos(int numVehiculos) { this.numVehiculos = numVehiculos; }

    @Override
    public String toString() {
        return "Cliente{" +
                "dni='" + dni + '\'' +
                ", nombre='" + nombre + '\'' +
                ", telefono='" + telefono + '\'' +
                ", numVehiculos=" + numVehiculos +
                '}';
    }
}
