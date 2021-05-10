package ch.epfl.sdp.peakar.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Map.Entry;

import ch.epfl.sdp.peakar.R;
import ch.epfl.sdp.peakar.TestMenuBarActivity;
import ch.epfl.sdp.peakar.camera.CameraActivity;
import ch.epfl.sdp.peakar.gallery.GalleryActivity;
import ch.epfl.sdp.peakar.general.SettingsActivity;
import ch.epfl.sdp.peakar.map.MapActivity;

/**
 * Static handler for MenuBar.
 * With the function setup to be called from activities that show the menu bar.
 */
public class MenuBarHandler {

    /**
     * Map for pairing the icon with class to send intent
     */
    private static final HashMap<Integer, Class<?>> iconClassMap = new HashMap<>();
    static {
        iconClassMap.put(R.id.menu_bar_settings, SettingsActivity.class);
        iconClassMap.put(R.id.menu_bar_gallery, GalleryActivity.class);
        iconClassMap.put(R.id.menu_bar_camera, CameraActivity.class);
        iconClassMap.put(R.id.menu_bar_map, MapActivity.class);
        // TODO: iconClassMap.put(R.id.menu_bar_social, SocialActivity.class);
        iconClassMap.put(R.id.menu_bar_social, TestMenuBarActivity.class);
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
                });
            }
        }
    }

    /**
     * Given an id to an icon make it appear selected.
     * @param activity given activity.
     * @param viewId id to icon to "select".
     */
    private static void selectIcon(AppCompatActivity activity, int viewId) {
        ImageButton v = activity.findViewById(viewId);
        Drawable d = v.getDrawable();
        d.setColorFilter(
                new PorterDuffColorFilter(activity.getColor(R.color.LightGreen),
                        PorterDuff.Mode.SRC_ATOP));
        v.setImageDrawable(d);
    }
}
