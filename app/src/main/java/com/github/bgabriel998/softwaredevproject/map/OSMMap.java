package com.github.bgabriel998.softwaredevproject.map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.core.content.res.ResourcesCompat;

import com.github.bgabriel998.softwaredevproject.points.POIPoint;
import com.github.bgabriel998.softwaredevproject.user.account.FirebaseAccount;
import com.github.bgabriel998.softwaredevproject.R;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.MapTileIndex;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.infowindow.InfoWindow;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class OSMMap {

    /*Constants for map creation and initialization*/
    private static final float TILE_SCALING_FACTOR = 1.5f;
    private static final float DEFAULT_ZOOM_FACTOR = 3.5f;
    private static final float BOUNDING_BOX_ZOOM_FACTOR = 1.7f;

    /*Attributes*/
    private final MapView mapView;
    private final Context context;
    private MyLocationNewOverlay locationOverlay = null;
    private boolean isSatellite = false;

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
        mapView.setHorizontalMapRepetitionEnabled(true);
        mapView.setVerticalMapRepetitionEnabled(false);
        mapView.setScrollableAreaLimitLatitude(MapView.getTileSystem().getMaxLatitude(), MapView.getTileSystem().getMinLatitude(), 0);
    }

    /**
     * Display user location on map
     */
    public void displayUserLocation(){
        locationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(context),mapView);
        locationOverlay.enableMyLocation();
        mapView.getOverlays().add(locationOverlay);
    }

    /**
     * Zoom on user Location
     */
    public void zoomOnUserLocation(){
        if(locationOverlay != null) {
            mapView.getController().setZoom(13.0);
            mapView.getController().animateTo(locationOverlay.getMyLocation());
        }
    }

    /**
     * Zoom on user Location
     * @param zoomOnUserLocationButton
     * @param changeMapTileSourceButton
     */
    @SuppressLint("UseCompatLoadingForDrawables")
    public void changeMapTileSource(ImageButton zoomOnUserLocationButton, ImageButton changeMapTileSourceButton){
        if(isSatellite){
            //Set default map
            mapView.setTileSource(TileSourceFactory.MAPNIK);
            zoomOnUserLocationButton.setBackground(context.getResources().getDrawable(R.drawable.button_bg_round));
            changeMapTileSourceButton.setBackground(context.getResources().getDrawable(R.drawable.button_bg_round));
            isSatellite = false;
        }
        else{
            String[] urlArray = {"http://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/"};
            mapView.setTileSource(new OnlineTileSourceBase("ARCGisOnline", 0, 18, 256, "", urlArray) {
                @Override
                public String getTileURLString(long pMapTileIndex) {
                    String mImageFilenameEnding = ".png";
                    return getBaseUrl() + MapTileIndex.getZoom(pMapTileIndex) + "/"
                            + MapTileIndex.getY(pMapTileIndex) + "/" + MapTileIndex.getX(pMapTileIndex)
                            + mImageFilenameEnding;
                }
            });
            zoomOnUserLocationButton.setBackground(context.getResources().getDrawable(R.drawable.button_bg_round_white));
            changeMapTileSourceButton.setBackground(context.getResources().getDrawable(R.drawable.button_bg_round_white));
            isSatellite = true;
        }
    }

    /**
     * Scan the list of discovered POIs and add a marker on the map for each of them
     * if the user account is invalid, no peaks are displayed
     */
    public void setMarkersForDiscoveredPeaks(FirebaseAccount userAccount, boolean isSignedIn){
        if(isSignedIn) {
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
            Toast toast = Toast.makeText(context,context.getString(R.string.toast_no_account),Toast.LENGTH_LONG);
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
        if(userAccount.getDiscoveredPeaks().size() != 0) {
            BoundingBox boundingBox = computeArea(new ArrayList<>(userAccount.getDiscoveredPeaks()));
            zoomToBounds(boundingBox.increaseByScale(BOUNDING_BOX_ZOOM_FACTOR));
            mapView.invalidate();
        }
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
        AtomicReference<Double> north = new AtomicReference<>( points.get(0).getLatitude());
        AtomicReference<Double> south = new AtomicReference<>( points.get(0).getLatitude());
        AtomicReference<Double> west = new AtomicReference<>( points.get(0).getLongitude());
        AtomicReference<Double> east = new AtomicReference<>( points.get(0).getLongitude());
        points.forEach(loc -> {
                    north.set(Math.max(loc.getLatitude(), north.get()));
                    south.set(Math.min(loc.getLatitude(), south.get()));
                    west.set(Math.min(loc.getLongitude(), west.get()));
                    east.set(Math.max(loc.getLongitude(), east.get()));
                }
        );
        return new BoundingBox(north.get(), east.get(), south.get(), west.get());
    }
}
