package com.github.bgabriel998.softwaredevproject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;

import com.github.ravifrancesco.softwaredevproject.DownloadTopographyTask;
import com.github.ravifrancesco.softwaredevproject.ElevationMap;
import com.github.ravifrancesco.softwaredevproject.LineOfSight;
import com.github.ravifrancesco.softwaredevproject.POIPoint;
import com.github.ravifrancesco.softwaredevproject.Point;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;

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
            // saveToJson(); TODO fix this part
            this.finish();
        });

        this.osmMap = new OSMMap(this, findViewById(R.id.settingsMapView));

        osmMap.displayUserLocation();
        osmMap.displayMapScaleBar();
        osmMap.displayCompassOverlay();

        osmMap.enablePinOnClick(() -> this.okButton.setVisibility(View.VISIBLE), (p) -> this.selectedPoint = p);

    }

    // TODO fix this part
    @SuppressLint("StaticFieldLeak")
    public void saveToJson(){

        JSONObject json = new JSONObject();

        if (selectedPoint == null) { Log.d("Debug" , "wat"); }

        ComputePOIPoints computePOIPoints = new ComputePOIPoints(selectedPoint);
        List<POIPoint> poiPoints = ComputePOIPoints.POIPoints;

        Log.d("Debug", String.valueOf(poiPoints.size()));

        final Pair<int[][], Double>[] elevationMap = new Pair[1];

        new DownloadTopographyTask(){
            @Override
            public void onResponseReceived(Pair<int[][], Double> topography) {
                super.onResponseReceived(topography);
                elevationMap[0] = topography;
            }
        }.execute(selectedPoint);

        try {

            if (elevationMap[0] == null) { Log.d("Debug", "blind"); }
            json.put("elevationMap", elevationMap[0]);
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

