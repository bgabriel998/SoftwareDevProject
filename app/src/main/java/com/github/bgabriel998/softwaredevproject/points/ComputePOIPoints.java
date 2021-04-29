package com.github.bgabriel998.softwaredevproject.points;

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
import java.util.TreeMap;
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
     * Gets the labeled POIs
     * @param userPoint userPoint for which the labeled POIs are computed
     */
    private static void getLabeledPOIs(UserPoint userPoint){
        new DownloadTopographyTask(){
            @Override
            public void onResponseReceived(Pair<int[][], Double> topography) {
                super.onResponseReceived(topography);
                LineOfSight lineOfSight = new LineOfSight(topography, userPoint);
                labeledPOIPoints = lineOfSight.getVisiblePointsLabeled(POIPoints);

                highestPOIPoints = filterHighestPOIs(labeledPOIPoints);

                userPoint.update();
            }
        }.execute(userPoint);
    }

    private static Map<POIPoint, Boolean> filterHighestPOIs(Map<POIPoint, Boolean> labeledPOIPoints) {
        Map<POIPoint, Boolean> sortedPoisByAlt = labeledPOIPoints.entrySet().stream()
                .sorted(Collections.reverseOrder(Comparator.comparingDouble(p -> p.getKey().altitude)))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));

        Map<POIPoint, Boolean> filteredPOIs = new HashMap<>();

        Boolean addPoiPoint;
        for (Map.Entry<POIPoint, Boolean> poiPoints : sortedPoisByAlt.entrySet()) {
            addPoiPoint = true;
            for (Map.Entry<POIPoint, Boolean> resPoiPoints : filteredPOIs.entrySet()) {
                if(Math.abs(poiPoints.getKey().getHorizontalBearing()
                        - resPoiPoints.getKey().getHorizontalBearing()) <= 5){
                    addPoiPoint = false;
                }
            }
            if(addPoiPoint){
                filteredPOIs.put(poiPoints.getKey(), poiPoints.getValue());
            }
        }

        return filteredPOIs;
    }
}