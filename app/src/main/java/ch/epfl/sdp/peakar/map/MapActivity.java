package ch.epfl.sdp.peakar.map;

import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import ch.epfl.sdp.peakar.R;
import ch.epfl.sdp.peakar.user.services.Authentication;

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


        //Display markers on the map
        osmMap.setMarkersForDiscoveredPeaks(Authentication.getInstance().getAuthAccount() != null);

        osmMap.displayUserLocation();

        zoomOnUserLocationButton = (ImageButton) findViewById(R.id.zoomOnUserLocation);
        zoomOnUserLocationButton.setOnClickListener(v -> osmMap.zoomOnUserLocation());

        changeMapTileSourceButton = (ImageButton) findViewById(R.id.changeMapTile);
        changeMapTileSourceButton.setOnClickListener(v -> osmMap.changeMapTileSource(zoomOnUserLocationButton,changeMapTileSourceButton ));

    }


}

