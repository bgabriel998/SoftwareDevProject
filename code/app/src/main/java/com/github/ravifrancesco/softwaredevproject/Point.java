package com.github.ravifrancesco.softwaredevproject;

import android.util.Log;

public class Point {

    final static double EARTH_RADIUS = 6378137; // value in meters

    private double latitude;
    private double longitude;

    private double altitude;

    public Point(double latitude, double longitude, double altitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
    }

    /**
     * Computes distance between this Point and other Point
     */
    public double computeDistance(Point other) {

        double squaredDistance;

        double rThis = EARTH_RADIUS + this.altitude;
        double rOther = EARTH_RADIUS + other.altitude;

        double latThis = Math.toRadians(this.latitude);
        double lonThis = Math.toRadians(this.longitude);

        double latOther = Math.toRadians(other.latitude);
        double lonOther = Math.toRadians(other.longitude);

        // computing distance in spherical polar coordinates
        squaredDistance = Math.pow(rThis, 2) + Math.pow(rOther, 2) -
                2*rThis*rOther*(
                        Math.cos(latThis)*Math.cos(latOther)*Math.cos(lonThis - lonOther) +
                        Math.sin(latThis)*Math.sin(latOther)
                );

        return Math.sqrt(squaredDistance);

    }

    public void setLatitude(double latitude) { this.latitude = latitude; }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getAltitude() {
        return altitude;
    }

}
