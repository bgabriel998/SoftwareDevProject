package ch.epfl.sdp.peakar.points;

import android.content.Context;
import android.util.Log;

import androidx.core.util.Pair;

import org.osmdroid.util.BoundingBox;

import ch.epfl.sdp.peakar.utils.SettingsUtilities;

/**
 * ElevationMap is a class that represents the elevation map of the bounding box sorrounding the user.
 * The Elevation map is retrieved using the OpenTopography API as an AAIGrid and then converted into
 * an array of integers representing the height. Using the SRTMGL3 data a precision of 3 arc second
 * (~90 meter) is obtained.
 *
 * A method to obtain an updated elevation map is provided.
 * A method to obtain the altitude at a certain location (using coordinates or indexes is provided).
 * A method to compute the indexes of coordinates for accessing the topography map is provided.
 * A method to obtain the map cell size in arcs/s is provided.
 */
public class ElevationMap {

    static final int MINIMUM_DISTANCE_FOR_UPDATE = 2000;    // minimum distance in m between user and old
    // bounding center to update bounding center

    private final UserPoint userPoint;
    private BoundingBox boundingBox;
    private POIPoint boundingBoxCenter;

    private static int[][] topographyMap;
    private static double mapCellSize;

    private static Context context;

    /**
     * Constructor for the ElevationMap.
     *
     * @param topography    pair with topography map and cell size.
     * @param userPoint     the user point around which the bounding box is computed.
     * @param context       context of the application.
     */
    @SuppressWarnings("ConstantConditions")
    public ElevationMap(Pair<int[][], Double> topography, UserPoint userPoint, Context context) {
        this.userPoint = userPoint;
        Log.d("Debug", String.valueOf(SettingsUtilities.getSelectedRange(context)));
        this.boundingBox = userPoint.computeBoundingBox(SettingsUtilities.getSelectedRange(context));
        this.boundingBoxCenter = new POIPoint(this.boundingBox.getCenterWithDateLine());
        topographyMap = topography.first;
        mapCellSize = topography.second;
        ElevationMap.context = context;
    }

    /**
     * This method handles the download of the AAIGrid and building of the matrix representing
     * the elevation map.
     */
    private static void downloadTopographyMap(UserPoint userPoint) {
        new DownloadTopographyTask(context){
            @SuppressWarnings("ConstantConditions")
            @Override
            public void onResponseReceived(androidx.core.util.Pair<int[][], Double> topography) {
                super.onResponseReceived(topography);
                topographyMap = topography.first;
                mapCellSize = topography.second;
            }
        }.execute(userPoint);
    }

    /**
     * Public method to retrieve the elevation map.
     *
     * @return  an int[][] array representing the elevation map.
     */
    public int[][] getTopographyMap() {
        updateElevationMatrix();
        return topographyMap;
    }

    /**
     * This method handles the updating of the elevation map. First the distance between the center
     * of the current bounding box and the current user location is computed.
     *
     * If less than MINIMUM_DISTANCE_FOR_UPDATE no new map is downloaded, otherwise a new map is
     * downloaded.
     */
    public void updateElevationMatrix() {

        Log.d("3D MAP", "Distance from old bounding center: " + userPoint.computeFlatDistance(boundingBoxCenter));

        if (userPoint.computeFlatDistance(boundingBoxCenter) > MINIMUM_DISTANCE_FOR_UPDATE) {
            Log.d("3D MAP",  "Updating");
            boundingBox = userPoint.computeBoundingBox(SettingsUtilities.getSelectedRange(context));
            boundingBoxCenter = new POIPoint(boundingBox.getCenterWithDateLine());
            Log.d("3D MAP",  "New Map download");
            downloadTopographyMap(userPoint);
        }

        Log.d("3D MAP",  "Finished updating");

    }

    /**
     * This method returns the altitude at a given location.
     *
     * @param latitude  latitude (in degrees).
     * @param longitude longitude (in degrees).
     * @return          elevation at the given location (in meters).
     */
    @SuppressWarnings("ConstantConditions")
    public int getAltitudeAtLocation(double latitude, double longitude) {

        Pair<Integer, Integer> indexes = getIndexesFromCoordinates(latitude, longitude);

        if (indexes != null) {
            int row = indexes.first;
            int col = indexes.second;
            //Log.d("3D MAP", "Accessing map at indexes (" + row + ", " + col + ")");
            //Log.d("3D MAP", "Height = " + topographyMap[row][col]);
            return topographyMap[row][col];
        } else {
            return 0;
        }

    }

    /**
     * This method returns the altitude at a given location.
     *
     * @param row   row to access
     * @param col   col to access
     * @return      elevation at the given location (in meters).
     */
    public int getAltitudeAtLocation(int row, int col) {

        int clampedRow = Math.max(0, Math.min(topographyMap.length-1, row));
        int clampedCol = Math.max(0, Math.min(topographyMap[0].length-1, col));

        //Log.d("3D MAP", "Accessing map at indexes (" + clampedRow + ", " + clampedCol + ")");
        //Log.d("3D MAP", "Height = " + topographyMap[clampedRow][clampedCol]);
        return topographyMap[clampedRow][clampedCol];

    }

    /**
     * Method that converts coordinates into indexes for accessing the topography map matrix.
     *
     * @param latitude  latitude (in degrees).
     * @param longitude longitude (in degrees).
     * @return          returns a Pair with the first element being the row, the second the column.
     */
    public Pair<Integer, Integer> getIndexesFromCoordinates(double latitude, double longitude) {

        if (topographyMap != null) {
            double distanceFromCenterRow = latitude - boundingBox.getCenterLatitude();
            double distanceFromCenterCol = longitude - boundingBox.getCenterLongitude();
            int row = (int) (topographyMap.length / 2 - distanceFromCenterRow / mapCellSize);
            int col = (int) (topographyMap[0].length / 2 + distanceFromCenterCol / mapCellSize);
            return new Pair<>(
                    Math.max(0, Math.min(topographyMap.length-1, row)),
                    Math.max(0, Math.min(topographyMap[0].length-1, col)));
        } else {
            return null;
        }

    }

    /**
     * Getter for the map cell size
     *
     * @return double representing the size of the cells in arcs/s
     */
    public double getMapCellSize() {
        return mapCellSize;
    }

    /**
     * Getter for the west longitude of the bounding box
     *
     * @return  west longitude of the bounding box (in degrees)
     */
    public double getBoundingBoxWestLong() {
        return boundingBox.getLonWest();
    }

    /**
     * Getter for the north latitude of the bounding box
     *
     * @return  north latitude of the bounding box (in degrees)
     */
    public double getBoundingBoxNorthLat() {
        return boundingBox.getLatNorth();
    }

}