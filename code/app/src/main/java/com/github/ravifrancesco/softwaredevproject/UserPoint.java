package com.github.ravifrancesco.softwaredevproject;

import android.content.Context;
import android.util.Log;

import com.google.gson.internal.$Gson$Preconditions;

import java.util.Observable;
import java.util.Observer;

public class UserPoint extends Point {

    private GPSTracker gpsTracker;

    private double accuracy;

    public UserPoint(Context mContext) {
        super(0, 0, 0);
        gpsTracker = new GPSTracker(mContext, this);
    }

    public void update() {
        super.setLatitude(gpsTracker.getLatitude());
        super.setLongitude(gpsTracker.getLongitude());
        super.setAltitude(gpsTracker.getAltitude());
        accuracy = gpsTracker.getAccuracy();
    }

    public double getAccuracy() {
        return accuracy;
    }

    public boolean canGetLocation() {
        return gpsTracker.canGetLocation();
    }

}
