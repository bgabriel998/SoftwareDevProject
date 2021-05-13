package ch.epfl.sdp.peakar.camera;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.hardware.camera2.CameraAccessException;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;
import androidx.preference.PreferenceManager;

import java.io.IOException;
import java.util.Locale;

import ch.epfl.sdp.peakar.R;
import ch.epfl.sdp.peakar.points.UserPoint;
import ch.epfl.sdp.peakar.user.profile.ProfileLauncherActivity;
import ch.epfl.sdp.peakar.utils.CameraUtilities;
import ch.epfl.sdp.peakar.utils.StorageHandler;

/**
 * CameraActivity handles the AR part of the application.
 */
public class CameraActivity extends AppCompatActivity{


    private static final int FLASH_TIME_MS = 5;
    //Widgets
    private CameraPreview cameraPreview;
    private CameraUiView cameraUiView;
    private TextView headingHorizontal;
    private TextView headingVertical;
    private TextView fovHorizontal;
    private TextView fovVertical;
    private TextView userLocation;
    private TextView userAltitude;
    private Compass compass;
    private View flash;

    //SharedPreferences
    private SharedPreferences sharedPref;

    private boolean showDevOptions;

    private static final String DISPLAY_ALL_POIS = "0";

    private ImageView compassMiniature;
    private TextView headingCompass;

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

        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        showDevOptions = sharedPref.getBoolean(getResources().getString(R.string.devOptions_key), false);
        displayDeveloperOptions(showDevOptions);

        cameraUiView = findViewById(R.id.compass);
        flash = findViewById(R.id.take_picture_flash);

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

        if (showDevOptions && cameraFieldOfView!=null) {
            //Set text for demo/debug
            fovHorizontal.setText(String.format(Locale.ENGLISH, "%.1f °", cameraFieldOfView.first));
            fovVertical.setText(String.format(Locale.ENGLISH, "%.1f °", cameraFieldOfView.second));
            UserPoint userPoint = UserPoint.getInstance(this);
            userLocation.setText(String.format(Locale.ENGLISH, "%.4f °, %.4f °", userPoint.getLatitude(), userPoint.getLongitude()));
            userAltitude.setText(String.format(Locale.ENGLISH, "%.1f m", userPoint.getAltitude()));
        }

        cameraUiView.setRange(cameraFieldOfView);

        //Create new compass
        compass = new Compass(this);

        compassMiniature = findViewById(R.id.compassMiniature);
        headingCompass = findViewById(R.id.headingCompass);

        //Bind the compassListener with the compass
        compass.setListener(getCompassListener());
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
            compassMiniature.setRotation(-1*heading);
            //Update the textviews with the new headings
            int headingInt = (int)heading == 360 ? 0 : (int)heading;
            headingCompass.setText(String.format(Locale.ENGLISH, "%d°", headingInt));
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
        flash.setVisibility(View.VISIBLE);
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
        //Set visibility to invisible again after FLASH_TIME_MS
        flash.postDelayed(() -> flash.setVisibility(View.GONE), FLASH_TIME_MS);
    }

    /**
     * Displays the developer options (horizontal and vertical heading and the camera fov) if
     * devOption is true.
     * @param devOption Boolean, to determine if the developer options are shown or not
     */
    private void displayDeveloperOptions(boolean devOption) {
        headingHorizontal = findViewById(R.id.headingHorizontal);
        headingVertical = findViewById(R.id.headingVertical);
        fovHorizontal = findViewById(R.id.fovHorizontal);
        fovVertical = findViewById(R.id.fovVertical);
        userLocation = findViewById(R.id.userLocation);
        userAltitude = findViewById(R.id.userAltitude);

        headingHorizontal.setVisibility(devOption ? View.VISIBLE : View.GONE);
        headingVertical.setVisibility(devOption ? View.VISIBLE : View.GONE);
        fovHorizontal.setVisibility(devOption ? View.VISIBLE : View.GONE);
        fovVertical.setVisibility(devOption ? View.VISIBLE : View.GONE);
        userLocation.setVisibility(devOption ? View.VISIBLE : View.GONE);
        userAltitude.setVisibility(devOption ? View.VISIBLE : View.GONE);
    }

    /**
     * Callback for the switchDisplayPOIs ImageButton, iterates over the different representation modes:
     * 1. Display all POIs
     * 2. Display only POIs in line of sight
     * 3. Display only POIS out of line of sight
     *
     * @param view ImageButton
     */
    public void switchDisplayPOIMode(View view) {
        flash.setVisibility(View.VISIBLE);
        String displayPOIsKey = getResources().getString(R.string.displayPOIs_key);
        String mode = sharedPref.getString(displayPOIsKey, DISPLAY_ALL_POIS);
        int actualMode = Integer.parseInt(mode);
        int newMode = (actualMode + 1) % 3;

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(displayPOIsKey, "" + newMode);
        editor.apply();
        flash.postDelayed(() -> flash.setVisibility(View.GONE), FLASH_TIME_MS);
    }

    /**
     * Callback for the switchDisplayPOIs ImageButton, if true, then display the compass
     *
     * @param view CompassButton
     */
    public void switchDisplayCompass(View view) {
        String displayCompassString = getResources().getString(R.string.displayCompass_key);
        boolean displayCompass = sharedPref.getBoolean(displayCompassString, false);

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(displayCompassString, !displayCompass);
        editor.apply();
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

    /**
     * Callback for the profile button
     */
    public void profileButton(View view) {
        Intent intent = new Intent(this, ProfileLauncherActivity.class);
        startActivity(intent);
    }
}