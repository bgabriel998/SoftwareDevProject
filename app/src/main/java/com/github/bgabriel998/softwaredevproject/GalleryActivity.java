package com.github.bgabriel998.softwaredevproject;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class GalleryActivity extends AppCompatActivity {

    private static final String  TOOLBAR_TITLE = "Gallery";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ToolbarHandler.SetupToolbar(this, TOOLBAR_TITLE);
    }
}