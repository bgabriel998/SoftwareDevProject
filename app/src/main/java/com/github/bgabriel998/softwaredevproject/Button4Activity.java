package com.github.bgabriel998.softwaredevproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;

import com.github.ravifrancesco.softwaredevproject.UserPoint;
import com.google.android.gms.location.FusedLocationProviderClient;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.osmdroid.bonuspack.location.POI;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;

public class Button4Activity extends AppCompatActivity {
    long start;
    long end;

    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            Log.v("Test",""+location.getLatitude());
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_button4);


        LocationManager mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, (long) 1000,
                1.0F, (android.location.LocationListener) mLocationListener, null);

        //Location location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        //Log.v("","");

        UserPoint userLocation = new UserPoint(this);
        userLocation.update();


        GeonamesHandler handler = new GeonamesHandler(userLocation,20,4000,40) {
            @Override
            public void onResponseReceived(Object result) {
                end = System.currentTimeMillis();
                Log.v("GEONAMES","elapsed "+(end-start));
                ArrayList<POI> pois = (ArrayList<POI>) result;

                for(POI point : pois)
                    Log.v("GEONAMES","Name: "+point.mType + "alt: "+point.mLocation.getAltitude());
                Log.v("GEONAMES","size: "+pois.size());
            }
        };

        Log.v("GEONAMES","Start Get");
        start = System.currentTimeMillis();
        handler.execute();

    }

}