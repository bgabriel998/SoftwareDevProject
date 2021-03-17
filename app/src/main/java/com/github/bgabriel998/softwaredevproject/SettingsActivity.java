package com.github.bgabriel998.softwaredevproject;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

public class SettingsActivity extends AppCompatActivity {

    private static final String  TOOLBAR_TITLE = "Settings";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        SetupToolbar();
    }

    /* Adds a finish as a listener to toolbar back button.
     Sets the toolbar title. */
    private void SetupToolbar(){
        ImageButton backToolbarButton = findViewById(R.id.toolbarBackButton);
        backToolbarButton.setOnClickListener(v -> finish());
        TextView toolbarTitle = findViewById(R.id.toolbarTitle);
        toolbarTitle.setText(TOOLBAR_TITLE);
    }
}