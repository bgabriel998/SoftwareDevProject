package com.github.ravifrancesco.softwaredevproject;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.ActivityCompat;

/**
 * GPSTracker is a class that provides update on the user location for the UserPoint class.
 * It is observed by userPoint.
 * It incapsulates the state information needed for retrieving the user location.
 * It extends Service and implements LocationListener, that allow for the UserPoint to get
 * notified and update when a  change in location is detected
 *
 * <ul>
 * <li> MIN_DISTANCE_CHANGE_FOR_UPDATES is the minimum delta in meters that can be detected
 * <li> MIN_TIME_BW_UPDATES is the minimum time in seconds to pass to request a new location
 * </ul>
 * <p>
 */
public class GPSTracker extends Service implements LocationListener {

    private final Context mContext;

    // flag for GPS status
    private boolean isGPSEnabled = false;

    // flag for network status
    private boolean isNetworkEnabled = false;

    // flag for GPS status
    private boolean canGetLocation = false;

    protected  Location location; // location

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0; // 1 meter

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1; // 1 second

    // Declaring a Location Manager
    protected LocationManager locationManager;

    // User Point
    UserPoint userPoint;

    /**
     * Constructor for GPSTracker class
     *
     * @param mContext current context of the application
     * @param userPoint observer UserPoint
     */
    public GPSTracker(Context mContext, UserPoint userPoint) {
        this.mContext = mContext;
        this.userPoint = userPoint;
        getLocation();
    }

    /**
     * Method used to get updates on the location. Once called it will request a new location to the
     * location manager and update the location.
     * Before requesting the location the method will check if the requisites are satisfied, and
     * will request a new location via NETWORK_PROVIDER if it is available, otherwise it will request
     * it through the GPS_PROVIDER (less precise)
     *
     * In case it fails to do so it will stop the program and print the stack strace of the error
     */
    private void getLocation() {
        try {

            checkLocationManagerStatus();
            if (!isGPSEnabled && !isNetworkEnabled) {
                // display some errors
            } else {
                this.canGetLocation = true;
                if (isNetworkEnabled) { // First get location from Network Provider
                    setLocation(LocationManager.NETWORK_PROVIDER);
                    Log.d("Provider", "Network");
                } else if (isGPSEnabled) { // if GPS Enabled get lat/long using GPS Services
                    if (location == null) {
                        setLocation(LocationManager.GPS_PROVIDER);
                        Log.d("Provider", "GPS Enabled");
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Method that checks which providers are enabled for requesting location
     * It will update class variable isNetworkEnabled and isGPSEnabled accordingly
     */
    private void checkLocationManagerStatus() {
        locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);

        // getting network status
        isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        // getting GPS status
        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    /**
     * This method handles the request to update the location.
     * First it checks if the app has the proper permissions, then it will request a new location
     * through the location manager
     *
     * @param selectedProvider  string indicating the chosen provider for requesting location
     */
    private void setLocation(String selectedProvider) {
        //check the permission
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) mContext, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
        }
        // request location
        locationManager.requestLocationUpdates(
                selectedProvider,
                MIN_TIME_BW_UPDATES,
                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
        // set new location
        if (locationManager != null) {
            location = locationManager.getLastKnownLocation(selectedProvider);
        }
    }

    /**
     *
     * @return latitude (in degrees)
     */
    public double getLatitude(){

        double latitude = 0;

        if(location != null){
            latitude = location.getLatitude();
        }

        // return latitude
        return latitude;
    }

    /**
     *
     * @return longitude (in degrees)
     */
    public double getLongitude(){

        double longitude = 0;

        if(location != null){
            longitude = location.getLongitude();
        }

        // return longitude
        return longitude;
    }

    /**
     *
     * @return altitude (in meters)
     */
    public double getAltitude(){

        double altitude = 0;

        if(location != null){
            altitude = location.getAltitude();
        }

        // return altitude
        return altitude;
    }

    /**
     *
     * @return accuracy (in meters)
     */
    public double getAccuracy() {

        double accuracy = 0;

        if(location != null){
            accuracy = location.getAccuracy();
        }

        // return accuracy
        return accuracy;

    }


    /**
     *
     * @return  <code>true</code> if is able to get the current location;
     *          <code>false</code> otherwise.
     */
    public boolean canGetLocation() {
        return this.canGetLocation;
    }

    /**
     * Method from interface LocationListener.
     * Called when the location has changed and locations are being delivered in batches.
     * Once called it will update the UserPoint that is observing this object.
     *
     * @param location current location
     */
    @Override
    public void onLocationChanged(Location location) {
        getLocation();
        userPoint.update();
    }

    /**
     * Method from interface LocationListener.
     * Called when the provider this listener is registered with becomes disabled.
     * Throws an error.
     *
     * @param provider current provider
     */
    @Override
    public void onProviderDisabled(String provider) {
        // print some error
    }

    /**
     * Method from interface LocationListener.
     * Called when a provider this listener is registered with becomes enabled.
     * Once called it will update the UserPoint that is observing this object.
     *
     * @param provider current provider
     */
    @Override
    public void onProviderEnabled(String provider) {
        getLocation();
        userPoint.update();
    }

    /**
     * Method from interface LocationListener.
     * This method was deprecated in API level 29. This callback will never be invoked on Android Q and above.
     *  Once called it will update the UserPoint that is observing this object.
     *
     * @param provider current provider
     * @param status current status
     * @param extras extras
     */
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        getLocation();
        userPoint.update();
    }

    /**
     * Method from class Service
     *
     * @param arg0 intent
     * @return the communication channel to the service.
     */
    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

}
