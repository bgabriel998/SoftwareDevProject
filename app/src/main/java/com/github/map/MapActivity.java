package com.github.map;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.bgabriel998.softwaredevproject.R;
import com.github.giommok.softwaredevproject.FirebaseAccount;
import com.github.ravifrancesco.softwaredevproject.POIPoint;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.infowindow.InfoWindow;
import java.util.HashSet;
import java.util.List;

public class MapActivity extends AppCompatActivity {
    private static final float TILE_SCALING_FACTOR = 1.5f;
    private static final float DEFAULT_ZOOM_FACTOR = 3.5f;
    private MapView mapView = null;
    private Context applicationContext = null;
    private IMapController mapController = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        applicationContext = getApplicationContext();
        Configuration.getInstance().load(applicationContext, PreferenceManager.getDefaultSharedPreferences(applicationContext));

        mapView = initMapView();

        FirebaseAccount account = FirebaseAccount.getAccount();
        setMarkersForDiscoveredPeaks(account.getDiscoveredPeaks(),account.getDiscoveredCountryHighPointNames(),this);
    }


    @Override
    public void onResume() {
        super.onResume();;
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    /**
     * Initializes map layout, zoom, scaling factor etc...
     * @return initialized mapView
     */
    private MapView initMapView(){
        MapView retMapView = (MapView) findViewById(R.id.map);
        retMapView.setTileSource(TileSourceFactory.MAPNIK);
        retMapView.setTilesScaleFactor(TILE_SCALING_FACTOR);
        IMapController mapController  = retMapView.getController();
        mapController.setZoom(DEFAULT_ZOOM_FACTOR);
        retMapView.setBuiltInZoomControls(false);
        retMapView.setMultiTouchControls(true);
        return  retMapView;
    }


    /**
     * Scan the list of discovered POIs and add a marker on the map for each of them
     * @param discoveredPeaks list of discovered peaks
     * @param context app context
     */
    private void setMarkersForDiscoveredPeaks(HashSet<POIPoint> discoveredPeaks, List<String> countryHighPointsName, Context context){
        //iterate over all POI
        for(POIPoint poi : discoveredPeaks){
            GeoPoint startPoint = new GeoPoint(poi.getLatitude(), poi.getLongitude());
            Marker startMarker = new Marker(mapView);

            //Set marker icon
            startMarker.setIcon(getCustomMarkerIcon(poi,countryHighPointsName));

            startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            startMarker.setPosition(startPoint);
            startMarker.setTitle(poi.getName() + "\n" + ((long)poi.getAltitude()));

            InfoWindow infoWindow = new CustomInfoWindow(R.layout.bonuspack_bubble, mapView);
            startMarker.setInfoWindow(infoWindow);
            infoWindow.onOpen(startMarker);
            mapView.getOverlays().add(startMarker);
        }
        mapView.invalidate();
    }


    /**
     * Sets marker icon depending on if the peak is the country highest peak or not.
     * Sets gold marker if country high point and black if not.
     * @param currentPOI POI to set the marker on
     * @param countryHighPointsName list of country highest points
     * @return marker icon
     */
    private Drawable getCustomMarkerIcon(POIPoint currentPOI,List<String> countryHighPointsName){
        //Check if the peak is in the list of countries highest point
        if(countryHighPointsName.contains(currentPOI.getName())){
            return ResourcesCompat.getDrawable(getResources(), R.drawable.ic_mountain_marker_resize_gold, null);
        }
        else{
            return ResourcesCompat.getDrawable(getResources(), R.drawable.ic_mountain_marker_resize, null);
        }
    }


    /**
     * Custom info window for markers. The design from the info
     * window provided by osmdroid pack was ugly so this class
     * extends the default one
     */
    private class CustomInfoWindow extends InfoWindow{
        public CustomInfoWindow(int layoutResId, MapView mapView) {
            super(layoutResId, mapView);
        }

        /**
         * Callback method called on maker info window closing
         */
        public void onClose() {

        }

        /**
         * Callback method called when the user clicks on the marker
         * @param arg0 marker object. The Title field of this object is used to pass
         *             info about the peak to the custom info window
         */
        public void onOpen(Object arg0) {
            //retrieve views
            LinearLayout layout = (LinearLayout) mView.findViewById(R.id.bubble_layout);
            TextView txtTitle = (TextView) mView.findViewById(R.id.bubble_title);
            TextView txtDescription = (TextView) mView.findViewById(R.id.bubble_description);

            //Text information are passed to this class using the arg0 marker input
            //The title field of the marker is used to transfer the string
            String[] rawStringArray = ((Marker)arg0).getTitle().split("\n");
            String peakName = rawStringArray[0];
            String peakHeight = rawStringArray[1];
            //... Handle other fields to display here

            //Set the peak name
            txtTitle.setText(peakName);
            //Set peak height in meters
            txtDescription.setText(getResources().getString(R.string.marker_altitude,peakHeight));

            layout.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    //... add here method to redirect to the collection of peaks (activity)
                }
            });

            //Handle hide and show Info window
            Marker current = (Marker) arg0;
            toggleWindowVisibility(current, layout);

        }

        /**
         * toggles the marker visibility
         * @param marker info window marker
         * @param layout info window layout
         */
        private void toggleWindowVisibility(Marker marker, LinearLayout layout){
            for (int i = 0; i < mMapView.getOverlays().size(); ++i) {
                Overlay o = mMapView.getOverlays().get(i);
                if (o instanceof Marker) {
                    Marker m = (Marker) o;
                    if (m != marker) {
                        //Toggle marker visibility
                        if (layout.getVisibility() == View.GONE) {
                            layout.setVisibility(View.VISIBLE);
                        } else {
                            layout.setVisibility(View.GONE);
                        }
                    }
                }
            }
        }
    }

}

