package ch.epfl.sdp.peakar.general;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;
import androidx.preference.PreferenceManager;

import org.osmdroid.bonuspack.location.POI;
import org.osmdroid.views.MapView;

import java.util.ArrayList;
import java.util.List;

import ch.epfl.sdp.peakar.R;
import ch.epfl.sdp.peakar.database.Database;
import ch.epfl.sdp.peakar.map.OSMMap;
import ch.epfl.sdp.peakar.points.ComputePOIPoints;
import ch.epfl.sdp.peakar.points.DownloadTopographyTask;
import ch.epfl.sdp.peakar.points.GeonamesHandler;
import ch.epfl.sdp.peakar.points.POIPoint;
import ch.epfl.sdp.peakar.points.Point;
import ch.epfl.sdp.peakar.utils.OfflineContentContainer;
import ch.epfl.sdp.peakar.utils.SettingsUtilities;
import ch.epfl.sdp.peakar.utils.StorageHandler;

import static ch.epfl.sdp.peakar.utils.StatusBarHandler.StatusBarTransparentBlack;

/**
 * Activity that allows the user to select a point around which compute and
 * download POIPoints and an elevation map for offline usage.
 */
public class SettingsMapActivity extends AppCompatActivity {
    
    private Button downloadButton;
    private View loadingView;
    private OSMMap osmMap;

    Activity thisActivity;
    Context thisContext;

    private Point selectedPoint;

    private boolean downloadRunning;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_map);

        StatusBarTransparentBlack(this);

        downloadRunning = false;

        downloadButton = findViewById(R.id.downloadButton);
        downloadButton.setOnClickListener(v -> saveToJson());

        loadingView = findViewById(R.id.loadingView);

        thisActivity = this;
        thisContext = this;

        MapView mapView = findViewById(R.id.settingsMapView);
        osmMap = new OSMMap(this, mapView);

        // Invisible button and loading circle
        downloadButton.setVisibility(View.INVISIBLE);
        loadingView.setVisibility(View.GONE);

        osmMap.displayUserLocation();

        osmMap.enablePinOnClick(() -> downloadButton.setVisibility(View.VISIBLE), (p) -> selectedPoint = p);

        ImageButton zoomOnUserLocationButton = findViewById(R.id.zoomOnUserLocation);
        zoomOnUserLocationButton.setOnClickListener(v -> osmMap.zoomOnUserLocation());

        ImageButton changeMapTileSourceButton = findViewById(R.id.changeMapTile);
        changeMapTileSourceButton.setOnClickListener(v -> osmMap.changeMapTileSource(zoomOnUserLocationButton,changeMapTileSourceButton ));
    }

    /**
     * Downloads and saves the POIs and elevation map around the selectedPoint.
     */
    public void saveToJson() {

        downloadRunning = true;

        // Disable Touch
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        // Display loading bar
        downloadButton.setVisibility(View.GONE);
        loadingView.bringToFront();

        loadingView.setVisibility(View.VISIBLE);

        OfflineContentContainer saveObject = new OfflineContentContainer();

        addBoundingBoxToContainer(saveObject, selectedPoint);

        addMapAndPOIsToContainer(saveObject, selectedPoint);

        Database.getInstance().setOfflineMode();
    }

    /**
     * Adds the bounding box to the json
     *  @param saveObject        map that will contain the bounding box.
     * @param selectedPoint     center point of the bounding box.
     */
    private void addBoundingBoxToContainer(OfflineContentContainer saveObject, Point selectedPoint) {
        saveObject.boundingBox = selectedPoint.computeBoundingBox(SettingsUtilities.getSelectedRange(this));
    }

    /**
     * Handles downloading of the elevation map and the POI list and then adds.
     * them to the input json. The part regarding the POIs is nested inside this method.
     *  @param saveObject    map that will contain the POIpoints and the elevationmap.
     * @param selectedPoint selected point around which the offline content will be downloaded.
     */
    @SuppressLint("StaticFieldLeak")
    private void addMapAndPOIsToContainer(OfflineContentContainer saveObject, Point selectedPoint) {
        new DownloadTopographyTask(this){
            @SuppressLint("StaticFieldLeak")
            @Override
            public void onResponseReceived(Pair<int[][], Double> topography) {
                super.onResponseReceived(topography);
                saveObject.topography = topography;
                addPOIsToContainer(saveObject, selectedPoint);
            }
        }.execute(selectedPoint);
    }

    /**
     * Handles downloading of the the POI list and then adds
     * them to the input json. After this process is finished the activity
     * is terminated.
     *  @param saveObject    json that will contain the bounding box.
     * @param selectedPoint selected point around which the offline content will be downloaded.
     */
    @SuppressLint("StaticFieldLeak")
    private void addPOIsToContainer(OfflineContentContainer saveObject, Point selectedPoint) {
        new GeonamesHandler(selectedPoint, thisContext){
            @Override
            public void onResponseReceived(ArrayList<POI> result) {
                List<POIPoint> POIPoints = new ArrayList<>();
                if(result!=null){
                    for(POI poi : result){
                        POIPoint poiPoint = new POIPoint(poi);
                        poiPoint.setHorizontalBearing(selectedPoint);
                        poiPoint.setVerticalBearing(selectedPoint);
                        POIPoints.add(poiPoint);
                    }
                }

                saveObject.POIPoints = POIPoints;
                StorageHandler.saveOfflineContentContainer(saveObject, thisContext);
                Toast.makeText(thisContext,thisContext.getResources().getString(R.string.offline_mode_on_toast), Toast.LENGTH_SHORT).show();

                ComputePOIPoints computePOIPoints = ComputePOIPoints.getInstance(thisContext);
                computePOIPoints.update(null, null);

                thisActivity.finish();

            }
        }.execute();
    }

    @Override
    public void onBackPressed() {

         if (downloadRunning) {
             Toast.makeText(this,this.getResources().getString(R.string.download_running), Toast.LENGTH_SHORT).show();
         } else {
             SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

             // Offline mode not activated, reset shared preference
             prefs.edit()
                     .putBoolean(this.getResources().getString(R.string.offline_mode_key), false)
                     .apply();

             Intent intent = new Intent(this, SettingsActivity.class);
             startActivity(intent);
             this.finish();
         }
    }
}
