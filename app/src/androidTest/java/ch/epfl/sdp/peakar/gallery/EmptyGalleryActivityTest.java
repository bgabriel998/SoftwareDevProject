package ch.epfl.sdp.peakar.gallery;

import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import ch.epfl.sdp.peakar.R;
import ch.epfl.sdp.peakar.utils.MenuBarTestHelper;
import ch.epfl.sdp.peakar.utils.UITestHelper;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static ch.epfl.sdp.peakar.utils.UITestHelper.withBackgroundColor;

/**
 * Tests for a gallery activity when the gallery is empty
 */
@RunWith(AndroidJUnit4.class)
public class EmptyGalleryActivityTest {
    @Rule
    public ActivityScenarioRule<GalleryActivity> testRule = new ActivityScenarioRule<>(GalleryActivity.class);

    @BeforeClass
    public static void clearGallery(){
        UITestHelper.ClearGallery();
    }

    /* Create Intent */
    @Before
    public void setup(){
        Intents.init();
    }

    /* Release Intent */
    @After
    public void cleanUp(){
        Intents.release();
    }


    /* Test that menu bars gallery icon works as intended */
    @Test
    public void TestMenuBarGallery(){
        MenuBarTestHelper.TestSelectedIconButton(R.id.menu_bar_gallery);
    }

    /* Test that menu bars map icon works as intended */
    @Test
    public void TestMenuBarMap(){
        MenuBarTestHelper.TestClickableIconButton(R.id.menu_bar_map);
    }

    /* Test that menu bars social icon works as intended */
    @Test
    public void TestMenuBarSocial(){
        MenuBarTestHelper.TestClickableIconButton(R.id.menu_bar_social);
    }

    /* Test that the top bar color is correct */
    @Test
    public void TestTopBarColor() {
        onView(ViewMatchers.withId(R.id.top_bar)).check(matches(withBackgroundColor(R.color.LightGrey)));
    }

    /* Test that the top bar dots button is visible */
    @Test
    public void TestTopBarDotsButton() {
        onView(ViewMatchers.withId(R.id.top_bar_dots_button)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
    }

    /* Test if that the gallery empty text is visible and contains correct text, when gallery is empty */
    @Test
    public void TestGalleryEmptyViewVisible(){
        ViewInteraction galleryEmptyText = onView(withId(R.id.gallery_empty));
        galleryEmptyText.check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
        galleryEmptyText.check(matches(withText(R.string.empty_gallery)));
    }
}
