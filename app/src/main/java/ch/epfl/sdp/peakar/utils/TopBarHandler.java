package ch.epfl.sdp.peakar.utils;

import android.app.Activity;
import android.view.View;
import android.widget.ImageView;

import ch.epfl.sdp.peakar.R;

import static ch.epfl.sdp.peakar.utils.UIUtils.setTintColor;

public class TopBarHandler {

    public static void setupGreyTopBar(Activity activity){
        activity.findViewById(R.id.top_bar).setBackgroundColor(activity.getColor(R.color.LightGrey));
        setTintColor(activity.findViewById(R.id.top_bar_profile_button), R.color.DarkGreen);
        setTintColor(activity.findViewById(R.id.top_bar_dots_button), R.color.DarkGreen);
        HideAll(activity);
    }

    private static void HideAll(Activity activity) {
        activity.findViewById(R.id.top_bar_title).setVisibility(View.INVISIBLE);
        activity.findViewById(R.id.top_bar_dots_button).setVisibility(View.INVISIBLE);
    }
}
