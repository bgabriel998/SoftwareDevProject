package com.github.bgabriel998.softwaredevproject;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

public class MapActivity extends AppCompatActivity {

    private static final String  TOOLBAR_TITLE = "Map";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        ToolbarHandler.SetupToolbar(this, TOOLBAR_TITLE);
    }
}