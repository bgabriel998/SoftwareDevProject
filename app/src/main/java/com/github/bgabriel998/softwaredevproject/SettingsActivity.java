package com.github.bgabriel998.softwaredevproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;

import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

public class SettingsActivity extends AppCompatActivity {

    private static final String  TOOLBAR_TITLE = "Settings";

    private Switch offlineModeSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ToolbarHandler.SetupToolbar(this, TOOLBAR_TITLE);

        offlineModeSwitch = findViewById(R.id.offline_mode_switch);
        setupOfflineModeSwitch();
    }

    public void setupOfflineModeSwitch() {
        //Set a CheckedChange Listener for Switch Button
        offlineModeSwitch.setOnCheckedChangeListener((cb, on) -> {
            if(on) {
                Intent intent = new Intent(this, SettingsMapActivity.class);
                startActivity(intent);
            } else {
                //Do something when Switch is off/unchecked
            }
        });
    }





}