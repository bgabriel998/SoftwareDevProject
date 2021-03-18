package com.github.ravifrancesco.softwaredevproject;

import android.content.Context;

import android.util.Log;

import com.google.gson.internal.$Gson$Preconditions;

import org.osmdroid.util.BoundingBox;

import java.util.Observable;
import java.util.Observer;


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

    private final double ADJUST_COORDINATES = 0.008983112; // 1km in degrees at equator.

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
        super.setAltitude(gpsTracker.getAltitude());
        super.setLatitude(gpsTracker.getLatitude());
        super.setLongitude(gpsTracker.getLongitude());
        accuracy = gpsTracker.getAccuracy();
    }

    /**
     * Update latitude and longitude for testing purposes
     * This function is there to test the geonames file
     * PLEASE DO NOT USE THIS FUNCTION IN CODE !!!
     */
    public void updateMock(){
        super.setLatitude(46.519251915333676);
        super.setLongitude(6.558563221333525);
        super.setAltitude(220);
    }


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
