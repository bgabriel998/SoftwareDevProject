package com.github.bgabriel998.softwaredevproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

public class CollectionActivity extends AppCompatActivity {

    private static final String  TOOLBAR_TITLE = "Collections";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection);
        SetupToolbar();

        Button collectedButton = findViewById(R.id.collected);
        collectedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                collectedButton(v);
            }
        });
    }

    /* Adds a finish as a listener to toolbar back button.
     Sets the toolbar title. */
    private void SetupToolbar(){
        ImageButton backToolbarButton = findViewById(R.id.goBackButton);
        backToolbarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView toolbarTitle = findViewById(R.id.toolbarTitle);
        toolbarTitle.setText(TOOLBAR_TITLE);
    }

    /* Changes view to MountainActivity */
    public void collectedButton(View view) {
        // TODO Get name and create right mountain activity
        Intent intent = new Intent(this, MountainActivity.class);
        startActivity(intent);
    }
}