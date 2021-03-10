package com.github.bgabriel998.softwaredevproject;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.github.ravifrancesco.softwaredevproject.GPSTracker;

public class Button7Activity extends AppCompatActivity implements View.OnClickListener {

    private GPSTracker gpsTracker;

    TextView latitudeTV;
    TextView longitudeTV;
    TextView altitudeTV;
    TextView accuracyTV;

    Button getLocationButton;

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_button7);

        latitudeTV = findViewById(R.id.LatitudePlainText);
        longitudeTV = findViewById(R.id.LongitudePlainText);
        altitudeTV = findViewById(R.id.AltitudePlainText);
        accuracyTV = findViewById(R.id.AccuracyPlainText);

        getLocationButton = findViewById(R.id.GetLocationButton);

        getLocationButton.setOnClickListener(this);

        try {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            }
        } catch (Exception e){
            e.printStackTrace();
        }

    }

    private void getLocation(View view){
        gpsTracker = new GPSTracker(Button7Activity.this);
        if(gpsTracker.canGetLocation()){
            double latitude = gpsTracker.getLatitude();
            double longitude = gpsTracker.getLongitude();
            double altitude = gpsTracker.getAltitude();
            double accuracy = gpsTracker.getAccuracy();
            latitudeTV.setText(latitude + "°");
            longitudeTV.setText(longitude + "°");
            altitudeTV.setText(altitude + " m");
            accuracyTV.setText(accuracy + " m");
        }else{
            gpsTracker.showSettingsAlert();
        }
    }

    @Override
    public void onClick(View view) {
        getLocation(view);
    }
}