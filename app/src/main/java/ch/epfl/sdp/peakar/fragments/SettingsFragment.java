package ch.epfl.sdp.peakar.fragments;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import ch.epfl.sdp.peakar.R;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
    }

    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
        return fragment;
    }
}