package com.github.bgabriel998.softwaredevproject;

import android.content.Context;

import androidx.core.util.Pair;

import com.github.ravifrancesco.softwaredevproject.DownloadTopographyTask;
import com.github.ravifrancesco.softwaredevproject.LineOfSight;
import com.github.ravifrancesco.softwaredevproject.POIPoint;
import com.github.ravifrancesco.softwaredevproject.Point;
import com.github.ravifrancesco.softwaredevproject.UserPoint;

import org.osmdroid.bonuspack.location.POI;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    public static UserPoint userPoint;

    /**
     * Constructor of ComputePOIPoints, updates userPoint and gets the POIs for the userPoint
     * @param context Context of activity
     */
    public ComputePOIPoints(Context context){
        POIPoints = new ArrayList<>();
        userPoint = new UserPoint(context);
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
            }
        }.execute(userPoint);
    }
}