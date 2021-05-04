package ch.epfl.sdp.peakar.points;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


public class POICacheTest {

    @BeforeClass
    public static void setup(){
        //Remove potentially created file (due to test suite)
        deleteCacheFile();
    }


    @AfterClass
    public static void cleanup(){
        //Remove manually created file
        deleteCacheFile();
    }

    public static void deleteCacheFile(){
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

    /*Test the saving and the retrieving to/from cache*/
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

        //Create new cache file
        POICache.getInstance().savePOIDataToCache(inputArrayList,boundingBox,context);

        //Check if file is present
        assertTrue(POICache.getInstance().isCacheFilePresent(context));
        //Check if the user is in the BB
        assertTrue(POICache.getInstance().isUserInBoundingBox(UserPoint.getInstance(context),context));
        //Read created file
        ArrayList<POIPoint> result = POICache.getInstance().getCachedPOIPoints(context);
        //Check cache file content
        assertEquals(inputArrayList.size(), result.size());
        assertTrue(inputArrayList.contains(result.get(0)));
        assertTrue(inputArrayList.contains(result.get(1)));
        assertTrue(inputArrayList.contains(result.get(2)));
        assertTrue(inputArrayList.contains(result.get(3)));
    }
}
