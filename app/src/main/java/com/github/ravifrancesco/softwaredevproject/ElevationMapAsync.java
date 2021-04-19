package com.github.ravifrancesco.softwaredevproject;

import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.HttpClientBuilder;
import org.osmdroid.bonuspack.location.POI;
import org.osmdroid.util.BoundingBox;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ElevationMapAsync extends AsyncTask<Void, Void, int[][]> implements ElevationMapAsyncIF {

    static final int BOUNDING_BOX_RANGE = 20; //range of the bounding box in km
    static final int MINIMUM_DISTANCE_FOR_UPDATE = 2000;    // minimum distance in m between user and old
    // bounding center to update bounding center

    private final String BASE_URL = "https://portal.opentopography.org/API/globaldem";
    private final String DEM_TYPE = "SRTMGL3";
    private final String OUTPUT_FORMAT = "AAIGrid";

    private final UserPoint userPoint;
    private static BoundingBox boundingBox;
    private POIPoint boundingBoxCenter;

    private int[][] topographyMap;
    private static double mapCellSize;

    /**
     * Constructor for the ElevationMap.
     *
     * @param userPoint the user point around which the bounding box is computed.
     */
    public ElevationMapAsync(UserPoint userPoint) {
        this.userPoint = userPoint;
        this.boundingBox = userPoint.computeBoundingBox(BOUNDING_BOX_RANGE);
        this.boundingBoxCenter = new POIPoint(this.boundingBox.getCenterWithDateLine());
    }


    /**
     * This method handles the download of the AAIGrid and building of the matrix representing
     * the elevation map.
     */
    @Override
    protected int[][] doInBackground(Void... voids) {
        URL url = generateURL();
        try {
            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpRequestBase base = new HttpGet(url.toString());
            HttpResponse response = httpClient.execute(base);

            if (response.getStatusLine().getStatusCode() == 200) {
                return parseResponse(response);
            } else {
                Log.d("3d MAP", "Http error code: " + response.getStatusLine().getStatusCode());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(int[][] ints) {
        super.onPostExecute(ints);
        onResponseReceived(ints);
    }


    /**
     * This method handles the parsing of the response obtained via HTTP request. It saves the
     * size of the matrix to build and passes them to the buildMapGrid method.
     *
     * @param response  HTTPResponse to parse.
     */
    private int[][] parseResponse(HttpResponse response) {
        try {
            Scanner responseObj = new Scanner(response.getEntity().getContent());
            int nCol =  Integer.parseInt(responseObj.nextLine().replaceAll("[\\D]", ""));
            int nRow = Integer.parseInt(responseObj.nextLine().replaceAll("[\\D]", ""));
            return buildMapGrid(nRow, nCol, responseObj);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * This method builds the matrix and saves it in the object.
     *
     * @param nRow          height of the matrix.
     * @param nCol          width of the matrix.
     * @param responseObj   Scanner to read the HTTPResponse.
     */
    private int[][] buildMapGrid(int nRow, int nCol, Scanner responseObj) {

        // skip 2 lines (x, y corner)
        responseObj.nextLine();
        responseObj.nextLine();

        // get cell size
        mapCellSize = Double.parseDouble(responseObj.nextLine().replaceAll("[a-zA-Z]", ""));

        // skip another line (NODATA_value)
        responseObj.nextLine();

        // build matrix
        topographyMap = IntStream.range(0, nRow)
                .boxed()
                .map(i -> Arrays.stream(responseObj.nextLine().substring(1).split(" ", nCol))
                        .mapToInt(Integer::parseInt)
                        .toArray())
                .toArray(int[][]::new);

        Log.d("3D MAP", "Generated Map with size (" + nRow + ", " + nCol +")");
        Log.d("3D MAP", "Cell size = " + mapCellSize);

        return topographyMap;

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
            doInBackground();
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
     * @param row   row to access
     * @param col   col to access
     * @return      elevation at the given location (in meters).
     */
    public static int getAltitudeAtLocation(int row, int col, int[][] topographyMap) {

        int clampedRow = Math.max(0, Math.min(topographyMap.length-1, row));
        int clampedCol = Math.max(0, Math.min(topographyMap[0].length-1, col));

        Log.d("3D MAP", "Accessing map at indexes (" + clampedRow + ", " + clampedCol + ")");
        Log.d("3D MAP", "Height = " + topographyMap[clampedRow][clampedCol]);
        return topographyMap[clampedRow][clampedCol];

    }

    /**
     * Method that converts coordinates into indexes for accessing the topography map matrix.
     *
     * @param latitude  latitude (in degrees).
     * @param longitude longitude (in degrees).
     * @return          returns a Pair with the first element being the row, the second the column.
     */
    public static Pair<Integer, Integer> getIndexesFromCoordinates(double latitude, double longitude, int[][] topographyMap) {

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

    @Override
    public void onResponseReceived(int[][] topography) {
        this.topographyMap = topography;
    }
}
