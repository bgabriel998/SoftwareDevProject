package com.github.ravifrancesco.softwaredevproject;

import android.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LineOfSight {

    static final int ELEVATION_DIFFERENCE_THRESHOLD = 50; // in meters

    private UserPoint userPoint;

    private ElevationMap elevationMap;
    private double mapCellSize;
    private double boundingBoxWestLon;

    public LineOfSight(UserPoint userPoint) {
        this.userPoint = userPoint;
        this.elevationMap = new ElevationMap(this.userPoint);
    }

    public List<POIPoint> getVisiblePoints(List<POIPoint> poiPoints) {

        this.mapCellSize = elevationMap.getMapCellSize();
        this.boundingBoxWestLon = elevationMap.getBoundingBoxWestLong();

        Pair<Integer, Integer> userIndexes = elevationMap
                .getIndexesFromCoordinates(userPoint.getLatitude(), userPoint.getLongitude());
        double userLongitude = userPoint.getLongitude();
        int userAltitude = (int) userPoint.getAltitude();

        return poiPoints.parallelStream()
                .filter(p -> isVisible(p, userIndexes, userLongitude, userAltitude))
                .collect(Collectors.toList());

    }

    private boolean isVisible(POIPoint poiPoint, Pair<Integer, Integer> userIndexes,
                              double userLongitude, int userAltitude) {

        Pair<Integer, Integer> poiIndexes = elevationMap
                .getIndexesFromCoordinates(poiPoint.getLatitude(), poiPoint.getLongitude());
        double poiLongitude = poiPoint.getLongitude();
        int poiAltitude = (int) poiPoint.getAltitude();

        double slope = (poiAltitude - userAltitude) / (poiLongitude - userLongitude);

        List<Pair<Integer, Integer>> line = bresenham(  userIndexes.first, userIndexes.second,
                                                        poiIndexes.first, poiIndexes.second);

         return line.parallelStream()
                .map(p -> elevationMap.getAltitudeAtLocation(p.first, p.second) -
                        computeMaxAltitude(userLongitude, userAltitude, p.second, slope) >
                        ELEVATION_DIFFERENCE_THRESHOLD)
                .reduce(true, (vis, v) -> vis &&  v);

    }

    private List<Pair<Integer, Integer>> bresenham(int x1, int y1, int x2, int y2) {

        int m_new = 2 * (y2 - y1);
        int slope_error_new = m_new - (x2 - x1);

        List<Pair<Integer, Integer>> line = new ArrayList<>();

        for (int x = x1, y = y1; x <= x2; x++) {

            line.add(new Pair<>(x, y));

            // Add slope to increment angle formed
            slope_error_new += m_new;

            // Slope error reached limit, time to
            // increment y and update slope error.
            if (slope_error_new >= 0)
            {
                y++;
                slope_error_new -= 2 * (x2 - x1);
            }
        }

        return line;

    }

    private int computeMaxAltitude(double userLongitude, double userAltitude,
                                   int rowIndex, double slope) {

        double longitude = rowIndex * mapCellSize + boundingBoxWestLon;

        return (int) (slope*(longitude - userLongitude) + userAltitude);

    }

}
