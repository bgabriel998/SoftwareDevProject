package ch.epfl.sdp.peakar.points;

import androidx.core.util.Pair;

public interface DownloadTopographyTaskInterface {
    void onResponseReceived(Pair<int[][], Double> topography);
}
