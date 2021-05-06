package ch.epfl.sdp.peakar.points;

import android.os.AsyncTask;

import androidx.core.util.Pair;

/**
 * DownloadTopographyTask is a async task that downloads the elevation map of the bounding box sorrounding the user.
 * The Elevation map is retrieved using the OpenTopography API as an AAIGrid and then converted into
 * an array of integers representing the height. Using the SRTMGL3 data a precision of 3 arc second
 * (~90 meter) is obtained.
 *
 */
@SuppressWarnings("deprecation")
public class DownloadTopographyTask extends AsyncTask<Point, Void, Pair<int[][], Double>>
        implements DownloadTopographyTaskInterface {

    @Override
    protected Pair<int[][], Double> doInBackground(Point... userPoints) {
        HttpClientTopographyMap httpClient = new HttpClientTopographyMap(userPoints[0]);
        return httpClient.getTopographyMap();
    }

    @Override
    protected void onPostExecute(Pair<int[][], Double> topography) {
        super.onPostExecute(topography);
        onResponseReceived(topography);
    }

    @Override
    public void onResponseReceived(Pair<int[][], Double> topography) {
    }


}