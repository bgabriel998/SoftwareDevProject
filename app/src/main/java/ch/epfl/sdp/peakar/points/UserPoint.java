package ch.epfl.sdp.peakar.points;

import android.content.Context;
import android.util.Log;

import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;


/**
 * UserPoint is a class that represents a general point on earth.
 *
 * This class is a singleton: only one instance of this class is available on the
 * entire program.
 *
 * It adds a component to represent the accuracy of the user's location and customLocation:
 * <ul>
 * <li>accuracy accuracy in meters of the user location
 * <li>customLocation if true, the userPoint location is not updated by the GPS tracker
 * </ul>
 * <p>
 * It provides a constructor that takes a GPSTracker as an input.
 * It provides a method to get updates on the user location.
 *
 * This class should be used as a observer that observes a GPSTracker.
 *
 * This class can be observed; The updates will be called only if the location changes
 * more than MIN_DISTANCE_FOR_UPDATES meters.
 */
public final class UserPoint extends Point {

    public static final int MIN_DISTANCE_FOR_UPDATES = 100; // in meters

    private static UserPoint single_instance = null; // singleton instance
  
    private final GPSTracker gpsTracker;

    private double accuracy;

    private boolean customLocation;

    private List<Observer> observers;

    private Point lastLocation;

    /**
     * Constructor for the UserPoint. Private because it is a singleton.
     *
     * @param mContext  the current context of the app
     */
    private UserPoint(Context mContext) {
        super(GPSTracker.DEFAULT_LAT, GPSTracker.DEFAULT_LON, GPSTracker.DEFAULT_ALT);
        gpsTracker = new GPSTracker(mContext, this);
        customLocation = false;
        single_instance = this;
        observers = new LinkedList<>();
        lastLocation = new Point(0,0,0);
    }

    /**
     * Allows to add observers to the UserPoint.
     *
     * @param o observer to add
     */
    public void addObserver(Observer o) {
        this.observers.add(o);
    }

    /**
     * Method to get the singleton instance of this class. If the class was already
     * initialized the parameter will be ignored.
     *
     * @param mContext  context of the application.
     * @return          single instance of the user point.
     */
    public static UserPoint getInstance(Context mContext) {
        return single_instance == null ? new UserPoint(mContext) : single_instance;
    }

    /**
     * Method that is used to update the current user location.
     *
     * It updates the observers if the point has moved more than MIN_DISTANCE_FOR_UPDATES
     * from the last saved location.
     */
    public void update() {
        if (!customLocation) {
            super.setLatitude(gpsTracker.getLatitude());
            super.setLongitude(gpsTracker.getLongitude());
            super.setAltitude(gpsTracker.getAltitude());
            accuracy = gpsTracker.getAccuracy();
        }
        if (this.computeDistance(lastLocation) > MIN_DISTANCE_FOR_UPDATES) {
            lastLocation.setLatitude(this.latitude);
            lastLocation.setLongitude(this.longitude);
            lastLocation.setAltitude(this.altitude);
            observers.forEach(o -> o.update(null, null));
        }
    }

    /**
     * Getter for the accuracy.
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
     * Method used to set custom user location
     *
     * @param lat user latitude (in degrees)
     * @param lon user longitude (in degrees)
     * @param alt user altitude (in meters)
     * @param acc user accuracy (in meters)
     */
    public void setLocation(double lat, double lon, double alt, double acc) {
        customLocation = true;
        super.latitude = lat;
        super.longitude = lon;
        super.altitude = alt;
        this.accuracy = acc;
    }

    /**
     * Sets customLocation to false
     */
    public void switchToRealLocation() {
        customLocation = false;
    }

    /**
     * To check if the custom location is on
     *
     * @return  <code>true</code> if customLocation is enabled;
     *          <code>false</code> otherwise.
     */
    public boolean isCustomLocation() {
        return customLocation;
    }

}
