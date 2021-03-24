package com.github.ravifrancesco.softwaredevproject;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class GeoTIFFMap {

    static final int BOUNDING_BOX_RANGE = 20; //range of the bounding box in km
    static final int MINIMUM_DISTANCE_FOR_UPDATE = 2000;    // minimum distance in m between user and old
                                                            // bounding center to update bounding center

    private final String BASE_URL = "https://portal.opentopography.org/API/globaldem";
    private final String DEM_TYPE = "SRTMGL3";
    private final String OUTPUT_FORMAT = "GTiff";


    private UserPoint userPoint;
    private BoundingBox boundingBox;

    private Bitmap topographyMapBitmap;

    public GeoTIFFMap(UserPoint userPoint) {
        this.userPoint = userPoint;
    }

    private void downloadTopographyMap() {
        URL url = generateURL();
        try {
            topographyMapBitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());
        } catch (IOException e) {
            Log.d("3D MAP DOWNLOAD", "Failed");
            e.printStackTrace();
        }
    }

    private boolean updateBoundingBox() {

        POIPoint oldBoundingCenter = new POIPoint(boundingBox.getCenterWithDateLine());

        if (userPoint.computeFlatDistance(oldBoundingCenter) > MINIMUM_DISTANCE_FOR_UPDATE) {
            downloadTopographyMap();
            return true;
        } else {
            return false;
        }

    }

    private URL generateURL() {

        String south = String.valueOf(boundingBox.getLatSouth());
        String north = String.valueOf(boundingBox.getLatNorth());
        String west = String.valueOf(boundingBox.getLonWest());
        String east = String.valueOf(boundingBox.getLonEast());

        try {
            return new URL( BASE_URL + "?" +
                            "?demtype=" + DEM_TYPE + "&" +
                            "south=" + south + "&" +
                            "north=" + north + "&" +
                            "west=" + west + "&" +
                            "east=" + east + "&" +
                            "outputFormat=" + OUTPUT_FORMAT);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }

    }







}
