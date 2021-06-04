package ch.epfl.sdp.peakar.gallery;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.intent.matcher.IntentMatchers;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import ch.epfl.sdp.peakar.R;
import ch.epfl.sdp.peakar.general.MainActivity;
import ch.epfl.sdp.peakar.utils.UITestHelper;
import ch.epfl.sdp.peakar.utils.StorageHandler;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static ch.epfl.sdp.peakar.utils.MainPagerAdapter.GALLERY_FRAGMENT_INDEX;
import static org.hamcrest.Matchers.allOf;

/**
 * Tests for a gallery activity with images
 */
@RunWith(AndroidJUnit4.class)
public class FullGalleryFragmentTest {
    @Rule
    public ActivityScenarioRule<MainActivity> testRule = new ActivityScenarioRule<>(MainActivity.class);

    /* Fill gallery with images */
    @BeforeClass
    public static void fillGallery(){
        for (int i = 0; i < 20; i++){
            UITestHelper.AddImageFile(String.format("TestImage%d", i));
        }
    }

    /* Clear gallery */
    @AfterClass
    public static void removeGallery(){
        UITestHelper.ClearGallery();
    }

    /* Create Intent */
    @Before
    public void setup(){
        Intents.init();
        testRule.getScenario().onActivity(activity -> activity.setCurrentPagerItem(GALLERY_FRAGMENT_INDEX));
    }

    /* Release Intent */
    @After
    public void cleanUp(){
        Intents.release();
    }

    /* Test if that the gallery empty text is visible and contains correct text, when gallery is empty */
    @Test
    public void TestGalleryEmptyGone(){
        onView(ViewMatchers.withId(R.id.gallery_empty)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
    }

    /* Test to scroll trough elements in recycler view and press last item, to see correct intent is sent */
    @Test
    public void TestPressLastItem(){
        String path = StorageHandler.getOutputDirectoryMedia(ApplicationProvider.getApplicationContext())
                + "/TestImage0";
        onView(withId(R.id.gallery_recyclerview))
                .perform(RecyclerViewActions.actionOnItemAtPosition(19,
                        click()));
        intended(allOf(IntentMatchers.hasComponent(ImageActivity.class.getName()),
                IntentMatchers.hasExtra(ImageActivity.IMAGE_PATH_INTENT, path)));
    }
}
