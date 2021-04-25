package com.github.bgabriel998.softwaredevproject.points;

import org.junit.Test;
import org.osmdroid.util.GeoPoint;

import java.util.Random;

import static org.junit.Assert.assertEquals;

public class GeoPointTest {

    @Test
    public void POIPointTest() {

        Random r = new Random();

        double latitude = r.nextDouble();
        double longitude = r.nextDouble();
        double altitude = r.nextDouble();

        GeoPoint geopoint = new GeoPoint(latitude, longitude, altitude);

        assertEquals(latitude, geopoint.getLatitude(), 0);
        assertEquals(longitude, geopoint.getLongitude(), 0);
        assertEquals(altitude, geopoint.getAltitude(), 0);

    }

}
