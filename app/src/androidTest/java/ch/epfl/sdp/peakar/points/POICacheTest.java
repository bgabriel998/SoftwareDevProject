package ch.epfl.sdp.peakar.points;

import android.Manifest;
import android.content.Context;

import androidx.core.util.Pair;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.rule.GrantPermissionRule;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;

import java.io.File;
import java.util.ArrayList;

import static ch.epfl.sdp.peakar.TestingConstants.AIGUILLE_DU_PLAN_ALT;
import static ch.epfl.sdp.peakar.TestingConstants.AIGUILLE_DU_PLAN_LAT;
import static ch.epfl.sdp.peakar.TestingConstants.AIGUILLE_DU_PLAN_LONG;
import static ch.epfl.sdp.peakar.TestingConstants.AIGUILLE_DU_PLAN_NAME;
import static ch.epfl.sdp.peakar.TestingConstants.CACHE_FILE_NAME_TEST;
import static ch.epfl.sdp.peakar.TestingConstants.DENT_DU_GEANT_ALT;
import static ch.epfl.sdp.peakar.TestingConstants.DENT_DU_GEANT_LAT;
import static ch.epfl.sdp.peakar.TestingConstants.DENT_DU_GEANT_LONG;
import static ch.epfl.sdp.peakar.TestingConstants.DENT_DU_GEANT_NAME;
import static ch.epfl.sdp.peakar.TestingConstants.MOCK_LOCATION_ALT_CHAMONIX;
import static ch.epfl.sdp.peakar.TestingConstants.MOCK_LOCATION_LAT_CHAMONIX;
import static ch.epfl.sdp.peakar.TestingConstants.MOCK_LOCATION_LON_CHAMONIX;
import static ch.epfl.sdp.peakar.TestingConstants.MONT_BLANC_ALT;
import static ch.epfl.sdp.peakar.TestingConstants.MONT_BLANC_LAT;
import static ch.epfl.sdp.peakar.TestingConstants.MONT_BLANC_LONG;
import static ch.epfl.sdp.peakar.TestingConstants.MONT_BLANC_NAME;
import static ch.epfl.sdp.peakar.TestingConstants.POINTE_DE_LAPAZ_ALT;
import static ch.epfl.sdp.peakar.TestingConstants.POINTE_DE_LAPAZ_LAT;
import static ch.epfl.sdp.peakar.TestingConstants.POINTE_DE_LAPAZ_LONG;
import static ch.epfl.sdp.peakar.TestingConstants.POINTE_DE_LAPAZ_NAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


public class POICacheTest {

