package com.github.bgabriel998.softwaredevproject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.hardware.camera2.CameraAccessException;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.view.PreviewView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.util.Pair;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

public class Button1Activity extends AppCompatActivity {

    //Widgets
    private PreviewView previewView;
    private CameraPreview cameraPreview;
    private CompassView compassView;
    private TextView headingHorizontal;
    private TextView headingVertical;
    private TextView fovHorizontal;
    private TextView fovVertical;
    private Compass compass;
    private Context context;


    private File outputDirectory;

    private final String FILENAME = "yyyy-MM-dd-HH-mm-ss-SSS";
    private final String PHOTO_EXTENSION = ".jpg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = this;

        //Hide status-bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_button1);

        //Camera-view
        previewView = findViewById(R.id.cameraPreview);

        // TextView that will tell the user what degree he's heading
        // Used for demo and debug
        headingHorizontal =  findViewById(R.id.headingHorizontal);
        headingVertical =  findViewById(R.id.headingVertical);
        // TextView that will tell the user what fov in degrees
        // Used for demo and debug
        fovHorizontal =  findViewById(R.id.fovHorizontal);
        fovVertical =  findViewById(R.id.fovVertical);

        //Create compass view
        compassView = findViewById(R.id.compass);

        //Create camera preview on the previewView
        cameraPreview = new CameraPreview(this, previewView);

        //Setup the compass
        startCompass();

        outputDirectory = getOutputDirectory(this);
    }

    /**
     * startCompass creates the compass and initializes the compass listener
     */
    public void startCompass() {
        //Get the fov of the camera
        Pair<Float, Float> cameraFieldOfView = new Pair<>(0f, 0f);
        try {
            cameraFieldOfView = cameraPreview.getFieldOfView();
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        //Set text for demo/debug
        fovHorizontal.setText(String.format(Locale.ENGLISH,"%.1f °", cameraFieldOfView.first));
        fovVertical.setText(String.format(Locale.ENGLISH,"%.1f °", cameraFieldOfView.second));

        //Get device orientation
        int orientation = getResources().getConfiguration().orientation;

        if(cameraFieldOfView.first != null && cameraFieldOfView.second != null){
            //Set range depending on the camera fov
            //Switch horizontal and vertical fov depending on the orientation
            compassView.setRange(orientation==Configuration.ORIENTATION_LANDSCAPE ?
                    cameraFieldOfView.first : cameraFieldOfView.second);
        }

        //Create new compass
        compass = new Compass(this);

        //Create compass listener
        CompassListener compassListener = getCompassListener();

        //Bind the compassListener with the compass
        compass.setListener(compassListener);
    }

    /**
     * getCompassListener returns a CompassListener which updates the compass view and the textviews
     * with the actual heading
     * @return CompassListener for the compass
     */
    private CompassListener getCompassListener() {
        return new CompassListener() {
            @Override
            public void onNewHeading(float heading, float headingV) {
                //Update the compass when the heading changes
                compassView.setDegrees(heading, headingV);
                //Update the textviews with the new headings
                headingHorizontal.setText(String.format(Locale.ENGLISH,"%.1f °", heading));
                headingVertical.setText(String.format(Locale.ENGLISH,"%.1f °", headingV));
            }
        };
    }

    /**
     * onPause release the sensor listener from compass when user leave the application
     * without closing it (app running in background)
     * @Override
     * @return nothing
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
     * @Override
     * @return nothing
     */
    @Override
    protected void onResume() {
        super.onResume();
        //starts the compass
        startCompass();
    }

    /**
     *  Unbind and shutdown camera before exiting camera and stop the compass
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraPreview.destroy();
        compass.stop();
    }

    /**
     * Handle orientation changes
     * @param newConfig new device configuration
     */
    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        //Restart camera preview after orientation change
        cameraPreview.destroy();
        cameraPreview = new CameraPreview(this, previewView);

        ImageButton takePictureButton = findViewById(R.id.takePicture);

        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) takePictureButton.getLayoutParams();

        //Change constraints of the ImageButton depending on the orientation
        if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE){
            params.rightToRight = R.id.cameraLayout;
            params.topToTop = R.id.cameraLayout;
            params.bottomToBottom = R.id.cameraLayout;
            params.endToEnd = -1;
            params.startToStart = -1;
            params.rightMargin = 10;

        }
        else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            params.rightToRight = -1;
            params.topToTop = -1;
            params.endToEnd = R.id.cameraLayout;
            params.startToStart = R.id.cameraLayout;
            params.bottomToBottom = R.id.compass;
            params.rightMargin = 0;
        }

        //Change the constraints of the button
        takePictureButton.setLayoutParams(params);
    }

    /**
     * Callback for the takePicture ImageButton takes two pictures, one of the camera and one with the UI
     * @param view ImageButton
     * @throws IOException if the bitmap could not be stored
     */
    public void takePicture(View view) throws IOException {
        //Takes a picture without the UI
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
     * @param bitmap Bitmap that is to be stored
     * @throws IOException thrown when bitmap could not be stores
     */
    private void storeBitmap(Bitmap bitmap) throws IOException {
        //Create the file
        File screenshotFile = createFile(context, FILENAME, PHOTO_EXTENSION);
        FileOutputStream outputStream = new FileOutputStream(screenshotFile);
        int quality = 100;
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
        outputStream.flush();
        outputStream.close();
    }

    /**
     * Creates a file which will be used to store the bitmaps
     * @param format Format of file name
     * @param extension File extension
     * @return A file with the
     */
    static File createFile(Context context, String format, String extension){
        return new File(getOutputDirectory(context), new SimpleDateFormat(format, Locale.ENGLISH).format(System.currentTimeMillis()) + extension);
    }

    /**
     * Returns outpudirectory to store images. Use externel media if it is available, our app's
     * file directory otherwise
     * @return outputdirectory as a File
     */
    public static File getOutputDirectory(Context context){
        Context appContext = context.getApplicationContext();
        File mediaDir;
        File[] mediaDirs = context.getExternalMediaDirs();
        mediaDir = mediaDirs != null ? mediaDirs[0] : null;
        return (mediaDir!=null && mediaDir.exists()) ? mediaDir : appContext.getFilesDir();
    }
}