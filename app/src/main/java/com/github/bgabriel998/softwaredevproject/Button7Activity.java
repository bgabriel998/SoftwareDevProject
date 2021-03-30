package com.github.bgabriel998.softwaredevproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.ravifrancesco.softwaredevproject.GPSTracker;
import com.github.ravifrancesco.softwaredevproject.GeoTIFFMap;
import com.github.ravifrancesco.softwaredevproject.Point;
import com.github.ravifrancesco.softwaredevproject.UserPoint;

import java.io.InputStream;

public class Button7Activity extends AppCompatActivity implements View.OnClickListener {

    TextView latitudeTV;
    TextView longitudeTV;
    TextView altitudeTV;
    TextView accuracyTV;

    TextView distanceTV;
    TextView URLTV;

    Button getLocationButton;

    Button startTrackingButton;
    Button stopTrackingButton;

    UserPoint userPoint;

    GeoTIFFMap geoTIFFMap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_button7);

        latitudeTV = findViewById(R.id.LatitudeTextView);
        longitudeTV = findViewById(R.id.LongitudeTextView);
        altitudeTV = findViewById(R.id.AltitudeTextView);
        accuracyTV = findViewById(R.id.AccuracyTextView);

        distanceTV = findViewById(R.id.DistanceTextView);

        URLTV = findViewById(R.id.URLTextView);

        getLocationButton = findViewById(R.id.GetLocationButton);

        getLocationButton.setOnClickListener(this);

        userPoint = new UserPoint(Button7Activity.this);
        geoTIFFMap = new GeoTIFFMap(userPoint);

        try {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            }
        } catch (Exception e){
            e.printStackTrace();
        }

    }

    private void getLocation(View view){
        if(userPoint.canGetLocation()){
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
            String result = geoTIFFMap.generateURL().toString();
            URLTV.setText(result);

            // show The Image in a ImageView
            new DownloadImageTask((ImageView) findViewById(R.id.image))
                    .execute(result);

        }else{
            // display some error message
        }



    }


    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }

    @Override
    public void onClick(View view) {
        getLocation(view);
    }
}