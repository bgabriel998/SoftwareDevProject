package ch.epfl.sdp.peakar.utils;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import ch.epfl.sdp.peakar.points.POIPoint;

/**
 * Utility class for the ComputePOIPoints class, contains utility methods to compute the POIPoints
 * such as the filtering of the POIPoints
 */
public final class POIPointsUtilities {
    private static final int HALF_MARKER_SIZE_WIDTH = 3;
    private static final int HALF_MARKER_SIZE_HEIGHT = 5;

    /**
     * Filters the labeled POIPoints and takes only the highest s.t. there are no other markers
     * within 6° horizontally. POIPoints in the line of sight are prioritised. If a POIPoint is in
     * the line of sight but has a smaller altitude then another POIPoint that is close but that is
     * not in the line of sight, then only the POIPoint that is in the line of sight will be added
     * @param labeledPOIPoints labeled POIPoints that get sorted
     * @return Map<POIPoint, Boolean> of the highest filtered POIs
     */
    public static Map<POIPoint, Boolean> filterHighestPOIs(Map<POIPoint, Boolean> labeledPOIPoints) {
        //sort the labeledPOIPoints by altitude in decreasing order
        Map<POIPoint, Boolean> sortedPoisByAlt = labeledPOIPoints.entrySet().stream()
                .sorted(Collections.reverseOrder(Comparator.comparingDouble(p -> p.getKey().getAltitude())))
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
