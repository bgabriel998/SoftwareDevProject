package com.github.ravifrancesco.softwaredevproject;

import android.content.Context;
import android.os.AsyncTask;

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
    private AsyncTask downloadTask;
    //private static List<POIPoint>
    @Before
    public void setup() throws InterruptedException {
        Context mContext = ApplicationProvider.getApplicationContext();

        userPoint = new UserPoint(mContext);
        userPoint.setLocation(GPSTracker.DEFAULT_LAT, GPSTracker.DEFAULT_LON,GPSTracker.DEFAULT_ALT, GPSTracker.DEFAULT_ACC);
        downloadTask = new DownloadTopographyTask(){
            @Override
            public void onResponseReceived(Pair<int[][], Double> topography) {
                super.onResponseReceived(topography);
                topographyPair = topography;
            }
        }.execute(userPoint);

        //Wait for the map to be downloaded
        int counter=0;
        while(topographyPair==null && counter<40){
            Thread.sleep(500);
            counter++;
        }
        //Wait for the map to be downloaded
        counter=0;
        while(topographyPair.first==null && counter<40){
            Thread.sleep(500);
            counter++;
        }
    }

    @Test
    public void topographyNull(){
        Pair<int[][], Double> topo = new Pair<>(null, 0.0);
        elevationMapAsync = new ElevationMapAsync(topo, userPoint);
        Assert.assertNull(elevationMapAsync.getIndexesFromCoordinates(userPoint.getLatitude(), userPoint.getLongitude()));
        Assert.assertEquals(0, elevationMapAsync.getAltitudeAtLocation(userPoint.getLatitude(), userPoint.getLongitude()));
    }

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
        while(elevationMap.getTopographyMap()==oldTopographyMap && counter<60){
            Thread.sleep(500);
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
     *
     */
    @Test
    public void getVisiblePoints(){
//        LineOfSightAsync lineOfSight = new LineOfSightAsync(topographyPair, userPoint);
//        POIPoint p1 = new POIPoint("p1", userPoint.getLatitude()+0.1, userPoint.getLongitude()+0.1, (long) (userPoint.getAltitude() + 2000));
//        POIPoint p2 = new POIPoint("p2", userPoint.getLatitude()+0.1, userPoint.getLongitude()-0.1, (long) (userPoint.getAltitude() + 2000));
//        POIPoint p3 = new POIPoint("p3", userPoint.getLatitude()-0.1, userPoint.getLongitude()+0.1, (long) (userPoint.getAltitude() - 2000));
//        POIPoint p4 = new POIPoint("p4", userPoint.getLatitude()-0.1, userPoint.getLongitude()-0.1, (long) (userPoint.getAltitude() - 2000));
//        List<POIPoint> poiPoints = new ArrayList<>();
//        poiPoints.add(p1);
//        poiPoints.add(p2);
//        poiPoints.add(p3);
//        poiPoints.add(p4);
//        List<POIPoint> visiblePOIPoints = lineOfSight.getVisiblePoints(poiPoints);
//
//        List<POIPoint> poiPointsVisible = new ArrayList<>();
//        poiPointsVisible.add(p1);
//        poiPointsVisible.add(p2);
//
//        Assert.assertEquals(poiPointsVisible, visiblePOIPoints);

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
        Assert.assertEquals(new HashSet<POIPoint>(lineOfSight.getVisiblePoints(pointsToCheck)), new HashSet<POIPoint>(visiblePoints));
    }
}
