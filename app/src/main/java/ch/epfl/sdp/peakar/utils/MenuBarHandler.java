package ch.epfl.sdp.peakar.utils;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ch.epfl.sdp.peakar.R;
import ch.epfl.sdp.peakar.camera.CameraActivity;
import ch.epfl.sdp.peakar.gallery.GalleryActivity;
import ch.epfl.sdp.peakar.general.SettingsActivity;
import ch.epfl.sdp.peakar.map.MapActivity;
import ch.epfl.sdp.peakar.social.SocialActivity;

import static ch.epfl.sdp.peakar.utils.UIUtils.setTintColor;

/**
 * Static handler for MenuBar.
 * With the function setup to be called from activities that show the menu bar.
 */
public class MenuBarHandler {

    /**
     * Map for pairing the icon with class to send intent
     */
    private static final ArrayList<Class<?>> iconClassesIndex = new ArrayList<>(Arrays.asList(
            SettingsActivity.class, GalleryActivity.class, CameraActivity.class,
            MapActivity.class, SocialActivity.class));

    /**
     * Map for pairing the icon with class to send intent
     */
    private static final HashMap<Integer, Class<?>> iconClassMap = new HashMap<>();
    static {
        iconClassMap.put(R.id.menu_bar_settings, SettingsActivity.class);
        iconClassMap.put(R.id.menu_bar_gallery, GalleryActivity.class);
        iconClassMap.put(R.id.menu_bar_camera, CameraActivity.class);
        iconClassMap.put(R.id.menu_bar_map, MapActivity.class);
        iconClassMap.put(R.id.menu_bar_social, SocialActivity.class);
    }

    /**
     * Map for pairing icon with pointer to icon
     */
    private static final HashMap<Integer, Integer> iconPointerMap = new HashMap<>();
    static {
        iconPointerMap.put(R.id.menu_bar_settings, R.id.menu_bar_settings_pointer);
        iconPointerMap.put(R.id.menu_bar_gallery, R.id.menu_bar_gallery_pointer);
        iconPointerMap.put(R.id.menu_bar_camera, R.id.menu_bar_camera_pointer);
        iconPointerMap.put(R.id.menu_bar_map, R.id.menu_bar_map_pointer);
        iconPointerMap.put(R.id.menu_bar_social, R.id.menu_bar_social_pointer);
    }

    /**
     * Sets up the Menu Bar, given the activity the bar is currently displaying
     * to make that icon non clickable and appear selected.
     * All other icons will send intent to start the corresponding activity.
     * @param activity current active activity.
     */
    public static void setup(AppCompatActivity activity) {
        activity.getWindow().setNavigationBarColor(activity.getColor(R.color.Black));
        for (Entry<Integer, Class<?>> pair : iconClassMap.entrySet()) {
            if (pair.getValue() == activity.getClass()) {
                selectIcon(activity, pair.getKey());
            }
            else {
                activity.findViewById(iconPointerMap.get(pair.getKey())).setVisibility(View.INVISIBLE);
                activity.findViewById(pair.getKey()).setOnClickListener(v ->  {
                    Class<?> classToStart = iconClassMap.get(v.getId());
                    Context context = v.getContext();
                    Intent setIntent = new Intent(context, classToStart);
                    context.startActivity(setIntent);
                    setAnimationTransition(activity, classToStart);
                });
            }
        }
    }

    /**
     * Set the correct transition
     * if the activity should slide left or right based on the activity to start.
     * @param activity current activity
     * @param toClass class of next activity.
     */
    private static void setAnimationTransition(AppCompatActivity activity, Class<?> toClass) {
        int fromIndex = iconClassesIndex.indexOf(activity.getClass());
        int toIndex = iconClassesIndex.indexOf(toClass);

        if (fromIndex < toIndex) {
            activity.overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
        }

        if (fromIndex > toIndex) {
            activity.overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
        }
    }

    /**
     * Given an id to an icon make it appear selected.
     * @param activity given activity.
     * @param viewId id to icon to "select".
     */
    private static void selectIcon(AppCompatActivity activity, int viewId) {
        setTintColor((ImageView)activity.findViewById(viewId), R.color.LightGreen);
    }
}
