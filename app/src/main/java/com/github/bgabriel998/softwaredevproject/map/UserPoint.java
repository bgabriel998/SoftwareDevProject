package com.github.bgabriel998.softwaredevproject.map;

import android.content.Context;

import com.github.bgabriel998.softwaredevproject.map.GPSTracker;
import com.github.bgabriel998.softwaredevproject.map.Point;

import org.osmdroid.util.BoundingBox;


/**
 * UserPoint is a class that represents a general point on earth.
 * It adds a component to represent the accuracy of the user's location and customLocation:
 * <ul>
 * <li>accuracy accuracy in meters of the user location
 * <li>customLocation if true, the userPoint location is not updated by the GPS tracker
 * </ul>
 * <p>
 * It provides a constructor that takes a GPSTracker as an input.
 * It provides a method to get updates on the user location.
 *
 * This class should be used as a observer that observes a GPSTracker.
 */
public class UserPoint extends Point {
  
    private GPSTracker gpsTracker;

    private final double ADJUST_COORDINATES = 0.008983112; // 1km in degrees at equator.

    private double accuracy;

    private boolean customLocation;

    /**
     * Constructor for the UserPoint.
     *
     * @param mContext  the current context of the app
     */
    public UserPoint(Context mContext) {
        super(GPSTracker.DEFAULT_LAT, GPSTracker.DEFAULT_LON, GPSTracker.DEFAULT_ALT);
        gpsTracker = new GPSTracker(mContext, this);
        customLocation = false;
    }

    public UserPoint(double lat, double lon, double alt){
        super(lat,lon,alt);
    }

    /**
     * Method that is used to update the current user location.
     */

    public void update() {
        if (!customLocation) {
            super.setLatitude(gpsTracker.getLatitude());
            super.setLongitude(gpsTracker.getLongitude());
            super.setAltitude(gpsTracker.getAltitude());
            accuracy = gpsTracker.getAccuracy();
        }
    }

    /*
     *
     * @return accuracy of the current location (in meters)
     */
    public double getAccuracy() {
        return accuracy;
    }

    /**
     *
     * @return  <code>true</code> if GPSTracker is able to get the current location;
     *          <code>false</code> otherwise.
     */
    public boolean canGetLocation() {
        return gpsTracker.canGetLocation();
    }

    /**
     * Method used to set custom user location
     *
     * @param lat user latitude (in degrees)
     * @param lon user longitude (in degrees)
     * @param alt user altitude (in meters)
     * @param acc user accuracy (in meters)
     */
    public void setLocation(double lat, double lon, double alt, double acc) {
        customLocation = true;
        super.latitude = lat;
        super.longitude = lon;
        super.altitude = alt;
        this.accuracy = acc;
    }

    /**
     * Sets customLocation to false
     */
    public void switchToRealLocation() {
        customLocation = false;
    }

    /**
     * To check if the custom location is on
     *
     * @return  <code>true</code> if customLocation is enabled;
     *          <code>false</code> otherwise.
     */
    public boolean isCustomLocation() {
        return customLocation;
    }

    /**
     * Computes bounding box around
     * @param rangeInKm range around user point to compute the bounding box (in km)
     * @return Bounding box around user point
     */
    public BoundingBox computeBoundingBox(double rangeInKm){
        double north = super.getLatitude() + ( rangeInKm * ADJUST_COORDINATES);
        double south = super.getLatitude() - ( rangeInKm * ADJUST_COORDINATES);
        double lngRatio = 1/Math.cos(Math.toRadians(super.getLatitude()));
        double east = super.getLongitude() + (rangeInKm * ADJUST_COORDINATES) * lngRatio;
        double west = super.getLongitude() - (rangeInKm * ADJUST_COORDINATES) * lngRatio;
        return new BoundingBox(north,east,south,west);
    }

}