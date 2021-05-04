package ch.epfl.sdp.peakar.points;

import org.osmdroid.util.BoundingBox;

import java.util.ArrayList;

/**
 * Class used to create object that will be serialized and deserialized
 * in POICache.java file.
 * in JSON format
 */
public class POICacheContent {
    /*List of POIs contained in the cache*/
    private final ArrayList<POIPoint> cachedPOIPoints;
    /*Bounding box saved to cache*/
    private final BoundingBox cachedBoundingBox;

    /**
     * Constructor
     * @param cachedPOIPoints list of cached POI points
     * @param cachedBoundingBox cached bounding box
     */
    public POICacheContent(ArrayList<POIPoint> cachedPOIPoints,BoundingBox cachedBoundingBox){
        this.cachedBoundingBox = cachedBoundingBox;
        this.cachedPOIPoints = cachedPOIPoints;
    }

    /**
     * @return array list of POI
     */
    public ArrayList<POIPoint> getCachedPOIPoints() {
        return cachedPOIPoints;
    }

    /**
     * @return bounding box
     */
    public BoundingBox getCachedBoundingBox() {
        return cachedBoundingBox;
    }
}
