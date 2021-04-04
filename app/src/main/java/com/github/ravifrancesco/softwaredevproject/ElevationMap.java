package com.github.ravifrancesco.softwaredevproject;

import android.util.Log;

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
 *
 * TODO create line of sight
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

    private int[][] topographyMap;

    /**
     * Constructor for the ElevationMap.
     *
     * @param userPoint the user point around which the bounding box is computed.
     */
    public ElevationMap(UserPoint userPoint) {
        this.userPoint = userPoint;
        this.boundingBox = userPoint.computeBoundingBox(BOUNDING_BOX_RANGE);
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

        // skip lines to get to first row
        int linesToSkip = 4;
        for (int i =0; i < linesToSkip; i++) {
            responseObj.nextLine();
        }

        // build matrix
        this.topographyMap = IntStream.range(0, nRow)
                .boxed()
                .map(i -> Arrays.stream(responseObj.nextLine().substring(1).split(" ", nCol))
                        .mapToInt(Integer::parseInt)
                        .toArray())
                .toArray(int[][]::new);

        Log.d("3D MAP", "Generated Map with size (" + nRow + ", " + nCol +")");

    }

    /**
     * Public method to retrieve the elevation map.
     *
     * @return  an int[][] array representing the elevation map.
     */
    public int[][] getTopographyMap() {

        updateBoundingBox();
        return topographyMap;

    }

    /**
     * This method handles the updating of the elevation map. First the distance between the center
     * of the current bounding box and the current user location is computed.
     *
     * If less than MINIMUM_DISTANCE_FOR_UPDATE no new map is downloaded, otherwise a new map is
     * downloaded.
     */
    private void updateBoundingBox() {

        Log.d("3D MAP",  "Updating");
        POIPoint oldBoundingCenter = new POIPoint(boundingBox.getCenterWithDateLine());
        Log.d("3D MAP", "Distance from old bounding center: " + userPoint.computeFlatDistance(oldBoundingCenter));

        if (userPoint.computeFlatDistance(oldBoundingCenter) > MINIMUM_DISTANCE_FOR_UPDATE) {
            Log.d("3D MAP",  "New Map download");
            boundingBox = userPoint.computeBoundingBox(BOUNDING_BOX_RANGE);
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

}
