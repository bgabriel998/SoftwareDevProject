package com.github.ravifrancesco.softwaredevproject;

import android.content.Context;

import java.util.Observable;
import java.util.Observer;

public class UserPoint extends Point implements Observer {

    private UserLocationHandler userLocationHandler;

    private double accuracy;

    public UserPoint(double latitude, double longitude, double altitude, Context mContext) {
        super(latitude, longitude, altitude);
        userLocationHandler = new UserLocationHandler(mContext);
        userLocationHandler.addObserver(this);
    }

    public void startTracking() {
        userLocationHandler.startTracking();
    }

    public void stopTracking() {
        userLocationHandler.stopTracking();
    }

    @Override
    public void update(Observable o, Object arg) {
        super.setLatitude(userLocationHandler.getLatitude());
        super.setLongitude(userLocationHandler.getLongitude());
        super.setLatitude(userLocationHandler.getAltitude());
        accuracy = userLocationHandler.getAccuracy();
    }

    public double getAccuracy() {
        return accuracy;
    }

}
