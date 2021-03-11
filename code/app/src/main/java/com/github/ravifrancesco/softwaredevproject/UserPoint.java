package com.github.ravifrancesco.softwaredevproject;

import android.content.Context;
import android.util.Log;

import com.google.gson.internal.$Gson$Preconditions;

import java.util.Observable;
import java.util.Observer;

public class UserPoint extends Point {

    //private UserLocationHandler userLocationHandler;
    private GPSTracker gpsTracker;

    private double accuracy;

    public UserPoint(Context mContext) {
        super(0, 0, 0);
        gpsTracker = new GPSTracker(mContext, this);
        //userLocationHandler.addObserver(this);
    }

    /*
    public void startTracking() {
        userLocationHandler.startTracking();
    }

    public void stopTracking() {
        userLocationHandler.stopTracking();
    }
     */

    /*
    @Override
    public void update(Observable o, Object arg) {
        super.setLatitude(userLocationHandler.getLatitude());
        super.setLongitude(userLocationHandler.getLongitude());
        super.setAltitude(userLocationHandler.getAltitude());
        accuracy = userLocationHandler.getAccuracy();
    }
     */

    public void update() {
        super.setLatitude(gpsTracker.getLatitude());
        super.setLongitude(gpsTracker.getLongitude());
        super.setAltitude(gpsTracker.getAltitude());
        accuracy = gpsTracker.getAccuracy();
    }

    public double getAccuracy() {
        return accuracy;
    }

    /*
    public boolean isTracking() {
        return userLocationHandler.isTracking();
    }
     */

    public boolean canGetLocation() {
        return gpsTracker.canGetLocation();
    }

}
