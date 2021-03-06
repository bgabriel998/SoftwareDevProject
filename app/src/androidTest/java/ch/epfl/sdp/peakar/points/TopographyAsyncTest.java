package ch.epfl.sdp.peakar.points;

import android.content.Context;

import androidx.core.util.Pair;
import androidx.test.core.app.ApplicationProvider;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import ch.epfl.sdp.peakar.utils.SettingsUtilities;
import ch.epfl.sdp.peakar.utils.TestingConstants;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
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
    public void A_topographyNull(){
        Pair<int[][], Double> topo = new Pair<>(null, 0.0);
        ElevationMap elevationMapNull = new ElevationMap(topo, userPoint, mContext);
        Assert.assertNull(elevationMapNull.getIndexesFromCoordinates(userPoint.getLatitude(), userPoint.getLongitude()));
        Assert.assertEquals(0, elevationMapNull.getAltitudeAtLocation(userPoint.getLatitude(), userPoint.getLongitude()));
    }

    /**
     * Tests that the topographyMap is not null
     */
    @Test
    public void B_topographyNotNull(){
        Assert.assertNotNull(topographyPair);
        Assert.assertNotNull(topographyPair.first);
        ElevationMap elevationMap = new ElevationMap(topographyPair, userPoint, mContext);
        Assert.assertArrayEquals(topographyPair.first, elevationMap.getTopographyMap());
    }

    /**
     * Test that the map cell size is computed correctly
     */
    @Test
    public void C_getMapCellSizeTest() {

        UserPoint userPoint = UserPoint.getInstance(mContext);
        userPoint.setLocation(GPSTracker.DEFAULT_LAT, GPSTracker.DEFAULT_LON,GPSTracker.DEFAULT_ALT, GPSTracker.DEFAULT_ACC);

        ElevationMap elevationMap = new ElevationMap(topographyPair, userPoint, mContext);

        Assert.assertEquals(0.000833333333, elevationMap.getMapCellSize(), 0.00000001);
    }

    /**
     * Test that the bounding box is returned correctly
     */
    @Test
    public void D_getBoundingBoxWestLongTest() {

        UserPoint userPoint = UserPoint.getInstance(mContext);
        userPoint.setLocation(GPSTracker.DEFAULT_LAT, GPSTracker.DEFAULT_LON,GPSTracker.DEFAULT_ALT, GPSTracker.DEFAULT_ACC);

        ElevationMap elevationMap = new ElevationMap(topographyPair, userPoint, mContext);

        Assert.assertEquals(userPoint.computeBoundingBox(SettingsUtilities.getSelectedRange(mContext)).getLonWest(), elevationMap.getBoundingBoxWestLong(), 0.00000001);
    }

    /**
     * This tests checks if the POIPoints are labeled correctly by the
     * getVisiblePointsLabeled method.
     */
    @Test
    public void E_getVisiblePointsLabeledTest() {

        LineOfSight lineOfSight = new LineOfSight(topographyPair, userPoint, mContext);

        // PoiPoints to check
        List<POIPoint> pointsToCheck = new ArrayList<>();
        POIPoint point1 = new POIPoint(new GeoPoint(28.011581, 86.907036, 6200));
        POIPoint point2 = new POIPoint(new GeoPoint(28.017394, 86.922196, 7000));
        POIPoint point3 = new POIPoint(new GeoPoint(27.987947, 86.933671, 8000));
        point1.setName("point1");
        point2.setName("point2");
        point3.setName("point3");
        // points that should not be visible
        POIPoint point4 = new POIPoint(new GeoPoint(27.951538, 86.928781, 6400));
        point4.setName("point4");

        // Add the points to the points to check
        pointsToCheck.add(point1);
        pointsToCheck.add(point2);
        pointsToCheck.add(point3);
        pointsToCheck.add(point4);

        // Create the test map
        Map<POIPoint, Boolean> labeledPOIPoints = new HashMap<>();
        labeledPOIPoints.put(point1, true);
        labeledPOIPoints.put(point2, true);
        labeledPOIPoints.put(point3, true);
        labeledPOIPoints.put(point4, false);

        // Check if the points are labeled correctly
        Assert.assertEquals(labeledPOIPoints, lineOfSight.getVisiblePointsLabeled(pointsToCheck));

    }

    /**
     * This tests checks if the POIPoints are filtered correctly by the
     * getVisiblePoints method.
     */
    @Test
    public void F_getVisiblePointsTest(){

        LineOfSight lineOfSight = new LineOfSight(topographyPair, userPoint, mContext);

        // PoiPoints to check
        List<POIPoint> pointsToCheck = new ArrayList<>();
        // POIPoints that should be visible
        List<POIPoint> visiblePoints = new ArrayList<>();
        POIPoint point1 = new POIPoint(new GeoPoint(28.011581, 86.907036, 6200));
        POIPoint point2 = new POIPoint(new GeoPoint(28.017394, 86.922196, 7000));
        POIPoint point3 = new POIPoint(new GeoPoint(27.987947, 86.933671, 8000));
        point1.setName("point1");
        point2.setName("point2");
        point3.setName("point3");
        visiblePoints.add(point1);
        visiblePoints.add(point2);
        visiblePoints.add(point3);

        // points that should not be visible
        POIPoint point4 = new POIPoint(new GeoPoint(27.951538, 86.928781, 6400));
        point4.setName("point4");


        // Add the points to the points to check
        pointsToCheck.add(point1);
        pointsToCheck.add(point2);
        pointsToCheck.add(point3);
        pointsToCheck.add(point4);

        // Check if the points are filtered correctly
        Assert.assertEquals(new HashSet<>(lineOfSight.getVisiblePoints(pointsToCheck)), new HashSet<>(visiblePoints));
    }

    /**
     * Checks if the altitude is calculated correctly and that the elevation map gets udated correctly
     */
    @Test
    public void G_getAltitudeAndIndexesTest() throws InterruptedException {

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
        Assert.assertEquals(TestingConstants.MOUNT_EVEREST_ALT, elevationMap.getAltitudeAtLocation(indexes.first, indexes.second), 200);

        int[][] oldTopographyMap = topographyPair.first;

        // set location near the Mont Blanc
        userPoint.setLocation(TestingConstants.NEAR_MONT_BLANC_ONE_LAT, TestingConstants.NEAR_MONT_BLANC_ONE_LON, 0, 0);
        elevationMap.updateElevationMatrix();

        //Wait for the map to be updated
        int counter=0;
        while(elevationMap.getTopographyMap()==oldTopographyMap && counter<50){
            Thread.sleep(TestingConstants.THREAD_SLEEP_1S);
            counter++;
        }

        // check the altitude around the Mont Blanc Peak using indexes
        indexes = elevationMap.getIndexesFromCoordinates(TestingConstants.NEAR_MONT_BLANC_TWO_LAT, TestingConstants.NEAR_MONT_BLANC_TWO_LON);
        // check the indexes around the Mont Blanc Peak
        Assert.assertEquals(new Pair<>(502, 790), indexes);
        Assert.assertNotNull(indexes);
        Assert.assertNotNull(indexes.first);
        Assert.assertNotNull(indexes.second);
        Assert.assertEquals(TestingConstants.NEAR_MONT_BLANC_TWO_ALT, elevationMap.getAltitudeAtLocation(indexes.first, indexes.second), 200);
        // check the altitude around the Mont Blanc Peak using coordinates
        Assert.assertEquals(TestingConstants.NEAR_MONT_BLANC_TWO_ALT, elevationMap.getAltitudeAtLocation(TestingConstants.NEAR_MONT_BLANC_TWO_LAT, TestingConstants.NEAR_MONT_BLANC_TWO_LON), 200);
    }

}
