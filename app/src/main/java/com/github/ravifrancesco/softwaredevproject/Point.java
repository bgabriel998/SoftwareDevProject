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

    protected double latitude;
    protected double longitude;

    protected double altitude;

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

        double rThis = EARTH_RADIUS + this.altitude;
        double rOther = EARTH_RADIUS + other.altitude;

        return computeSphericalDistance(other, rThis, rOther);

    }

    /**
     * Method for computing distance between two point as the crow flies,
     * without taking in account the altitude of the two points
     *
     * @param other the other point to compute the distance
     * @return      a value in meters representing the distance
     */
    public double computeFlatDistance(Point other) {

        double rThis = EARTH_RADIUS;
        double rOther = EARTH_RADIUS;

        return computeSphericalDistance(other, rThis, rOther);

    }

    /**
     * Given the radious of the two points, it computes the distance in spherical coordinates
     *
     * @param other     the other point to compute the distance
     * @param rThis     the radius of this point
     * @param rOther    the radius of the other point
     * @return          a value in meters representing the distance
     */
    private double computeSphericalDistance(Point other, double rThis, double rOther) {

        double squaredDistance;

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
