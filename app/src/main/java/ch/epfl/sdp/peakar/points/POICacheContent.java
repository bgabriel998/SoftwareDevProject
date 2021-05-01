package ch.epfl.sdp.peakar.points;

import org.osmdroid.util.BoundingBox;

import java.util.ArrayList;

public class POICacheContent {
    /*List of POIs contained in the cache*/
    private ArrayList<POIPoint> cachedPOIPoints;
    /*Bounding box saved to cache*/
    private BoundingBox cachedBoundingBox;

    /**
     * Constructor
     * @param cachedPOIPoints list of cached POI points
     * @param cachedBoundingBox cached bounding box
     */
    public POICacheContent(ArrayList<POIPoint> cachedPOIPoints,BoundingBox cachedBoundingBox){
        this.cachedBoundingBox = cachedBoundingBox;
        this.cachedPOIPoints = cachedPOIPoints;
    }

    public ArrayList<POIPoint> getCachedPOIPoints() {
        return cachedPOIPoints;
    }

    public BoundingBox getCachedBoundingBox() {
        return cachedBoundingBox;
    }
}
