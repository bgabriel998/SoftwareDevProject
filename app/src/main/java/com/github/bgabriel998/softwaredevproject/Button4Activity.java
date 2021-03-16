package com.github.bgabriel998.softwaredevproject;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;

import com.github.ravifrancesco.softwaredevproject.UserPoint;

import org.osmdroid.bonuspack.location.POI;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;

public class Button4Activity extends AppCompatActivity {
    long start;
    long end;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_button4);

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