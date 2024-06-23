package com.example.nss.model;

public class Locat {
    double latitude,longitude;

    public Locat() {
    }

    // Constructor with arguments
    public Locat(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getlatitude() {
        return latitude;
    }

    public double getlongitude() {
        return longitude;
    }
}
