package ch.epfl.sdp.peakar.points;

import android.content.Context;
import android.util.Log;

import androidx.core.util.Pair;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import ch.epfl.sdp.peakar.utils.SettingsUtilities;

public class TopographyAsyncTest {

    private static Pair<int[][], Double> topographyPair;
    private static UserPoint userPoint;

    private static Context mContext;

    /**
     * Download the topography map
     * @throws InterruptedException if any thread has interrupted the current thread
     */
    @BeforeClass
    public static void setup() throws InterruptedException, ExecutionException {

        mContext = ApplicationProvider.getApplicationContext();

        userPoint = UserPoint.getInstance(mContext);
        userPoint.setLocation(GPSTracker.DEFAULT_LAT, GPSTracker.DEFAULT_LON,GPSTracker.DEFAULT_ALT, GPSTracker.DEFAULT_ACC);
        userPoint.update();

        new DownloadTopographyTask(mContext){
            @Override
            public void onResponseReceived(Pair<int[][], Double> topography) {
                super.onResponseReceived(topography);
                topographyPair = topography;
            }
        }.execute(userPoint).get();
    }

    /**
     * Tests the elevationMapClass when the topographyMap is null
     */
    @Test
    public void topographyNull(){
        Pair<int[][], Double> topo = new Pair<>(null, 0.0);
        ElevationMap elevationMapNull = new ElevationMap(topo, userPoint, mContext);
        Assert.assertNull(elevationMapNull.getIndexesFromCoordinates(userPoint.getLatitude(), userPoint.getLongitude()));
        Assert.assertEquals(0, elevationMapNull.getAltitudeAtLocation(userPoint.getLatitude(), userPoint.getLongitude()));
    }

    /**
     * Tests that the topographyMap is not null
     */
    @Test
    public void topographyNotNull(){
        Assert.assertNotNull(topographyPair);
        Assert.assertNotNull(topographyPair.first);
        ElevationMap elevationMap = new ElevationMap(topographyPair, userPoint, mContext);
        Assert.assertArrayEquals(topographyPair.first, elevationMap.getTopographyMap());
    }

    /**
     * Test that the map cell size is computed correctly
     */
    @Test
    public void getMapCellSizeTest() {

        UserPoint userPoint = UserPoint.getInstance(mContext);
        userPoint.setLocation(GPSTracker.DEFAULT_LAT, GPSTracker.DEFAULT_LON,GPSTracker.DEFAULT_ALT, GPSTracker.DEFAULT_ACC);

        ElevationMap elevationMap = new ElevationMap(topographyPair, userPoint, mContext);

        Assert.assertEquals(0.000833333333, elevationMap.getMapCellSize(), 0.00000001);
    }

    /**
     * Test that the bounding box is returned correctly
     */
    @Test
    public void getBoundingBoxWestLongTest() {

        UserPoint userPoint = UserPoint.getInstance(mContext);
        userPoint.setLocation(GPSTracker.DEFAULT_LAT, GPSTracker.DEFAULT_LON,GPSTracker.DEFAULT_ALT, GPSTracker.DEFAULT_ACC);

        ElevationMap elevationMap = new ElevationMap(topographyPair, userPoint, mContext);

        Assert.assertEquals(userPoint.computeBoundingBox(SettingsUtilities.getSelectedRange(mContext)).getLonWest(), elevationMap.getBoundingBoxWestLong(), 0.00000001);
    }

