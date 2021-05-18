package ch.epfl.sdp.peakar.general;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import java.util.Locale;

import ch.epfl.sdp.peakar.R;
import ch.epfl.sdp.peakar.points.ComputePOIPoints;
import ch.epfl.sdp.peakar.points.POICache;
import ch.epfl.sdp.peakar.utils.ToolbarHandler;

public class SettingsActivity extends AppCompatActivity {

    private SharedPreferences sharedPref;

    /**
     * Preference listener. Callback that is triggered
     * every time a preference is changed
     * A switch case differentiate which preference
     * has been modified
     */
    private final SharedPreferences.OnSharedPreferenceChangeListener listener =
            (prefs, key) -> {
                switch (key){
                    case "measSys_preference":
                        measurementSystemChanged();
                        break;
                    case "range_preference":
                        rangeChanged();
                        break;
                    case "language_preference":
                        languageChanged((String) prefs.getAll().get("language_preference"));
                        break;
                    case "offline_mode_preference":
                        offlineModeChanged();
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        ToolbarHandler.SetupToolbarCustom(this, this, getString(R.string.toolbar_settings));

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

        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPref.registerOnSharedPreferenceChangeListener(listener);

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

    @Override
    public void onDestroy() {
        super.onDestroy();
        sharedPref.unregisterOnSharedPreferenceChangeListener(listener);
    }

    @Override
    public void onPause() {
        super.onPause();
        sharedPref.unregisterOnSharedPreferenceChangeListener(listener);
    }

    @Override
    public void onResume() {
        super.onResume();
        sharedPref.registerOnSharedPreferenceChangeListener(listener);
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
     * Change language
     * @param value language code (2 letters)
     */
    private void languageChanged(String value){
        switch (value){
            case "german":
                setLocale("de");
                break;
            case "italian":
                setLocale("it");
                break;
            case "french":
                setLocale("fr");
                break;
            case "english":
                setLocale("en");
                break;
            case "swedish":
                setLocale("sv");
                break;
        }
    }

    /**
     * Change application language by changing global configuration
     * This function reloads the activity to see the change directly
     * To avoid the activity-change animation, this function also
     * overrides the transition
     * @param lang language code
     */
    public void setLocale(String lang) {
        Locale myLocale = new Locale(lang);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.setLocale(myLocale);
        //update locale configuration with new language
        res.updateConfiguration(conf, dm);
        finish();
        //Override transition and restart activity
        overridePendingTransition(0, 0);
        startActivity(getIntent());
        overridePendingTransition(0, 0);
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
        } else {
            ComputePOIPoints computePOIPoints = ComputePOIPoints.getInstance(this);
            computePOIPoints.update(null, null);
            Toast.makeText(this,this.getResources().getString(R.string.offline_mode_off_toast), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        sharedPref.unregisterOnSharedPreferenceChangeListener(listener);
    }

}

