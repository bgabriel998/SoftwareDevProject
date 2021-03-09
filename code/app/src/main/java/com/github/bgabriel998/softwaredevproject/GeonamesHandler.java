package com.github.bgabriel998.softwaredevproject;


import android.util.Log;

import org.osmdroid.bonuspack.location.GeoNamesPOIProvider;
import org.osmdroid.bonuspack.location.POI;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;

public class GeonamesHandler {

    private static final int GET_POI_MAX_RESULTS        =          200;     //Maximum result for function GetPOI
    private static final double GET_POI_MAX_RANGE       =         19.9;     //Maximum range to get Poi

    private final GeoNamesPOIProvider poiProvider;
    private Thread queryThread;

    /**
     * Initializes provider
     * @param username Geonames provider username
     */
    public GeonamesHandler(String username){
        if(username == null || username.isEmpty())
            throw new IllegalArgumentException("GeonamesHandler username provider can't be null");
        poiProvider = new GeoNamesPOIProvider(username);
    }

    /**
     * getPOI get points of interest nearby given location
     * using geonames provider
     * @param userLocation user location as GeoPoint
     * @return List of point of interest near user location
     */
    private ArrayList<POI> getPOI(GeoPoint userLocation){
        if(userLocation == null)
            throw new IllegalArgumentException("userLocation can't be null");
        //List containing all POIs around userLocation
        ArrayList<POI> results = new ArrayList<>();
        //Search POI close to the given location, using a limit of results and a given range
        results = poiProvider.getPOICloseTo(userLocation,GET_POI_MAX_RESULTS,GET_POI_MAX_RANGE);
        return results;
    }

    /**
     * filterPOI : filter out point of interest list. The result list contains only peaks
     * @param rawPOIList list containing all points of interests around Geopoint
     * @return filtered list of points of interests containing only peaks
     */
    private ArrayList<POI> filterPOI(ArrayList<POI> rawPOIList){
        if(rawPOIList == null) throw new IllegalStateException("raw POI list is undefined");
        //Arraylist containing only mountains POI
        ArrayList<POI> results = new ArrayList<>();
        for(POI point : rawPOIList){
            //filter the poi's to get only the mountains
            if(point.mCategory.equals("mountain")){
                results.add(point);
            }
        }
        return results;
    }

    /**
     * getSurroundingPeaks: returns a list of geopoints corresponding to the mountains
     * around user location
     * @param userLocation Geopoint corresponding to user Location
     * @return List of mountains around user location
     * @throws InterruptedException interrupt exception occurs if thread is interrupted
     */
    public ArrayList<POI> getSurroundingPeaks(GeoPoint userLocation) throws InterruptedException {
        final ArrayList<POI>[] pois = new ArrayList[]{new ArrayList<POI>()};
        final ArrayList<POI>[] filtered = new ArrayList[]{new ArrayList<POI>()};
        queryThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    GeonamesHandler handler = new GeonamesHandler("bgabrie1");
                    pois[0] = handler.getPOI(userLocation);
                    filtered[0] = handler.filterPOI(pois[0]);
                }
                catch (Exception e){
                    Log.e("GeonamesHandler", e.toString());
                }
            }
        });
        queryThread.start();
        queryThread.join();
        return filtered[0];
    }
    
}
