package com.github.bgabriel998.softwaredevproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.github.ravifrancesco.softwaredevproject.GPSTracker;
import com.github.ravifrancesco.softwaredevproject.Point;
import com.github.ravifrancesco.softwaredevproject.UserPoint;

public class Button7Activity extends AppCompatActivity implements View.OnClickListener {

    TextView latitudeTV;
    TextView longitudeTV;
    TextView altitudeTV;
    TextView accuracyTV;

    TextView distanceTV;

    Button getLocationButton;

    Button startTrackingButton;
    Button stopTrackingButton;

    UserPoint userPoint;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_button7);

        latitudeTV = findViewById(R.id.LatitudeTextView);
        longitudeTV = findViewById(R.id.LongitudeTextView);
        altitudeTV = findViewById(R.id.AltitudeTextView);
        accuracyTV = findViewById(R.id.AccuracyTextView);

        distanceTV = findViewById(R.id.DistanceTextView);

        getLocationButton = findViewById(R.id.GetLocationButton);

        startTrackingButton = findViewById(R.id.StartTrackingButton);
        stopTrackingButton = findViewById(R.id.StopTrackingButton);

        getLocationButton.setOnClickListener(this);

        startTrackingButton.setOnClickListener(this);
        stopTrackingButton.setOnClickListener(this);

        userPoint = new UserPoint(Button7Activity.this);

        try {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            }
        } catch (Exception e){
            e.printStackTrace();
        }

    }

    private void getLocation(View view){
        if(userPoint.isTracking() && userPoint.canGetLocation()){
            double latitude = userPoint.getLatitude();
            double longitude = userPoint.getLongitude();
            double altitude = userPoint.getAltitude();
            int accuracy = (int) userPoint.getAccuracy();
            double distance = userPoint.computeDistance(new Point(46.5197, 6.6323,495.0));
            latitudeTV.setText(latitude + "°");
            longitudeTV.setText(longitude + "°");
            altitudeTV.setText(String.format("%.1f m", altitude));
            accuracyTV.setText(accuracy + " m");
            distanceTV.setText(String.format("%.1f km", distance/1000));
        }else{
            // display some error message
        }
    }

    private void startTracking() {
        userPoint.startTracking();
    }

    private void stopTracking() {
        userPoint.stopTracking();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.GetLocationButton:
                getLocation(view);
                break;
            case R.id.StartTrackingButton:
                startTracking();
                break;
            case R.id.StopTrackingButton:
                stopTracking();
                break;
        }
    }
}