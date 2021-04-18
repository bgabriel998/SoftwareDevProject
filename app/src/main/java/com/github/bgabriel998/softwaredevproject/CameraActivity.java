package com.github.bgabriel998.softwaredevproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.github.map.MapActivity;

public class CameraActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
    }

    /* Calls finish */
    public void backButton(View view) {
        finish();
    }

    /* Changes view to MapActivity */
    public void mapButton(View view) {
        Intent intent = new Intent(this, MapActivity.class);
        startActivity(intent);
    }

    /* Changes view to SettingsActivity */
    public void cameraButton(View view) {
        // TODO TAKE PICTURE
    }
}