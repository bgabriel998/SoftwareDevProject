package ch.epfl.sdp.peakar.points;

import android.content.Context;
import android.util.Log;

import androidx.test.core.app.ApplicationProvider;

import com.google.gson.Gson;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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

    private static UserPoint userPoint;
    private static Context context;
    private static ArrayList<POIPoint> inputArrayList;


    @BeforeClass
    public static void setup(){
        //Create file containing POIs manually
        context = ApplicationProvider.getApplicationContext();
        Assert.assertNotNull(context);
        userPoint = UserPoint.getInstance(context);
        userPoint.setLocation(
                MOCK_LOCATION_LAT_CHAMONIX,
                MOCK_LOCATION_LON_CHAMONIX,
                MOCK_LOCATION_ALT_CHAMONIX,
                0);
        File file = new File(context.getCacheDir(),CACHE_FILE_NAME_TEST);

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

        inputArrayList = new ArrayList<POIPoint>();
        inputArrayList.add(point_2);
        inputArrayList.add(point_1);
        inputArrayList.add(point_3);
        inputArrayList.add(point_4);
        BoundingBox boundingBox = userPoint.computeBoundingBox(GeonamesHandler.DEFAULT_RANGE_IN_KM);
        //Create mock cache content
        POICacheContent poiCacheContent = new POICacheContent(inputArrayList,boundingBox);
        Gson gson = new Gson();
        //Convert to JSON
        String serializesCache = gson.toJson(poiCacheContent);
        File dataDir = context.getCacheDir();
        File outputFile = new File(dataDir,CACHE_FILE_NAME_TEST);
        //Save serialized version
        try{
            FileWriter writer = new FileWriter(outputFile);
            writer.append(serializesCache);
            writer.flush();
            writer.close();
        }
        catch(IOException e){
            Log.e("Exception", "File write failed cache POIPoints: " + e.toString());
        }

    }

    @AfterClass
    public static void cleanup(){
        //Remove manually created file
        File cacheDir = context.getCacheDir();
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
        POICache poiCache = POICache.getInstance();
        BoundingBox boundingBox = userPoint.computeBoundingBox(GeonamesHandler.DEFAULT_RANGE_IN_KM);
        poiCache.savePOIDataToCache(inputArrayList,boundingBox,context);
        ArrayList<POIPoint> result = poiCache.getCachedPOIPoints(context);
        assertEquals(result.size(), inputArrayList.size());
        assertTrue(inputArrayList.contains(result.get(0)));
        assertTrue(inputArrayList.contains(result.get(1)));
        assertTrue(inputArrayList.contains(result.get(2)));
        assertTrue(inputArrayList.contains(result.get(3)));
    }

    /*check if the user is in the bounding box*/
    @Test
    public void testPOICacheIsUserInBoundingBox(){
        POICache poiCache = POICache.getInstance();
        userPoint.setLocation(
                MOCK_LOCATION_LAT_CHAMONIX,
                MOCK_LOCATION_LON_CHAMONIX,
                MOCK_LOCATION_ALT_CHAMONIX,
                0);
        assertTrue(poiCache.isUserInBoundingBox(userPoint,context));
    }

    /*Check if the cache file is present*/
    @Test
    public void testPOICacheIsCacheFilePresent(){
        POICache poiCache = POICache.getInstance();
        assertTrue(poiCache.isCacheFilePresent(context));
    }


}
