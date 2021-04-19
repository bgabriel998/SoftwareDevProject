package com.github.ravifrancesco.softwaredevproject;

import android.content.Context;

import androidx.core.util.Pair;
import androidx.test.core.app.ApplicationProvider;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class TopographyAsyncTest {

    private static Pair<int[][], Double> topographyPair;
    private static UserPoint userPoint;
    private ElevationMapAsync elevationMapAsync;

    /**
     * Download the topography map
     * @throws InterruptedException if any thread has interrupted the current thread
     */
    @Before
    public void setup() throws InterruptedException {
        Context mContext = ApplicationProvider.getApplicationContext();

        userPoint = new UserPoint(mContext);
        userPoint.setLocation(GPSTracker.DEFAULT_LAT, GPSTracker.DEFAULT_LON,GPSTracker.DEFAULT_ALT, GPSTracker.DEFAULT_ACC);
        new DownloadTopographyTask(){
            @Override
            public void onResponseReceived(Pair<int[][], Double> topography) {
                super.onResponseReceived(topography);
                topographyPair = topography;
            }
        }.execute(userPoint);

        //Wait for the map to be downloaded
        int counter=0;
        while(topographyPair==null && counter<20){
            Thread.sleep(1000);
            counter++;
        }
        counter=0;
        while(topographyPair.first==null && counter<20){
            Thread.sleep(1000);
            counter++;
        }
    }

    /**
     * Tests the ElevationMapAsyncClass when the topographyMap is null
     */
    @Test
    public void topographyNull(){
        Pair<int[][], Double> topo = new Pair<>(null, 0.0);
        elevationMapAsync = new ElevationMapAsync(topo, userPoint);
        Assert.assertNull(elevationMapAsync.getIndexesFromCoordinates(userPoint.getLatitude(), userPoint.getLongitude()));
        Assert.assertEquals(0, elevationMapAsync.getAltitudeAtLocation(userPoint.getLatitude(), userPoint.getLongitude()));
    }

    /**
     * Tests that the topographyMap is not null
     */
    @Test
    public void topographyNotNull(){
        Assert.assertNotNull(topographyPair);
        Assert.assertNotNull(topographyPair.first);
        elevationMapAsync = new ElevationMapAsync(topographyPair, userPoint);
        Assert.assertArrayEquals(topographyPair.first, elevationMapAsync.getTopographyMap());
    }

    /**
     * Same test as @see ElevationMapTest.getAltitudeTest.java but with ElevationMapAsync
     */
    @Test
    public void getAltitudeTest() throws InterruptedException {

        Context mContext = ApplicationProvider.getApplicationContext();

        UserPoint userPoint = new UserPoint(mContext);
        userPoint.setLocation(GPSTracker.DEFAULT_LAT, GPSTracker.DEFAULT_LON,GPSTracker.DEFAULT_ALT, GPSTracker.DEFAULT_ACC);

        ElevationMapAsync elevationMap = new ElevationMapAsync(topographyPair, userPoint);

        // check the altitude around the Everest Peak using coordinates
        Assert.assertEquals(8849, elevationMap.getAltitudeAtLocation(GPSTracker.DEFAULT_LAT, GPSTracker.DEFAULT_LON), 200);
        // check the altitude around the Everest Peak using indexes
        Pair<Integer, Integer> indexes = elevationMap.getIndexesFromCoordinates(GPSTracker.DEFAULT_LAT, GPSTracker.DEFAULT_LON);
        Assert.assertEquals(8849, elevationMap.getAltitudeAtLocation(indexes.first, indexes.second), 200);

        int[][] oldTopographyMap = topographyPair.first;

        // set location near the Mont Blanc
        userPoint.setLocation(45.802537, 6.850328, 0, 0);
        elevationMap.updateElevationMatrix();

        //Wait for the map to be updated
        int counter=0;
        while(elevationMap.getTopographyMap()==oldTopographyMap && counter<30){
            Thread.sleep(1000);
            counter++;
        }

        // check the altitude around the Mont Blanc Peak using coordinates
        Assert.assertEquals(4808, elevationMap.getAltitudeAtLocation(45.8326, 6.8652), 200);
        // check the altitude around the Mont Blanc Peak using indexes
        indexes = elevationMap.getIndexesFromCoordinates(45.8326, 6.8652);
        Assert.assertEquals(4808, elevationMap.getAltitudeAtLocation(indexes.first, indexes.second), 200);
    }

    /**
     * Test LineOfSightAsync.getVisiblePoints to see if the POIPoints are correctly filtered
     * @see LineOfSightTest
     *
     */
    @Test
    public void getVisiblePoints(){
        LineOfSightAsync lineOfSight = new LineOfSightAsync(topographyPair, userPoint);

        // PoiPoints to check
        List<POIPoint> pointsToCheck = new ArrayList<>();
        // POIPoints that should be visible
        List<POIPoint> visiblePoints = new ArrayList<>();
        POIPoint point1 = new POIPoint(new GeoPoint(28.011581, 86.907036, 6200));
        POIPoint point2 = new POIPoint(new GeoPoint(28.017394, 86.922196, 7000));
        point1.setName("point1");
        point2.setName("point2");
        visiblePoints.add(point1);
        visiblePoints.add(point2);

        // points that should not be visible
        POIPoint point3 = new POIPoint(new GeoPoint(27.951538, 86.928781, 6400));
        POIPoint point4 = new POIPoint(new GeoPoint(27.987947, 86.933671, 8000));
        point3.setName("point3");
        point4.setName("point4");


        // Add the points to the points to check
        pointsToCheck.add(point1);
        pointsToCheck.add(point2);
        pointsToCheck.add(point3);
        pointsToCheck.add(point4);

        // Check if the points are filtered correctly
        Assert.assertEquals(new HashSet<>(lineOfSight.getVisiblePoints(pointsToCheck)), new HashSet<>(visiblePoints));
    }
}
