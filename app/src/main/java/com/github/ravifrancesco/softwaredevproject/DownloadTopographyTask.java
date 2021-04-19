package com.github.ravifrancesco.softwaredevproject;

import android.os.AsyncTask;
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

public class DownloadTopographyTask extends AsyncTask<Void, Void, int[][]> implements DownloadTopographyTaskIF {
    static final int BOUNDING_BOX_RANGE = 20; //range of the bounding box in km

    private final String BASE_URL = "https://portal.opentopography.org/API/globaldem";
    private final String DEM_TYPE = "SRTMGL3";
    private final String OUTPUT_FORMAT = "AAIGrid";

    private final BoundingBox boundingBox;

    /**
     * Constructor for the ElevationMap.
     *
     * @param userPoint the user point around which the bounding box is computed.
     */
    public DownloadTopographyTask(UserPoint userPoint) {
        this.boundingBox = userPoint.computeBoundingBox(BOUNDING_BOX_RANGE);
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

        return topographyMap;
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
    public void onResponseReceived(int[][] topography) {
    }


}

interface DownloadTopographyTaskIF {
    public void onResponseReceived(int[][] topography);
}
