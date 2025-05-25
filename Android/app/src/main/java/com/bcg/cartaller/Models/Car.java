package com.bcg.cartaller.Models;

/**public class Car {
    public String licensePlate; //matricula
    public Customer customer;
    public String brand;
    public String model;
    public int year;
    public int cliente_id; //es lo que usa la BD para relacionar ambas tablas

    //Constructores (vacio y con atributos) - Getters y Setters - toString
    public Car() {
    }

    public Car(String licensePlate, Customer customer) {
        this.licensePlate = licensePlate;
        this.customer = customer;
    }

    public Car(String licensePlate, String marca, String model, int year, int cliente_id) {
        this.licensePlate = licensePlate;
        this.brand = marca;
        this.model = model;
        this.year = year;
        this.cliente_id = cliente_id;
    }


    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getCliente_id() {
        return cliente_id;
    }

    public void setCliente_id(int cliente_id) {
        this.cliente_id = cliente_id;
    }

    public Customer getCliente() {
        return customer;
    }

    public void setCliente(Customer customer) {
        this.customer = customer;
    }

    @Override
    public String toString() {
        return "Car{" +
                "carRegistration='" + licensePlate + '\'' +
                ", customer=" + customer +
                ", brand='" + brand + '\'' +
                ", model='" + model + '\'' +
                ", year=" + year +
                ", cliente_id=" + cliente_id +
                '}';
    }
}*/


import com.google.gson.annotations.SerializedName;

public class Car {

    @SerializedName("matricula")
    public String licensePlate;

    @SerializedName("marca")
    public String brand;

    @SerializedName("modelo")
    public String model;

    @SerializedName("anio")
    public int year;

    @SerializedName("cliente_id")
    public int customerId;


    public Customer customer;

    // Constructores
    public Car() {}

    public Car(String licensePlate, Customer customer) {
        this.licensePlate = licensePlate;
        this.customer = customer;
    }

    public Car(String licensePlate, String brand, String model, int year, int customerId) {
        this.licensePlate = licensePlate;
        this.brand = brand;
        this.model = model;
        this.year = year;
        this.customerId = customerId;
    }

    // Getters and setters
    public String getLicensePlate() { return licensePlate; }
    public void setLicensePlate(String licensePlate) { this.licensePlate = licensePlate; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }

    @Override
    public String toString() {
        return "Car{" +
                "licensePlate='" + licensePlate + '\'' +
                ", brand='" + brand + '\'' +
                ", model='" + model + '\'' +
                ", year=" + year +
                ", customerId=" + customerId +
                ", customer=" + customer +
                '}';
    }
}

