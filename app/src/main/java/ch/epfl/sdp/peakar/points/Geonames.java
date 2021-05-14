package ch.epfl.sdp.peakar.points;

import org.osmdroid.bonuspack.location.POI;

import java.util.ArrayList;

public interface Geonames {
    void onResponseReceived(ArrayList<POI> result);
}