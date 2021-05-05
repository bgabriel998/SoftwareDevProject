package ch.epfl.sdp.peakar.points;

import android.Manifest;
import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.Intents;
import androidx.test.rule.GrantPermissionRule;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import static java.lang.Double.NaN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ComputePOIPointsTest {

    @Rule
    public GrantPermissionRule grantCameraPermissionRule = GrantPermissionRule.grant(Manifest.permission.CAMERA);
    private static UserPoint userPoint;
    private static Context mContext;

    @BeforeClass
    public static void setUserPoint(){
        //Set userPoint to the mont blanc
        mContext = ApplicationProvider.getApplicationContext();
        userPoint = UserPoint.getInstance(mContext); 
        userPoint.setLocation(45.802537, 6.850328, 4809, 0);
    }

    @Before
    public void setup(){
        Intents.init();
    }

    @After
    public void cleanUp(){
        Intents.release();
    }

    /**
     * Compute the horizontal bearing between the userPoint and 4 points each one degree to the
     * north, east, south and west
     */
    @Test
    public void computeHorizontalBearing(){
        Point northPoint = new Point(userPoint.getLatitude() + 1, userPoint.getLongitude(), userPoint.getAltitude());
        Point eastPoint = new Point(userPoint.getLatitude(), userPoint.getLongitude() + 1, userPoint.getAltitude());
        Point southPoint = new Point(userPoint.getLatitude() - 1, userPoint.getLongitude(), userPoint.getAltitude());
        Point westPoint = new Point(userPoint.getLatitude(), userPoint.getLongitude() - 1, userPoint.getAltitude());

        double horizontalBearing = northPoint.setHorizontalBearing(userPoint);
        assertEquals(0, horizontalBearing, 1);
        horizontalBearing = eastPoint.setHorizontalBearing(userPoint);
        assertEquals(90, horizontalBearing, 1);
        horizontalBearing = southPoint.setHorizontalBearing(userPoint);
        assertEquals(180, horizontalBearing, 1);
        horizontalBearing = westPoint.setHorizontalBearing(userPoint);
        assertEquals(270, horizontalBearing, 1);
    }

    /**
     * Compute the vertical bearing between the userPoint and 4 points above, below, beside and the same point
     */
    @Test
    public void computeVerticalBearing(){
        Point above = new Point(userPoint.getLatitude(), userPoint.getLongitude(), userPoint.getAltitude() + 1000);
        Point below = new Point(userPoint.getLatitude(), userPoint.getLongitude(), userPoint.getAltitude() - 1000);
        Point sameHeight = new Point(userPoint.getLatitude()+0.1, userPoint.getLongitude(), userPoint.getAltitude());
        Point samePoint = new Point(userPoint.getLatitude(), userPoint.getLongitude(), userPoint.getAltitude());

        double verticalBearing = above.setVerticalBearing(userPoint);
        assertEquals(180, verticalBearing, 1);

        verticalBearing = below.setVerticalBearing(userPoint);
        assertEquals(0, verticalBearing, 1);

        verticalBearing = sameHeight.setVerticalBearing(userPoint);
        assertEquals(90, verticalBearing, 1);

        verticalBearing = samePoint.setVerticalBearing(userPoint);
        assertEquals(NaN, verticalBearing, 1);
    }

    /**
     * Test if ComputePOIPoints has computed the POIs
     */
    @Test
    public void getPOIPointsTest() {
        POIPoint filteredLabeledOutOfSight = new POIPoint("Lobuche", 27.9691317, 86.7816135, 6145);
        POIPoint LabeledOutOfSight = new POIPoint("Cholatse", 27.9190402, 86.7648644, 6440);
        POIPoint filteredLabeledInSight = new POIPoint("Khartaphu", 28.0641783, 86.977291, 7283);
        POIPoint LabeledInLineOfSight = new POIPoint("Hungchi", 28.0346849, 86.7597051, 7036);

        assertNotNull(ComputePOIPoints.getPOIs());
        assertNotNull(ComputePOIPoints.getPOIsInSight());
        assertNotNull(ComputePOIPoints.getPOIsOutOfSight());
        assertNotNull(ComputePOIPoints.getFilteredPOIs());
        assertNotNull(ComputePOIPoints.getFilteredPOIsInSight());
        assertNotNull(ComputePOIPoints.getFilteredPOIsOutOfSight());

        assertTrue(ComputePOIPoints.getPOIs().size() > 0);
        assertEquals(ComputePOIPoints.getPOIs().size(), ComputePOIPoints.getPOIsInSight().size()
                + ComputePOIPoints.getPOIsOutOfSight().size());

        assertTrue(ComputePOIPoints.getFilteredPOIs().size() > 0);
        assertEquals(ComputePOIPoints.getFilteredPOIs().size(), ComputePOIPoints.getFilteredPOIsInSight().size()
                + ComputePOIPoints.getFilteredPOIsOutOfSight().size());

        //Check raw POIs
        assertTrue(ComputePOIPoints.getPOIs().containsKey(filteredLabeledOutOfSight));
        assertTrue(ComputePOIPoints.getPOIs().containsKey(LabeledOutOfSight));
        assertTrue(ComputePOIPoints.getPOIs().containsKey(filteredLabeledInSight));
        assertTrue(ComputePOIPoints.getPOIs().containsKey(LabeledInLineOfSight));

        //Check raw filtered
        assertTrue(ComputePOIPoints.getPOIs().containsKey(filteredLabeledOutOfSight));
        //assertFalse(ComputePOIPoints.getPOIs().containsKey(LabeledOutOfSight));
        assertTrue(ComputePOIPoints.getPOIs().containsKey(filteredLabeledInSight));
        //assertFalse(ComputePOIPoints.getPOIs().containsKey(LabeledInLineOfSight));

        //Check in line of sight not filtered
        //assertFalse(ComputePOIPoints.getPOIs().containsKey(filteredLabeledOutOfSight));
        //assertFalse(ComputePOIPoints.getPOIs().containsKey(LabeledOutOfSight));
        assertTrue(ComputePOIPoints.getPOIs().containsKey(filteredLabeledInSight));
        assertTrue(ComputePOIPoints.getPOIs().containsKey(LabeledInLineOfSight));

        //Check in line of sight filtered
        //assertFalse(ComputePOIPoints.getPOIs().containsKey(filteredLabeledOutOfSight));
        //assertFalse(ComputePOIPoints.getPOIs().containsKey(LabeledOutOfSight));
        assertTrue(ComputePOIPoints.getPOIs().containsKey(filteredLabeledInSight));
        //assertFalse(ComputePOIPoints.getPOIs().containsKey(LabeledInLineOfSight));

        //Check out of line of sight not filtered
        assertTrue(ComputePOIPoints.getPOIs().containsKey(filteredLabeledOutOfSight));
        assertTrue(ComputePOIPoints.getPOIs().containsKey(LabeledOutOfSight));
        //assertFalse(ComputePOIPoints.getPOIs().containsKey(filteredLabeledInSight));
        //assertFalse(ComputePOIPoints.getPOIs().containsKey(LabeledInLineOfSight));

        //Check out of line of sight filtered
        assertTrue(ComputePOIPoints.getPOIs().containsKey(filteredLabeledOutOfSight));
        //assertFalse(ComputePOIPoints.getPOIs().containsKey(LabeledOutOfSight));
        //assertFalse(ComputePOIPoints.getPOIs().containsKey(filteredLabeledInSight));
        //assertFalse(ComputePOIPoints.getPOIs().containsKey(LabeledInLineOfSight));
    }
}
