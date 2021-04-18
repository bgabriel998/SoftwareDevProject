package com.github.bgabriel998.softwaredevproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;

import com.github.giommok.softwaredevproject.FirebaseAccount;
import com.github.ravifrancesco.softwaredevproject.POIPoint;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.infowindow.InfoWindow;
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.HashSet;

public class Button3Activity extends AppCompatActivity {

    private final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;
    private MapView mapView = null;
    private GeoPoint userLocation = null;
    private Context applicationContext = null;
    private IMapController mapController = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applicationContext = getApplicationContext();
        Configuration.getInstance().load(applicationContext, PreferenceManager.getDefaultSharedPreferences(applicationContext));

        //Request permissions
        setContentView(R.layout.activity_button3);
        requestPermissionsIfNecessary(new String[] {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        });




        mapView = (MapView) findViewById(R.id.map);
        mapView.setTileSource(TileSourceFactory.OpenTopo);
        mapView.setTilesScaleFactor(1.2f);

        MyLocationNewOverlay mLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(applicationContext),mapView);
        mLocationOverlay.enableMyLocation();
        mapView.getOverlays().add(mLocationOverlay);

        mapController = mapView.getController();
        mapController.setZoom(3.5);


        mapView.setBuiltInZoomControls(false);
        mapView.setMultiTouchControls(true);

        DisplayMetrics dm = applicationContext.getResources().getDisplayMetrics();
        ScaleBarOverlay mScaleBarOverlay = new ScaleBarOverlay(mapView);
        mScaleBarOverlay.setCentred(false);

        mScaleBarOverlay.setScaleBarOffset(dm.widthPixels, 10);
        mapView.getOverlays().add(mScaleBarOverlay);
        FirebaseAccount account = FirebaseAccount.getAccount();
        HashSet<POIPoint> discoveredPeaks = account.getDiscoveredPeaks();
        setMarkersForDiscoveredPeaks(discoveredPeaks,this);
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (int i = 0; i < grantResults.length; i++) {
            permissionsToRequest.add(permissions[i]);
        }
        if (permissionsToRequest.size() > 0) {
            ActivityCompat.requestPermissions(
                    this,
                    permissionsToRequest.toArray(new String[0]),
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    private void requestPermissionsIfNecessary(String[] permissions) {
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted
                permissionsToRequest.add(permission);
            }
        }
        if (permissionsToRequest.size() > 0) {
            ActivityCompat.requestPermissions(
                    this,
                    permissionsToRequest.toArray(new String[0]),
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }


    /**
     * Scan the list of discovered POIs and add a marker on the map for each of them
     * @param discoveredPeaks list of discovered peaks
     * @param context app context
     */
    private void setMarkersForDiscoveredPeaks(HashSet<POIPoint> discoveredPeaks,Context context){
        //iterate over all POI
        for(POIPoint poi : discoveredPeaks){
            GeoPoint startPoint = new GeoPoint(poi.getLatitude(), poi.getLongitude());
            Marker startMarker = new Marker(mapView);

            Drawable dr = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_mountain_marker_resize, null);
            startMarker.setIcon(dr);
            startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            startMarker.setPosition(startPoint);
            startMarker.setTitle(poi.getName() + " : \n\t" + poi.getAltitude() + "m");
            mapView.getOverlays().add(startMarker);
        }
        mapView.invalidate();
    }

}

