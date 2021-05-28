package ch.epfl.sdp.peakar.points;

import android.content.Context;
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

import ch.epfl.sdp.peakar.utils.SettingsUtilities;

import static android.app.PendingIntent.getActivity;

/**
 * Class to request the topography map of the bounding box around a point.
 * The Elevation map is retrieved using the OpenTopography API as an AAIGrid and then converted into
 * an array of integers representing the height. Using the SRTMGL3 data a precision of 3 arc second
 * (~90 meter) is obtained.
 *
 * This class should not be used to directly get the topographyMap. To get the topographyMap use
 * the DownloadTopographyTask.
 */
public class HttpClientTopographyMap {

    static final int HTTP_OK_CODE = 200; //range of the bounding box in km

    private static final String BASE_URL = "https://portal.opentopography.org/API/globaldem";
    private static final String DEM_TYPE = "SRTMGL3";
    private static final String OUTPUT_FORMAT = "AAIGrid";

    private Pair<int[][], Double> result;

    private final BoundingBox boundingBox;

    private Context context;

    /**
     * Constructor of class that handles the download of the AAIGrid and building of the matrix representing
     * the elevation map.
     *
     * @param point     point around which compute the topography map
     * @param context   context of the application.
     */
    public HttpClientTopographyMap(Point point, Context context){
        boundingBox = point.computeBoundingBox(SettingsUtilities.getSelectedRange(context));
        URL url = generateURL();

        try {
            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpRequestBase base = new HttpGet(url.toString());
            HttpResponse response = httpClient.execute(base);

            if (response.getStatusLine().getStatusCode() == HTTP_OK_CODE) {
                result =  parseResponse(response);
            } else {
                Log.d("3d MAP", "Http error code: " + response.getStatusLine().getStatusCode());
                result = null;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets the topography map and the mapCellSize
     * @return Pair<int[][], Double> that contains the topographyMap and the mapCellSize
     */
    public Pair<int[][], Double> getTopographyMap() {
           return result;
    }

    /**
     * This method handles the parsing of the response obtained via HTTP request. It saves the
     * size of the matrix to build and passes them to the buildMapGrid method.
     *
     * @param response  HTTPResponse to parse.
     * @return Pair<int[][], Double> that contains the topographyMap and the mapCellSize
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
     * @return Pair<int[][], Double> that contains the topographyMap and the mapCellSize
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
}
