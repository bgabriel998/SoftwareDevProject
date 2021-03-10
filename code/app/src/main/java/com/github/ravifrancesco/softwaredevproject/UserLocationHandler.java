package com.github.ravifrancesco.softwaredevproject;

import android.content.Context;

import com.github.bgabriel998.softwaredevproject.Button7Activity;

import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class UserLocationHandler extends Observable {

    private int updateInterval = 1000; // update interval in ms

    private boolean running = false;

    private final Context mContext;

    private GPSTracker gpsTracker;

    private double latitude;
    private double longitude;
    private double altitude;
    private double accuracy;

    public UserLocationHandler(Context mContext) {
        this.mContext = mContext;
    }

    public void startTracking() {

        running = true;

        new Thread(() -> {
            while(running) {
                synchronized (this) {
                    requestLocation();
                }
            }
        }).start();

    }

    public void stopTracking() {
        running = false;
    }

    private void requestLocation() {

        gpsTracker = new GPSTracker(mContext);

        updateLatitude(gpsTracker.getLatitude());
        updateLongitude(gpsTracker.getLongitude());
        updateAltitude(gpsTracker.getAltitude());
        updateAccuracy(gpsTracker.getAccuracy());



        notifyObservers(this);

    }

    private void updateLatitude(double tempLatitude) {

        if(tempLatitude != latitude) {
            latitude = tempLatitude;
            hasChanged();
        }

    }

    private void updateLongitude(double tempLongitude) {

        if(tempLongitude != longitude) {
            latitude = tempLongitude;
            hasChanged();
        }

    }

    private void updateAltitude(double tempAltitude) {

        if(tempAltitude != tempAltitude) {
            altitude = tempAltitude;
            hasChanged();
        }

    }

    private void updateAccuracy(double tempAccuracy) {

        if(tempAccuracy != accuracy) {
            accuracy = tempAccuracy;
            hasChanged();
        }

    }

    public void setUpdateInterval(int updateInterval) {
        this.updateInterval = updateInterval;
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

    public double getAccuracy() {
        return accuracy;
    }

}
