package ch.epfl.sdp.peakar.camera;

import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.camera2.CameraAccessException;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;

import java.io.IOException;
import java.util.Locale;

import ch.epfl.sdp.peakar.R;
import ch.epfl.sdp.peakar.map.MapActivity;
import ch.epfl.sdp.peakar.points.ComputePOIPoints;
import ch.epfl.sdp.peakar.utils.CameraUtilities;
import ch.epfl.sdp.peakar.utils.StorageHandler;

/**
 * CameraActivity handles the AR part of the application.
 */
public class CameraActivity extends AppCompatActivity{


    //Widgets
    private CameraPreview cameraPreview;
    private CameraUiView cameraUiView;
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
        cameraUiView = findViewById(R.id.compass);

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
            cameraFieldOfView = CameraUtilities.getFieldOfView(this);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        if (cameraFieldOfView != null) {
            //Set text for demo/debug
            fovHorizontal.setText(String.format(Locale.ENGLISH, "%.1f °", cameraFieldOfView.first));
            fovVertical.setText(String.format(Locale.ENGLISH, "%.1f °", cameraFieldOfView.second));
        }

        cameraUiView.setRange(cameraFieldOfView);

        //Create new compass
        compass = new Compass(this);

        //Create compass listener
        CompassListenerInterface compassListener = getCompassListener();

        //Bind the compassListener with the compass
        compass.setListener(compassListener);

        //Set the POIs for the compass
        //TODO check settings to select the POIPoints
        cameraUiView.setPOIs(ComputePOIPoints.POIPoints, ComputePOIPoints.highestPOIPoints);
    }

    /**
     * getCompassListener returns a CompassListener which updates the compass view and the textviews
     * with the actual heading
     *
     * @return CompassListener for the compass
     */
    private CompassListenerInterface getCompassListener() {
        return (heading, headingV) -> {
            //Update the compass when the heading changes
            cameraUiView.setDegrees(heading, headingV);
            //Update the textviews with the new headings
            headingHorizontal.setText(String.format(Locale.ENGLISH, "%.1f °", heading));
            headingVertical.setText(String.format(Locale.ENGLISH, "%.1f °", headingV));
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
        Bitmap compassBitmap = cameraUiView.getBitmap();
        //Combine the two bitmaps
        Bitmap bitmap = CameraUtilities.combineBitmaps(cameraBitmap, compassBitmap);
        //Store the bitmap on the user device
        StorageHandler.storeBitmap(this, bitmap);
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