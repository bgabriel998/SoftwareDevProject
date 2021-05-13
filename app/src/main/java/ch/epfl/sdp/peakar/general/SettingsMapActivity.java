package ch.epfl.sdp.peakar.general;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;
import androidx.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.osmdroid.bonuspack.location.POI;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import ch.epfl.sdp.peakar.R;
import ch.epfl.sdp.peakar.map.OSMMap;
import ch.epfl.sdp.peakar.points.DownloadTopographyTask;
import ch.epfl.sdp.peakar.points.GeonamesHandler;
import ch.epfl.sdp.peakar.points.POIPoint;
import ch.epfl.sdp.peakar.points.Point;
import ch.epfl.sdp.peakar.utils.OfflineContentContainer;
import ch.epfl.sdp.peakar.utils.ToolbarHandler;

/**
 * Activity that allows the user to select a point around which compute and
 * download POIPoints and an elevation map for offline usage.
 * TODO modify GUI once final GUI is decided
 */
public class SettingsMapActivity extends AppCompatActivity {

    // CONSTANTS
    private static final String  TOOLBAR_TITLE = "Offline Mode";
    public static final String OFFLINE_CONTENT_FILE =  "offline_content.txt";

    private OSMMap osmMap = null;

    private Button downloadButton;
    private View loadingView;

    Activity thisActivity;
    Context thisContext;

    private Point selectedPoint;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_map);

        // Setup the toolbar
        ToolbarHandler.SetupToolbar(this, TOOLBAR_TITLE);

        downloadButton = findViewById(R.id.downloadButton);
        downloadButton.setOnClickListener(v -> saveToJson());

        loadingView = findViewById(R.id.loadingView);

        thisActivity = this;
        thisContext = this;

        osmMap = new OSMMap(this, findViewById(R.id.settingsMapView));

        // Invisible button and loading circle
        downloadButton.setVisibility(View.GONE);
        loadingView.setVisibility(View.GONE);

        osmMap.displayUserLocation();

        osmMap.enablePinOnClick(() -> downloadButton.setVisibility(View.VISIBLE), (p) -> selectedPoint = p);

    }

    /**
     * Downloads and saves the POIs and elevation map around the selectedPoint.
     */
    // TODO handle disconnected from server (discuss with others)
    public void saveToJson() {

        OfflineContentContainer saveObject = new OfflineContentContainer();

        addBoundingBoxToContainer(saveObject, selectedPoint);

        addMapAndPOIsToContainer(saveObject, selectedPoint);

    }

    /**
     * Adds the bounding box to the json
     *  @param saveObject        map that will contain the bounding box.
     * @param selectedPoint     center point of the bounding box.
     */
    private void addBoundingBoxToContainer(OfflineContentContainer saveObject, Point selectedPoint) {
        saveObject.boundingBox = selectedPoint.computeBoundingBox(GeonamesHandler.DEFAULT_RANGE_IN_KM);
    }

    /**
     * Handles downloading of the elevation map and the POI list and then adds.
     * them to the input json. The part regarding the POIs is nested inside this method.
     *  @param saveObject    map that will contain the POIpoints and the elevationmap.
     * @param selectedPoint selected point around which the offline content will be downloaded.
     */
    @SuppressLint("StaticFieldLeak")
    private void addMapAndPOIsToContainer(OfflineContentContainer saveObject, Point selectedPoint) {
        new DownloadTopographyTask(){
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
        new GeonamesHandler(selectedPoint){
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
                saveJson(saveObject);
                Toast.makeText(thisContext,thisContext.getResources().getString(R.string.offline_mode_on_toast), Toast.LENGTH_SHORT).show();
                thisActivity.finish();

            }
        }.execute();
    }

    /**
     * Saves the json file as a .txt.
     *
     * @param saveObject  json to save.
     */
    private void saveJson(OfflineContentContainer saveObject) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonString = gson.toJson(saveObject);
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(openFileOutput(OFFLINE_CONTENT_FILE, Context.MODE_PRIVATE));
            outputStreamWriter.write(jsonString);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    @Override
    public void onBackPressed() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        // Offline mode not activated, reset shared preference
        prefs.edit()
                .putBoolean(this.getResources().getString(R.string.offline_mode_key), false)
                .apply();

        this.finish();

    }

}
