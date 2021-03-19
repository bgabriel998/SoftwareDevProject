package com.github.bgabriel998.softwaredevproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final String[] CAMERA_PERMISSION = new String[]{Manifest.permission.CAMERA};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void button1(View view) {
        if(hasCameraPermission()){
            Intent intent = new Intent(this, Button1Activity.class);
            startActivity(intent);
        }
        else{
            requestCameraPermission();
        }
    }

    private boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(
                this,
                CAMERA_PERMISSION,
                CAMERA_REQUEST_CODE
        );
    }

    public void button2(View view) {
        Intent intent = new Intent(this, Button2Activity.class);
        startActivity(intent);
    }

    public void button3(View view) {
        Intent intent = new Intent(this, Button3Activity.class);
        startActivity(intent);
    }

    public void button4(View view) {
        Intent intent = new Intent(this, Button4Activity.class);
        startActivity(intent);
    }

    public void button5(View view) {
        Intent intent = new Intent(this, Button5Activity.class);
        startActivity(intent);
    }

    public void button6(View view) {
        Intent intent = new Intent(this, Button6Activity.class);
        startActivity(intent);
    }

    public void button7(View view) {
        Intent intent = new Intent(this, Button7Activity.class);
        startActivity(intent);
    }

    public void button8(View view) {
        Intent intent = new Intent(this, Button8Activity.class);
        startActivity(intent);
    }

    /**
     * Use onRequestPermissionsResult to open activity as soon as as camera permission was granted
     * @param requestCode Indicates the permission code
     * @param permissions List of permissions
     * @param grantResults Indicates if permission is granted or not
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_REQUEST_CODE) {
            // When request is cancelled, the results array are empty
            if((grantResults.length >0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                //Permission granted, start the activity
                Intent intent = new Intent(this, Button1Activity.class);
                startActivity(intent);
            }
        }
    }
}