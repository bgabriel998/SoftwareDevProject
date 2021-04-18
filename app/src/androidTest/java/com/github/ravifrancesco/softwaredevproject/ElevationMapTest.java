package com.github.ravifrancesco.softwaredevproject;

import android.content.Context;
import android.util.Pair;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Assert;
import org.junit.Test;

public class ElevationMapTest {

    /**
     * Test geoTIFF API helper class constructor
     */
    @Test
    public void elevationMapConstructorTest() {

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
    public void getTopographyMapTest() {

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

    /**
     * Check that indexes for accessing the matrix are computed correctly
     */
    @Test
    public void getIndexesTest() {

        Context mContext = ApplicationProvider.getApplicationContext();

        UserPoint userPoint = new UserPoint(mContext);
        userPoint.setLocation(GPSTracker.DEFAULT_LAT, GPSTracker.DEFAULT_LON,GPSTracker.DEFAULT_ALT, GPSTracker.DEFAULT_ACC);

        ElevationMap elevationMap = new ElevationMap(userPoint);

        // check the indexes around the Everest Peak
        Assert.assertEquals(new Pair<>(215, 244), elevationMap.getIndexesFromCoordinates(GPSTracker.DEFAULT_LAT, GPSTracker.DEFAULT_LON));

        // set location near the Mont Blanc
        userPoint.setLocation(45.802537, 6.850328, 0, 0);
        elevationMap.updateElevationMatrix();

        // check the indexes around the Mont Blanc Peak
        Assert.assertEquals(new Pair<>(178, 326), elevationMap.getIndexesFromCoordinates(45.8326, 6.8652));
    }

    @Test
    public void getAltitudeTest() {

        Context mContext = ApplicationProvider.getApplicationContext();

        UserPoint userPoint = new UserPoint(mContext);
        userPoint.setLocation(GPSTracker.DEFAULT_LAT, GPSTracker.DEFAULT_LON,GPSTracker.DEFAULT_ALT, GPSTracker.DEFAULT_ACC);

        ElevationMap elevationMap = new ElevationMap(userPoint);

        // check the altitude around the Everest Peak using coordinates
        Assert.assertEquals(8849, elevationMap.getAltitudeAtLocation(GPSTracker.DEFAULT_LAT, GPSTracker.DEFAULT_LON), 200);
        // check the altitude around the Everest Peak using indexes
        Pair<Integer, Integer> indexes = elevationMap.getIndexesFromCoordinates(GPSTracker.DEFAULT_LAT, GPSTracker.DEFAULT_LON);
        Assert.assertEquals(8849, elevationMap.getAltitudeAtLocation(indexes.first, indexes.second), 200);


        // set location near the Mont Blanc
        userPoint.setLocation(45.802537, 6.850328, 0, 0);
        elevationMap.updateElevationMatrix();

        // check the altitude around the Mont Blanc Peak using coordinates
        Assert.assertEquals(4808, elevationMap.getAltitudeAtLocation(45.8326, 6.8652), 200);
        // check the altitude around the Mont Blanc Peak using indexes
        indexes = elevationMap.getIndexesFromCoordinates(45.8326, 6.8652);
        Assert.assertEquals(4808, elevationMap.getAltitudeAtLocation(indexes.first, indexes.second), 200);

    }

    @Test
    public void getMapCellSizeTest() {

        Context mContext = ApplicationProvider.getApplicationContext();

        UserPoint userPoint = new UserPoint(mContext);
        userPoint.setLocation(GPSTracker.DEFAULT_LAT, GPSTracker.DEFAULT_LON,GPSTracker.DEFAULT_ALT, GPSTracker.DEFAULT_ACC);

        ElevationMap elevationMap = new ElevationMap(userPoint);

        Assert.assertEquals(0.000833333333, elevationMap.getMapCellSize(), 0.00000001);


    }

    @Test
    public void getBoundingBoxWestLongTest() {

        Context mContext = ApplicationProvider.getApplicationContext();

        UserPoint userPoint = new UserPoint(mContext);
        userPoint.setLocation(GPSTracker.DEFAULT_LAT, GPSTracker.DEFAULT_LON,GPSTracker.DEFAULT_ALT, GPSTracker.DEFAULT_ACC);

        ElevationMap elevationMap = new ElevationMap(userPoint);

        Assert.assertEquals(userPoint.computeBoundingBox(ElevationMap.BOUNDING_BOX_RANGE).getLonWest(), elevationMap.getBoundingBoxWestLong(), 0.00000001);

    }



}
