package ch.epfl.sdp.peakar.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.hardware.camera2.CameraAccessException;
import android.hardware.display.DisplayManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.preference.PreferenceManager;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ch.epfl.sdp.peakar.R;
import ch.epfl.sdp.peakar.camera.CameraUiView;
import ch.epfl.sdp.peakar.camera.Compass;
import ch.epfl.sdp.peakar.camera.CompassListener;
import ch.epfl.sdp.peakar.points.POIPoint;
import ch.epfl.sdp.peakar.points.UserPoint;
import ch.epfl.sdp.peakar.user.score.UserScore;
import ch.epfl.sdp.peakar.user.services.Account;
import ch.epfl.sdp.peakar.user.services.AuthAccount;
import ch.epfl.sdp.peakar.user.services.AuthService;
import ch.epfl.sdp.peakar.utils.CameraUtilities;
import ch.epfl.sdp.peakar.utils.StorageHandler;

import static ch.epfl.sdp.peakar.general.MainActivity.lastFragmentIndex;
import static ch.epfl.sdp.peakar.utils.MyPagerAdapter.CAMERA_FRAGMENT_INDEX;
import static ch.epfl.sdp.peakar.utils.MenuBarHandlerFragments.updateSelectedIcon;
import static ch.epfl.sdp.peakar.utils.StatusBarHandlerFragments.StatusBarTransparentBlack;
import static ch.epfl.sdp.peakar.utils.TopBarHandlerFragments.setupTransparentTopBar;

/**
 * A {@link Fragment} subclass that represents the camera-preview.
 * Use the {@link CameraFragment#newInstance} factory method to
 * create an instance of this fragment.
 * See: https://github.com/android/camera-samples/blob/main/CameraXBasic
 */
public class CameraFragment extends Fragment{

    private PreviewView previewView;
    private ProcessCameraProvider cameraProvider;
    private ImageCapture imageCapture;
    private ExecutorService cameraExecutor;
    private Context context;

    private final int FILE_LENGTH = 27;

    private ImageAnalysis imageAnalysis;

    private int previewDisplayId = -1;

    private DisplayManager displayManager;

    private DisplayManager.DisplayListener displayListener;

    String lastToast = null;

    private boolean returnToFragment;


    private static final int FLASH_TIME_MS = 5;
    //Widgets
    private CameraUiView cameraUiView;
    private TextView headingHorizontal;
    private TextView headingVertical;
    private TextView fovHorVer;
    private TextView userLocation;
    private TextView userAltitude;
    private Compass compass;
    private View flash;
    private TextView displayModeText;

    //SharedPreferences
    private SharedPreferences sharedPref;

    private boolean showDevOptions;

    private static final String DISPLAY_ALL_POIS = "0";

    private ImageView compassMiniature;
    private TextView headingCompass;

    private ConstraintLayout container;

    /**
     * Constructor for the CameraPreview
     * Is required to be empty for the fragments
     */
    public CameraFragment() {}

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment CameraPreview.
     */
    public static CameraFragment newInstance() {
        return new CameraFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        returnToFragment = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_camera, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        container = (ConstraintLayout) view;
        previewView = container.findViewById(R.id.cameraPreviewFragment);

        //Initialize background executor
        cameraExecutor = Executors.newSingleThreadExecutor();

        initialiseListeners();

        //Configure context
        context = getContext();

        //Wait for the view to be properly laid out
        previewView.post(() -> {
            previewDisplayId = previewView.getDisplay().getDisplayId();
            setUpCamera();
            setUpUI();
        });
    }

    private void initialiseListeners() {
        //Register display listener
        displayManager.registerDisplayListener(displayListener, null);

        ImageButton takePicture = container.findViewById(R.id.takePicture);
        takePicture.setOnClickListener(this::takePictureListener);
        ImageButton changeCompass = container.findViewById(R.id.compassMiniature);
        changeCompass.setOnClickListener(this::switchDisplayCompass);
        ImageButton changeDisplayedPOIs = container.findViewById(R.id.switchDisplayPOIs);
        changeDisplayedPOIs.setOnClickListener(this::switchDisplayPOIMode);
    }

