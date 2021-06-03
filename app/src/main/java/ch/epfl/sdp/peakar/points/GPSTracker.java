package ch.epfl.sdp.peakar.points;

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
 * <li> MIN_TIME_BW_UPDATES is the minimum time in milliseconds to pass to request a new location
 * </ul>
 * <p>
 */
public class GPSTracker extends Service implements LocationListener {

    // fixed coordinates
    public static final double DEFAULT_LAT = 27.988056;
    public static final double DEFAULT_LON = 86.925278;
    public static final double DEFAULT_ALT = 8848.86;
    public static final double DEFAULT_ACC = 0.0;

    // current app context
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
    private static final long MIN_TIME_BW_UPDATES = 1000; // 1 second

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
            // First get location from Network Provider
            if (isNetworkEnabled) { setLocationProvider(LocationManager.NETWORK_PROVIDER, "Network"); }
            // Then set location from GPS Provider
            if (isGPSEnabled && location == null) { setLocationProvider(LocationManager.GPS_PROVIDER, "GPS"); }

            // handle case were no provider is enabled
            if (!isNetworkEnabled && !isGPSEnabled) { Log.d("No provider enabled", "Using default coordinates"); }

            //handle null location, set default location
            if (location == null) {
                setDefaultLocation();
                Log.d("Unable to retrieve location", "Using default coordinates");
            }

        } catch (Exception e) {
            Log.d("GPS ERROR", "read stack trace");
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
     * Sets the provider for the location manager
     *
     * @param locationManagerProvider   selected provider
     * @param logMessage                message to log (name of the provider)
     */
    private void setLocationProvider(String locationManagerProvider, String logMessage) {
        this.canGetLocation = true;
        setLocation(locationManagerProvider);
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
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(
                    selectedProvider,
                    MIN_TIME_BW_UPDATES,
                    MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
            // set new location
            if (locationManager != null) {
                location = locationManager.getLastKnownLocation(selectedProvider);
            }
        }
    }

    /**
     * Set the default location
     */
    private void setDefaultLocation() {
        location = new Location("");
        location.setLatitude(DEFAULT_LAT);
        location.setLongitude(DEFAULT_LON);
        location.setAltitude(DEFAULT_ALT);
        location.setAccuracy((float)DEFAULT_ACC);
    }

    /**
     *
     * @return latitude (in degrees)
     */
    public double getLatitude(){
        return location != null ? location.getLatitude() : DEFAULT_LAT;
    }

    /**
     *
     * @return longitude (in degrees)
     */
    public double getLongitude(){
        return location != null ? location.getLongitude() : DEFAULT_LON;
    }

    /**
     *
     * @return altitude (in meters)
     */
    public double getAltitude(){
        return location != null ? location.getAltitude() : DEFAULT_ALT;
    }

    /**
     *
     * @return accuracy (in meters)
     */
    public double getAccuracy() {
        return location != null ? location.getAccuracy() : DEFAULT_ACC;
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
