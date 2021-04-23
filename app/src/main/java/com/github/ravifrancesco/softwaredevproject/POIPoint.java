package com.github.ravifrancesco.softwaredevproject;

import org.osmdroid.bonuspack.location.POI;
import org.osmdroid.util.GeoPoint;

import java.util.Objects;

/**
 * POIPoint is a class that extends Point.java and represents a POI in the map.
 *
 * It provides a constructor to build a POIPoint from a org.osmdroid.util.GeoPoint.
 */
public class POIPoint extends Point {

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private String name;

    /**
     * Constructor for POIPoint.
     *
     * @param geoPoint GEOPoint from org.osmdroid.util.GeoPoint
     */
    public POIPoint(GeoPoint geoPoint) {
        super(geoPoint.getLatitude(), geoPoint.getLongitude(), geoPoint.getAltitude());
    }

    /**
     * Constructor for POIPoint
     * @param point POI point
     */
    public POIPoint(POI point) {
        super(point.mLocation.getLatitude(), point.mLocation.getLongitude(), point.mLocation.getAltitude());
        this.setName(point.mType);
    }

    /**
     * Constructor for POI Point
     * @param name peak name
     * @param latitude peak latitude
     * @param longitude peak longitude
     * @param altitude peak height (in meters)
     */
    public POIPoint(String name, double latitude, double longitude, long altitude){
        super(latitude, longitude, altitude);
        this.setName(name);

    }

    /**
     * Override method for POIPoint HashSet comparison
     * @return hashcode
     */
    @Override
    public int hashCode() {
        return Objects.hash(name.hashCode(), this.getLatitude(), this.getLongitude(), this.getAltitude());
    }

    /**
     * Override method for POIPoint HashSet comparison
     * Compare only the name of the POI
     * @return
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof POIPoint))
            return false;
        if (obj == this)
            return true;
        return getName().equals(((POIPoint)obj).getName());
    }
}
