package com.github.ravifrancesco.softwaredevproject;

public class Point {

    static double earthRadius = 6378137.0; // value in meters

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
        double rThis;
        double rOther;

        rThis = earthRadius + altitude;
        rOther = earthRadius + other.altitude;

        // computing distance in spherical polar coordinates
        squaredDistance = Math.pow(rThis, 2) + Math.pow(rOther, 2) -
                2*rThis*rOther*(
                        Math.cos(latitude)*Math.cos(other.latitude)*Math.cos(longitude - other.longitude) +
                        Math.sin(latitude)*Math.sin(other.latitude)
                );

        return Math.sqrt(squaredDistance);
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setHeight(double altitude) {
        this.altitude = altitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getHeight() {
        return altitude;
    }

}
