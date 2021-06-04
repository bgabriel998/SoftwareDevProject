
package ch.epfl.sdp.peakar.points;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import androidx.core.util.Pair;
import androidx.preference.PreferenceManager;

import org.osmdroid.bonuspack.location.POI;
import org.osmdroid.util.BoundingBox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import ch.epfl.sdp.peakar.R;
import ch.epfl.sdp.peakar.utils.OfflineContentContainer;
import ch.epfl.sdp.peakar.utils.SettingsUtilities;
import ch.epfl.sdp.peakar.utils.StorageHandler;

import static ch.epfl.sdp.peakar.utils.POIPointsUtilities.filterHighestPOIs;

/**
 * Requests the POIPoints around the user location and then downloads the topography map once the
 * POIPoints are available.
 *
 * To use, simply call computePOIPointsInstance.POIPoints to get a List of POIPoints or
 * computePOIPointsInstance to get a map with the POIPoints as keys and a boolean indicating if the POIPoint
 * is visible or not. If no points are available or if they are not computed yet, they will be null
 *
 * This class is a singleton class.
 *
 * This class can be observed, it will notify observers when new POIPoints are computed
 *
 */
public class ComputePOIPoints extends Observable implements Observer{

    private static final int MAX_LOADING_DISTANCE = 20000; // in m

    @SuppressLint("StaticFieldLeak")
    private static ComputePOIPoints single_instance = null; // singleton instance

    public static UserPoint userPoint;
  
    private final Context context;

    private final Map<POIPoint, Boolean> POIs;
    private  Map<POIPoint, Boolean> filteredPOIPoints;
    private  Map<POIPoint, Boolean> labeledPOIs;
    private  Map<POIPoint, Boolean> filteredLabeledPOIPoints;
    private  Map<POIPoint, Boolean> labeledPOIsInSight;
    private  Map<POIPoint, Boolean> filteredLabeledPOIsInSight;
    private  Map<POIPoint, Boolean> labeledPOIsOutOfSight;
    private  Map<POIPoint, Boolean> filteredLabeledPOIsOutOfSight;

    private static boolean isLineOfSightAvailable;

    /**
     * Constructor of computePOIPointsInstance, updates userPoint and gets the POIs for the userPoint
     * @param context Context of activity
     */
    private ComputePOIPoints(Context context){
        single_instance = this;
        POIs = new HashMap<>();
        this.context = context;
        userPoint = UserPoint.getInstance(context);
        userPoint.update();
        userPoint.addObserver(this);
        getPOIs(userPoint);
    }

    /**
     * Method to get the singleton instance of this class. If the class was already
     * initialized the parameter will be ignored.
     *
     * @param mContext  context of the application.
     * @return          single instance of the user point.
     */
    public static ComputePOIPoints getInstance(Context mContext) {
        return single_instance == null ? new ComputePOIPoints(mContext) : single_instance;
    }


    /**
     * Retrieves list of surrounding POIs either from cache
     * or from provider. If the cached data corresponds to
     * the user location, no download is made and the POIs
     * are retrieved from cached file
     * @param userPoint user location
     */
    private void getPOIs(UserPoint userPoint){
        // clear the old points
        POIs.clear();

        isLineOfSightAvailable = false;

        // first check that if offline mode is active
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean offlineModeValue = prefs.getBoolean(context.getResources().getString(R.string.offline_mode_key), false);
        if (offlineModeValue) {
           getPOIsOffline(userPoint);
           return;
        }
        //Retrieve cache instance
        POICache poiCache = POICache.getInstance();
        //Check if file is present and if user is in BB
        if( isCachingAllowed()
            && poiCache.isCacheFilePresent(context.getApplicationContext().getCacheDir())
            && poiCache.isUserInBoundingBox(userPoint, context.getCacheDir())){
            getPOIsFromCache(userPoint);
        }
        else{
            getPOIsFromProvider(userPoint);
        }
    }

    /**
     * Handles the creation and filtration of the list of the POIPoints when offline mode
     * is enabled. Gets the labeled POIs from a JSONObject and filters them. The points are not
     * added if the userPoint is more than MAX_LOADING_DISTANCE from the center of the downloaded
     * bounding box.
     * @param userPoint around which the list is computed.
     */
    private void getPOIsOffline(UserPoint userPoint) {
        try {
            OfflineContentContainer offlineContent = StorageHandler.readOfflineContentContainer(context);
            BoundingBox boundingBox = offlineContent.boundingBox;

            double distance = userPoint.computeFlatDistance(new POIPoint(boundingBox.getCenterWithDateLine()));

            Log.d("computePOIPointsInstance", "Distance = " + distance);

            if (distance < MAX_LOADING_DISTANCE) {
                Pair<int[][], Double> topography = offlineContent.topography;
                for(POIPoint poiPoint : offlineContent.POIPoints){
                    poiPoint.setHorizontalBearing(userPoint);
                    poiPoint.setVerticalBearing(userPoint);
                    poiPoint.setDistanceToUser(userPoint);
                    POIs.put(poiPoint, false);
                }
                applyFilteringLabeledPOIs(topography);
            } else {
                Toast.makeText(context, context.getResources().getString(R.string.outsideOfDownloadedBox), Toast.LENGTH_LONG).show();
                resetPOIs();
                Log.d("computePOIPointsInstance", "Distance is > " + MAX_LOADING_DISTANCE);
            }
        } catch (IOException e) {
            Log.d("computePOIPointsInstance", "There was an error reading the file");
        }
    }

