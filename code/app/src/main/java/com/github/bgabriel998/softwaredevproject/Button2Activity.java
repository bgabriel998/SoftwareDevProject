package com.github.bgabriel998.softwaredevproject;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import org.osmdroid.bonuspack.location.GeoNamesPOIProvider;
import org.osmdroid.bonuspack.location.POI;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;


public class Button2Activity extends AppCompatActivity {

    private Thread thread;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_button2);

        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    GeonamesHandler handler = new GeonamesHandler("bgabrie1");
                    GeoPoint point = new GeoPoint(45.9258378624377, 6.878492964884342);
                    ArrayList<POI> pois = handler.getPOI(point);
                    ArrayList<POI> filtered = handler.filterPOI(pois);
                    Log.v("GEONAMES PEAK","Number of POI : "+pois.size());
                    Log.v("GEONAMES PEAK","Number of peaks : "+filtered.size());
                    for(POI points : filtered){

                        Log.v("GEONAMES PEAK", "Name: " + points.mType+" Alt: " +points.mLocation.getAltitude());
                    }
                }
                catch (Exception e){
                    Log.v("GEONAMES_TAG", e.toString());
                }
            }
        });

        thread.start();

    }
}


