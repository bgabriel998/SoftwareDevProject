package com.github.bgabriel998.softwaredevproject.points;

import org.osmdroid.bonuspack.location.POI;

import java.util.ArrayList;

public interface GeonamesHandlerIF {
    public void onResponseReceived(ArrayList<POI> result);
}