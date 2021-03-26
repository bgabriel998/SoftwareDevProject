package com.github.bgabriel998.softwaredevproject;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.view.PreviewView;

import java.util.Locale;

public class Button1Activity extends AppCompatActivity {

    //Widgets
    private CameraPreview cameraPreview;
    private CompassView compassView;
    private TextView headingHorizontal;
    private TextView headingVertical;
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
        headingHorizontal.setVisibility(View.VISIBLE);
        headingVertical =  findViewById(R.id.headingVertical);
        headingVertical.setVisibility(View.VISIBLE);

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
        compass = new Compass(this);
        CompassListener compassListener = getCompassListener();
        compass.setListener(compassListener);
    }

    /**
     * getCompassListener returns a CompassListener which updates the compass view and the textviews
     * with the actual heading
     * @return
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