package ch.epfl.sdp.peakar.fragments;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import ch.epfl.sdp.peakar.R;

import static ch.epfl.sdp.peakar.utils.StatusBarHandlerFragments.StatusBarLightGrey;
import static ch.epfl.sdp.peakar.utils.TopBarHandlerFragments.setVisibilityTopBar;
import static ch.epfl.sdp.peakar.utils.TopBarHandlerFragments.setupGreyTopBar;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
    }

    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
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