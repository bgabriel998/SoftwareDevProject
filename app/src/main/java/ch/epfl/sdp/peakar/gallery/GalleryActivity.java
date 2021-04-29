package ch.epfl.sdp.peakar.gallery;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import ch.epfl.sdp.peakar.camera.CameraActivity;
import ch.epfl.sdp.peakar.R;
import ch.epfl.sdp.peakar.utils.ToolbarHandler;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
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
     */
    private void setupGallery(){
        List<String> imagePaths = getImagePaths();
        if (!imagePaths.isEmpty()) {
            findViewById(R.id.gallery_empty).setVisibility(View.GONE);
        }

        GalleryAdapter galleryAdapter = new GalleryAdapter(this, imagePaths, imagePath -> {
            Intent intent = new Intent(this, ImageActivity.class);
            intent.putExtra(ImageActivity.IMAGE_PATH_INTENT, imagePath);
            startActivity(intent);
        });

        RecyclerView recyclerView = findViewById(R.id.gallery_recyclerview);
        recyclerView.setLayoutManager(new GridLayoutManager(this, COLUMNS));
        recyclerView.setAdapter(galleryAdapter);
    }

    /**
     * Retrieves all paths the images and sends back a list of them.
     * @return list of all image paths.
     */
    private List<String> getImagePaths(){
        List<String> imagePaths = new ArrayList<>();
        File[] imgFiles = CameraActivity.getOutputDirectory(this).listFiles();
        if (imgFiles != null) {
            for (File imgFile : imgFiles) {
                imagePaths.add(imgFile.getPath());
            }
        }
        imagePaths.sort(Collections.reverseOrder());
        return imagePaths;
    }
}