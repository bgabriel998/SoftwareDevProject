package ch.epfl.sdp.peakar.points;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.core.util.Pair;
import androidx.preference.PreferenceManager;

import com.google.gson.Gson;

import org.osmdroid.bonuspack.location.POI;
import org.osmdroid.util.BoundingBox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.stream.Collectors;

import ch.epfl.sdp.peakar.R;
import ch.epfl.sdp.peakar.general.SettingsMapActivity;
import ch.epfl.sdp.peakar.utils.OfflineContentContainer;

/**
 * Requests the POIPoints around the user location and then downloads the topography map once the
 * POIPoints are available.
 *
 * To use, simply call ComputePOIPoints.POIPoints to get a List of POIPoints or
 * ComputePOIPoints to get a map with the POIPoints as keys and a boolean indicating if the POIPoint
 * is visible or not. If no points are available or if they are not computed yet, they will be null
 *
 * TODO handle the updates more efficiently
 */
public class ComputePOIPoints implements Observer {

    private static final int MAX_LOADING_DISTANCE = 20000; // in m

    private static final int HALF_MARKER_SIZE_WIDTH = 3;
    private static final int HALF_MARKER_SIZE_HEIGHT = 5;

    public static UserPoint userPoint;
  
    @SuppressLint("StaticFieldLeak")
    public static Context ctx;

    private static Map<POIPoint, Boolean> POIs;
    private static Map<POIPoint, Boolean> filteredPOIPoints;
    private static Map<POIPoint, Boolean> labeledPOIs;
    private static Map<POIPoint, Boolean> filteredLabeledPOIPoints;
    private static Map<POIPoint, Boolean> labeledPOIsInSight;
    private static Map<POIPoint, Boolean> filteredLabeledPOIsInSight;
    private static Map<POIPoint, Boolean> labeledPOIsOutOfSight;
    private static Map<POIPoint, Boolean> filteredLabeledPOIsOutOfSight;

    private static boolean isLineOfSightAvailable = false;

    /**
     * Constructor of ComputePOIPoints, updates userPoint and gets the POIs for the userPoint
     * @param context Context of activity
     */
    public ComputePOIPoints(Context context){
        POIs = new HashMap<>();
        ctx = context;
        userPoint = UserPoint.getInstance(context);
        userPoint.update();
        userPoint.addObserver(this);
        getPOIs(userPoint);
    }


    /**
     * Retrieves list of surrounding POIs either from cache
     * or from provider. If the cached data corresponds to
     * the user location, no download is made and the POIs
     * are retrieved from cached file
     * @param userPoint user location
     */
    private static void getPOIs(UserPoint userPoint){
        // clear the old points
        POIs.clear();

        // first check that if offline mode is active
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        boolean offlineModeValue = prefs.getBoolean(ctx.getResources().getString(R.string.offline_mode_key), false);
        if (offlineModeValue) {
           getPOISOffline(userPoint);
           return;
        }
        //Retrieve cache instance
        POICache poiCache = POICache.getInstance();
        //Check if file is present and if user is in BB
        if(poiCache.isCacheFilePresent(ctx.getApplicationContext().getCacheDir()) && poiCache.isUserInBoundingBox(userPoint, ctx.getCacheDir()))
            getPOIsFromCache(userPoint);
        else
            Log.d("DEBUG", "Downmloaded");
            getPOIsFromProvider(userPoint);
    }

    /**
     * Get surrounding POIs from cache
     * @param userPoint location of the user
     */
    private static void getPOIsFromCache(UserPoint userPoint){
        ArrayList<POIPoint> cachedPOIs = POICache.getInstance().getCachedPOIPoints(ctx.getCacheDir());

        cachedPOIs.forEach(poiPoint -> {
            poiPoint.setHorizontalBearing(userPoint);
            poiPoint.setVerticalBearing(userPoint);
            POIs.put(poiPoint, false);
        });
        //TODO use this method to merge caching of the POIs and 3D map
        getLabeledPOIs(userPoint);
        //TODO =========================================<<
    }

