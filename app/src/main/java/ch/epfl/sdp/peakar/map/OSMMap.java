package ch.epfl.sdp.peakar.map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.view.MotionEvent;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.core.content.res.ResourcesCompat;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.MapTileIndex;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.infowindow.InfoWindow;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import ch.epfl.sdp.peakar.R;
import ch.epfl.sdp.peakar.points.POIPoint;
import ch.epfl.sdp.peakar.points.Point;
import ch.epfl.sdp.peakar.user.services.AuthAccount;
import ch.epfl.sdp.peakar.user.services.AuthService;
import ch.epfl.sdp.peakar.utils.SettingsUtilities;

/**
 * Class used to create an OSMMap
 */
public class OSMMap {

    /*Constants for map creation and initialization*/
    private static final float TILE_SCALING_FACTOR = 1.5f;
    private static final float DEFAULT_ZOOM_FACTOR = 3.5f;
    private static final float BOUNDING_BOX_ZOOM_FACTOR = 1.7f;
    private static final String POLYGON_BOUNDING_BOX_COLOR = "#1EFFE70E";
    //Provider URL for satellite view
    private static final String SATELLITE_MAP_PROVIDER = "http://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/";

    /*Attributes*/
    private final MapView mapView;
    private final Context context;
    private MyLocationNewOverlay locationOverlay = null;
    private boolean isSatellite = false;
    private final List<Marker> markers;
    private HashSet<POIPoint> discoveredPeaks;

    /**
     * Class constructor
     * @param context context
     * @param view mapView
     */
    public OSMMap(Context context, MapView view){
        this.context = context;
        markers = new ArrayList<>();
        discoveredPeaks = new HashSet<>();
        Context applicationContext = context.getApplicationContext();
        Configuration.getInstance().load(applicationContext, PreferenceManager.getDefaultSharedPreferences(applicationContext));
        mapView = view;
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
        mapView.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);
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
     * @param zoomOnUserLocationButton button used to zoom on user location
     * @param changeMapTileSourceButton button used to change the map tiles
     */
    @SuppressLint("UseCompatLoadingForDrawables")
    public void changeMapTileSource(ImageButton zoomOnUserLocationButton, ImageButton changeMapTileSourceButton){
        if(isSatellite){
            //Set default map
            mapView.setTileSource(TileSourceFactory.MAPNIK);
            zoomOnUserLocationButton.setBackground(context.getResources().getDrawable(R.drawable.button_bg_round,null));
            changeMapTileSourceButton.setBackground(context.getResources().getDrawable(R.drawable.button_bg_round,null));
            isSatellite = false;
        }
        else{
            String[] urlArray = {SATELLITE_MAP_PROVIDER};
            mapView.setTileSource(new OnlineTileSourceBase("ARCGisOnline", 0, 18, 256, "", urlArray) {
                @Override
                public String getTileURLString(long pMapTileIndex) {
                    String mImageFilenameEnding = ".png";
                    return getBaseUrl() + MapTileIndex.getZoom(pMapTileIndex) + "/"
                            + MapTileIndex.getY(pMapTileIndex) + "/" + MapTileIndex.getX(pMapTileIndex)
                            + mImageFilenameEnding;
                }
            });
            zoomOnUserLocationButton.setBackground(context.getResources().getDrawable(R.drawable.button_bg_round_white,null));
            changeMapTileSourceButton.setBackground(context.getResources().getDrawable(R.drawable.button_bg_round_white,null));
            isSatellite = true;
        }
    }

