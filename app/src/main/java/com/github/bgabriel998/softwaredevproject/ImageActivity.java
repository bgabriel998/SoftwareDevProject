package com.github.bgabriel998.softwaredevproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

public class ImageActivity extends AppCompatActivity {

    private static final String  TOOLBAR_TITLE = "Image";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        ToolbarHandler.SetupToolbar(this, TOOLBAR_TITLE);

        Intent intent = getIntent();
        setImage(intent.getIntExtra("image", -1));
    }

    /**
     * Set the fullscreen image.
     * TODO get path and find correct image in folder
     * @param imageID id for image in resource
     */
    private void setImage(int imageID) {
        ImageView view = findViewById(R.id.fullscreen_image);
        view.setImageResource(imageID);
    }
}