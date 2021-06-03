package ch.epfl.sdp.peakar.utils;

import androidx.test.espresso.matcher.ViewMatchers;

import java.util.HashMap;

import ch.epfl.sdp.peakar.R;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;

/**
 * Class used to simplify testing all menu bars buttons.
 */
public class MenuBarTestHelperFragments {

    /**
     * Map for pairing the icon with class to send intent
     */
    private static final HashMap<Integer, Integer> iconClassMap = new HashMap<>();
    static {
        iconClassMap.put(R.id.menu_bar_settings, -1);
        iconClassMap.put(R.id.menu_bar_gallery, R.id.galleryFragmentLayout);
        iconClassMap.put(R.id.menu_bar_camera, R.id.cameraFragmentLayout);
        iconClassMap.put(R.id.menu_bar_map, R.id.mapFragmentLayout);
        iconClassMap.put(R.id.menu_bar_social, R.id.socialFragmentLayout);
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
     * Test if a button is clickable and that its pointer is invisible
     * @param iconId id for icon to test
     * @return the id of the fragment layout, -1 when settings fragment
     */
    public static int TestClickableIconButton(int iconId) {
        onView(ViewMatchers.withId(iconPointerMap.get(iconId))).check(
                matches(withEffectiveVisibility(ViewMatchers.Visibility.INVISIBLE)));
        onView(ViewMatchers.withId(iconId)).perform(click());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return iconClassMap.get(iconId);

    }

    /**
     * Test that icons pointer is invisible
     * @param iconId id for icon to test
     */
    public static void TestSelectedIconButton(int iconId){
        onView(ViewMatchers.withId(iconPointerMap.get(iconId))).check(
                matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
    }
}
