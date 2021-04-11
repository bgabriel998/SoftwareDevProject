package com.github.ravifrancesco.softwaredevproject;

import android.util.Log;
import android.util.Pair;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.HttpClientBuilder;
import org.osmdroid.util.BoundingBox;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Scanner;
import java.util.stream.IntStream;

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
 *
 * TODO handle updates of the map (to do after implementation of other elements of the app)
 * TODO handle caching of the map
 * TODO decide if an async download is necessary
 */
public class ElevationMap {

    static final int BOUNDING_BOX_RANGE = 20; //range of the bounding box in km
    static final int MINIMUM_DISTANCE_FOR_UPDATE = 2000;    // minimum distance in m between user and old
                                                            // bounding center to update bounding center

    private final String BASE_URL = "https://portal.opentopography.org/API/globaldem";
    private final String DEM_TYPE = "SRTMGL3";
    private final String OUTPUT_FORMAT = "AAIGrid";

    private final UserPoint userPoint;
    private BoundingBox boundingBox;
    private POIPoint boundingBoxCenter;

    private int[][] topographyMap;
    private double mapCellSize;

    /**
     * Constructor for the ElevationMap.
     *
     * @param userPoint the user point around which the bounding box is computed.
     */
    public ElevationMap(UserPoint userPoint) {
        this.userPoint = userPoint;
        this.boundingBox = userPoint.computeBoundingBox(BOUNDING_BOX_RANGE);
        this.boundingBoxCenter = new POIPoint(this.boundingBox.getCenterWithDateLine());
        downloadTopographyMap();
    }

    /**
     * This method handles the download of the AAIGrid and building of the matrix representing
     * the elevation map.
     */
    private void downloadTopographyMap() {
        URL url = generateURL();
        try {
            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpRequestBase base = new HttpGet(url.toString());
            HttpResponse response = httpClient.execute(base);

            if (response.getStatusLine().getStatusCode() == 200) {
                parseResponse(response);
            } else {
                Log.d("3d MAP", "Http error code: " + response.getStatusLine().getStatusCode());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method handles the parsing of the response obtained via HTTP request. It saves the
     * size of the matrix to build and passes them to the buildMapGrid method.
     *
     * @param response  HTTPResponse to parse.
     */
    private void parseResponse(HttpResponse response) {

        try {
            Scanner responseObj = new Scanner(response.getEntity().getContent());
            int nCol =  Integer.parseInt(responseObj.nextLine().replaceAll("[\\D]", ""));
            int nRow = Integer.parseInt(responseObj.nextLine().replaceAll("[\\D]", ""));
            buildMapGrid(nRow, nCol, responseObj);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * This method builds the matrix and saves it in the object.
     *
     * @param nRow          height of the matrix.
     * @param nCol          width of the matrix.
     * @param responseObj   Scanner to read the HTTPResponse.
     */
    private void buildMapGrid(int nRow, int nCol, Scanner responseObj) {

        // skip 2 lines (x, y corner)
        responseObj.nextLine();
        responseObj.nextLine();

        // get cell size
        this.mapCellSize = Double.parseDouble(responseObj.nextLine().replaceAll("[a-zA-Z]", ""));

        // skip another line (NODATA_value)
        responseObj.nextLine();

        // build matrix
        this.topographyMap = IntStream.range(0, nRow)
                .boxed()
                .map(i -> Arrays.stream(responseObj.nextLine().substring(1).split(" ", nCol))
                        .mapToInt(Integer::parseInt)
                        .toArray())
                .toArray(int[][]::new);

        Log.d("3D MAP", "Generated Map with size (" + nRow + ", " + nCol +")");
        Log.d("3D MAP", "Cell size = " + this.mapCellSize);

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
            boundingBox = userPoint.computeBoundingBox(BOUNDING_BOX_RANGE);
            boundingBoxCenter = new POIPoint(boundingBox.getCenterWithDateLine());
            Log.d("3D MAP",  "New Map download");
            downloadTopographyMap();
        }

        Log.d("3D MAP",  "Finished updating");

    }

    /**
     * This method handles the generation of the URL for downloading the map. To generate the url
     * this static parameters are used:
     * <ul>
     * <li>BASE_URL
     * <li>DEM_TYPE
     * <li>OUTPUT_FORMAT
     * </ul>
     * <p>
     * To find more about this parameter please check OpenTopographyAPI documentation
     *
     * @see <a href="https://portal.opentopography.org/apidocs/">OpenTopographyAPI</a>
     *
     * @return  a URL to make the request for downloading the map
     */
    private URL generateURL() {

        String south = String.valueOf(boundingBox.getLatSouth());
        String north = String.valueOf(boundingBox.getLatNorth());
        String west = String.valueOf(boundingBox.getLonWest());
        String east = String.valueOf(boundingBox.getLonEast());

        try {
            URL url = new URL( BASE_URL + "?" +
                            "demtype=" + DEM_TYPE + "&" +
                            "south=" + south + "&" +
                            "north=" + north + "&" +
                            "west=" + west + "&" +
                            "east=" + east + "&" +
                            "outputFormat=" + OUTPUT_FORMAT);
            Log.d("3D MAP", "Generated url: " + url.toString());
            return url;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }

    }

    /**
     * This method returns the altitude at a given location.
     *
     * @param latitude  latitude (in degrees).
     * @param longitude longitude (in degrees).
     * @return          elevation at the given location (in meters).
     */
    public int getAltitudeAtLocation(double latitude, double longitude) {

        Pair<Integer, Integer> indexes = getIndexesFromCoordinates(latitude, longitude);

        if (indexes != null) {
            int row = indexes.first;
            int col = indexes.second;
            Log.d("3D MAP", "Accessing map at indexes (" + row + ", " + col + ")");
            Log.d("3D MAP", "Height = " + this.topographyMap[row][col]);
            return this.topographyMap[row][col];
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

        Log.d("3D MAP", "Accessing map at indexes (" + clampedRow + ", " + clampedCol + ")");
        Log.d("3D MAP", "Height = " + this.topographyMap[clampedRow][clampedCol]);
        return this.topographyMap[clampedRow][clampedCol];

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
            int row = (int) (topographyMap.length / 2 - distanceFromCenterRow / this.mapCellSize);
            int col = (int) (topographyMap[0].length / 2 + distanceFromCenterCol / this.mapCellSize);
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
}
