package ch.epfl.sdp.peakar.utils;

import android.app.Activity;
import android.view.View;
import android.widget.ImageView;

import ch.epfl.sdp.peakar.R;

import static ch.epfl.sdp.peakar.utils.UIUtils.setTintColor;

/**
 * Only contains static functions to control the top bar that is to be
 * displayed in several activities.
 */
public class TopBarHandler {

    /**
     * Sets up the top bar in it's grey form.
     * By coloring top bar grey and setting all icons to DarkGreen
     * @param activity activity that displays bar.
     */
    public static void setupGreyTopBar(Activity activity){
        activity.findViewById(R.id.top_bar).setBackgroundColor(activity.getColor(R.color.LightGrey));
        setTintColor(activity.findViewById(R.id.top_bar_profile_button), R.color.DarkGreen);
        setTintColor(activity.findViewById(R.id.top_bar_dots_button), R.color.DarkGreen);
        HideAll(activity);
    }

    /**
     * Hide all UI except profile button.
     * Used when setting up top bar to later be able to pick which icons, etc. to display
     * @param activity activity that displays bar.
     */
    private static void HideAll(Activity activity) {
        activity.findViewById(R.id.top_bar_title).setVisibility(View.INVISIBLE);
        activity.findViewById(R.id.top_bar_dots_button).setVisibility(View.INVISIBLE);
    }
}
