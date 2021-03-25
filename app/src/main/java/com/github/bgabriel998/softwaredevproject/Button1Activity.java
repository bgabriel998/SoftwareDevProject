package com.github.bgabriel998.softwaredevproject;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.view.PreviewView;

public class Button1Activity extends AppCompatActivity {

    private PreviewView previewView;
    private CameraPreview cameraPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_button1);

        //Camera-view
        previewView = findViewById(R.id.view_finder);

        //Create camera preview on the previewView
        cameraPreview = new CameraPreview(this, previewView);
    }

    /**
     *  Unbind and shutdown camera before exiting camera
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();

        cameraPreview.destroy();
    }
}