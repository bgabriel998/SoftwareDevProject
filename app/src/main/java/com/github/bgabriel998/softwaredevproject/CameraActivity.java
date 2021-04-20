package com.github.bgabriel998.softwaredevproject;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.hardware.camera2.CameraAccessException;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class CameraActivity extends AppCompatActivity{

    //Widgets
    private CameraPreview cameraPreview;
    private CompassView compassView;
    private TextView headingHorizontal;
    private TextView headingVertical;
    private TextView fovHorizontal;
    private TextView fovVertical;
    private Compass compass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        //Add the camera fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .add(R.id.fragment_camera, CameraPreview.newInstance(), null)
                    .commitNow();
        }

        //Hide status-bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // TextView that will tell the user what degree he's heading
        // Used for demo and debug
        headingHorizontal = findViewById(R.id.headingHorizontal);
        headingVertical = findViewById(R.id.headingVertical);
        // TextView that will tell the user what fov in degrees
        // Used for demo and debug
        fovHorizontal = findViewById(R.id.fovHorizontal);
        fovVertical = findViewById(R.id.fovVertical);

        //Create compass view
        compassView = findViewById(R.id.compass);

        //Create camera preview on the previewView
        cameraPreview = (CameraPreview) getSupportFragmentManager().findFragmentById(R.id.fragment_camera);

        //Setup the compass
        startCompass();
    }

    /**
     * startCompass creates the compass and initializes the compass listener
     */
    public void startCompass() {
        //Get the fov of the camera
        Pair<Float, Float> cameraFieldOfView = new Pair<>(0f, 0f);
        try {
            cameraFieldOfView = cameraPreview.getFieldOfView(this);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        if (cameraFieldOfView != null) {
            //Set text for demo/debug
            fovHorizontal.setText(String.format(Locale.ENGLISH, "%.1f °", cameraFieldOfView.first));
            fovVertical.setText(String.format(Locale.ENGLISH, "%.1f °", cameraFieldOfView.second));
        }

        compassView.setRange(cameraFieldOfView);

        //Create new compass
        compass = new Compass(this);

        //Create compass listener
        CompassListener compassListener = getCompassListener();

        //Bind the compassListener with the compass
        compass.setListener(compassListener);

        //Set the POIs for the compass
        compassView.setPOIs(ComputePOIPoints.POIPoints, ComputePOIPoints.POIPointsLineOfSight, ComputePOIPoints.userPoint);
    }

    /**
     * getCompassListener returns a CompassListener which updates the compass view and the textviews
     * with the actual heading
     *
     * @return CompassListener for the compass
     */
    private CompassListener getCompassListener() {
        return new CompassListener() {
            @Override
            public void onNewHeading(float heading, float headingV) {
                //Update the compass when the heading changes
                compassView.setDegrees(heading, headingV);
                //Update the textviews with the new headings
                headingHorizontal.setText(String.format(Locale.ENGLISH, "%.1f °", heading));
                headingVertical.setText(String.format(Locale.ENGLISH, "%.1f °", headingV));
            }
        };
    }

    /**
     * onPause release the sensor listener from compass when user leave the application
     * without closing it (app running in background)
     */
    @Override
    protected void onPause() {
        super.onPause();
        // releases the sensor listeners of the compass
        compass.stop();
    }

    /**
     * onResume restarts the compass listener from when user reopens the application
     * without closing it (app running in background)
     */
    @Override
    protected void onResume() {
        super.onResume();
        //starts the compass
        startCompass();
    }

    /**
     * Unbind and shutdown camera before exiting camera and stop the compass
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        compass.stop();
    }

    /**
     * Callback for the takePicture ImageButton takes two pictures, one of the camera and one with the UI
     *
     * @param view ImageButton
     * @throws IOException if the bitmap could not be stored
     */
    public void takePictureListener(View view) throws IOException {
        //Take a picture with the camera without the UI
        cameraPreview.takePicture();
        //Create a bitmap of the camera preview
        Bitmap cameraBitmap = cameraPreview.getBitmap();
        //Create a bitmap of the compass-view
        Bitmap compassBitmap = compassView.getBitmap();
        //Combine the two bitmaps
        Bitmap bitmap = overlay(cameraBitmap, compassBitmap);
        //Store the bitmap on the user device
        storeBitmap(bitmap);
    }

    /**
     * Combines two bitmaps into one
     *
     * @param base first bitmap
     * @param overlay second bitmap that is drawn on first bitmap
     * @return A bitmap that combine the two bitmaps
     */
    private Bitmap overlay(Bitmap base, Bitmap overlay) {
        Bitmap bitmap = Bitmap.createBitmap(base.getWidth(), base.getHeight(), base.getConfig());
        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(base, new Matrix(), null);
        canvas.drawBitmap(overlay, new Matrix(), null);
        return bitmap;
    }

    /**
     * Stores the bitmap on the device.
     *
     * @param bitmap Bitmap that is to be stored
     * @throws IOException thrown when bitmap could not be stores
     */
    private void storeBitmap(Bitmap bitmap) throws IOException {
        //Create the file
        String FILENAME = "yyyy-MM-dd-HH-mm-ss-SSS";
        String PHOTO_EXTENSION = ".jpg";
        File screenshotFile = createFile(this, FILENAME, PHOTO_EXTENSION);
        FileOutputStream outputStream = new FileOutputStream(screenshotFile);
        int quality = 100;
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
        outputStream.flush();
        outputStream.close();
    }

    /**
     * Creates a file which will be used to store the bitmaps
     *
     * @param format    Format of file name
     * @param extension File extension
     * @return A file with the
     */
    static File createFile(Context context, String format, String extension) {
        return new File(getOutputDirectory(context),
                new SimpleDateFormat(format, Locale.ENGLISH).format(System.currentTimeMillis()) + extension);
    }

    /**
     * Returns outpudirectory to store images. Use externel media if it is available, our app's
     * file directory otherwise
     *
     * @return outputdirectory as a File
     */
    public static File getOutputDirectory(Context context) {
        Context appContext = context.getApplicationContext();
        File mediaDir;
        File[] mediaDirs = context.getExternalMediaDirs();
        mediaDir = mediaDirs != null ? mediaDirs[0] : null;
        return (mediaDir != null && mediaDir.exists()) ? mediaDir : appContext.getFilesDir();
    }

    /**
     * Used for testing, gets the last displayed toast
     * @return Returns the last displayed toast
     */
    public String getLastToast(){
        return cameraPreview.lastToast;
    }

    /**
     * Used for testing, sets the last displayed toast
     * @param lastToast String that was displayed
     */
    public void setLastToast(String lastToast){
        cameraPreview.lastToast = lastToast;
    }

    /* Calls finish */
    public void backButton(View view) {
        finish();
    }

    /* Changes view to MapActivity */
    public void mapButton(View view) {
        Intent intent = new Intent(this, MapActivity.class);
        startActivity(intent);
    }
}