    @Rule
    public GrantPermissionRule grantCameraPermissionRule1 = GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE);
    @Rule
    public GrantPermissionRule grantCameraPermissionRule2 = GrantPermissionRule.grant(Manifest.permission.READ_EXTERNAL_STORAGE);


    private static Pair<int[][], Double> topographyPair;
    private static UserPoint userPoint;

    @BeforeClass
    public static void setup() throws InterruptedException {
        //Remove potentially created file (due to test suite)
        deleteCacheFile();
        Context mContext = ApplicationProvider.getApplicationContext();
        userPoint = UserPoint.getInstance(mContext);
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
        Assert.assertNotNull(topographyPair);
        while(topographyPair.first==null && counter<20){
            Thread.sleep(1000);
            counter++;
        }
    }


    @AfterClass
    public static void cleanup(){
        //Remove manually created file
        deleteCacheFile();
    }

    /*Remove cache file --> method used in this test file only*/
    private static void deleteCacheFile(){
        File cacheDir = ApplicationProvider.getApplicationContext().getCacheDir();
        File outputFile = new File(cacheDir,CACHE_FILE_NAME_TEST);
        //noinspection ResultOfMethodCallIgnored
        outputFile.delete();
    }


    /*Test get instance method*/
    @Test
    public void testPOICacheGetInstance(){
        POICache poiCache = POICache.getInstance();
        assertNotNull(poiCache);
    }

    /*Test the saving and the retrieving to/from cache
    * Before this test no old cache file should be present
    * Data is written to the cache manually
    * Data is then retrieved
    *
    */
    @Test
    public void testPOICacheSaveRetrievePOIDataToCache(){
        //Create file containing POIs manually
        Context context = ApplicationProvider.getApplicationContext();
        Assert.assertNotNull(context);
        UserPoint userPoint = UserPoint.getInstance(context);
        GeoPoint geoPoint_1 = new GeoPoint(MONT_BLANC_LAT,MONT_BLANC_LONG,MONT_BLANC_ALT);
        POIPoint point_1 = new POIPoint(geoPoint_1);
        point_1.setName(MONT_BLANC_NAME);

        GeoPoint geoPoint_2 = new GeoPoint(DENT_DU_GEANT_LAT, DENT_DU_GEANT_LONG,DENT_DU_GEANT_ALT);
        POIPoint point_2 = new POIPoint(geoPoint_2);
        point_2.setName(DENT_DU_GEANT_NAME);

        GeoPoint geoPoint_3 = new GeoPoint(AIGUILLE_DU_PLAN_LAT, AIGUILLE_DU_PLAN_LONG,AIGUILLE_DU_PLAN_ALT);
        POIPoint point_3 = new POIPoint(geoPoint_3);
        point_3.setName(AIGUILLE_DU_PLAN_NAME);

        GeoPoint geoPoint_4 = new GeoPoint(POINTE_DE_LAPAZ_LAT, POINTE_DE_LAPAZ_LONG,POINTE_DE_LAPAZ_ALT);
        POIPoint point_4 = new POIPoint(geoPoint_4);
        point_4.setName(POINTE_DE_LAPAZ_NAME);
        ArrayList<POIPoint> inputArrayList;
        inputArrayList = new ArrayList<>();
        inputArrayList.add(point_2);
        inputArrayList.add(point_1);
        inputArrayList.add(point_3);
        inputArrayList.add(point_4);
        userPoint.setLocation(
                MOCK_LOCATION_LAT_CHAMONIX,
                MOCK_LOCATION_LON_CHAMONIX,
                MOCK_LOCATION_ALT_CHAMONIX,
                0);
        BoundingBox boundingBox = userPoint.computeBoundingBox(GeonamesHandler.DEFAULT_RANGE_IN_KM);

        new DownloadTopographyTask(){
            @Override
            public void onResponseReceived(Pair<int[][], Double> topography) {
                super.onResponseReceived(topography);
                //Save POIs, BB and topography to the cache

            }
        }.execute(userPoint);

        //Check if file is present
        assertFalse("Old Cache file found",POICache.getInstance().isCacheFilePresent(context.getCacheDir()));

        //Create new cache file
        POICache.getInstance().savePOIDataToCache(inputArrayList,boundingBox,topographyPair,context.getCacheDir());

        //Check if file is present
        assertTrue("Cache file not found",POICache.getInstance().isCacheFilePresent(context.getCacheDir()));
        //Check if the user is in the BB
        assertTrue("isUserInBoundingBox returned false",POICache.getInstance().isUserInBoundingBox(UserPoint.getInstance(context),context.getCacheDir()));
        //Read created file
        ArrayList<POIPoint> result = POICache.getInstance().getCachedPOIPoints(context.getCacheDir());
        //Check cache file content
        assertEquals("Cache: data written doesn't correspond to data retrieved...",inputArrayList.size(), result.size());
        assertTrue(inputArrayList.contains(result.get(0)));
        assertTrue(inputArrayList.contains(result.get(1)));
        assertTrue(inputArrayList.contains(result.get(2)));
        assertTrue(inputArrayList.contains(result.get(3)));

        Pair<int[][], Double> topography = POICache.getInstance().getCachedTopography(context.getCacheDir());
        //Compare retrieved topo map with initial map
        assertEquals(topographyPair.first,topography.first);
        assertEquals(topographyPair.second,topography.second);
    }
}
