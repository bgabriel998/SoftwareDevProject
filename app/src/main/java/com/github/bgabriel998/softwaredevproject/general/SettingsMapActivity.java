package com.github.bgabriel998.softwaredevproject.general;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;

import com.github.bgabriel998.softwaredevproject.R;
import com.github.bgabriel998.softwaredevproject.map.OSMMap;
import com.github.bgabriel998.softwaredevproject.points.DownloadTopographyTask;
import com.github.bgabriel998.softwaredevproject.points.GeonamesHandler;
import com.github.bgabriel998.softwaredevproject.points.Point;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.atomic.AtomicInteger;

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
        // TODO add other features of the app after merge

        osmMap.enablePinOnClick(() -> this.okButton.setVisibility(View.VISIBLE), (p) -> this.selectedPoint = p);

    }

    // TODO fix this part
    // TODO refactor
    // TODO handle disconnected from server
    @SuppressLint("StaticFieldLeak")
    public void saveToJson() {

        AtomicInteger lock = new AtomicInteger(2);
        JSONObject json = new JSONObject();

        try {
            json.put("BoundingBox", selectedPoint.computeBoundingBox(GeonamesHandler.DEFAULT_RANGE_IN_KM));
            lock.getAndDecrement();
        } catch (JSONException e) {
            e.printStackTrace();
            lock.getAndDecrement();
        }

        new DownloadTopographyTask(){
            @Override
            public void onResponseReceived(Pair<int[][], Double> topography) {
                super.onResponseReceived(topography);
                Log.d("Debug", "entrance");
                /*
                try {
                    synchronized (lock) {
                        json.put("ELevationMap", topography);
                        Log.d("Debug", String.valueOf(lock.getAndDecrement()));
                        lock.notify();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("Debug", String.valueOf(lock.getAndDecrement()));
                    lock.notify();
                }
                 */
            }
        }.execute(selectedPoint);

        try {

            // TODO display loading bar

            synchronized (lock) {
                while (lock.get() > 0) {
                    Log.d("Debug", "Waiting: " + lock.get());
                    lock.wait();
                }
            }

            // TODO remove loading bar

            String jsonString = json.toString();

            //FileOutputStream fos = this.openFileOutput("Save", Context.MODE_PRIVATE);
            //fos.write(jsonString.getBytes());
            //fos.close();

            Log.d("JSON" , json.toString());

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }



}