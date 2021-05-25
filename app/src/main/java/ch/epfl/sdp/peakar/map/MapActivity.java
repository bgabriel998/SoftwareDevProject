package ch.epfl.sdp.peakar.map;

import android.app.UiAutomation;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;

import ch.epfl.sdp.peakar.R;
import ch.epfl.sdp.peakar.user.services.AuthService;
import ch.epfl.sdp.peakar.utils.MenuBarHandler;
import ch.epfl.sdp.peakar.utils.OnSwipeTouchListener;

import static ch.epfl.sdp.peakar.utils.StatusBarHandler.StatusBarTransparentBlack;
import static ch.epfl.sdp.peakar.utils.TopBarHandler.setupTransparentTopBar;

public class MapActivity extends AppCompatActivity {

    private OSMMap osmMap = null;
    private ImageButton zoomOnUserLocationButton = null;
    private ImageButton changeMapTileSourceButton = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        StatusBarTransparentBlack(this);
        setupTransparentTopBar(this, R.color.Black);
        MenuBarHandler.setup(this);
        //TODO remove from map or adapt
        //findViewById(R.id.map).setOnTouchListener(new OnSwipeTouchListener(this));

        //Instantiate Map
        osmMap = new OSMMap(this, findViewById(R.id.map));


        //Display markers on the map
        osmMap.setMarkersForDiscoveredPeaks(AuthService.getInstance().getAuthAccount() != null);

        osmMap.displayUserLocation();

        zoomOnUserLocationButton = (ImageButton) findViewById(R.id.zoomOnUserLocation);
        zoomOnUserLocationButton.setOnClickListener(v -> osmMap.zoomOnUserLocation());

        changeMapTileSourceButton = (ImageButton) findViewById(R.id.changeMapTile);
        changeMapTileSourceButton.setOnClickListener(v -> osmMap.changeMapTileSource(zoomOnUserLocationButton,changeMapTileSourceButton ));

    }

    public OSMMap getOsmMap() {
        return osmMap;
    }
}

