package com.bcg.cartaller.Models;

/**
public class Job {
    public String id;
    public String status;
    public String description;
    public Car car;
    public String start_date;
    public String end_date;
    public String comment;
    public String image;
    public String mecanico_id;

    //Constructores (vacio y con atributos) - Getters y Setters - toString
    public Job() {
    }

    public Job(String id, String status, String description, Car car) {
        this.id = id;
        this.status = status;
        this.description = description;
        this.car = car;
    }

    public Job(String id, String description, String start_date, String end_date, String status, String comment, String image, Car car, String mecanico_id) {
        this.id = id;
        this.description = description;
        this.start_date = start_date;
        this.end_date = end_date;
        this.status = status;
        this.comment = comment;
        this.image = image;
        this.car = car;
        this.mecanico_id = mecanico_id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Car getVehiculo() {
        return car;
    }

    public void setVehiculo(Car car) {
        this.car = car;
    }

    public String getStart_date() {
        return start_date;
    }

    public void setStart_date(String start_date) {
        this.start_date = start_date;
    }

    public String getEnd_date() {
        return end_date;
    }

    public void setEnd_date(String end_date) {
        this.end_date = end_date;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getMecanico_id() {
        return mecanico_id;
    }

    public void setMecanico_id(String mecanico_id) {
        this.mecanico_id = mecanico_id;
    }

    @Override
    public String toString() {
        return "Job{" +
                "id='" + id + '\'' +
                ", status='" + status + '\'' +
                ", description='" + description + '\'' +
                ", car=" + car +
                ", start_date='" + start_date + '\'' +
                ", end_date='" + end_date + '\'' +
                ", comment='" + comment + '\'' +
                ", image='" + image + '\'' +
                ", mecanico_id='" + mecanico_id + '\'' +
                '}';
    }
}*/

import com.google.gson.annotations.SerializedName;

public class Job {

    @SerializedName("id")
    public String id;

    @SerializedName("estado")
    public String status;

    @SerializedName("descripcion")
    public String description;

    @SerializedName("fecha_inicio")
    public String startDate;

    @SerializedName("fecha_fin")
    public String endDate;

    @SerializedName("comentario")
    public String comment;

    @SerializedName("imagen")
    public String image;

    @SerializedName("mecanico_id")
    public String mechanicId;


    public Car car;

    // Constructores
    public Job() {}

    public Job(String id, String status, String description, Car car) {
        this.id = id;
        this.status = status;
        this.description = description;
        this.car = car;
    }

    public Job(String id, String description, String startDate, String endDate, String status, String comment, String image, Car car, String mechanicId) {
        this.id = id;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.comment = comment;
        this.image = image;
        this.car = car;
        this.mechanicId = mechanicId;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public String getMechanicId() { return mechanicId; }
    public void setMechanicId(String mechanicId) { this.mechanicId = mechanicId; }

    public Car getCar() { return car; }
    public void setCar(Car car) { this.car = car; }

    @Override
    public String toString() {
        return "Job{" +
                "id='" + id + '\'' +
                ", status='" + status + '\'' +
                ", description='" + description + '\'' +
                ", startDate='" + startDate + '\'' +
                ", endDate='" + endDate + '\'' +
                ", comment='" + comment + '\'' +
                ", image='" + image + '\'' +
                ", mechanicId='" + mechanicId + '\'' +
                ", car=" + car +
                '}';
    }
}

