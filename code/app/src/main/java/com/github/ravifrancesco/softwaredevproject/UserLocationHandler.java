package com.github.ravifrancesco.softwaredevproject;

import android.content.Context;
import android.os.Looper;
import android.util.Log;

import java.util.Observable;

@Deprecated
public class UserLocationHandler extends Observable {

    private int updateInterval = 10000; // update interval in ms

    private boolean tracking = false;

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

        tracking = true;

        new Thread(() -> {
            Looper.prepare();
            while(tracking) {
                requestLocation();
                try {
                    Thread.sleep(updateInterval);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    public void stopTracking() {
        tracking = false;
    }

    public synchronized void requestLocation() {

        //gpsTracker = new GPSTracker(mContext);

        updateLatitude(gpsTracker.getLatitude());
        updateLongitude(gpsTracker.getLongitude());
        updateAltitude(gpsTracker.getAltitude());
        updateAccuracy(gpsTracker.getAccuracy());

        notifyObservers();

    }

    private void updateLatitude(double tempLatitude) {

        if(tempLatitude != latitude) {
            latitude = tempLatitude;
            setChanged();
        }

    }

    private void updateLongitude(double tempLongitude) {

        if(tempLongitude != longitude) {
            longitude = tempLongitude;
            setChanged();
        }

    }

    private void updateAltitude(double tempAltitude) {

        if(tempAltitude != altitude) {
            altitude = tempAltitude;
            setChanged();
        }

    }

    private void updateAccuracy(double tempAccuracy) {

        if(tempAccuracy != accuracy) {
            accuracy = tempAccuracy;
            setChanged();
        }

    }

    public void setUpdateInterval(int updateInterval) {
        this.updateInterval = updateInterval;
    }

    public synchronized double getLatitude() {
        return latitude;
    }

    public synchronized double getLongitude() {
        return longitude;
    }

    public synchronized double getAltitude() {
        return altitude;
    }

    public synchronized double getAccuracy() {
        return accuracy;
    }

    public boolean isTracking() {
        return tracking;
    }

    public boolean canGetLocation() {
        return  gpsTracker.canGetLocation();
    }

}