    /**
     * Gets the POIs for the userPoint from Provider
     * @param userPoint location of the user
     */
    private static void getPOIsFromProvider(UserPoint userPoint){
        new GeonamesHandler(userPoint){
            @Override
            public void onResponseReceived(ArrayList<POI> result) {
                if(result!=null){
                    for(POI poi : result){
                        POIPoint poiPoint = new POIPoint(poi);
                        poiPoint.setHorizontalBearing(userPoint);
                        poiPoint.setVerticalBearing(userPoint);
                        POIs.put(poiPoint, false);
                    }
                    filteredPOIPoints = filterHighestPOIs(POIs);
                    POICache.getInstance().savePOIDataToCache(new ArrayList<>(POIs.keySet()),
                            userPoint.computeBoundingBox(GeonamesHandler.DEFAULT_RANGE_IN_KM),
                            ctx.getCacheDir());
                    getLabeledPOIs(userPoint);
                }
            }
        }.execute();
    }

    /**
     * Handles the creation and filtration of the list of the POIPoints when offline mode
     * is enables.
     *
     * @param userPoint around which the list is computed.
     */
    private static void getPOISOffline(UserPoint userPoint) {

        try {
            OfflineContentContainer offlineContent = readFromFile();
            getLabeledPOIsOffline(userPoint, offlineContent);
        } catch (IOException e) {
            Log.d("ComputePOIPoints", "There was an error reading the file");
        }

    }

    /**
     * Helper method to load the downloaded json.
     *
     * @return an OfflineContainer containing the downloaded content.
     */
    private static OfflineContentContainer readFromFile() throws IOException {

        Gson gson = new Gson();

        String ret = "";

        InputStream inputStream =  ctx.openFileInput(SettingsMapActivity.OFFLINE_CONTENT_FILE);
        if ( inputStream != null ) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String receiveString;
            StringBuilder stringBuilder = new StringBuilder();
            while ( (receiveString = bufferedReader.readLine()) != null ) {
                stringBuilder.append(receiveString);
            }
            inputStream.close();
            ret = stringBuilder.toString();
        }

