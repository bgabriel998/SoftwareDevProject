package ch.epfl.sdp.peakar.utils;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;

import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import ch.epfl.sdp.peakar.R;
import ch.epfl.sdp.peakar.fragments.CameraFragment;
import ch.epfl.sdp.peakar.user.profile.ProfileLauncherActivity;

import static ch.epfl.sdp.peakar.utils.UIUtils.setSwitchColor;
import static ch.epfl.sdp.peakar.utils.UIUtils.setText;
import static ch.epfl.sdp.peakar.utils.UIUtils.setTextStyle;
import static ch.epfl.sdp.peakar.utils.UIUtils.setTintColor;

/**
 * Only contains static functions to control the top bar that is to be
 * displayed in several activities.
 */
public class TopBarHandlerFragments {

    /**
     * Sets up the top bar in it's transparent form.
     * By coloring top bar transparent and setting all icons to a given color
     * @param fragment active fragment.
     * @param iconColorId color to set icons to.
     */
    public static void setupTransparentTopBar(Fragment fragment, int iconColorId){
        Activity activity = fragment.getActivity();
        assert activity != null;

        activity.findViewById(R.id.top_bar).setBackground(null);
        setTintColor(activity.findViewById(R.id.top_bar_profile_button), iconColorId);
        setTintColor(activity.findViewById(R.id.top_bar_dots_button), iconColorId);

        setTextStyle(activity.findViewById(R.id.top_bar_switch_text_left), iconColorId);
        setTextStyle(activity.findViewById(R.id.top_bar_switch_text_right), iconColorId);
        setSwitchColor(activity.findViewById(R.id.top_bar_switch_button), iconColorId, iconColorId);

        hideAll(activity);
        setupProfileButton(fragment);
    }

    /**
     * Sets up the top bar in it's grey form.
     * By coloring top bar grey and setting all icons to DarkGreen
     * @param fragment Active fragment.
     */
    public static void setupGreyTopBar(Fragment fragment){
        Activity activity = fragment.getActivity();
        assert activity != null;

        activity.findViewById(R.id.top_bar).setBackgroundColor(activity.getColor(R.color.LightGrey));
        setTintColor(activity.findViewById(R.id.top_bar_profile_button), R.color.DarkGreen);
        setTintColor(activity.findViewById(R.id.top_bar_dots_button), R.color.DarkGreen);

        setTextStyle(activity.findViewById(R.id.top_bar_switch_text_left), R.style.SmallText_DarkGreen);
        setTextStyle(activity.findViewById(R.id.top_bar_switch_text_right), R.style.SmallText_DarkGreen);
        setSwitchColor(activity.findViewById(R.id.top_bar_switch_button), R.color.DarkGreen, R.color.LightGrey);

        hideAll(activity);
        setupProfileButton(fragment);
    }

    /**
     * Hide all UI except profile button.
     * Used when setting up top bar to later be able to pick which icons, etc. to display
     * @param activity activity that displays bar.
     */
    private static void hideAll(Activity activity) {
        activity.findViewById(R.id.top_bar_title).setVisibility(View.INVISIBLE);
        activity.findViewById(R.id.top_bar_dots_button).setVisibility(View.INVISIBLE);
        activity.findViewById(R.id.top_bar_switch).setVisibility(View.INVISIBLE);
    }

    /**
     * Set listener to profile button for starting the profile view.
     * @param fragment active fragment
     */
    private static void setupProfileButton(Fragment fragment) {
        Activity activity = fragment.getActivity();
        assert activity != null;
        ImageButton profileButton = activity.findViewById(R.id.top_bar_profile_button);
        if(fragment instanceof CameraFragment){
            activity.findViewById(R.id.profile_background).setVisibility(View.VISIBLE);
        }
        else{
            activity.findViewById(R.id.profile_background).setVisibility(View.INVISIBLE);
        }
        profileButton.setOnClickListener(v -> {
            Intent intent = new Intent(activity, ProfileLauncherActivity.class);
            activity.startActivity(intent);
        });
    }

    /**
     * Setup the switch given two texts and a listener for switch event
     * @param activity currently using
     * @param leftText text that appears left of switch.
     * @param rightText text that appears right of switch.
     * @param listener listener to event when switch is pressed.
     */
    public static void setupSwitch(Activity activity, String leftText, String rightText,
                                   CompoundButton.OnCheckedChangeListener listener) {
        setText(activity.findViewById(R.id.top_bar_switch_text_left), leftText);
        setText(activity.findViewById(R.id.top_bar_switch_text_right), rightText);

        SwitchCompat sw = activity.findViewById(R.id.top_bar_switch_button);
        sw.setOnCheckedChangeListener(listener);
        activity.findViewById(R.id.top_bar_switch).setVisibility(View.VISIBLE);
    }

    /**
     * Setup the dots button given a listener that gets called when dots button is clicked.
     * @param activity currently using
     * @param listener to be called when dots button is clicked
     */
    public static void setupDots(Activity activity, View.OnClickListener listener){
        ImageButton dots = activity.findViewById(R.id.top_bar_dots_button);
        dots.setOnClickListener(listener);
        dots.setVisibility(View.VISIBLE);
    }

    /**
     * Sets the visibility of the top bar
     * @param fragment active fragment
     * @param visible If true visible else invisible
     */
    public static void setVisibilityTopBar(Fragment fragment, boolean visible){
        Activity activity = fragment.getActivity();
        assert activity != null;
        activity.findViewById(R.id.top_bar).setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
    }
}
