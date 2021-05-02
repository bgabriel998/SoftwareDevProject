package ch.epfl.sdp.peakar.points;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

import org.osmdroid.util.BoundingBox;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class POICache {
    //Constants
    private static final float INNER_BOUNDING_BOX_SCALING_FACTOR = 0.5f;
    private static final String CACHE_FILE_NAME = "POIPointsCache.txt";


    private static POICache instance;
    /*List of POIs contained in the cache*/
    private static ArrayList<POIPoint> cachedPOIPoints;
    /*Bounding box saved to cache*/
    private static BoundingBox cachedBoundingBox;

    /**
     * Private constructor: the class is a singleton
     */
    private POICache(){
        cachedPOIPoints = null;
        cachedBoundingBox = null;
    }

    /**
     * Get singleton
     * @return POICache instance
     */
    public static POICache getInstance(){
        if(instance == null)
            instance = new POICache();
        return instance;
    }

    /**
     * Save downloaded POIs and bounding box to cache file
     */
    public void savePOIDataToCache(ArrayList<POIPoint> cachedPOIPoints, BoundingBox cachedBoundingBox, Context context){
        //Create a new object with all needed information to save in JSON
        POICacheContent poiCacheContent = new POICacheContent(cachedPOIPoints,cachedBoundingBox);
        Gson gson = new Gson();
        //Convert to JSON
        String serializesCache = gson.toJson(poiCacheContent);
        saveJson(serializesCache,context);
    }

    /**
     * Save the serialized JSON to text file
     * @param serializedObject string in json format
     * @param context context
     */
    private static void saveJson(String serializedObject,Context context){
        File cacheDir = context.getCacheDir();
        File outputFile = new File(cacheDir,CACHE_FILE_NAME);
        try{
            FileWriter writer = new FileWriter(outputFile);
            writer.append(serializedObject);
            writer.flush();
            writer.close();
        }
        catch(IOException e){
            Log.e("Exception", "File write failed cache POIPoints: " + e.toString());
        }
    }


    /**
     * Read text file and retrieve serialized
     * @return serialized string
     */
    private static String readJSON(Context context){
        File file = new File(context.getCacheDir(),CACHE_FILE_NAME);
        StringBuilder fileContent = new StringBuilder();
        try{
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = bufferedReader.readLine()) != null){
                fileContent.append(line);
                fileContent.append('\n');
            }
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileContent.toString();
    }

    /**
     * Retrieve POI data from cache and overwrite cachedPOIPoints and cachedBoundingBox
     * with cache file values
     */
    private static void retrievePOIDataFromCache(Context context){
        String fileContent = readJSON(context);
        Gson gson = new Gson();
        POICacheContent poiCacheContent = gson.fromJson(fileContent, POICacheContent.class);
        cachedPOIPoints = poiCacheContent.getCachedPOIPoints();
        cachedBoundingBox = poiCacheContent.getCachedBoundingBox();
    }

    /**
     * Check if the user is located inside the middle part of the bounding box
     * Using cached bounding box, a shrink-ed bounding box is computed.
     * The methods then checks if the user position is somewhere inside this
     * smaller bounding box
     * @param userPoint user location
     * @return true if the user is in the middle of the cached bounding box
     *          false if not
     */
    public boolean isUserInBoundingBox(UserPoint userPoint,Context context){
        //Get the bounding box from file if not already present
        if(cachedBoundingBox == null)
            retrievePOIDataFromCache(context);
        //Decrease the size of the bounding box by half
        //BoundingBox innerBox = cachedBoundingBox.increaseByScale(INNER_BOUNDING_BOX_SCALING_FACTOR);
        return cachedBoundingBox.contains(userPoint.getLatitude(), userPoint.getLongitude());
    }

    public boolean isCacheFilePresent(Context context){
        File cacheDir = context.getCacheDir();
        File file = new File(cacheDir,CACHE_FILE_NAME);
        return file.exists();
    }

    /**
     * Returns the list of POI points from the cache
     * @return list of POI Points
     */
    public ArrayList<POIPoint> getCachedPOIPoints(Context context){
        if(cachedPOIPoints == null)
            retrievePOIDataFromCache(context);
        return cachedPOIPoints;
    }
}
