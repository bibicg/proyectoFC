package com.bcg.cartaller.Models;

/**
 * POJO Cliente
 * Pendiente reorganizar y limpiar al final, pq hay muchos constructores que, aunque son aceptados
 * en java por la sobrecarga de métodos, no me parece óptimo.
 */
public class Cliente {
    public int id;
    public String dni;
    public String nombre;
    public String apellidos;
    public String email;
    public String direccion;
    public String telefono;
    public int numVehiculos = 0; //no me está funcionando

    /**
     * CONSTRUCTORES
     */
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

    //Constructor que uso en ClientSearchFragment para poder modificar un cliente existente:
    public Cliente(int id, String dni, String nombre, String apellidos, String email, String direccion, String telefono, int numVehiculos) {
        this.id = id;
        this.dni = dni;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.email = email;
        this.direccion = direccion;
        this.telefono = telefono;
        this.numVehiculos = numVehiculos;
    }

    //constructor para busqueda de cliente por dni:
    public Cliente(int id, String dni, String nombre, String apellidos, String telefono, String email, String direccion) {
        this.id = id;
        this.dni = dni;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.telefono = telefono;
        this.email = email;
        this.direccion = direccion;
    }

    /**
     * GETTERS & SETTERS
     * @return
     */

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

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        email = email;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    @Override
    public String toString() {
        return "Cliente{" +
                "id=" + id +
                ", dni='" + dni + '\'' +
                ", nombre='" + nombre + '\'' +
                ", apellidos='" + apellidos + '\'' +
                ", email='" + email + '\'' +
                ", direccion='" + direccion + '\'' +
                ", telefono='" + telefono + '\'' +
                ", numVehiculos=" + numVehiculos +
                '}';
    }
}
