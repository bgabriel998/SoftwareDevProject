package com.github.bgabriel998.softwaredevproject;

import android.content.res.Configuration;
import android.hardware.camera2.CameraAccessException;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.view.PreviewView;
import androidx.core.util.Pair;

import java.util.Locale;

public class Button1Activity extends AppCompatActivity {

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

        //Hide status-bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_button1);

        //Camera-view
        PreviewView previewView = findViewById(R.id.view_finder);

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
            if(orientation==Configuration.ORIENTATION_PORTRAIT){
                compassView.setRange(cameraFieldOfView.first, cameraFieldOfView.second);
            }
            else if(orientation==Configuration.ORIENTATION_LANDSCAPE){
                compassView.setRange(cameraFieldOfView.second, cameraFieldOfView.first);
            }
        }

        //Create new compass
        compass = new Compass(this);

        //Create compass listener
        CompassListener compassListener = getCompassListener();

        //Bind the compassListener with the compass
        compass.setListener(compassListener);

        ComputePOIPoints computePOIPoints = new ComputePOIPoints(this);
        compassView.setPOIs(ComputePOIPoints.POIPoints, computePOIPoints.userPoint);
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
}