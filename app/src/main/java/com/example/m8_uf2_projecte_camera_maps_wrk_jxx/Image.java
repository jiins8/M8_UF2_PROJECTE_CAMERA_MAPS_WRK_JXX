package com.example.m8_uf2_projecte_camera_maps_wrk_jxx;

public class Image {

    private String imageUrl;
    private double latitude;
    private double longitude;

    public Image() {
    }

    public Image(String imageUrl, double latitude, double longitude) {
        this.imageUrl = imageUrl;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Getters and setters
    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
