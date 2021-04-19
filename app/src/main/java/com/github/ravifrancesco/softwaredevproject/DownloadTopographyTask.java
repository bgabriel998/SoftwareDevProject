package com.github.ravifrancesco.softwaredevproject;

import android.os.AsyncTask;
import android.util.Log;

import androidx.core.util.Pair;

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

public class DownloadTopographyTask extends AsyncTask<UserPoint, Void, Pair<int[][], Double>> implements DownloadTopographyTaskIF {
    static final int BOUNDING_BOX_RANGE = 20; //range of the bounding box in km

    private final String BASE_URL = "https://portal.opentopography.org/API/globaldem";
    private final String DEM_TYPE = "SRTMGL3";
    private final String OUTPUT_FORMAT = "AAIGrid";

    private BoundingBox boundingBox;

    /**
     * This method handles the download of the AAIGrid and building of the matrix representing
     * the elevation map.
     * @param userPoints UserPoint
     * @return Pair<int[][], Double> that contains the topographyMap and the mapCellSize
     */
    @Override
    protected Pair<int[][], Double> doInBackground(UserPoint... userPoints) {
        boundingBox = userPoints[0].computeBoundingBox(BOUNDING_BOX_RANGE);
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

    /**
     * After the download call onResponseReceived to
     * @param topography Pair<int[][], Double> that contains the topographyMap and the mapCellSize
     */
    @Override
    protected void onPostExecute(Pair<int[][], Double> topography) {
        super.onPostExecute(topography);
        onResponseReceived(topography);
    }


    /**
     * This method handles the parsing of the response obtained via HTTP request. It saves the
     * size of the matrix to build and passes them to the buildMapGrid method.
     *
     * @param response  HTTPResponse to parse.
     */
    private Pair<int[][], Double> parseResponse(HttpResponse response) {
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
    private Pair<int[][], Double> buildMapGrid(int nRow, int nCol, Scanner responseObj) {

        // skip 2 lines (x, y corner)
        responseObj.nextLine();
        responseObj.nextLine();

        // get cell size
        double mapCellSize = Double.parseDouble(responseObj.nextLine().replaceAll("[a-zA-Z]", ""));

        // skip another line (NODATA_value)
        responseObj.nextLine();

        // build matrix
        int[][] topographyMap = IntStream.range(0, nRow)
                .boxed()
                .map(i -> Arrays.stream(responseObj.nextLine().substring(1).split(" ", nCol))
                        .mapToInt(Integer::parseInt)
                        .toArray())
                .toArray(int[][]::new);

        Log.d("3D MAP", "Generated Map with size (" + nRow + ", " + nCol +")");
        Log.d("3D MAP", "Cell size = " + mapCellSize);

        return new Pair<>(topographyMap, mapCellSize);
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



    @Override
    public void onResponseReceived(Pair<int[][], Double> topography) {
    }


}

interface DownloadTopographyTaskIF {
    void onResponseReceived(Pair<int[][], Double> topography);
}
