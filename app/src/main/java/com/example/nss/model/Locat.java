package com.example.nss.model;

public class Locat {
    double latitude,longitude;

    public Locat(double lat, double lon) {
        this.latitude = lat;
        this.longitude = lon;
    }

    public double getlatitude() {
        return latitude;
    }

    public double getlongitude() {
        return longitude;
    }
}
