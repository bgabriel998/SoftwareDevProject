package com.github.ravifrancesco.softwaredevproject;

import org.osmdroid.bonuspack.location.POI;
import org.osmdroid.util.GeoPoint;

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

    public POIPoint(POI point) {
        super(point.mLocation.getLatitude(), point.mLocation.getLongitude(), point.mLocation.getAltitude());
        this.setName(point.mType);
    }



}
