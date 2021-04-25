package com.github.bgabriel998.softwaredevproject.general;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.github.bgabriel998.softwaredevproject.R;
import com.github.bgabriel998.softwaredevproject.utils.ToolbarHandler;

public class SettingsActivity extends AppCompatActivity {

    private static final String  TOOLBAR_TITLE = "Settings";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ToolbarHandler.SetupToolbar(this, TOOLBAR_TITLE);
    }
}