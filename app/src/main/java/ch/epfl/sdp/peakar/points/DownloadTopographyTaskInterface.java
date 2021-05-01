package ch.epfl.sdp.peakar.points;

import androidx.core.util.Pair;

/**
 * Interface for the download of the topography task
 */
public interface DownloadTopographyTaskInterface {
    void onResponseReceived(Pair<int[][], Double> topography);
}