    /**
     * Get surrounding POIs and topography map from cache
     * @param userPoint location of the user
     */
    private void getPOIsFromCache(UserPoint userPoint){
        ArrayList<POIPoint> cachedPOIs = POICache.getInstance().getCachedPOIPoints(context.getCacheDir());
        cachedPOIs.forEach(poiPoint -> {
            poiPoint.setHorizontalBearing(userPoint);
            poiPoint.setVerticalBearing(userPoint);
            poiPoint.setDistanceToUser(userPoint);
            POIs.put(poiPoint, false);
        });
        //Retrieve topography map from cache
        Pair<int[][], Double> cachedTopography = POICache.getInstance().getCachedTopography(context.getCacheDir());
        if(cachedTopography != null){
            applyFilteringLabeledPOIs(cachedTopography);
        }
        else{
            getLabeledPOIs(userPoint);
        }
    }

    /**
     * Gets the POIs for the userPoint from Provider
     * @param userPoint location of the user
     */
    @SuppressLint("StaticFieldLeak")
    private void getPOIsFromProvider(UserPoint userPoint){
        new GeonamesHandler(userPoint,context){
            @Override
            public void onResponseReceived(ArrayList<POI> result) {
                if(result!=null){
                    for(POI poi : result){
                        POIPoint poiPoint = new POIPoint(poi);
                        poiPoint.setHorizontalBearing(userPoint);
                        poiPoint.setVerticalBearing(userPoint);
                        poiPoint.setDistanceToUser(userPoint);
                        POIs.put(poiPoint, false);
                    }
                    filteredPOIPoints = filterHighestPOIs(POIs);
                    setChanged();
                    notifyObservers();
                    getLabeledPOIs(userPoint);
                }
            }
        }.execute();
    }

    /**
     * Gets the labeled POIs and filters them.
     *
     * @param userPoint userPoint for which the labeled POIs are computed.
     */
    @SuppressLint("StaticFieldLeak")
    private void getLabeledPOIs(UserPoint userPoint){
        new DownloadTopographyTask(context){
            @Override
            public void onResponseReceived(Pair<int[][], Double> topography) {
                super.onResponseReceived(topography);

                applyFilteringLabeledPOIs(topography);

                //Save POIs, BB and topography to the cache
                POICache.getInstance().savePOIDataToCache(new ArrayList<>(POIs.keySet()),
                        userPoint.computeBoundingBox(SettingsUtilities.getSelectedRange(context)),
                        topography,
                        context.getCacheDir());
            }
        }.execute(userPoint);
    }

    /**
     * Check if the user has allowed the caching in the
     * @return true if the caching is allowed in the settings
     */
    private boolean isCachingAllowed(){
        //Get shared preferences
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(context.getResources().getString(R.string.disable_caching_key), true);
    }

    /**
     * Apply filtering using topography map on POI list and updates the observers.
     * @param topography topography map
     */
    private void applyFilteringLabeledPOIs(Pair<int[][], Double> topography){
        LineOfSight lineOfSight = new LineOfSight(topography, userPoint, context);
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

        setChanged();
        notifyObservers();
    }

    /**
     * Reset lists of POIPoints and requests new ones
     */
    private void resetPOIs() {
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
    public Boolean isLineOfSightAvailable(){
        return isLineOfSightAvailable;
    }

    /**
     * Gets a Map of all surrounding POIPoints
     * @return Map<POIPoint, Boolean> containing POIPoints.
     */
    public Map<POIPoint, Boolean> getPOIs(){
        return isLineOfSightAvailable ? labeledPOIs : POIs;
    }

    /**
     * Gets a filtered Map of all surrounding POIPoints
     * @return Map<POIPoint, Boolean> containing the filtered POIPoints.
     */
    public Map<POIPoint, Boolean> getFilteredPOIs(){
        return isLineOfSightAvailable ? filteredLabeledPOIPoints : filteredPOIPoints;
    }

    /**
     * Gets a Map of the POIPoints that are in the line of sight
     * @return Map<POIPoint, Boolean> containing only the POIPoints in the lineOfSight.
     */
    public Map<POIPoint, Boolean> getPOIsInSight(){
        return isLineOfSightAvailable ? labeledPOIsInSight : POIs;
    }

    /**
     * Gets a Map of the POIPoints that are in the line of sight and filtered
     * @return Map<POIPoint, Boolean> containing the filtered POIPoints in the lineOfSight.
     */
    public Map<POIPoint, Boolean> getFilteredPOIsInSight(){
        return isLineOfSightAvailable ? filteredLabeledPOIsInSight : filteredPOIPoints;
    }

    /**
     * Gets a Map of the POIPoints that are out of the line of sight
     * @return Map<POIPoint, Boolean> containing only the POIPoints out of the lineOfSight.
     */
    public Map<POIPoint, Boolean> getPOIsOutOfSight(){
        return isLineOfSightAvailable ? labeledPOIsOutOfSight : POIs;
    }

    /**
     * Gets a Map of the POIPoints that are out of the line of sight and filtered
     * @return Map<POIPoint, Boolean> containing the filtered POIPoints out of the lineOfSight.
     */
    public Map<POIPoint, Boolean> getFilteredPOIsOutOfSight(){
        return isLineOfSightAvailable ? filteredLabeledPOIsOutOfSight : filteredPOIPoints;
    }

    @Override
    public void update(Observable o, Object arg) {
        getPOIs(userPoint);
    }

}
