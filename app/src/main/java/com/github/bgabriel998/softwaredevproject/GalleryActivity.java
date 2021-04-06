package com.github.bgabriel998.softwaredevproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

public class GalleryActivity extends AppCompatActivity {

    private static final String  TOOLBAR_TITLE = "Gallery";
    private static final int COLUMNS = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        ToolbarHandler.SetupToolbar(this, TOOLBAR_TITLE);

        setupGallery();
    }

    /**
     * Gets the recycler view and fills it up with all images.
     * TODO images list should be paths instead.
     */
    private void setupGallery(){
        RecyclerView recyclerView = findViewById(R.id.gallery_recyclerview);

        List<Integer> images = getImages();

        GalleryAdapter galleryAdapter = new GalleryAdapter(this, images, image -> {
            Intent intent = new Intent(this, ImageActivity.class);
            intent.putExtra("image", image);
            startActivity(intent);
        });

        recyclerView.setLayoutManager(new GridLayoutManager(this, COLUMNS));
        recyclerView.setAdapter(galleryAdapter);
    }

    /**
     * Retrieves all images and sends back a list of them.
     * TODO Get all paths and send back a List<String>
     * @return list of all images.
     */
    private List<Integer> getImages(){
        int numberOfTempImages = 20;
        List<Integer> images = new ArrayList<>();
        for (int i = 0; i < numberOfTempImages; i++) {
            images.add(R.drawable.temp_camera);
        }

        return images;
    }
}