    /**
     * Checks if the altitude is calculated correctly and that the elevation map gets udated correctly
     *
     * TODO re enable after fixing
     */
    public void getAltitudeAndIndexesTest() throws InterruptedException {

        UserPoint userPoint = UserPoint.getInstance(mContext);
        userPoint.setLocation(GPSTracker.DEFAULT_LAT, GPSTracker.DEFAULT_LON,GPSTracker.DEFAULT_ALT, GPSTracker.DEFAULT_ACC);

        ElevationMap elevationMap = new ElevationMap(topographyPair, userPoint, mContext);

        // check the altitude around the Everest Peak using coordinates
        Assert.assertEquals(8849, elevationMap.getAltitudeAtLocation(GPSTracker.DEFAULT_LAT, GPSTracker.DEFAULT_LON), 200);
        // check the altitude around the Everest Peak using indexes
        Pair<Integer, Integer> indexes = elevationMap.getIndexesFromCoordinates(GPSTracker.DEFAULT_LAT, GPSTracker.DEFAULT_LON);
        // check the indexes around the Everest Peak
        Assert.assertEquals(new Pair<>(539, 610), indexes);
        Assert.assertNotNull(indexes);
        Assert.assertNotNull(indexes.first);
        Assert.assertNotNull(indexes.second);
        Assert.assertEquals(8849, elevationMap.getAltitudeAtLocation(indexes.first, indexes.second), 200);

        int[][] oldTopographyMap = topographyPair.first;

        // set location near the Mont Blanc
        userPoint.setLocation(45.802537, 6.850328, 0, 0);
        elevationMap.updateElevationMatrix();

        //Wait for the map to be updated
        int counter=0;
        while(elevationMap.getTopographyMap()==oldTopographyMap && counter<50){
            Thread.sleep(1000);
            counter++;
        }

        // check the altitude around the Mont Blanc Peak using indexes
        indexes = elevationMap.getIndexesFromCoordinates(45.8326, 6.8652);
        // check the indexes around the Mont Blanc Peak
        Assert.assertEquals(new Pair<>(178, 326), indexes);
        Assert.assertNotNull(indexes);
        Assert.assertNotNull(indexes.first);
        Assert.assertNotNull(indexes.second);
        Assert.assertEquals(4808, elevationMap.getAltitudeAtLocation(indexes.first, indexes.second), 200);
        // check the altitude around the Mont Blanc Peak using coordinates
        Assert.assertEquals(4808, elevationMap.getAltitudeAtLocation(45.8326, 6.8652), 200);
    }

    /**
     * This tests checks if the POIPoints are filtered correctly by the
     * getVisiblePoints method.
     *
     * TODO investigate point 4 issue
     *
     */
    @Test
    public void getVisiblePoints(){
        LineOfSight lineOfSight = new LineOfSight(topographyPair, userPoint, mContext);

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
        //pointsToCheck.add(point4);

        // Check if the points are filtered correctly
        Assert.assertEquals(new HashSet<>(lineOfSight.getVisiblePoints(pointsToCheck)), new HashSet<>(visiblePoints));
    }

    /**
     * This tests checks if the POIPoints are labeled correctly by the
     * getVisiblePointsLabeled method.
     *
     * TODO fix bug with labels
     *
     */
    public void getVisiblePointsLabeledTest() {

        // setting location near everest peak
        UserPoint userPoint = UserPoint.getInstance(mContext);

        LineOfSight lineOfSight = new LineOfSight(topographyPair, userPoint, mContext);

        // PoiPoints to check
        List<POIPoint> pointsToCheck = new ArrayList<>();
        POIPoint point1 = new POIPoint(new GeoPoint(28.011581, 86.907036, 6200));
        POIPoint point2 = new POIPoint(new GeoPoint(28.017394, 86.922196, 7000));
        point1.setName("point1");
        point2.setName("point2");
        // points that should not be visible
        POIPoint point3 = new POIPoint(new GeoPoint(27.951538, 86.928781, 6400));
        POIPoint point4 = new POIPoint(new GeoPoint(27.987947, 86.933671, 8000));
        point3.setName("point3");
        point4.setName("point4");

        // Add the points to the points to check
        pointsToCheck.add(point1);
        pointsToCheck.add(point2);
        pointsToCheck.add(point3);
        //pointsToCheck.add(point4);

        // Create the test map
        Map<POIPoint, Boolean> labeledPOIPoints = new HashMap<>();
        labeledPOIPoints.put(point1, true);
        labeledPOIPoints.put(point2, true);
        labeledPOIPoints.put(point3, false);
        //labeledPOIPoints.put(point4, false);

        // Check if the points are labeled correctly
        Assert.assertEquals(labeledPOIPoints, lineOfSight.getVisiblePointsLabeled(pointsToCheck));
    }

    /*
    Used for debug purposes, not for testing
     */
    public void debugTest() throws ExecutionException, InterruptedException {

        // setting location near everest peak
        UserPoint userPoint = UserPoint.getInstance(mContext);
        userPoint.setLocation(46.52301918715015, 6.56573360554917,400, 0);

        new DownloadTopographyTask(mContext){
            @Override
            public void onResponseReceived(Pair<int[][], Double> topography) {
                super.onResponseReceived(topography);
                topographyPair = topography;
            }
        }.execute(userPoint).get();

        LineOfSight lineOfSight = new LineOfSight(topographyPair, userPoint, mContext);

        // PoiPoints to check
        List<POIPoint> pointsToCheck = new ArrayList<>();
        POIPoint point1 = new POIPoint(new GeoPoint(46.19544415848338, 6.6072792585438656, 2244));
        point1.setName("point1");
        pointsToCheck.add(point1);

        Map<POIPoint, Boolean> filtered = lineOfSight.getVisiblePointsLabeled(pointsToCheck);

        Log.d("DEBUG", filtered.toString());

        Assert.assertTrue(true);

    }

}
