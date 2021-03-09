package com.github.ravifrancesco.softwaredevproject;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.github.bgabriel998.softwaredevproject.MainActivity;
import com.github.bgabriel998.softwaredevproject.R;

public class GPSTrackerActivity extends AppCompatActivity implements View.OnClickListener {

    private GPSTracker gpsTracker;

    TextView latitudeTV;
    TextView longitudeTV;
    TextView altitudeTV;
    TextView accuracyTV;

    Button getLocationButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_button7);

        latitudeTV = (TextView)findViewById(R.id.LatitudePlainText);
        longitudeTV = (TextView)findViewById(R.id.LongitudePlainText);
        altitudeTV = (TextView)findViewById(R.id.AltitudePlainText);
        accuracyTV = (TextView)findViewById(R.id.AccuracyPlainText);

        getLocationButton = (Button)findViewById(R.id.GetLocationButton);

        getLocationButton.setOnClickListener(this);

        try {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void getLocation() {
        gpsTracker = new GPSTracker(GPSTrackerActivity.this);
        if(gpsTracker.canGetLocation()){
            double latitude = gpsTracker.getLatitude();
            double longitude = gpsTracker.getLongitude();
            double altitude = gpsTracker.getAltitude();
            double accuracy = gpsTracker.getAccuracy();
            latitudeTV.setText(String.valueOf(latitude));
            longitudeTV.setText(String.valueOf(longitude));
            altitudeTV.setText(String.valueOf(altitude));
            accuracyTV.setText(String.valueOf(accuracy));
        }else{
            gpsTracker.showSettingsAlert();
        }
    }

    @Override
    public void onClick(View v) {
        getLocation();
    }
}
