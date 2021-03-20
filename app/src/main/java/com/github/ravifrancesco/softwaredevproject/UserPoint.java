package com.github.ravifrancesco.softwaredevproject;

import android.content.Context;

/**
 * UserPoint is a class that represents a general point on earth.
 * It adds a component to represent the accuracy of the user's location:
 * <ul>
 * <li>accuracy
 * </ul>
 * <p>
 * It provides a constructor that takes a GPSTracker as an input.
 * It provides a method to get updates on the user location.
 *
 * This class should be used as a observer that observes a GPSTracker.
 */
public class UserPoint extends Point {

    private GPSTracker gpsTracker;

    private double accuracy;

    /**
     * Constructor for the UserPoint.
     *
     * @param mContext  the current context of the app
     */
    public UserPoint(Context mContext) {
        super(0, 0, 0);
        gpsTracker = new GPSTracker(mContext, this);
    }

    /**
     * Method that is used to update the current user location.
     */
    public void update() {
        super.setLatitude(gpsTracker.getLatitude());
        super.setLongitude(gpsTracker.getLongitude());
        super.setAltitude(gpsTracker.getAltitude());
        accuracy = gpsTracker.getAccuracy();
    }

    /**
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
        super.latitude = lat;
        super.longitude = lon;
        super.altitude = alt;
        this.accuracy = acc;
    }

}
