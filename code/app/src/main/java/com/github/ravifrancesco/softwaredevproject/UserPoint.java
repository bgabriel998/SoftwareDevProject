package com.github.ravifrancesco.softwaredevproject;

import android.content.Context;
import android.util.Log;

import java.util.Observable;
import java.util.Observer;

public class UserPoint extends Point implements Observer {

    private UserLocationHandler userLocationHandler;

    private double accuracy;

    public UserPoint(Context mContext) {
        super(0, 0, 0);
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
        super.setAltitude(userLocationHandler.getAltitude());
        accuracy = userLocationHandler.getAccuracy();
    }

    public double getAccuracy() {
        return accuracy;
    }

    public boolean isTracking() {
        return userLocationHandler.isTracking();
    }

    public boolean canGetLocation() {
        return userLocationHandler.canGetLocation();
    }

}
