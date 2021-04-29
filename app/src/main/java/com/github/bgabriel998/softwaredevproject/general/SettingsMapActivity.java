package com.github.bgabriel998.softwaredevproject.general;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;

import com.github.bgabriel998.softwaredevproject.R;
import com.github.bgabriel998.softwaredevproject.map.OSMMap;
import com.github.bgabriel998.softwaredevproject.points.DownloadTopographyTask;
import com.github.bgabriel998.softwaredevproject.points.GeonamesHandler;
import com.github.bgabriel998.softwaredevproject.points.POIPoint;
import com.github.bgabriel998.softwaredevproject.points.Point;

import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.bonuspack.location.POI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Activity that allows the user to select a point around which compute and
 * download POIPoints and an elevation map for offline usage.
 */
public class SettingsMapActivity extends AppCompatActivity {

    private OSMMap osmMap = null;

    Button backButton;
    Button okButton;

    Activity thisActivity;

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
        });

        this.thisActivity = this;

        this.osmMap = new OSMMap(this, findViewById(R.id.settingsMapView));

        osmMap.displayUserLocation();
        // TODO add other features of the app after merge

        osmMap.enablePinOnClick(() -> this.okButton.setVisibility(View.VISIBLE), (p) -> this.selectedPoint = p);

    }

    /**
     * Downloads and saves the POIs and elevation map around the selectedPoint.
     */
    // TODO handle disconnected from server (discuss with others)
    @SuppressLint("StaticFieldLeak")
    public void saveToJson() {

        AtomicInteger lock = new AtomicInteger(2);
        JSONObject json = new JSONObject();

        addBoundingBoxToJson(json, selectedPoint);

        addMapAndPOIsToJson(json, selectedPoint);

    }

    /**
     * Adds the bounding box to the json
     *
     * @param json          json that will contain the bounding box.
     * @param selectedPoint center point of the bounding box.
     */
    private void addBoundingBoxToJson(JSONObject json, Point selectedPoint) {
        try {
            json.put("BoundingBox", selectedPoint.computeBoundingBox(GeonamesHandler.DEFAULT_RANGE_IN_KM));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles downloading of the elevation map and the POI list and then adds.
     * them to the input json. The part regarding the POIs is nested inside this method.
     *
     * @param json          json that will contain the bounding box.
     * @param selectedPoint selected point around which the offline content will be downloaded.
     */
    @SuppressLint("StaticFieldLeak")
    private void addMapAndPOIsToJson(JSONObject json, Point selectedPoint) {
        new DownloadTopographyTask(){
            @SuppressLint("StaticFieldLeak")
            @Override
            public void onResponseReceived(Pair<int[][], Double> topography) {
                Log.d("Debug", "entrance");
                super.onResponseReceived(topography);
                try {
                    json.put("ELevationMap", topography);
                    addPOIsToJson(json, selectedPoint);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }.execute(selectedPoint);
    }

    /**
     * Handles downloading of the the POI list and then adds
     * them to the input json. After this process is finished the activity
     * is terminated.
     *
     * @param json          json that will contain the bounding box.
     * @param selectedPoint selected point around which the offline content will be downloaded.
     */
    @SuppressLint("StaticFieldLeak")
    private void addPOIsToJson(JSONObject json, Point selectedPoint) {
        new GeonamesHandler(selectedPoint){
            @Override
            public void onResponseReceived(ArrayList<POI> result) {
                List<POIPoint> POIPoints = new ArrayList<>();
                if(result!=null){
                    for(POI poi : result){
                        POIPoint poiPoint = new POIPoint(poi);
                        poiPoint.setHorizontalBearing(selectedPoint);
                        poiPoint.setVerticalBearing(selectedPoint);
                        POIPoints.add(poiPoint);
                    }
                }

                try {
                    json.put("POIPoints", POIPoints);
                    saveJson(json);
                    thisActivity.finish();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }.execute();
    }

    /**
     * Saves the json file as a .txt.
     *
     * @param json  json to save.
     */
    private void saveJson(JSONObject json) {
        String jsonString = json.toString();
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(openFileOutput("save.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write(jsonString);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
        Log.d("JSON" , json.toString());
    }


}