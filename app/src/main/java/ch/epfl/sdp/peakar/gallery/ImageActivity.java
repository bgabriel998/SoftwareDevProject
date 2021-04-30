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

import java.io.IOException;

public class ImageActivity extends AppCompatActivity {

    public static final String IMAGE_PATH_INTENT = "imagePath";
    private static final int ROTATE_DEGREES = 90;

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
        Bitmap imageBitmap = getBitmapUpwards(imagePath);

        view.setImageBitmap(imageBitmap);
    }

    /**
     * Get an image bitmap from a image path, where the image is the correct rotation
     * @param imagePath path to image
     * @return bitmap correctly rotated.
     */
    public static Bitmap getBitmapUpwards(String imagePath) {
        int orientation = getOrientation(imagePath);
        Bitmap imageBitmap = BitmapFactory.decodeFile(imagePath);
        if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
            imageBitmap = rotateBitmap(imageBitmap);
        }
        return imageBitmap;
    }

    /**
     * Returns the orientation of an image in on an image path
     * @param imagePath the path to image
     * @return the orientation
     */
    private static int getOrientation(String imagePath) {
        try {
            ExifInterface exifInterface = new ExifInterface(imagePath);
            return exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
        }
        catch (IOException e) {
            e.printStackTrace();
            return ExifInterface.ORIENTATION_NORMAL;
        }
    }

    /**
     * Rotate an image 90 degrees.
     * @param imageBitmap the image to rotate
     * @return the same image rotated 90 degrees
     */
    private static Bitmap rotateBitmap(Bitmap imageBitmap) {
        Matrix matrix = new Matrix();
        matrix.postRotate(ROTATE_DEGREES);
        return Bitmap.createBitmap(imageBitmap, 0, 0,
                imageBitmap.getWidth(), imageBitmap.getHeight(), matrix, true);
    }
}