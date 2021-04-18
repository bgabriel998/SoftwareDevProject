package com.github.bgabriel998.softwaredevproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.github.giommok.softwaredevproject.FirebaseAccount;
import com.github.ravifrancesco.softwaredevproject.POIPoint;

import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.infowindow.InfoWindow;
import java.util.HashSet;
import java.util.List;

public class MapActivity extends AppCompatActivity {

    private OSMMap osmMap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        //Instantiate Map
        osmMap = new OSMMap(this, findViewById(R.id.map));

        //Get user account
        FirebaseAccount account = FirebaseAccount.getAccount();
        //Display markers on the map
        osmMap.setMarkersForDiscoveredPeaks(account);

        osmMap.displayUserLocation();
    }
}