        Log.d("ComputePOIPoints", "Offline content downloaded");
        return gson.fromJson(ret, OfflineContentContainer.class);

    }

    /**
     * Gets the labeled POIs and filters them.
     *
     * @param userPoint userPoint for which the labeled POIs are computed.
     */
    private static void getLabeledPOIs(UserPoint userPoint){
        new DownloadTopographyTask(){
            @Override
            public void onResponseReceived(Pair<int[][], Double> topography) {
                super.onResponseReceived(topography);
                LineOfSight lineOfSight = new LineOfSight(topography, userPoint);
                labeledPOIs = lineOfSight.getVisiblePointsLabeled(new ArrayList<>(POIs.keySet()));
                filteredLabeledPOIPoints = filterHighestPOIs(labeledPOIs);

                labeledPOIsInSight = new HashMap<>();
                labeledPOIsOutOfSight = new HashMap<>();

                labeledPOIs.forEach((poi, inSight) -> {
                    if (inSight) {
                        labeledPOIsInSight.put(poi, inSight);
                    } else {
                        labeledPOIsOutOfSight.put(poi, inSight);
                    }
                });

                filteredLabeledPOIsInSight = filterHighestPOIs(labeledPOIsInSight);
                filteredLabeledPOIsOutOfSight = filterHighestPOIs(labeledPOIsOutOfSight);

                isLineOfSightAvailable = true;
            }
        }.execute(userPoint);
    }

    /**
     * Gets the labeled POIs from a JSONObject and filters them. The points are not added if the
     * userPoint is more thant MAX_LOADING_DISTANCE from the center of the downloaded bounding
     * box.
     *
     * @param userPoint         userPoint for which the labeled POIs are computed.
     * @param offlineContent    container containing offline content.
     */
    private static void getLabeledPOIsOffline(UserPoint userPoint, OfflineContentContainer offlineContent) {

        BoundingBox boundingBox = offlineContent.boundingBox;

        double distance = userPoint.computeFlatDistance(new POIPoint(boundingBox.getCenterWithDateLine()));

        Log.d("ComputePOIPoints", "Distance = " + distance);

        if (distance < MAX_LOADING_DISTANCE) {
            Pair<int[][], Double> topography = offlineContent.topography;
            for(POIPoint poiPoint : offlineContent.POIPoints){
              POIs.put(poiPoint, false);
            }
            LineOfSight lineOfSight = new LineOfSight(topography, userPoint);
            labeledPOIs = lineOfSight.getVisiblePointsLabeled(new ArrayList<>(POIs.keySet()));
            filteredLabeledPOIPoints = filterHighestPOIs(labeledPOIs);

            labeledPOIsInSight = new HashMap<>();
            labeledPOIsOutOfSight = new HashMap<>();

            labeledPOIs.forEach((poi, inSight) -> {
                if (inSight) {
                    labeledPOIsInSight.put(poi, inSight);
                } else {
                    labeledPOIsOutOfSight.put(poi, inSight);
                }
            });

            filteredLabeledPOIsInSight = filterHighestPOIs(labeledPOIsInSight);
            filteredLabeledPOIsOutOfSight = filterHighestPOIs(labeledPOIsOutOfSight);

            isLineOfSightAvailable = true;
        } else {
            resetPOIs();
            Log.d("ComputePOIPoints", "Distance is > " + MAX_LOADING_DISTANCE);
        }

    }

    /**
     * Reset lists of POIPoints
     */
    private static void resetPOIs() {
        if (filteredPOIPoints != null) filteredPOIPoints.clear();
        if (labeledPOIs != null) labeledPOIs.clear();
        if (filteredLabeledPOIPoints != null) filteredLabeledPOIPoints.clear();
        if (labeledPOIsInSight != null) labeledPOIsInSight.clear();
        if (filteredLabeledPOIsInSight != null) filteredLabeledPOIsInSight.clear();
        if (labeledPOIsOutOfSight != null) labeledPOIsOutOfSight.clear();
        if (filteredLabeledPOIsOutOfSight != null) filteredLabeledPOIsOutOfSight.clear();
    }


    /**
     * Checks if the line of sight is available or not
     * @return True if the line of sight is available, false otherwise
     */
    public static Boolean isLineOfSightAvailable(){
        return isLineOfSightAvailable;
    }

    /**
     * Gets a Map of all surrounding POIPoints
     * @return Map<POIPoint, Boolean> containing POIPoints.
     */
    public static Map<POIPoint, Boolean> getPOIs(){
        return isLineOfSightAvailable() ? labeledPOIs : POIs;
    }

    /**
     * Gets a filtered Map of all surrounding POIPoints
     * @return Map<POIPoint, Boolean> containing the filtered POIPoints.
     */
    public static Map<POIPoint, Boolean> getFilteredPOIs(){
        return isLineOfSightAvailable() ? filteredLabeledPOIPoints : filteredPOIPoints;
    }

    /**
     * Gets a Map of the POIPoints that are in the line of sight
     * @return Map<POIPoint, Boolean> containing only the POIPoints in the lineOfSight.
     */
    public static Map<POIPoint, Boolean> getPOIsInSight(){
        return isLineOfSightAvailable() ? labeledPOIsInSight : POIs;
    }

    /**
     * Gets a Map of the POIPoints that are in the line of sight and filtered
     * @return Map<POIPoint, Boolean> containing the filtered POIPoints in the lineOfSight.
     */
    public static Map<POIPoint, Boolean> getFilteredPOIsInSight(){
        return isLineOfSightAvailable() ? filteredLabeledPOIsInSight : filteredPOIPoints;
    }

    /**
     * Gets a Map of the POIPoints that are out of the line of sight
     * @return Map<POIPoint, Boolean> containing only the POIPoints out of the lineOfSight.
     */
    public static Map<POIPoint, Boolean> getPOIsOutOfSight(){
        return isLineOfSightAvailable() ? labeledPOIsOutOfSight : POIs;
    }

    /**
     * Gets a Map of the POIPoints that are out of the line of sight and filtered
     * @return Map<POIPoint, Boolean> containing the filtered POIPoints out of the lineOfSight.
     */
    public static Map<POIPoint, Boolean> getFilteredPOIsOutOfSight(){
        return isLineOfSightAvailable() ? filteredLabeledPOIsOutOfSight : filteredPOIPoints;
    }

    /**
     * Filters the labeled POIPoints and takes only the highest s.t. there are no other markers
     * within 6° horizontally. POIPoints in the line of sight are prioritised. If a POIPoint is in
     * the line of sight but has a smaller altitude then another POIPoint that is close but that is
     * not in the line of sight, then only the POIPoint that is in the line of sight will be added
     * @param labeledPOIPoints labeled POIPoints that get sorted
     * @return Map<POIPoint, Boolean> of the highest filtered POIs
     */
     static Map<POIPoint, Boolean> filterHighestPOIs(Map<POIPoint, Boolean> labeledPOIPoints) {
        //sort the labeledPOIPoints by altitude in decreasing order
        Map<POIPoint, Boolean> sortedPoisByAlt = labeledPOIPoints.entrySet().stream()
                .sorted(Collections.reverseOrder(Comparator.comparingDouble(p -> p.getKey().altitude)))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));

        //New map that contains only the filtered POIPoints
        Map<POIPoint, Boolean> filteredPOIs = new HashMap<>();

        //Iterate over all POIPoints in labeledPOIPoints
        for (Map.Entry<POIPoint, Boolean> poiPoint : sortedPoisByAlt.entrySet()) {
            if(comparePoiToFiltered(poiPoint, filteredPOIs)){
                filteredPOIs.put(poiPoint.getKey(), poiPoint.getValue());
            }
        }
        return filteredPOIs;
    }

    /**
     * Compares a entry of a Map<POIPoint, Boolean> to the Map of filteredPOIs to decide if
     * the POIPoint will be added or not to the filteredPOIs Map.
     * @param poiPoint Entry of a Map<POIPoint, Boolean> of the POIPoints that needs to be filtered
     * @param filteredPOIs Map<POIPoint, Boolean> that contains the filtered POIPOints
     * @return True if the POIPoint should be added, false otherwise
     */
    private static boolean comparePoiToFiltered(Map.Entry<POIPoint, Boolean> poiPoint, Map<POIPoint, Boolean> filteredPOIs) {
        //Iterate over all mountains in the filtered Map
        for (Map.Entry<POIPoint, Boolean> resPoiPoints : filteredPOIs.entrySet()) {
            //If the difference between one of the filtered POIPoints and the actual POIPoint
            //is less than HALF_MARKER_SIZE then do no add the marker
            if(Math.abs(poiPoint.getKey().getHorizontalBearing()
                    - resPoiPoints.getKey().getHorizontalBearing()) <= HALF_MARKER_SIZE_WIDTH){

                //Break if the markers are not close vertically
                if(Math.abs(poiPoint.getKey().getVerticalBearing()
                        - resPoiPoints.getKey().getVerticalBearing()) >= HALF_MARKER_SIZE_HEIGHT){
                    return true;
                }

                //If the POIPoint in the filtered Map is in the line of sight or the actual
                //POIPoint is not in the line of sight
                if(resPoiPoints.getValue() || !poiPoint.getValue()){
                    //Set addPoiPoint to false
                    return false;
                }
                //If the actual mountain is in the line of sight but the filtered is not
                else{
                    //Then remove the filtered
                    filteredPOIs.remove(resPoiPoints.getKey());
                    return true;
                }
            }
        }
        return true;
    }

    @Override
    public void update(Observable o, Object arg) {
        getPOIs(userPoint);
    }
}
