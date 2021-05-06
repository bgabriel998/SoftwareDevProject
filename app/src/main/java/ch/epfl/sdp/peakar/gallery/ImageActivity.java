package ch.epfl.sdp.peakar.gallery;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import androidx.exifinterface.media.ExifInterface;

import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ImageView;

import ch.epfl.sdp.peakar.R;
import ch.epfl.sdp.peakar.utils.ImageHandler;

import java.io.IOException;

public class ImageActivity extends AppCompatActivity {

    public static final String IMAGE_PATH_INTENT = "imagePath";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        Intent intent = getIntent();
        setImage(intent.getStringExtra(IMAGE_PATH_INTENT));

        //Hide status-bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    /**
     * Set the fullscreen image.
     * @param imagePath path to image
     */
    private void setImage(String imagePath) {
        ImageView view = findViewById(R.id.fullscreen_image);
        Bitmap imageBitmap = ImageHandler.getBitmapUpwards(imagePath);

        view.setImageBitmap(imageBitmap);
    }
}