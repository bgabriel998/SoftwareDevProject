package ch.epfl.sdp.peakar.points;

import android.content.Context;

import androidx.core.util.Pair;

import org.osmdroid.bonuspack.location.POI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Requests the POIPoints around the user location and then downloads the topography map once the
 * POIPoints are available.
 *
 * To use, simply call ComputePOIPoints.POIPoints to get a List of POIPoints or
 * ComputePOIPoints to get a map with the POIPoints as keys and a boolean indicating if the POIPoint
 * is visible or not. If no points are available or if they are not computed yet, they will be null
 */
public class ComputePOIPoints {
    public static List<POIPoint> POIPoints;
    public static Map<POIPoint, Boolean> labeledPOIPoints;
    public static Map<POIPoint, Boolean> highestPOIPoints;
    public static UserPoint userPoint;

    private static final int HALF_MARKER_SIZE_WIDTH = 3;
    private static final int HALF_MARKER_SIZE_HEIGHT = 5;

    /**
     * Constructor of ComputePOIPoints, updates userPoint and gets the POIs for the userPoint
     * @param context Context of activity
     */
    public ComputePOIPoints(Context context){
        POIPoints = new ArrayList<>();
        userPoint = UserPoint.getInstance(context);
        userPoint.update();
        getPOIs(userPoint);
    }

    /**
     * Gets the POIs for the userPoint
     * @param userPoint location of the user
     */
    private static void getPOIs(UserPoint userPoint){
        new GeonamesHandler(userPoint){
            @Override
            public void onResponseReceived(ArrayList<POI> result) {
                if(result!=null){
                    for(POI poi : result){
                        POIPoint poiPoint = new POIPoint(poi);
                        poiPoint.setHorizontalBearing(userPoint);
                        poiPoint.setVerticalBearing(userPoint);
                        POIPoints.add(poiPoint);
                    }
                    getLabeledPOIs(userPoint);
                }
            }
        }.execute();
    }

    /**
     * Gets the labeled POIs and filters them
     * @param userPoint userPoint for which the labeled POIs are computed
     */
    private static void getLabeledPOIs(UserPoint userPoint){
        new DownloadTopographyTask(){
            @Override
            public void onResponseReceived(Pair<int[][], Double> topography) {
                super.onResponseReceived(topography);
                LineOfSight lineOfSight = new LineOfSight(topography, userPoint);
                labeledPOIPoints = lineOfSight.getVisiblePointsLabeled(POIPoints);
                //Filter highest mountains
                highestPOIPoints = filterHighestPOIs(labeledPOIPoints);
            }
        }.execute(userPoint);
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
}