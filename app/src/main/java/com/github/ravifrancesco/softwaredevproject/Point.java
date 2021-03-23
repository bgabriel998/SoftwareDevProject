package com.github.ravifrancesco.softwaredevproject;

/**
 * Point is a class that represents a general point on earth.
 * A point is described by three components:
 * <ul>
 * <li>Latitude
 * <li>Longitude
 * <li>Altitude
 * </ul>
 * <p>
 * This class contains a method to compute distance in meters between two points.
 */
public class Point {

    final static double EARTH_RADIUS = 6378137; // value in meters

    private double latitude;
    private double longitude;

    private double altitude;

    /**
     * Constructor for the Point class.
     *
     * @param latitude  latitude of the point (in degrees)
     * @param longitude longitude of the point (in degrees)
     * @param altitude  altitude of the point (in meters)
     */
    public Point(double latitude, double longitude, double altitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
    }

    /**
     * Method for computing distance between two point as the crow flies.
     *
     * @param other the other point to compute the distance
     * @return      a value in meters representing the distance
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

    /**
     *
     * @param latitude  latitude to set (in degrees)
     */
    public void setLatitude(double latitude) { this.latitude = latitude; }

    /**
     *
     * @param longitude longitude to set (in degrees)
     */
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    /**
     *
     * @param altitude altitude to set (in meters)
     */
    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    /**
     *
     * @return point latitude (in degrees)
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     *
     * @return point longitude (in degrees)
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     *
     * @return point altitude (in meters)
     */
    public double getAltitude() {
        return altitude;
    }

}
