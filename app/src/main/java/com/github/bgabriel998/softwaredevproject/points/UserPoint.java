package com.github.bgabriel998.softwaredevproject.points;

import android.content.Context;

import org.osmdroid.util.BoundingBox;


/**
 * UserPoint is a class that represents a general point on earth.
 *
 * This class is a singleton: only one instance of this class is available on the
 * entire program.
 *
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
public final class UserPoint extends Point {

    private static UserPoint single_instance = null; // singleton instance
  
    private GPSTracker gpsTracker;

    private double accuracy;

    private boolean customLocation;

    /**
     * Constructor for the UserPoint. Private because it is a singleton.
     *
     * @param mContext  the current context of the app
     */
    private UserPoint(Context mContext) {
        super(GPSTracker.DEFAULT_LAT, GPSTracker.DEFAULT_LON, GPSTracker.DEFAULT_ALT);
        gpsTracker = new GPSTracker(mContext, this);
        customLocation = false;
        single_instance = this;
    }

    /**
     * Method to get the singleton instance of this class. If the class was already
     * initialized the parameter will be ignored.
     *
     * @param mContext  context of the application.
     * @return          single instance of the user point.
     */
    public static UserPoint getInstance(Context mContext) {
        return single_instance == null ? new UserPoint(mContext) : single_instance;
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
     * Getter for the accuracy.
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

}
