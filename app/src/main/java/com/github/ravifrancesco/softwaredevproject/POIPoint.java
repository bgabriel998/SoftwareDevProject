package com.github.ravifrancesco.softwaredevproject;

import org.osmdroid.util.GeoPoint;

public class POIPoint extends Point {

    // construct a POIPoint given a org.osmdroid.util.GeoPoint
    public POIPoint(GeoPoint geoPoint) {
        super(geoPoint.getLatitude(), geoPoint.getLongitude(), geoPoint.getAltitude());
    }

}
