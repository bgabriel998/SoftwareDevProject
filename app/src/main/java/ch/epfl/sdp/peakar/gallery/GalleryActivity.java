package ch.epfl.sdp.peakar.gallery;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ch.epfl.sdp.peakar.R;
import ch.epfl.sdp.peakar.utils.MenuBarHandler;
import ch.epfl.sdp.peakar.utils.OnSwipeTouchListener;
import ch.epfl.sdp.peakar.utils.StorageHandler;

import static ch.epfl.sdp.peakar.utils.StatusBarHandler.StatusBarLightGrey;
import static ch.epfl.sdp.peakar.utils.TopBarHandler.setupDots;
import static ch.epfl.sdp.peakar.utils.TopBarHandler.setupGreyTopBar;

public class GalleryActivity extends AppCompatActivity {

    private static final int COLUMNS = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        StatusBarLightGrey(this);
        setupGreyTopBar(this);
        MenuBarHandler.setup(this);

        setupDots(this, v -> {
            // TODO Sort images
        });

        setupGallery();
        findViewById(R.id.gallery_recyclerview).setOnTouchListener(new OnSwipeTouchListener(this));
    }

    /**
     * Gets the recycler view and fills it up with all images.
     */
    private void setupGallery(){
        List<String> imagePaths = StorageHandler.getImagePaths(this);
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

    @Override
    protected void onRestart() {
        super.onRestart();
        setupGallery();
        findViewById(R.id.gallery_recyclerview).setOnTouchListener(new OnSwipeTouchListener(this));
    }
}