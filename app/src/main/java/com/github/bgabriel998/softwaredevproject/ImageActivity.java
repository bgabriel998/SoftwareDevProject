package com.github.bgabriel998.softwaredevproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
        setImage(intent.getStringExtra("imagePath"));
    }

    /**
     * Set the fullscreen image.
     * @param imagePath path to image
     */
    private void setImage(String imagePath) {
        ImageView view = findViewById(R.id.fullscreen_image);
        Bitmap imageBitmap = BitmapFactory.decodeFile(imagePath);

        view.setImageBitmap(imageBitmap);
    }
}