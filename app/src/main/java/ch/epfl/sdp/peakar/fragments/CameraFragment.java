package ch.epfl.sdp.peakar.fragments;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.hardware.display.DisplayManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ch.epfl.sdp.peakar.R;
import ch.epfl.sdp.peakar.utils.CameraUtilities;
import ch.epfl.sdp.peakar.utils.StorageHandler;

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.camera_preview, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ConstraintLayout container = (ConstraintLayout) view;
        previewView = container.findViewById(R.id.cameraPreview);

        //Initialize background executor
        cameraExecutor = Executors.newSingleThreadExecutor();

        //Register display listener
        displayManager.registerDisplayListener(displayListener, null);

        //Configure context
        context = getContext();

        //Wait for the view to be properly laid out
        previewView.post(() -> {
            previewDisplayId = previewView.getDisplay().getDisplayId();

            setUpCamera();
        });
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
                    imageCapture.setTargetRotation(getView().getDisplay().getRotation());
                    imageAnalysis.setTargetRotation(getView().getDisplay().getRotation());
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
