package com.github.bgabriel998.softwaredevproject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.preference.PreferenceManager;
import android.view.ViewTreeObserver;
import android.widget.Toast;

import androidx.core.content.res.ResourcesCompat;

import com.github.giommok.softwaredevproject.FirebaseAccount;
import com.github.ravifrancesco.softwaredevproject.POIPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.location.POI;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.infowindow.InfoWindow;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class OSMMap {

    /*Constants for map creation and initialization*/
    private static final float TILE_SCALING_FACTOR = 1.5f;
    private static final float DEFAULT_ZOOM_FACTOR = 3.5f;
    private static final float BOUNDING_BOX_ZOOM_FACTOR = 1.7f;

    /*Attributes*/
    private final MapView mapView;
    private final Context context;

    /**
     * Class constructor
     * @param context context
     * @param view mapView
     */
    public OSMMap(Context context, MapView view){
        this.context = context;
        Context applicationContext = context.getApplicationContext();
        Configuration.getInstance().load(applicationContext, PreferenceManager.getDefaultSharedPreferences(applicationContext));
        mapView = (MapView) view;
        initMapView();
    }

    /**
     * MapView getter
     * @return mapView object
     */
    public MapView getMapView(){
        return mapView;
    }


    /**
     * Initializes map layout, zoom, scaling factor etc...
     */
    private void initMapView(){
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setTilesScaleFactor(TILE_SCALING_FACTOR);
        IMapController mapController  = mapView.getController();
        mapController.setZoom(DEFAULT_ZOOM_FACTOR);
        mapView.setBuiltInZoomControls(false);
        mapView.setMultiTouchControls(true);
    }

    /**
     * Display user location on map
     */
    public void displayUserLocation(){
        MyLocationNewOverlay mLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(context),mapView);
        Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_baseline_my_location_24);
        mLocationOverlay.setPersonIcon(largeIcon);
        mLocationOverlay.enableMyLocation();
        mapView.getOverlays().add(mLocationOverlay);
    }

    /**
     * Scan the list of discovered POIs and add a marker on the map for each of them
     * if the user account is invalid, no peaks are displayed
     */
    public void setMarkersForDiscoveredPeaks(FirebaseAccount userAccount){
        if(userAccount.isSignedIn()) {
            HashSet<POIPoint> discoveredPeaks = userAccount.getDiscoveredPeaks();
            List<String> countryHighPointsName = userAccount.getDiscoveredCountryHighPointNames();
            //iterate over all POI
            for (POIPoint poi : discoveredPeaks) {
                GeoPoint startPoint = new GeoPoint(poi.getLatitude(), poi.getLongitude());
                Marker startMarker = new Marker(mapView);

                //Set marker icon
                startMarker.setIcon(getCustomMarkerIcon(poi, countryHighPointsName));

                startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                startMarker.setPosition(startPoint);
                startMarker.setTitle(poi.getName() + "\n" + ((long) poi.getAltitude()));

                //Set info window
                InfoWindow infoWindow = new CustomInfoWindow(R.layout.bonuspack_bubble, mapView, context);
                startMarker.setInfoWindow(infoWindow);
                infoWindow.onOpen(startMarker);
                mapView.getOverlays().add(startMarker);
            }
            mapView.invalidate();
            setZoomBoundingBox(userAccount);
        }
        else{
            Toast toast= Toast.makeText(context,context.getString(R.string.toast_no_account),Toast.LENGTH_LONG);
            toast.show();
        }
    }

    /**
     * Sets marker icon depending on if the peak is the country highest peak or not.
     * Sets gold marker if country high point and black if not.
     * @param currentPOI POI to set the marker on
     * @param countryHighPointsName list of country highest points
     * @return marker icon
     */
    private Drawable getCustomMarkerIcon(POIPoint currentPOI, List<String> countryHighPointsName){
        //Check if the peak is in the list of countries highest point
        if(countryHighPointsName.contains(currentPOI.getName())){
            return ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_mountain_marker_resize_gold, null);
        }
        else{
            return ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_mountain_marker_resize, null);
        }
    }

    /**
     * Compute bounding box around discovered peaks and zoom on bounding
     * box
     * @param userAccount firebase account
     */
    private void setZoomBoundingBox(FirebaseAccount userAccount){
        //Create a bounding box and zoom in
        BoundingBox boundingBox = computeArea(new ArrayList<>(userAccount.getDiscoveredPeaks()));
        zoomToBounds(boundingBox.increaseByScale(BOUNDING_BOX_ZOOM_FACTOR));
        mapView.invalidate();
    }

    /**
     * zoom animation on map
     * @param box bounding box to zoom into
     */
    private void zoomToBounds(final BoundingBox box) {
        if (mapView.getHeight() > 0) {
            mapView.zoomToBoundingBox(box, true);

        } else {
            ViewTreeObserver vto = mapView.getViewTreeObserver();
            vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

                @Override
                public void onGlobalLayout() {
                    mapView.zoomToBoundingBox(box, true);
                    ViewTreeObserver vto2 = mapView.getViewTreeObserver();
                    vto2.removeOnGlobalLayoutListener(this);
                }
            });
        }
    }

    /**
     * Compute bounding box that encloses  all geoPoints
     * @param points list of poi points
     * @return bounding box
     */
    private BoundingBox computeArea(ArrayList<POIPoint> points) {
        double nord = 0, sud = 0, ovest = 0, est = 0;
        for (int i = 0; i < points.size(); i++) {
            if (points.get(i) == null) continue;

            double lat = points.get(i).getLatitude();
            double lon = points.get(i).getLongitude();

            if ((i == 0) || (lat > nord)) nord = lat;
            if ((i == 0) || (lat < sud)) sud = lat;
            if ((i == 0) || (lon < ovest)) ovest = lon;
            if ((i == 0) || (lon > est)) est = lon;
        }
        return new BoundingBox(nord, est, sud, ovest);

    }
}