    private void setUpUI() {
        sharedPref = PreferenceManager.getDefaultSharedPreferences(requireContext());

        displayModeText = container.findViewById(R.id.textDisplayPOImode);
        displayModeText.setText(sharedPref.getString(getResources().getString(R.string.displayPOIs_key), DISPLAY_ALL_POIS));

        showDevOptions = sharedPref.getBoolean(getResources().getString(R.string.devOptions_key), false);
        displayDeveloperOptions(showDevOptions);

        cameraUiView = container.findViewById(R.id.compass);
        flash = container.findViewById(R.id.take_picture_flash);

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
            cameraFieldOfView = CameraUtilities.getFieldOfView(requireContext());
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        if (showDevOptions && cameraFieldOfView!=null) {
            //Set text for demo/debug
            fovHorVer.setText(String.format(Locale.ENGLISH, "%.1f°, %.1f°", cameraFieldOfView.first, cameraFieldOfView.second));
            UserPoint userPoint = UserPoint.getInstance(getContext());
            userLocation.setText(String.format(Locale.ENGLISH, "%.4f °, %.4f °", userPoint.getLatitude(), userPoint.getLongitude()));
            userAltitude.setText(String.format(Locale.ENGLISH, "%.1f m", userPoint.getAltitude()));
        }

        cameraUiView.setRange(cameraFieldOfView);

        //Create new compass
        compass = new Compass(requireContext());

        compassMiniature = container.findViewById(R.id.compassMiniature);
        headingCompass = container.findViewById(R.id.headingCompass);

        //Bind the compassListener with the compass
        compass.setListener(getCompassListener());
    }

