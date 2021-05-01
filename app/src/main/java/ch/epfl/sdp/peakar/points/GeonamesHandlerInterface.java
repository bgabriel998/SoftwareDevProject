package ch.epfl.sdp.peakar.points;

import org.osmdroid.bonuspack.location.POI;

import java.util.ArrayList;

public interface GeonamesHandlerInterface {
    public void onResponseReceived(ArrayList<POI> result);
}