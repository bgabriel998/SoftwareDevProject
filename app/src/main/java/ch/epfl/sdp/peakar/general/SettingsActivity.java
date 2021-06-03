package ch.epfl.sdp.peakar.general;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import ch.epfl.sdp.peakar.R;
import ch.epfl.sdp.peakar.database.Database;
import ch.epfl.sdp.peakar.points.ComputePOIPoints;
import ch.epfl.sdp.peakar.points.POICache;
import ch.epfl.sdp.peakar.utils.MenuBarHandler;
import ch.epfl.sdp.peakar.utils.OnSwipeTouchListener;
import ch.epfl.sdp.peakar.utils.SettingsUtilities;

import static ch.epfl.sdp.peakar.utils.SettingsUtilities.updateLanguage;
import static ch.epfl.sdp.peakar.utils.StatusBarHandler.StatusBarLightGrey;

public class SettingsActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        StatusBarLightGrey(this);
        MenuBarHandler.setup(this);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        findViewById(R.id.settings).setOnTouchListener(new OnSwipeTouchListener(this));
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key){
            case "measSys_preference":
                measurementSystemChanged();
                break;
            case "range_preference":
                rangeChanged();
                break;
            case "language_preference":
                updateLanguage(this);
                SettingsUtilities.restartActivity(this);
                break;
            case "offline_mode_preference":
                offlineModeChanged();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        PreferenceManager.getDefaultSharedPreferences(this).
                registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        PreferenceManager.getDefaultSharedPreferences(this).
                unregisterOnSharedPreferenceChangeListener(this);
    }

    /**
     * Needed static class to implement the default android preferences
     */
    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
        }
    }

    /**
     * Change measurement system
     */
    private void measurementSystemChanged(){
        //TODO : Implement -> wait new UI to see if the settings should be impl or removed
        Toast.makeText(this,"Setting not implemented yet !", Toast.LENGTH_SHORT).show();
    }

    /**
     * Change measurement system callback
     * Deletes the cache file an recompute the POI list
     */
    private void rangeChanged(){
        POICache.getInstance().deleteCacheFile(this.getCacheDir());
        //recompute the POIs using the new range
        ComputePOIPoints.getInstance(this);
    }

    /**
     * If the offline mode is off, it will prompt the user to the SettingsMapActivity, otherwise
     * it will reconnect the app to the internet.
     */
    private void offlineModeChanged(){

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        boolean offlineModeValue = prefs.getBoolean(this.getResources().getString(R.string.offline_mode_key), false);

        if (offlineModeValue) {
            Intent setIntent = new Intent(this, SettingsMapActivity.class);
            startActivity(setIntent);
            finish();
        } else {
            // Connect to the DB again
            Database.getInstance().setOnlineMode();
            ComputePOIPoints computePOIPoints = ComputePOIPoints.getInstance(this);
            computePOIPoints.update(null, null);
            Toast.makeText(this,this.getResources().getString(R.string.offline_mode_off_toast), Toast.LENGTH_SHORT).show();
        }
    }
}

