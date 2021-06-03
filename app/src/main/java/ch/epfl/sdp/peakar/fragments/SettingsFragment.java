package ch.epfl.sdp.peakar.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import ch.epfl.sdp.peakar.R;
import ch.epfl.sdp.peakar.database.Database;
import ch.epfl.sdp.peakar.general.SettingsMapActivity;
import ch.epfl.sdp.peakar.points.ComputePOIPoints;
import ch.epfl.sdp.peakar.points.POICache;

import static ch.epfl.sdp.peakar.general.MainActivity.lastFragmentIndex;
import static ch.epfl.sdp.peakar.utils.MyPagerAdapter.SETTINGS_FRAGMENT_INDEX;
import static ch.epfl.sdp.peakar.utils.MenuBarHandlerFragments.updateSelectedIcon;
import static ch.epfl.sdp.peakar.utils.SettingsUtilities.restartActivity;
import static ch.epfl.sdp.peakar.utils.SettingsUtilities.updateLanguage;
import static ch.epfl.sdp.peakar.utils.StatusBarHandlerFragments.StatusBarLightGrey;
import static ch.epfl.sdp.peakar.utils.TopBarHandlerFragments.setVisibilityTopBar;

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final int NOTIFICATION_BAR_DP = 24;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
    }

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set top padding for the settings
        final RecyclerView rv = getListView();
        int marginInPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                NOTIFICATION_BAR_DP,
                getResources().getDisplayMetrics()
        );
        rv.setPadding(0, marginInPx, 0, 0);
    }

    @Override
    public void onResume() {
        super.onResume();
        requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        if(lastFragmentIndex.contains(SETTINGS_FRAGMENT_INDEX)) {
            lastFragmentIndex.remove((Object)SETTINGS_FRAGMENT_INDEX);
        }
        lastFragmentIndex.push(SETTINGS_FRAGMENT_INDEX);
        updateSelectedIcon(this);
        setVisibilityTopBar(this, false);
        StatusBarLightGrey(this);
        PreferenceManager.getDefaultSharedPreferences(requireContext()).
                registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        setVisibilityTopBar(this, true);
        PreferenceManager.getDefaultSharedPreferences(requireContext()).
                unregisterOnSharedPreferenceChangeListener(this);
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
                updateLanguage(requireContext());
                restartActivity(requireActivity());
                break;
            case "offline_mode_preference":
                offlineModeChanged();
        }
    }

    /**
     * Change measurement system
     */
    private void measurementSystemChanged(){
        //TODO : Implement -> wait new UI to see if the settings should be impl or removed
        Toast.makeText(requireContext(),"Setting not implemented yet !", Toast.LENGTH_SHORT).show();
    }

    /**
     * Change measurement system callback
     * Deletes the cache file an recompute the POI list
     */
    private void rangeChanged(){
        POICache.getInstance().deleteCacheFile(requireContext().getCacheDir());
        //recompute the POIs using the new range
        ComputePOIPoints.getInstance(requireContext()).update(null, null);
    }

    /**
     * If the offline mode is off, it will prompt the user to the SettingsMapActivity, otherwise
     * it will reconnect the app to the internet.
     */
    private void offlineModeChanged(){

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());

        boolean offlineModeValue = prefs.getBoolean(this.getResources().getString(R.string.offline_mode_key), false);

        if (offlineModeValue) {
            Intent setIntent = new Intent(requireContext(), SettingsMapActivity.class);
            startActivity(setIntent);
        } else {
            // Connect to the DB again
            Database.getInstance().setOnlineMode();
            ComputePOIPoints.getInstance(requireContext()).update(null, null);
            Toast.makeText(requireContext(), this.getResources().getString(R.string.offline_mode_off_toast), Toast.LENGTH_SHORT).show();
        }
    }
}