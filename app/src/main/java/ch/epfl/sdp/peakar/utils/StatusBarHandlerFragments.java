package ch.epfl.sdp.peakar.utils;

import android.app.Activity;
import android.view.View;
import android.view.Window;

import androidx.fragment.app.Fragment;

import ch.epfl.sdp.peakar.R;

/**
 * Handler to set Status bar to different colors.
 */
public class StatusBarHandlerFragments {

    /**
     * Set status bar to transparent with dark icons.
     * @param fragment given fragment.
     */
    public static void StatusBarTransparent(Fragment fragment){
        Activity activity = fragment.getActivity();
        assert activity != null;
        Window window = activity.getWindow();
        window.getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        window.setStatusBarColor(activity.getColor(R.color.Transparent));
    }

    /**
     * Set status bar to half transparent with light icons.
     * @param fragment given fragment.
     */
    public static void StatusBarTransparentBlack(Fragment fragment){
        Activity activity = fragment.getActivity();
        assert activity != null;
        Window window = activity.getWindow();
        window.getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        window.setStatusBarColor(activity.getColor(R.color.Black_Transparent));
    }

    /**
     * Set status bar to light grey with dark icons.
     * @param fragment given fragment.
     */
    public static void StatusBarLightGrey(Fragment fragment){
        Activity activity = fragment.getActivity();
        assert activity != null;
        Window window = activity.getWindow();
        window.getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        window.setStatusBarColor(activity.getColor(R.color.LightGrey));
    }
}
