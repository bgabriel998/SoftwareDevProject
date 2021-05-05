package ch.epfl.sdp.peakar.utils;

import androidx.core.util.Pair;

import org.osmdroid.util.BoundingBox;

import java.util.List;

import ch.epfl.sdp.peakar.points.POIPoint;

public class OfflineContentContainer {

    public BoundingBox boundingBox;
    public Pair<int[][], Double> topography;
    public List<POIPoint> POIPoints;

}
