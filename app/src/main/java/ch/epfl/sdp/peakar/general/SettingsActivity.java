package ch.epfl.sdp.peakar.general;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import java.util.Locale;

import ch.epfl.sdp.peakar.R;
import ch.epfl.sdp.peakar.utils.ToolbarHandler;

public class SettingsActivity extends AppCompatActivity {

    private static final String  TOOLBAR_TITLE = "Settings";
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
                    case "disable_caching":
                        disableCachingChanged();
                        break;
                    case "offline_mode_key":
                        offlineModeChanged();
                        break;
                    default:
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        ToolbarHandler.SetupToolbarCustom(this, this,TOOLBAR_TITLE);

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

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
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

    /**
     * Change measurement system
     */
    private void measurementSystemChanged(){
        //TODO : Implement
        Toast.makeText(this,"Setting not implemented yet !", Toast.LENGTH_SHORT).show();
    }

    /**
     * Change measurement system
     */
    private void rangeChanged(){
        //TODO : Implement
        Toast.makeText(this,"Setting not implemented yet !", Toast.LENGTH_SHORT).show();
    }

    /**
     * Change language
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
     * Change caching
     */
    private void disableCachingChanged(){
        //TODO : Implement
        Toast.makeText(this,"Setting not implemented yet !", Toast.LENGTH_SHORT).show();
    }

    /**
     * Change offline mode
     */
    private void offlineModeChanged(){
        //TODO Implement
        Toast.makeText(this,"Setting not implemented yet !", Toast.LENGTH_SHORT).show();
    }

    /** Changes view to SettingsMapActivity */
    public void offlineModeButton(View view) {
        Intent intent = new Intent(this, SettingsMapActivity.class);
        startActivity(intent);
    }
}

