package com.github.ravifrancesco.softwaredevproject;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Assert;
import org.junit.Test;

public class ElevationMapTest {
/**
 * Test geoTIFF API helper class constructor
 */
    @Test
    public void geoTIFFConstructorTest() {

        Context mContext = ApplicationProvider.getApplicationContext();

        UserPoint userPoint = new UserPoint(mContext);

        // testing if the constructor works correctly
        ElevationMap elevationMap = new ElevationMap(userPoint);

        Assert.assertTrue(true);

    }
/**
 * Check that getTopography method does effectively return a non-null value
 */
    @Test
    public void getTopographyMapBitmap() {

        Context mContext = ApplicationProvider.getApplicationContext();

        // setting everest location
        UserPoint userPoint = new UserPoint(mContext);
        userPoint.setLocation(GPSTracker.DEFAULT_LAT, GPSTracker.DEFAULT_LON,GPSTracker.DEFAULT_ALT, GPSTracker.DEFAULT_ACC);

        ElevationMap elevationMap = new ElevationMap(userPoint);

        // checking if there are no errors retrieving the AAIGrid
        int[][] topographyMap = elevationMap.getTopographyMap();
        Assert.assertFalse(topographyMap == null);

    }
/**
 *  Check the changing of the location to get Topography Map
 * Check with coordinates under the treshold and above
 */
    @Test
    public void updateMapTest() {

        Context mContext = ApplicationProvider.getApplicationContext();

        UserPoint userPoint = new UserPoint(mContext);
        userPoint.setLocation(GPSTracker.DEFAULT_LAT, GPSTracker.DEFAULT_LON,GPSTracker.DEFAULT_ALT, GPSTracker.DEFAULT_ACC);

        ElevationMap elevationMap = new ElevationMap(userPoint);

        // getting the map for the everest location
        int[][] topographyMapA = elevationMap.getTopographyMap();

        // setting the location to < ElevationMap.MINIMUM_DISTANCE_FOR_UPDATE
        userPoint.setLocation(27.994601, 86.933163, 0,0);

        // checking if the map returned is the same
        int[][] topographyMapB = elevationMap.getTopographyMap();
        Assert.assertTrue(topographyMapB.equals(topographyMapA));

        // setting the location to > ElevationMap.MINIMUM_DISTANCE_FOR_UPDATE
        userPoint.setLocation(26.042309, 86.943749, 0,0);

        // checking if the map returned is different
        int[][] topographyMapC = elevationMap.getTopographyMap();
        Assert.assertFalse(topographyMapC.equals(topographyMapA));

    }

}
