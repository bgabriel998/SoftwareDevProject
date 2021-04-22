package com.github.bgabriel998.softwaredevproject;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.github.ravifrancesco.softwaredevproject.ElevationMap;
import com.github.ravifrancesco.softwaredevproject.LineOfSight;
import com.github.ravifrancesco.softwaredevproject.POIPoint;
import com.github.ravifrancesco.softwaredevproject.Point;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;

public class SettingsMapActivity extends AppCompatActivity {

    private OSMMap osmMap = null;

    Button backButton;
    Button okButton;

    private Point selectedPoint;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_map);

        this.backButton = findViewById(R.id.settingsMapBackButton);
        this.backButton.setOnClickListener(v -> this.finish());

        this.okButton = findViewById(R.id.settingsMapOkButton);
        this.okButton.setOnClickListener(v -> {
            saveToJson();
            this.finish();
        });

        this.osmMap = new OSMMap(this, findViewById(R.id.settingsMapView));

        osmMap.displayUserLocation();
        osmMap.displayMapScaleBar();
        osmMap.displayCompassOverlay();

        osmMap.enablePinOnClick(() -> this.okButton.setVisibility(View.VISIBLE), (p) -> this.selectedPoint = p);

    }

    public void saveToJson(){

        JSONObject json = new JSONObject();

        if (selectedPoint == null) { Log.d("Debug" , "wat"); }

        ComputePOIPoints computePOIPoints = new ComputePOIPoints(selectedPoint);
        HashSet<POIPoint> poiPoints = ComputePOIPoints.POIPoints;

        try {

            json.put("LineOfSight", new LineOfSight(selectedPoint.computeBoundingBox(ElevationMap.BOUNDING_BOX_RANGE)));
            json.put("POIPoints", poiPoints);

            String jsonString = json.toString();

            FileOutputStream fos = this.openFileOutput("Save", Context.MODE_PRIVATE);
            fos.write(jsonString.getBytes());
            fos.close();

            Log.d("JSON" , json.toString());

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }



}

