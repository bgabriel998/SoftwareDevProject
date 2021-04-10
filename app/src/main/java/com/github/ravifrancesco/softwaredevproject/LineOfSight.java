package com.github.ravifrancesco.softwaredevproject;

import android.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class enables the computation of the visible POIPoints from the user's location.
 * It uses the ElevationMap class to compute the elevation of the terrain at a given location.
 *
 * The ELEVATION_DIFFERENCE_THRESHOLD represents the maximum acceptable difference in meters
 * between the line that connects the user to the POIPoint and the actual elevation of the
 * terrain in a given point.
 */
public class LineOfSight {

    static final int ELEVATION_DIFFERENCE_THRESHOLD = 50; // in meters

    private final UserPoint userPoint;

    private final ElevationMap elevationMap;
    private double mapCellSize;
    private double boundingBoxWestLon;

    /**
     * Constructor for the LineOfSight class.
     *
     * @param userPoint userPoint from wich the visible POIPoints are computed.
     */
    public LineOfSight(UserPoint userPoint) {
        this.userPoint = userPoint;
        this.elevationMap = new ElevationMap(this.userPoint);
    }

    /**
     * This method allows to filter the POIPoints that are not visible from the user's location.
     *
     * @param poiPoints a List of POIPoint to filter.
     * @return          a list of the visible POIPoints from the input list.
     */
    public List<POIPoint> getVisiblePoints(List<POIPoint> poiPoints) {

        elevationMap.updateElevationMatrix();

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

    /**
     * This method returns a map with the POPoints labeled with <code>true</code> if the
     * POIPoints is visible, <code>false</code> otherwise
     *
     * @param poiPoints a List of POIPoint to filter.
     * @return          a map with the labeled POIPoints
     */
    public Map<POIPoint, Boolean> getVisiblePointsLabeled(List<POIPoint> poiPoints) {

        elevationMap.updateElevationMatrix();

        this.mapCellSize = elevationMap.getMapCellSize();
        this.boundingBoxWestLon = elevationMap.getBoundingBoxWestLong();

        Pair<Integer, Integer> userIndexes = elevationMap
                .getIndexesFromCoordinates(userPoint.getLatitude(), userPoint.getLongitude());
        double userLongitude = userPoint.getLongitude();
        int userAltitude = (int) userPoint.getAltitude();

        Map<POIPoint, Boolean> labeledPOIPoints = new HashMap<>();

         poiPoints.parallelStream()
                 .forEach(p -> labeledPOIPoints.put(p, isVisible(p, userIndexes, userLongitude, userAltitude)));

         return labeledPOIPoints;

    }

    /**
     * This method computes a boolean that indicates if a single POIPoint is visible from
     * the user's location.
     *
     * @param poiPoint      POIPoint to determine if it is visible.
     * @param userIndexes   indexes representing the user's location on the elevation map grid.
     * @param userLongitude longitude of the user's location (in degrees).
     * @param userAltitude  altitude of the user's location (in meters).
     * @return              <code>true</code> if the POIPoint is visible from the user's location.
     * 	                    <code>false</code> otherwise.
     */
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
                        computeMaxElevation(userLongitude, userAltitude, p.second, slope) >
                        ELEVATION_DIFFERENCE_THRESHOLD)
                .reduce(true, (vis, v) -> vis &&  v);

    }

    /**
     * This method draws the line between the user's location and the POIPoint on the
     * elevation map grid. To do this the Bresenham algorithm is used:
     *
     * See <a href="Bresenham's line algorithm">https://en.wikipedia.org/wiki/Bresenham%27s_line_algorithm</a>
     *
     * @param x1    x index of the user's location on the elevation map grid.
     * @param y1    y index of the user's location on the elevation map grid.
     * @param x2    x index of the POIPoint on the elevation map grid.
     * @param y2    y index of the POIPoint on the elevation map grid.
     * @return      a List of indexes representing the line in the elevation map grid.
     */
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
            if (slope_error_new >= 0) {
                y++;
                slope_error_new -= 2 * (x2 - x1);
            }
        }

        return line;

    }

    /**
     * Method that computes the elevation of the line connecting the user's location and the
     * POIPoint at a given index of the elevation map grid.
     *
     * @param userLongitude longitude of the user's location (in degrees).
     * @param userAltitude  altitude of the user's location (in meters).
     * @param colIndex      index of the column to compute the elevation.
     * @param slope         slope of the line conncetgint the user's location and the POIPoint.
     * @return              elevation (in meters).
     */
    private int computeMaxElevation(double userLongitude, double userAltitude,
                                    int colIndex, double slope) {

        double longitude = colIndex * mapCellSize + boundingBoxWestLon;

        return (int) (slope*(longitude - userLongitude) + userAltitude);

    }

}
