package com.github.ravifrancesco.softwaredevproject;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.AsyncTask;
import android.util.Log;

import com.squareup.picasso.Picasso;

import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

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
        downloadTopographyMap();
    }

    private void downloadTopographyMap() {
        URL url = generateURL();
        try {
            Log.d("3D MAP", "Downloading");
            topographyMapBitmap = Picasso.get().load(url.toString()).get();
        } catch (IOException e) {
            Log.d("3D MAP", "Download Failed");
            e.printStackTrace();
        }
    }

    public Bitmap getTopographyMapBitmap() {

        updateBoundingBox();
        return topographyMapBitmap;

    }

    private void updateBoundingBox() {

        Log.d("3D MAP",  "Updating");
        POIPoint oldBoundingCenter = new POIPoint(boundingBox.getCenterWithDateLine());
        Log.d("3D MAP", "Distance from old bounding center: " + userPoint.computeFlatDistance(oldBoundingCenter));

        if (userPoint.computeFlatDistance(oldBoundingCenter) > MINIMUM_DISTANCE_FOR_UPDATE) {
            Log.d("3D MAP",  "New Map download");
            boundingBox = userPoint.computeBoundingBox(BOUNDING_BOX_RANGE);
            downloadTopographyMap();
        }

        Log.d("3D MAP",  "Finished updating");

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
            Log.d("3D MAP", "Generated url: " + url.toString());
            return url;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }

    }

}
