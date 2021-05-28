package ch.epfl.sdp.peakar.points;

import android.content.Context;
import android.util.Log;

import androidx.core.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
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

    static final int ELEVATION_DIFFERENCE_THRESHOLD = 100; // in meters

    private final UserPoint userPoint;

    private final ElevationMap elevationMap;
    private double mapCellSize;
    private double boundingBoxNorthLat;
    private double boundingBoxWestLon;

    private Context context;

    /**
     * Constructor for the LineOfSight class.
     *
     * @param topography    pair with topography map and cell size.
     * @param userPoint     userPoint from wich the visible POIPoints are computed.
     * @param context       context of the application.
     */
    public LineOfSight(Pair<int[][], Double> topography, UserPoint userPoint, Context context) {
        this.userPoint = userPoint;
        this.mapCellSize = topography.second;
        this.context = context;
        this.elevationMap = new ElevationMap(topography, this.userPoint, context);
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
        this.boundingBoxNorthLat = elevationMap.getBoundingBoxNorthLat();
        this.boundingBoxWestLon = elevationMap.getBoundingBoxWestLong();

        Pair<Integer, Integer> userIndexes = elevationMap
                .getIndexesFromCoordinates(userPoint.getLatitude(), userPoint.getLongitude());
        double userLatitude = userPoint.getLatitude();
        double userLongitude = userPoint.getLongitude();
        int userAltitude = (int) userPoint.getAltitude();

        return poiPoints.parallelStream()
                .filter(p -> isVisible(p, userIndexes, userLatitude, userLongitude, userAltitude))
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
        this.boundingBoxNorthLat = elevationMap.getBoundingBoxNorthLat();
        this.boundingBoxWestLon = elevationMap.getBoundingBoxWestLong();

        Pair<Integer, Integer> userIndexes = elevationMap
                .getIndexesFromCoordinates(userPoint.getLatitude(), userPoint.getLongitude());
        double userLatitude = userPoint.getLatitude();
        double userLongitude = userPoint.getLongitude();
        int userAltitude = (int) userPoint.getAltitude();

        Map<POIPoint, Boolean> labeledPOIPoints = Collections.synchronizedMap(new HashMap<>());

        poiPoints.parallelStream()
                .forEach(p -> labeledPOIPoints.put(p, isVisible(p, userIndexes, userLatitude, userLongitude, userAltitude)));

        return labeledPOIPoints;
    }

    /**
     * This method computes a boolean that indicates if a single POIPoint is visible from
     * the user's location.
     *
     * @param poiPoint      POIPoint to determine if it is visible.
     * @param userIndexes   indexes representing the user's location on the elevation map grid.
     * @param userLatitude  latitude of the user's location (in degrees).
     * @param userLongitude longitude of the user's location (in degrees).
     * @param userAltitude  altitude of the user's location (in meters).
     * @return              <code>true</code> if the POIPoint is visible from the user's location.
     * 	                    <code>false</code> otherwise.
     */
    private boolean isVisible(POIPoint poiPoint, Pair<Integer, Integer> userIndexes,
                              double userLatitude, double userLongitude, int userAltitude) {

        Pair<Integer, Integer> poiIndexes = elevationMap
                .getIndexesFromCoordinates(poiPoint.getLatitude(), poiPoint.getLongitude());

        double poiLatitude = poiPoint.getLatitude();
        double poiLongitude = poiPoint.getLongitude();
        int poiAltitude = (int) poiPoint.getAltitude();
        boolean useRow = Math.abs(userIndexes.first - poiIndexes.first) >
                Math.abs(userIndexes.second - poiIndexes.second);

        double slope = (poiAltitude - userAltitude) / (useRow ? (poiLatitude - userLatitude) : (poiLongitude - userLongitude));

        Log.d("DEBUG", String.valueOf(slope));

        List<Pair<Integer, Integer>> line = drawLine(  userIndexes.first, userIndexes.second,
                poiIndexes.first, poiIndexes.second);

        return line.stream()
                .peek(p -> Log.d("DEBUG", computeMaxElevation(userLatitude, userLongitude, userAltitude, p.first, p.second, useRow, slope) + " - " + elevationMap.getAltitudeAtLocation(p.first, p.second)  + " - " + (computeMaxElevation(userLatitude, userLongitude, userAltitude, p.first, p.second, useRow, slope) -
                        elevationMap.getAltitudeAtLocation(p.first, p.second))))
                .map(p -> computeMaxElevation(userLatitude, userLongitude, userAltitude, p.first, p.second, useRow, slope) -
                        elevationMap.getAltitudeAtLocation(p.first, p.second) >
                        - ELEVATION_DIFFERENCE_THRESHOLD)
                .reduce(true, (vis, v) -> vis && v);

    }

    /**
     * This method draws the line between the user's location and the POIPoint on the
     * elevation map grid.
     *
     * @param x1    x index of the user's location on the elevation map grid.
     * @param y1    y index of the user's location on the elevation map grid.
     * @param x2    x index of the POIPoint on the elevation map grid.
     * @param y2    y index of the POIPoint on the elevation map grid.
     * @return      a List of indexes representing the line in the elevation map grid.
     */
    private List<Pair<Integer, Integer>> drawLine(int x1, int y1, int x2, int y2) {

        int pixelX = x2 - x1;
        int pixelY = y2 - y1;

        Log.d("DEBUG" , "(" + x1 + "," + y1 + ") (" + x2 + "," + y2 + ")");

        List<Pair<Integer, Integer>> line = new ArrayList<>();

        int ratio;
        double error;
        double cumulatedError = 0;

        if (Math.abs(pixelY) >= Math.abs(pixelX)) {
            ratio = pixelX != 0 ? Math.abs(pixelY / pixelX) : Math.abs(pixelX);
            error = pixelX != 0 ? Math.abs((double) ratio - Math.abs(((double) pixelY / (double) pixelX))) : 0;
            for (int x = x1, y = y1; x1 < x2 ? x <= x2 : x >= x2; x = x1 < x2 ? x+1 : x-1) {
                for (int i = 0; i < ratio && (y1 < y2 ? y <= y2 : y >= y2); i++) {
                    line.add(new Pair<>(x, y));
                    y = y1 < y2 ? y+1 : y-1;
                    cumulatedError += error;
                    if (cumulatedError >= 1) {
                        line.add(new Pair<>(x, y));
                        y = y1 < y2 ? y+1 : y-1;
                        cumulatedError -= 1;
                    }
                }
            }
        } else {
            ratio = pixelY != 0 ? Math.abs(pixelX / pixelY) : Math.abs(pixelY);
            error = pixelY != 0 ? Math.abs((double) ratio - Math.abs(((double) pixelX / (double) pixelY))) : 0;
            for (int x = x1, y = y1; y1 < y2 ? y <= y2 : y >= y2; y = y1 < y2 ? y+1 : y-1) {
                for (int i = 0; i < ratio && (x1 < x2 ? x <= x2 : x >= x2); i++) {
                    line.add(new Pair<>(x, y));
                    x = x1 < x2 ? x+1 : x-1;
                    cumulatedError += error;
                    if (cumulatedError >= 1) {
                        line.add(new Pair<>(x, y));
                        x = x1 < x2 ? x+1 : x-1;
                        cumulatedError -= 1;
                    }
                }
            }
        }

        Log.d("DEBUG", line.toString());

        return line;

    }

    /**
     * Method that computes the elevation of the line connecting the user's location and the
     * POIPoint at a given index of the elevation map grid.
     *
     * @param userLatitude  latitude of the user's location (in degrees).
     * @param userLongitude longitude of the user's location (in degrees).
     * @param userAltitude  altitude of the user's location (in meters).
     * @param rowIndex      index of the row to compute the elevation.
     * @param colIndex      index of the column to compute the elevation.
     * @param useRow        boolean indicating the method to use row/column for computing the elevation.
     * @param slope         slope of the line connecting the user's location and the POIPoint.
     * @return              elevation (in meters).
     */
    private int computeMaxElevation(double userLatitude, double userLongitude,
                                    double userAltitude,
                                    int rowIndex, int colIndex, boolean useRow,
                                    double slope) {

        double latitude = -(rowIndex * mapCellSize) + boundingBoxNorthLat;
        double longitude = colIndex * mapCellSize + boundingBoxWestLon;

        Log.d("DEBUG", rowIndex + " - " + latitude + " - " + slope);

        return (int) (useRow ? (slope*(latitude - userLatitude) + userAltitude) : (slope*(longitude - userLongitude) + userAltitude));

    }

}
