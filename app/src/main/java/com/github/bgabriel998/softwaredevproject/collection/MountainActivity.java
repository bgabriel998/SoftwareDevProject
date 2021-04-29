package com.github.bgabriel998.softwaredevproject.collection;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.github.bgabriel998.softwaredevproject.R;
import com.github.bgabriel998.softwaredevproject.utils.ToolbarHandler;

import java.util.Locale;

public class MountainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mountain);

        Intent intent = getIntent();
        setupInformation(intent);

        ToolbarHandler.SetupToolbar(this, intent.getStringExtra("name"));
    }

    /**
     * Setup the information on the mountain activity from data of intent.
     * @param intent given intent
     */
    private void setupInformation(Intent intent){
        // Points
        TextView pointText = findViewById(R.id.pointText);
        pointText.setText(String.format(Locale.getDefault(), " %d",
                intent.getIntExtra("points", -1)));

        // Height
        TextView heightText = findViewById(R.id.heightText);
        heightText.setText(String.format(Locale.getDefault(), " %d",
                intent.getIntExtra("height", -1)));

        // Position
        TextView positionText = findViewById(R.id.positionText);
        positionText.setText(String.format(Locale.getDefault(), " (%.2f, %.2f)",
                intent.getFloatExtra("longitude", -1),
                intent.getFloatExtra("latitude", -1)));
    }
}