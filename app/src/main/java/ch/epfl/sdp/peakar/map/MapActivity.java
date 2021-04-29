package ch.epfl.sdp.peakar.map;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import ch.epfl.sdp.peakar.user.account.FirebaseAccount;
import ch.epfl.sdp.peakar.R;

public class MapActivity extends AppCompatActivity {

    public static OSMMap osmMap = null;

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

    }
}

