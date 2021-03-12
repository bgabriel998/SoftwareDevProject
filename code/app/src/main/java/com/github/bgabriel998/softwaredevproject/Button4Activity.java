package com.github.bgabriel998.softwaredevproject;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.github.ravifrancesco.softwaredevproject.UserPoint;

import org.osmdroid.bonuspack.location.POI;
import org.osmdroid.util.GeoPoint;
import org.w3c.dom.Text;

import java.util.ArrayList;

public class Button4Activity extends AppCompatActivity {

    private TextView positionTextView;
    private TextView resultsTextView;
    private Button getSurroundingPeaks;
    private UserPoint userPoint;
    private double latitude = 0.0F;
    private double longitude = 0.0F;
    private double altitude = 0.0F;
    private int accuracy = 0;
    private GeonamesHandler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_button4);
        positionTextView = findViewById(R.id.positionTextView);
        resultsTextView = findViewById(R.id.querryResultTextView);
        getSurroundingPeaks = findViewById(R.id.button9);

        userPoint = new UserPoint(this);
        handler = new GeonamesHandler("bgabrie1");
        getLocation();

        getSurroundingPeaks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLocation();
                handler.startGetSurroundingPeaks(new GeoPoint(latitude,longitude),200,10);
                try {
                    ArrayList<POI> results = handler.getSurroundingPeaksResult();
                    StringBuilder sb = new StringBuilder();
                    for(POI point : results){
                        sb.append("Name: "+point.mType +" Alt: "+point.mLocation.getAltitude()+"\n");
                    }
                    resultsTextView.setText(sb.toString());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    onClick(v);
                }


            }
        });
    }

    private void getLocation(){
        latitude = 45.91070396589361;
        longitude =  6.933626866680844;
        altitude = 1035;
        accuracy = 10;
        String positionStr = "Lat: "+latitude + " Long: "+longitude
                +" Alt: "+altitude+" Accuracy: "+accuracy+" m";
        positionTextView.setText(positionStr);
    }
}