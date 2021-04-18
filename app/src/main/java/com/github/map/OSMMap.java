package com.github.map;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.LinearLayout;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;

import com.github.bgabriel998.softwaredevproject.R;
import com.github.ravifrancesco.softwaredevproject.POIPoint;

import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.infowindow.InfoWindow;

import java.util.HashSet;
import java.util.List;

public class OSMMap {

    /*Constants for map creation and initialization*/
    private static final float TILE_SCALING_FACTOR = 1.5f;
    private static final float DEFAULT_ZOOM_FACTOR = 3.5f;

    /*Attributes*/
    private static OSMMap osmMap;
    private static MapView mapView;

    public OSMMap(Context context, View view){

    }

    public OSMMap getOsmMap(){
        if(osmMap == null){


            osmMap.initMapView();
        }
        return osmMap;
    }

    /**
     * Initializes map layout, zoom, scaling factor etc...
     */
    public void initMapView(){
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setTilesScaleFactor(TILE_SCALING_FACTOR);
        IMapController mapController  = mapView.getController();
        mapController.setZoom(DEFAULT_ZOOM_FACTOR);
        mapView.setBuiltInZoomControls(false);
        mapView.setMultiTouchControls(true);
    }


    /**
     * Scan the list of discovered POIs and add a marker on the map for each of them
     * @param discoveredPeaks list of discovered peaks
     * @param context app context
     */
 /*   private void setMarkersForDiscoveredPeaks(HashSet<POIPoint> discoveredPeaks, List<String> countryHighPointsName, Context context){
        //iterate over all POI
        for(POIPoint poi : discoveredPeaks){
            GeoPoint startPoint = new GeoPoint(poi.getLatitude(), poi.getLongitude());
            Marker startMarker = new Marker(mapView);

            //Set marker icon
            startMarker.setIcon(getCustomMarkerIcon(poi,countryHighPointsName));

            startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            startMarker.setPosition(startPoint);
            startMarker.setTitle(poi.getName() + "\n" + ((long)poi.getAltitude()));

            InfoWindow infoWindow = new MapActivity.CustomInfoWindow(R.layout.bonuspack_bubble, mapView);
            startMarker.setInfoWindow(infoWindow);
            infoWindow.onOpen(startMarker);
            mapView.getOverlays().add(startMarker);
        }
        mapView.invalidate();
    }

*/

    /**
     * Sets marker icon depending on if the peak is the country highest peak or not.
     * Sets gold marker if country high point and black if not.
     * @param currentPOI POI to set the marker on
     * @param countryHighPointsName list of country highest points
     * @return marker icon
     */
    private Drawable getCustomMarkerIcon(POIPoint currentPOI, List<String> countryHighPointsName, Context context){
        //Check if the peak is in the list of countries highest point
        if(countryHighPointsName.contains(currentPOI.getName())){
            return ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_mountain_marker_resize_gold, null);
        }
        else{
            return ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_mountain_marker_resize, null);
        }
    }


}
