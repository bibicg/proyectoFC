package com.bcg.cartaller.Models;
import com.google.gson.annotations.SerializedName;

/**
 * POJO Customer
 * Pendiente reorganizar y limpiar al final, pq hay muchos constructores que, aunque son aceptados
 * en java por la sobrecarga de métodos, no me parece óptimo.
 */
/**public class Customer {
    public int id;
    public String dni;
    public String name;
    public String surname;
    public String email;
    public String address;
    public String phone;
    public int numCars = 0; //no me está funcionando

    /**
     * CONSTRUCTORES
     */
/**    public Customer() {}

    //Condstructor que uso en ClientSearcFragment:
    public Customer(String dni, String name, String phone) {
        this.dni = dni;
        this.name = name;
        this.phone = phone;
    }

    public Customer(int id, String name, String phone) {
        this.id = id;
        this.name = name;
        this.phone = phone;
    }

    public Customer(int id, String dni, String name, String phone) {
        this.id = id;
        this.dni = dni;
        this.name = name;
        this.phone = phone;
    }

    //Constructor que uso en JobsSearchFrament:
    public Customer(String dni) {
        this.dni = dni;
    }

    //Constructor que uso en ClientSearchFragment para poder modificar un customer existente:
    public Customer(int id, String dni, String name, String surname, String email, String address, String phone, int numCars) {
        this.id = id;
        this.dni = dni;
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.address = address;
        this.phone = phone;
        this.numCars = numCars;
    }

    //constructor para busqueda de customer por dni:
    public Customer(int id, String dni, String name, String surname, String phone, String email, String address) {
        this.id = id;
        this.dni = dni;
        this.name = name;
        this.surname = surname;
        this.phone = phone;
        this.email = email;
        this.address = address;
    }

    /**
     * GETTERS & SETTERS
     * @return
     */

/**    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDni() { return dni; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public int getNumCars() { return numCars; }

    public void setDni(String dni) { this.dni = dni; }
    public void setName(String name) { this.name = name; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setNumCars(int numCars) { this.numCars = numCars; }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return "Customer{" +
                "id=" + id +
                ", dni='" + dni + '\'' +
                ", name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", email='" + email + '\'' +
                ", address='" + address + '\'' +
                ", phone='" + phone + '\'' +
                ", numCars=" + numCars +
                '}';
    }
}*/





public class Customer {

    public int id;

    @SerializedName("dni")
    public String dni;

    @SerializedName("nombre")
    public String name;

    @SerializedName("apellidos")
    public String surname;

    @SerializedName("email")
    public String email;

    @SerializedName("direccion")
    public String address;

    @SerializedName("telefono")
    public String phone;

    public int numCars = 0;

    // Constructores
    public Customer() {}

    public Customer(String dni) {
        this.dni = dni;
    }

    public Customer(String dni, String name, String phone) {
        this.dni = dni;
        this.name = name;
        this.phone = phone;
    }

    public Customer(int id, String name, String phone) {
        this.id = id;
        this.name = name;
        this.phone = phone;
    }

    public Customer(int id, String dni, String name, String phone) {
        this.id = id;
        this.dni = dni;
        this.name = name;
        this.phone = phone;
    }

    public Customer(int id, String dni, String name, String surname, String email, String address, String phone, int numCars) {
        this.id = id;
        this.dni = dni;
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.address = address;
        this.phone = phone;
        this.numCars = numCars;
    }

    public Customer(int id, String dni, String name, String surname, String phone, String email, String address) {
        this.id = id;
        this.dni = dni;
        this.name = name;
        this.surname = surname;
        this.phone = phone;
        this.email = email;
        this.address = address;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSurname() { return surname; }
    public void setSurname(String surname) { this.surname = surname; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public int getNumCars() { return numCars; }
    public void setNumCars(int numCars) { this.numCars = numCars; }

    @Override
    public String toString() {
        return "Customer{" +
                "id=" + id +
                ", dni='" + dni + '\'' +
                ", name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", email='" + email + '\'' +
                ", address='" + address + '\'' +
                ", phone='" + phone + '\'' +
                ", numCars=" + numCars +
                '}';
    }
}

