package com.github.bgabriel998.softwaredevproject.map;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.github.bgabriel998.softwaredevproject.user.account.FirebaseAccount;
import com.github.bgabriel998.softwaredevproject.R;

public class MapActivity extends AppCompatActivity {

    public static OSMMap osmMap = null;
    private ImageButton zoomOnUserLocationButton = null;
    private ImageButton changeMapTileSourceButton = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);



        //Instantiate Map
        osmMap = new OSMMap(this, findViewById(R.id.map));


        //Get user account
        FirebaseAccount account = FirebaseAccount.getAccount();
        //Display markers on the map
        osmMap.setMarkersForDiscoveredPeaks(account,account.isSignedIn());

        osmMap.displayUserLocation();

        zoomOnUserLocationButton = (ImageButton) findViewById(R.id.zoomOnUserLocation);
        zoomOnUserLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                osmMap.zoomOnUserLocation();
            }
        });

        changeMapTileSourceButton = (ImageButton) findViewById(R.id.changeMapTile);
        changeMapTileSourceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                osmMap.changeMapTileSource(zoomOnUserLocationButton,changeMapTileSourceButton );
            }
        });

    }


}

