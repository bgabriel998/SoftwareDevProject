package ch.epfl.sdp.peakar.fragments;

import android.content.pm.ActivityInfo;
import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import ch.epfl.sdp.peakar.R;

import static ch.epfl.sdp.peakar.general.MainActivity.lastFragmentIndex;
import static ch.epfl.sdp.peakar.general.MyPagerAdapter.SETTINGS_FRAGMENT_INDEX;
import static ch.epfl.sdp.peakar.utils.MenuBarHandlerFragments.updateSelectedIcon;
import static ch.epfl.sdp.peakar.utils.StatusBarHandlerFragments.StatusBarLightGrey;
import static ch.epfl.sdp.peakar.utils.TopBarHandlerFragments.setVisibilityTopBar;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
    }

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
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
        //MenuBarHandler.setup(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        setVisibilityTopBar(this, true);
    }
}