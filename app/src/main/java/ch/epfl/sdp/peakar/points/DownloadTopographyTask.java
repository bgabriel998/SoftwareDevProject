package ch.epfl.sdp.peakar.points;

import android.os.AsyncTask;

import androidx.core.util.Pair;

/**
 * DownloadTopographyTask is a async task that downloads the elevation map around a point.
 *
 */
@SuppressWarnings("deprecation")
public class DownloadTopographyTask extends AsyncTask<Point, Void, Pair<int[][], Double>>
        implements DownloadTopography {

    @Override
    protected Pair<int[][], Double> doInBackground(Point... points) {
        HttpClientTopographyMap httpClient = new HttpClientTopographyMap(points[0]);
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