    /**
     * getCompassListener returns a CompassListener which updates the compass view and the textviews
     * with the actual heading
     *
     * @return CompassListener for the compass
     */
    private CompassListener getCompassListener() {
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
     * Gets the currently logged in user account and adds the mountains to the discovered Peaks
     */
    private void addDiscoveredPOIsToDatabase(){
        List<POIPoint> discoveredPOIPoints = cameraUiView.getDiscoveredPOIPoints();
        AuthService service = AuthService.getInstance();
        AuthAccount acc = service.getAuthAccount();
        if(acc != null && !discoveredPOIPoints.isEmpty()){
            if(!acc.getUsername().equals(Account.USERNAME_BEFORE_REGISTRATION)){
                UserScore userScore = new UserScore(getContext());
                userScore.updateUserScoreAndDiscoveredPeaks((ArrayList<POIPoint>) discoveredPOIPoints);
            }
            else{
                Toast.makeText(getContext(), getResources().getString(R.string.setUsername), Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Callback for the takePicture ImageButton takes two pictures, one of the camera and one with the UI
     *
     * @param view ImageButton
     */
    public void takePictureListener(View view) {
        flash.setVisibility(View.VISIBLE);
        //Take a picture with the camera without the UI
        takePicture();
        //Create a bitmap of the camera preview
        Bitmap cameraBitmap = getBitmap();
        //Create a bitmap of the compass-view
        Bitmap compassBitmap = cameraUiView.getBitmap();
        //Combine the two bitmaps
        Bitmap bitmap = CameraUtilities.combineBitmaps(cameraBitmap, compassBitmap);
        //Store the bitmap on the user device
        try {
            StorageHandler.storeBitmap(getContext(), bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Set visibility to invisible again after FLASH_TIME_MS
        flash.postDelayed(() -> flash.setVisibility(View.GONE), FLASH_TIME_MS);
    }

    /**
     * Displays the developer options (horizontal and vertical heading and the camera fov) if
     * devOption is true.
     * @param devOption Boolean, to determine if the developer options are shown or not
     */
    private void displayDeveloperOptions(boolean devOption) {
        headingHorizontal = container.findViewById(R.id.headingHorizontal);
        headingVertical = container.findViewById(R.id.headingVertical);
        fovHorVer = container.findViewById(R.id.fovHorVer);
        userLocation = container.findViewById(R.id.userLocation);
        userAltitude = container.findViewById(R.id.userAltitude);

        headingHorizontal.setVisibility(devOption ? View.VISIBLE : View.GONE);
        headingVertical.setVisibility(devOption ? View.VISIBLE : View.GONE);
        fovHorVer.setVisibility(devOption ? View.VISIBLE : View.GONE);
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
        displayModeText.setText(String.valueOf(newMode));
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
        return lastToast;
    }

    /**
     * Used for testing, sets the last displayed toast
     * @param lastToast String that was displayed
     */
    public void setLastToast(String lastToast){
        this.lastToast = lastToast;
    }

    @Override
    public void onResume() {
        super.onResume();
        //Remove the fragment from the stack if it is already contained and then push it on the stack
        if(lastFragmentIndex.contains(CAMERA_FRAGMENT_INDEX)) {
            lastFragmentIndex.remove((Object)CAMERA_FRAGMENT_INDEX);
        }
        lastFragmentIndex.push(CAMERA_FRAGMENT_INDEX);
        initFragment();
        if(returnToFragment){
            //setUpCamera();
            startCompass();
            showDevOptions = sharedPref.getBoolean(getResources().getString(R.string.devOptions_key), false);
            displayDeveloperOptions(showDevOptions);
        }
        else
            returnToFragment = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        addDiscoveredPOIsToDatabase();
    }

    private void initFragment() {
        requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        updateSelectedIcon(this);
        StatusBarTransparentBlack(this);
        setupTransparentTopBar(this, R.color.White);
        //MenuBarHandler.setup(this);
    }

    /**
     * Create listeners after that the Fragment was attached and has a context
     * @param context context
     */
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        displayManager = (DisplayManager) requireContext().getSystemService(Context.DISPLAY_SERVICE);

        displayListener = new DisplayManager.DisplayListener() {
            @Override
            public void onDisplayAdded(int displayId) {}

            @Override
            public void onDisplayRemoved(int displayId) {}

            @Override
            public void onDisplayChanged(int displayId) {
                if(displayId == previewDisplayId){
                    imageCapture.setTargetRotation(requireView().getDisplay().getRotation());
                    imageAnalysis.setTargetRotation(requireView().getDisplay().getRotation());
                }
            }
        };
    }

    /**
     *  Unbind and shutdown camera before exiting camera
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        //Unbind use-cases before exiting
        cameraProvider.unbindAll();
        // Shut down our background executor
        cameraExecutor.shutdown();
        compass.stop();
        displayManager.unregisterDisplayListener(displayListener);
    }

    /**
     *  Setup cameraProvider and call bindPreview
     */
    private void setUpCamera(){
        //ProcessCameraProvider: Used to bind the lifecycle of cameras
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(context);

        cameraProviderFuture.addListener(() -> {
            //CameraProvider
            try {
                cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(context));
    }

    /**
     * Declare and bind preview and analysis use cases
     * @param cameraProvider used to bind the lifecycle of the camera
     */
    private void bindPreview(ProcessCameraProvider cameraProvider) {
        //Get screen metrics
        DisplayMetrics displayMetrics = new DisplayMetrics();
        previewView.getDisplay().getRealMetrics(displayMetrics);

        //Calculate aspectRatio
        int screenAspectRatio = CameraUtilities.aspectRatio(displayMetrics.widthPixels,
                displayMetrics.heightPixels);

        //Get screen rotation
        int rotation = previewView.getDisplay().getRotation();

        //CameraSelector
        CameraSelector cameraSelector = new CameraSelector.Builder()
                //Only use back facing camera
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        //preview
        Preview preview = new Preview.Builder()
                //Set aspect ratio but not resolution, resolution is optimized by CameraX
                .setTargetAspectRatio(screenAspectRatio)
                //Set initial rotation
                .setTargetRotation(rotation)
                .build();

        // ImageCapture
        imageCapture = new ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                // Set aspect ratio, let cameraX handle the resolution
                .setTargetAspectRatio(screenAspectRatio)
                // Set rotation
                .setTargetRotation(rotation)
                .build();

        //ImageAnalysis
        //Only deliver latest image to the analyzer
        imageAnalysis = new ImageAnalysis.Builder()
                .setTargetAspectRatio(screenAspectRatio)
                .setTargetRotation(rotation)
                //Only deliver latest image to the analyzer
                //.setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();
        //Assign analyzer to the instance
        imageAnalysis.setAnalyzer(cameraExecutor, ImageProxy::close);

        //Unbind use-cases before rebinding
        cameraProvider.unbindAll();

        //Bind use cases to camera
        cameraProvider.bindToLifecycle((LifecycleOwner) context, cameraSelector, preview, imageCapture, imageAnalysis);

        //Attach the viewfinder's surface provider to preview
        preview.setSurfaceProvider(previewView.getSurfaceProvider());
    }

    /**
     * Redraws the camera preview when configuration gets changed
     * @param newConfig new configuration
     */
    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //Redraw the cameraUI
        setUpCamera();
    }

    /**
     * Takes a picture of the camera-preview without the canvas drawn
     */
    public void takePicture(){
        //Create the file
        File photoFile = StorageHandler.createPhotoFile(context);

        //Configure output options
        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(
                photoFile).build();

        //Take the picture
        imageCapture.takePicture(outputOptions, cameraExecutor, onImageSavedCallback(photoFile));
    }

    /**
     * Creates the onImageSavedCallback for when a picture is saved
     * @param photoFile File that is saved
     * @return ImageCapture.onImageSavedCallback
     */
    private ImageCapture.OnImageSavedCallback onImageSavedCallback(File photoFile){
        return new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                //Get the Uri of the saved picture
                Uri savedUri = outputFileResults.getSavedUri() != null ? outputFileResults.getSavedUri() : Uri.fromFile(photoFile);
                lastToast = getResources().getString(R.string.pictureSavedSuccessfully) + " " +
                        savedUri.toString().substring(0, savedUri.toString().length() - FILE_LENGTH);
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                lastToast = getResources().getString(R.string.pictureSavedFailed) + " " + exception;
            }
        };
    }

    /**
     * Get the camera-preview as a bitmap
     * @return a bitmap of the camera-preview
     */
    public Bitmap getBitmap(){
        return previewView.getBitmap();
    }
}
