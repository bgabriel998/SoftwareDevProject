package com.github.ravifrancesco.softwaredevproject;

import android.content.Context;
import android.util.Log;

import com.google.gson.internal.$Gson$Preconditions;

import org.osmdroid.util.BoundingBox;

import java.util.Observable;
import java.util.Observer;

public class UserPoint extends Point {

    private final double ADJUST_COORDINATES = 0.008983112; // 1km in degrees at equator.

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
