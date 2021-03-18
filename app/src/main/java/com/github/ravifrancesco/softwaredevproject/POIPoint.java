package com.github.ravifrancesco.softwaredevproject;

import org.osmdroid.util.GeoPoint;

/**
 * POIPoint is a class that extends Point.java and represents a POI in the map.
 *
 * It provides a constructor to build a POIPoint from a org.osmdroid.util.GeoPoint.
 */
public class POIPoint extends Point {

    /**
     * Constructor for POIPoint.
     *
     * @param geoPoint GEOPoint from org.osmdroid.util.GeoPoint
     */
    public POIPoint(GeoPoint geoPoint) {
        super(geoPoint.getLatitude(), geoPoint.getLongitude(), geoPoint.getAltitude());
    }

}
