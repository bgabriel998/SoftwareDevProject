package ch.epfl.sdp.peakar.points;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.core.util.Pair;
import androidx.preference.PreferenceManager;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.Intents;
import androidx.test.rule.GrantPermissionRule;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.osmdroid.util.BoundingBox;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ch.epfl.sdp.peakar.R;
import ch.epfl.sdp.peakar.general.SettingsMapActivity;
import ch.epfl.sdp.peakar.utils.OfflineContentContainer;

import static java.lang.Double.NaN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ComputePOIPointsTest {

    private static ComputePOIPoints computePOIPointsInstance;

    @Rule
    public GrantPermissionRule grantCameraPermissionRule = GrantPermissionRule.grant(Manifest.permission.CAMERA);
    private static UserPoint userPoint;
    private static Context mContext;

    @BeforeClass
    public static void setUserPoint() {
        //Set userPoint to the mont blanc
        mContext = ApplicationProvider.getApplicationContext();
        userPoint = UserPoint.getInstance(mContext);
        computePOIPointsInstance = ComputePOIPoints.getInstance(mContext);
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
     * Test if computePOIPointsInstance has computed the POIs
     */

    // TODO Fix test.
    //@Test
    public void getPOIPointsTest() {
        POIPoint filteredLabeledOutOfSight = new POIPoint("Lobuche", 27.9691317, 86.7816135, 6145);
        POIPoint LabeledOutOfSight = new POIPoint("Baruntse", 27.8720615, 86.9796489, 7075);
        POIPoint filteredLabeledInSight = new POIPoint("Khartaphu", 28.0641783, 86.977291, 7283);
        POIPoint LabeledInLineOfSight = new POIPoint("Hungchi", 28.0346849, 86.7597051, 7036);

        assertNotNull(computePOIPointsInstance.getPOIs());
        assertNotNull(computePOIPointsInstance.getPOIsInSight());
        assertNotNull(computePOIPointsInstance.getPOIsOutOfSight());
        assertNotNull(computePOIPointsInstance.getFilteredPOIs());
        assertNotNull(computePOIPointsInstance.getFilteredPOIsInSight());
        assertNotNull(computePOIPointsInstance.getFilteredPOIsOutOfSight());

        assertTrue(computePOIPointsInstance.getPOIs().size() > 0);
        assertEquals(computePOIPointsInstance.getPOIs().size(), computePOIPointsInstance.getPOIsInSight().size()
                + computePOIPointsInstance.getPOIsOutOfSight().size());

        assertTrue(computePOIPointsInstance.getFilteredPOIs().size() > 0);
        assertEquals(computePOIPointsInstance.getFilteredPOIs().size(), computePOIPointsInstance.getFilteredPOIsInSight().size()
                + computePOIPointsInstance.getFilteredPOIsOutOfSight().size());

        //Check raw POIs
        assertTrue(computePOIPointsInstance.getPOIs().containsKey(filteredLabeledOutOfSight));
        assertTrue(computePOIPointsInstance.getPOIs().containsKey(LabeledOutOfSight));
        assertTrue(computePOIPointsInstance.getPOIs().containsKey(filteredLabeledInSight));
        assertTrue(computePOIPointsInstance.getPOIs().containsKey(LabeledInLineOfSight));

        //Check raw filtered
        assertTrue(computePOIPointsInstance.getFilteredPOIs().containsKey(filteredLabeledOutOfSight));
        assertFalse(computePOIPointsInstance.getFilteredPOIs().containsKey(LabeledOutOfSight));
        assertTrue(computePOIPointsInstance.getFilteredPOIs().containsKey(filteredLabeledInSight));
        assertFalse(computePOIPointsInstance.getFilteredPOIs().containsKey(LabeledInLineOfSight));

        //Check in line of sight not filtered
        assertFalse(computePOIPointsInstance.getPOIsInSight().containsKey(filteredLabeledOutOfSight));
        assertFalse(computePOIPointsInstance.getPOIsInSight().containsKey(LabeledOutOfSight));
        assertTrue(computePOIPointsInstance.getPOIsInSight().containsKey(filteredLabeledInSight));
        assertTrue(computePOIPointsInstance.getPOIsInSight().containsKey(LabeledInLineOfSight));

        //Check in line of sight filtered
        assertFalse(computePOIPointsInstance.getFilteredPOIsInSight().containsKey(filteredLabeledOutOfSight));
        assertFalse(computePOIPointsInstance.getFilteredPOIsInSight().containsKey(LabeledOutOfSight));
        assertTrue(computePOIPointsInstance.getFilteredPOIsInSight().containsKey(filteredLabeledInSight));
        assertFalse(computePOIPointsInstance.getFilteredPOIsInSight().containsKey(LabeledInLineOfSight));

        //Check out of line of sight not filtered
        assertTrue(computePOIPointsInstance.getPOIsOutOfSight().containsKey(filteredLabeledOutOfSight));
        assertTrue(computePOIPointsInstance.getPOIsOutOfSight().containsKey(LabeledOutOfSight));
        assertFalse(computePOIPointsInstance.getPOIsOutOfSight().containsKey(filteredLabeledInSight));
        assertFalse(computePOIPointsInstance.getPOIsOutOfSight().containsKey(LabeledInLineOfSight));

        //Check out of line of sight filtered
        assertTrue(computePOIPointsInstance.getFilteredPOIsOutOfSight().containsKey(filteredLabeledOutOfSight));
        assertFalse(computePOIPointsInstance.getFilteredPOIsOutOfSight().containsKey(LabeledOutOfSight));
        assertFalse(computePOIPointsInstance.getFilteredPOIsOutOfSight().containsKey(filteredLabeledInSight));
        assertFalse(computePOIPointsInstance.getFilteredPOIsOutOfSight().containsKey(LabeledInLineOfSight));
    }

    /* Test if the offline content is loaded correctly */
    @Test
    public void loadPOIsFromFileTest() {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

        // Activate offline mode
        // Offline mode not activated, reset shared preference
        prefs.edit()
                .putBoolean(mContext.getResources().getString(R.string.offline_mode_key), true)
                .apply();



        BoundingBox mBoundingBox = userPoint.computeBoundingBox(GeonamesHandler.DEFAULT_RANGE_IN_KM);
        System.out.println(mBoundingBox.getCenterWithDateLine());

        List<POIPoint> mPoiPoints = new ArrayList<>();
        POIPoint pointOne = new POIPoint("pointOne", 45.802540, 6.850330, 3000);
        POIPoint pointTwo = new POIPoint("pointTwo", 45.802536, 6.850326, 5000);
        mPoiPoints.add(pointOne);
        mPoiPoints.add(pointTwo);

        int[][] mElevationMap = {{0,1}, {2,3}};
        Pair<int[][], Double> mTopography =  new Pair<>(mElevationMap,  0.1);

        OfflineContentContainer offlineContentContainer = new OfflineContentContainer();
        offlineContentContainer.topography = mTopography;
        offlineContentContainer.boundingBox = mBoundingBox;
        offlineContentContainer.POIPoints = mPoiPoints;

        saveJson(offlineContentContainer);

        userPoint.update();

        Map<POIPoint, Boolean> loadedPOIPoints =  computePOIPointsInstance.getPOIs();

        Assert.assertTrue(loadedPOIPoints.containsKey(pointOne));
        Assert.assertTrue(loadedPOIPoints.containsKey(pointTwo));

        userPoint.setLocation(0, 0, 0, 0);

        userPoint.update();

        loadedPOIPoints =  computePOIPointsInstance.getPOIs();

        Assert.assertTrue(loadedPOIPoints.isEmpty());

        // Reset location
        userPoint.setLocation(45.802537, 6.850328, 4809, 0);

    }


    /* Helper method to save Json */
    private void saveJson(OfflineContentContainer saveObject) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonString = gson.toJson(saveObject);
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(mContext.openFileOutput(SettingsMapActivity.OFFLINE_CONTENT_FILE, Context.MODE_PRIVATE));
            outputStreamWriter.write(jsonString);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }
}
