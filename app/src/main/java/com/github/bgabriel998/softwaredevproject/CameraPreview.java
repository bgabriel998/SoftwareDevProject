package com.github.bgabriel998.softwaredevproject;

import android.content.Context;
import android.util.DisplayMetrics;


import androidx.camera.core.AspectRatio;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * CameraPreview is used to display the camera-preview of the phone on a view.
 */
public class CameraPreview{

    private final PreviewView previewView;
    private ProcessCameraProvider cameraProvider;
    private final ExecutorService cameraExecutor;
    private final Context context;

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
        cameraProvider.bindToLifecycle((LifecycleOwner) context, cameraSelector, preview, imageAnalysis);

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
        return (Math.abs(previewRatio - RATIO_4_3_VALUE) <= Math.abs(previewRatio - RATIO_16_9_VALUE)) ? RATIO_4_3 : RATIO_16_9;
            return AspectRatio.RATIO_4_3;
        }
        return AspectRatio.RATIO_16_9;
    }
}
