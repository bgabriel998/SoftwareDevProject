package ch.epfl.sdp.peakar.points;

import android.util.Log;

import com.google.gson.Gson;

import org.osmdroid.util.BoundingBox;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Class used to cache list of POIs in the surrounding
 * The list of POI is converted to JSON, the corresponding bounding box
 * is added to the JSON and everything is saved in the cache folder
 * of the android device
 */
public class POICache {
    //Constants
    private static final String CACHE_FILE_NAME = "POIPointsCache.txt";
    private static final float INNER_BOUNDING_BOX_SCALING_FACTOR = 0.5f;

    private static POICache instance;
    /*List of POIs contained in the cache*/
    private static ArrayList<POIPoint> cachedPOIPoints;
    /*Bounding box saved to cache*/
    private static BoundingBox cachedBoundingBox;

    private static Gson gson;

    /**
     * Private constructor: the class is a singleton
     */
    private POICache(){
        cachedPOIPoints = null;
        cachedBoundingBox = null;
        gson = new Gson();
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
    public void savePOIDataToCache(ArrayList<POIPoint> cachedPOIPoints, BoundingBox cachedBoundingBox, File path){
        //Create a new object with all needed information to save in JSON
        POICacheContent poiCacheContent = new POICacheContent(cachedPOIPoints,cachedBoundingBox);
        //Convert to JSON
        String serializesCache = gson.toJson(poiCacheContent);
        saveJson(serializesCache,path);
    }

    /**
     * Save the serialized JSON to text file
     * @param serializedObject string in json format
     * @param path path to cache file
     */
    private static void saveJson(String serializedObject,File path){
        File outputFile = new File(path,CACHE_FILE_NAME);
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
     * Retrieve POI data from cache and overwrite cachedPOIPoints and cachedBoundingBox
     * with cache file values
     */
    private static void retrievePOIDataFromCache(File path){
        String fileContent = readJSON(path);
        POICacheContent poiCacheContent = gson.fromJson(fileContent, POICacheContent.class);
        cachedPOIPoints = poiCacheContent.getCachedPOIPoints();
        cachedBoundingBox = poiCacheContent.getCachedBoundingBox();
    }


    /**
     * Read text file and retrieve serialized
     * @return serialized string
     */
    private static String readJSON(File path){
        File file = new File(path, CACHE_FILE_NAME);
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
     * Check if the user is located inside the middle part of the bounding box
     * Using cached bounding box, a shrink-ed bounding box is computed.
     * The methods then checks if the user position is somewhere inside this
     * smaller bounding box
     * @param userPoint user location
     * @return true if the user is in the middle of the cached bounding box
     *          false if not
     */
    public boolean isUserInBoundingBox(UserPoint userPoint,File path){
        //Get the bounding box from file if not already present
        if(cachedBoundingBox == null)
            retrievePOIDataFromCache(path);
        //Decrease the size of the bounding box by half
        BoundingBox innerBox = cachedBoundingBox.increaseByScale(INNER_BOUNDING_BOX_SCALING_FACTOR);
        return innerBox.contains(userPoint.getLatitude(), userPoint.getLongitude());
    }


    /**
     * Checks if cache file is present in device Cache folder
     * @param path path to cache file
     * @return true if file is present. False if no file found
     */
    public boolean isCacheFilePresent(File path){
        File file = new File(path,CACHE_FILE_NAME);
        return file.exists();
    }

    /**
     * Returns the list of POI points from the cache
     * @return list of POI Points
     */
    public ArrayList<POIPoint> getCachedPOIPoints(File file){
        if(cachedPOIPoints == null)
            retrievePOIDataFromCache(file);
        return cachedPOIPoints;
    }
}