    /**
     * Scan the list of discovered POIs and add a marker on the map for each of them
     * if the user account is invalid, no peaks are displayed
     */
    public void setMarkersForDiscoveredPeaks(boolean isSignedIn){
        if(isSignedIn) {
            discoveredPeaks = AuthService.getInstance().getAuthAccount().getDiscoveredPeaks();
            List<String> countryHighPointsName = AuthService.getInstance().getAuthAccount().getDiscoveredCountryHighPointNames();
            drawMarkersOnMap(countryHighPointsName, discoveredPeaks);
            mapView.invalidate();
            setZoomBoundingBox(AuthService.getInstance().getAuthAccount());
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
    private void setZoomBoundingBox(AuthAccount userAccount){
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

    /**
     * This method allows to select a point on the map. It allows to pass to the caller
     * the coordinates of the selected point and it will display a PushPint on the selected
     * point.
     *
     * @param listener      listener for the long press.
     * @param pointUpdater  used to pass the selected point to the caller.
     */
    public void enablePinOnClick(Runnable listener, Consumer<Point> pointUpdater) {

        Overlay touchOverlay = new Overlay(){

            ItemizedIconOverlay<OverlayItem> anotherItemizedIconOverlay = null;
            Polygon boundingBoxPolygon = null;

            @Override
            public boolean onLongPress(final MotionEvent e, final MapView mapView) {

                final Drawable marker = ResourcesCompat.getDrawable(context.getResources(), 
                        R.drawable.pushpin_marker, null);
                Projection proj = mapView.getProjection();

                GeoPoint loc = (GeoPoint) proj.fromPixels((int)e.getX(), (int)e.getY());
                ArrayList<OverlayItem> overlayArray = new ArrayList<>();
                GeoPoint addedGeoPoint = new GeoPoint(loc.getLatitude(), loc.getLongitude());
                OverlayItem mapItem = new OverlayItem("", "", addedGeoPoint);

                boundingBoxPolygon = drawBoundingBox(new POIPoint(addedGeoPoint), mapView, boundingBoxPolygon);

                pointUpdater.accept(new POIPoint(addedGeoPoint));
                mapItem.setMarker(marker);
                overlayArray.add(mapItem);

                if (anotherItemizedIconOverlay!=null) {
                    mapView.getOverlays().remove(anotherItemizedIconOverlay);
                    mapView.invalidate();
                }

                anotherItemizedIconOverlay = new ItemizedIconOverlay<>(context, overlayArray,null);
                mapView.getOverlays().add(anotherItemizedIconOverlay);
                mapView.invalidate();

                listener.run();
                return true;
            }

        };

        mapView.getOverlays().add(touchOverlay);
        mapView.invalidate();

    }

    /**
     * Allows to draw a bounding box on the map around the selectedPoint.
     *
     * @param selectedPoint         point around which drawing the bounding box.
     * @param mapView               mapView to draw the boundingBox polygon.
     * @param boundingBoxPolygon    boundingBoxPolygon already in the overlays.
     * @return                      the new Polygon that is drawn on the map.
     */
    public Polygon drawBoundingBox(Point selectedPoint, MapView mapView, Polygon boundingBoxPolygon) {

        List<GeoPoint> edges = new ArrayList<>();
        BoundingBox boundingBox = selectedPoint.computeBoundingBox(SettingsUtilities.getSelectedRange(context));

        edges.add(new GeoPoint(boundingBox.getLatNorth(), boundingBox.getLonWest()));
        edges.add(new GeoPoint(boundingBox.getLatNorth(), boundingBox.getLonEast()));
        edges.add(new GeoPoint(boundingBox.getLatSouth(), boundingBox.getLonEast()));
        edges.add(new GeoPoint(boundingBox.getLatSouth(), boundingBox.getLonWest()));

        if (boundingBoxPolygon != null) {
            mapView.getOverlays().remove(boundingBoxPolygon);
            mapView.invalidate();
        }

        boundingBoxPolygon = new Polygon();
        edges.add(edges.get(0));    //forces the loop to close(connect last point to first point)
        boundingBoxPolygon.getFillPaint().setColor(Color.parseColor(POLYGON_BOUNDING_BOX_COLOR)); //set fill color
        boundingBoxPolygon.setPoints(edges);

        mapView.getOverlays().add(boundingBoxPolygon);

        return boundingBoxPolygon;

    }

    public void updateMarkers(boolean isSignedIn){
        if(isSignedIn) {
            //Gets the discovered peaks from the database
            HashSet<POIPoint> newDiscoveredPeaks = AuthService.getInstance().getAuthAccount().getDiscoveredPeaks();
            //Holds only the ones that are not already drawn
            newDiscoveredPeaks.removeAll(discoveredPeaks);
            //Adds the one that were not drawn before
            discoveredPeaks.addAll(newDiscoveredPeaks);
            List<String> countryHighPointsName = AuthService.getInstance().getAuthAccount().getDiscoveredCountryHighPointNames();
            //iterate over all new POIs
            drawMarkersOnMap(countryHighPointsName, newDiscoveredPeaks);
        }
        else{
            Toast toast = Toast.makeText(context,context.getString(R.string.toast_no_account),Toast.LENGTH_LONG);
            toast.show();
            mapView.getOverlays().removeAll(markers);
            discoveredPeaks.clear();
        }
        mapView.invalidate();
    }

    private void drawMarkersOnMap(List<String> countryHighPointsName, HashSet<POIPoint> discoveredPeaks) {
        for(POIPoint poi : discoveredPeaks){
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
            markers.add(startMarker);
        }
    }

}
