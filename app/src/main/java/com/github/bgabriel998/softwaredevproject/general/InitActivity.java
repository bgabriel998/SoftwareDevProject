package com.github.bgabriel998.softwaredevproject.general;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.github.bgabriel998.softwaredevproject.R;
import com.github.bgabriel998.softwaredevproject.map.ComputePOIPoints;
import com.github.bgabriel998.softwaredevproject.user.account.FirebaseAccount;
import com.google.firebase.FirebaseApp;

import java.util.ArrayList;
import java.util.Arrays;

public class InitActivity extends AppCompatActivity {

    private final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init);

        FirebaseApp.initializeApp(this);

        ProgressBar progressBar = findViewById(R.id.progressBarInitActivity);
        progressBar.setVisibility(View.VISIBLE);
        initApp();

        //Request permissions
        setContentView(R.layout.activity_map);
        requestPermissionsIfNecessary(new String[] {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
                //Add here permissions
        });

        //Opening the main menu
        Intent intent = new Intent(this, MainMenuActivity.class);
        startActivity(intent);

    }


    /**
     * Init application global stuff before opening the main menu
     */
    private synchronized void initApp(){
        FirebaseAccount firebaseAccount = FirebaseAccount.getAccount();
        firebaseAccount.synchronizeUserProfile();

        //Request and compute the POIPoints
        new ComputePOIPoints(this);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        permissionsToRequest.addAll(Arrays.asList(permissions).subList(0, grantResults.length));
        if (permissionsToRequest.size() > 0) {
            ActivityCompat.requestPermissions(
                    this,
                    permissionsToRequest.toArray(new String[0]),
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    private void requestPermissionsIfNecessary(String[] permissions) {
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted
                permissionsToRequest.add(permission);
            }
        }
        if (permissionsToRequest.size() > 0) {
            ActivityCompat.requestPermissions(
                    this,
                    permissionsToRequest.toArray(new String[0]),
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }


}
