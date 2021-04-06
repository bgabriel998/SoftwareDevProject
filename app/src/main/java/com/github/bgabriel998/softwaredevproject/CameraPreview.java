package com.github.bgabriel998.softwaredevproject;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.SizeF;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * CameraPreview is used to display the camera-preview of the phone on a view.
 */
public class CameraPreview extends Fragment{

    private final PreviewView previewView;
    private ProcessCameraProvider cameraProvider;
    private ImageCapture imageCapture;
    private final ExecutorService cameraExecutor;
    private final Context context;

    private final String FILENAME = "yyyy-MM-dd-HH-mm-ss-SSS";
    private final String PHOTO_EXTENSION = ".jpg";
    private final int PICTURE_TAKEN = 1;
    private final int FAILED_TO_TAKE_PICTURE = 0;

    /**
     * Constructor for the CameraPreview
     * @param context Context of the activity on which the camera-preview is drawn
     * @param previewView View on which the camera-preview is binded to
     */
    public CameraPreview(Context context, PreviewView previewView){
        this.context = context;
        this.previewView = previewView;

        //Initialize background executor
        cameraExecutor = Executors.newSingleThreadExecutor();

        //Wait for the view to be properly laid out and then setup the camera
        this.previewView.post(this::setUpCamera);
    }

    /**
     *  Unbind and shutdown camera before exiting camera
     */
    void destroy() {
        //Unbind use-cases before exiting
        cameraProvider.unbindAll();

        // Shut down our background executor
        cameraExecutor.shutdown();
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
        int screenAspectRatio = aspectRatio(displayMetrics.widthPixels, displayMetrics.heightPixels);

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
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
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
     * Calculate the aspect ratio of the display in function of the width and height of the screen
     * @param width width of the preview in Pixels
     * @param height height of the preview in Pixels
     * @return Aspect ratio of the phone
     */
    private int aspectRatio(int width, int height){
        double previewRatio = (double)Math.max(width, height)/Math.min(width, height);
        double RATIO_16_9_VALUE = 16.0 / 9.0;
        double RATIO_4_3_VALUE = 4.0 / 3.0;
        return (Math.abs(previewRatio - RATIO_4_3_VALUE) <= Math.abs(previewRatio - RATIO_16_9_VALUE))
                ? AspectRatio.RATIO_4_3 : AspectRatio.RATIO_16_9;
    }

    /**
     * Get the horizontal and vertical field of view of the back-facing amera
     * @return null if there is no camera, else the horizontal and vertical
     * field of view in degrees
     */
    public Pair<Float, Float> getFieldOfView() throws CameraAccessException {
        //Create package manager to check if the device has a camera
        PackageManager pm = context.getPackageManager();
        if (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return calculateFOV();
        }
        return null;
    }

    /**
     * Calculates the horizontal and vertical field of view of the back-facing camera
     * @return Pair of the horizontal and vertical fov
     */
    private Pair<Float, Float> calculateFOV() throws CameraAccessException {
        //Create camera manager
        CameraManager cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        double horizontalAngle = 0;
        double verticalAngle = 0;
        //Go through every camera to get the back-facing camera
        for (final String cameraId : cameraManager.getCameraIdList()) {
            //Check if camera is back-facing
            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
            int lensFacing = characteristics.get(CameraCharacteristics.LENS_FACING);
            if (lensFacing == CameraCharacteristics.LENS_FACING_BACK) {
                //If camera is back-facing, calculate the fov
                //Initialize horiontal and vertical fov
                //Get sizes of the lenses
                float focalLength = characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)[0];
                SizeF physicalSize = characteristics.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE);
                float width = physicalSize.getWidth();
                float height = physicalSize.getHeight();
                //Calculate the fovs
                horizontalAngle = 2 * Math.atan(width / (2 * focalLength));
                verticalAngle = 2 * Math.atan(height / (2 * focalLength));

            }
        }
        return new Pair<>((float) Math.toDegrees(horizontalAngle), (float) Math.toDegrees(verticalAngle));
    }

    /**
     * Takes a picture of the camera-preview without the canvas drawn
     */
    public void takePicture(){
        //Create the file
        File photoFile = Button1Activity.createFile(context, FILENAME, PHOTO_EXTENSION);

        //Configure output options
        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(
                photoFile).build();

        //Take the picture
        imageCapture.takePicture(outputOptions, cameraExecutor, new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                //Get the Uri of the saved picture
                Uri savedUri = outputFileResults.getSavedUri() != null ? outputFileResults.getSavedUri() : Uri.fromFile(photoFile);
                handler.sendMessage(handler.obtainMessage(PICTURE_TAKEN, savedUri));
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                handler.sendMessage(handler.obtainMessage(FAILED_TO_TAKE_PICTURE, exception));
            }

            //Create a handler to display the toast inside of the onImageSaved callback
            final Handler handler = new Handler(Looper.getMainLooper()){
                @Override
                public void handleMessage(@NonNull Message msg) {
                    super.handleMessage(msg);
                    //Make the toasts depending on the Message code
                    if(msg.what == FAILED_TO_TAKE_PICTURE){
                        Toast.makeText(context, "Failed to take picture: " + msg.obj.toString(), Toast.LENGTH_LONG).show();
                    }
                    else if(msg.what == PICTURE_TAKEN){
                        Toast.makeText(context, "Picture saved at: " + msg.obj.toString(), Toast.LENGTH_LONG).show();
                    }
                }
            };
        });
    }

    /**
     * Get the camera-preview as a bitmap
     * @return a bitmap of the camera-preview
     */
    public Bitmap getBitmap(){
        return previewView.getBitmap();
    }
}
