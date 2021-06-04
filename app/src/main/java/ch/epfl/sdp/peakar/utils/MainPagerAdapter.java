package ch.epfl.sdp.peakar.utils;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import ch.epfl.sdp.peakar.fragments.CameraFragment;
import ch.epfl.sdp.peakar.fragments.GalleryFragment;
import ch.epfl.sdp.peakar.fragments.MapFragment;
import ch.epfl.sdp.peakar.fragments.SettingsFragment;
import ch.epfl.sdp.peakar.fragments.SocialFragment;

/**
 * Adapter that handles the displaying of the different main fragments of the application
 */
public class MainPagerAdapter extends FragmentStateAdapter {

    public static final int CAMERA_FRAGMENT_INDEX = 2;
    public static final int MAP_FRAGMENT_INDEX = 3;
    public static final int SOCIAL_FRAGMENT_INDEX = 4;
    public static final int SETTINGS_FRAGMENT_INDEX = 0;
    public static final int GALLERY_FRAGMENT_INDEX = 1;
    public static final int NUM_FRAGMENTS = 5;

    /**
     * Constructor of the MainPagerAdapter
     * @param fa FragmentActivity that contains the fragments
     */
    public MainPagerAdapter(FragmentActivity fa) {
        super(fa);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch(position){
            case MAP_FRAGMENT_INDEX:
                return MapFragment.newInstance();
            case SOCIAL_FRAGMENT_INDEX:
                return SocialFragment.newInstance();
            case SETTINGS_FRAGMENT_INDEX:
                return SettingsFragment.newInstance();
            case GALLERY_FRAGMENT_INDEX:
                return GalleryFragment.newInstance();
            case CAMERA_FRAGMENT_INDEX:
            default:
                return CameraFragment.newInstance();
        }
    }

    @Override
    public int getItemCount() {
        return NUM_FRAGMENTS;
    }
}
