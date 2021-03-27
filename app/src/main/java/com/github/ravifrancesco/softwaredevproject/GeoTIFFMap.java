package com.github.ravifrancesco.softwaredevproject;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.AsyncTask;
import android.util.Log;

import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
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
        this.boundingBox = userPoint.computeBoundingBox(BOUNDING_BOX_RANGE);
        //downloadTopographyMap();
    }

    private void downloadTopographyMap() {
        URL url = generateURL();
        try {
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            topographyMapBitmap = BitmapFactory.decodeStream(input);
        } catch (IOException e) {
            Log.d("3D MAP DOWNLOAD", "Failed");
            e.printStackTrace();
        }
    }

    public Bitmap getTopographyMapBitmap() {

        updateBoundingBox();
        return topographyMapBitmap;

    }

    private void updateBoundingBox() {

        POIPoint oldBoundingCenter = new POIPoint(boundingBox.getCenterWithDateLine());

        if (userPoint.computeFlatDistance(oldBoundingCenter) > MINIMUM_DISTANCE_FOR_UPDATE) {
            boundingBox = userPoint.computeBoundingBox(BOUNDING_BOX_RANGE);
            downloadTopographyMap();
        }

    }

    // TODO change it back to private
    public URL generateURL() {

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
            Log.d("GENERATED URL", url.toString());
            return url;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }

    }

}
