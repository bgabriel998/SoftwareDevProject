package com.github.bgabriel998.softwaredevproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.github.map.MapActivity;

public class MainMenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
    }

    /** Changes view to SettingsActivity */
    public void settingsButton(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    /** Changes view to ProfileActivity */
    public void profileButton(View view) {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
    }

    /** Changes view to CameraActivity */
    public void cameraButton(View view) {
        Intent intent = new Intent(this, CameraActivity.class);
        startActivity(intent);
    }

    /** Changes view to CollectionActivity */
    public void collectionButton(View view) {
        Intent intent = new Intent(this, CollectionActivity.class);
        startActivity(intent);
    }

    /** Changes view to RankingsActivity */
    public void rankingsButton(View view) {
        Intent intent = new Intent(this, RankingsActivity.class);
        startActivity(intent);
    }

    /** Changes view to GalleryActivity */
    public void galleryButton(View view) {
        Intent intent = new Intent(this, GalleryActivity.class);
        startActivity(intent);
    }

    /** Changes view to GalleryActivity */
    public void mapButton(View view) {
        Intent intent = new Intent(this, MapActivity.class);
        startActivity(intent);
    }
}