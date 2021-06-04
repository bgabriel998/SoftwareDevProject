package ch.epfl.sdp.peakar.fragments;

import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import ch.epfl.sdp.peakar.R;
import ch.epfl.sdp.peakar.general.MainActivity;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.swipeLeft;
import static androidx.test.espresso.action.ViewActions.swipeRight;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static ch.epfl.sdp.peakar.utils.MainPagerAdapter.SETTINGS_FRAGMENT_INDEX;
import static ch.epfl.sdp.peakar.utils.TestingConstants.THREAD_SLEEP_1S;
import static junit.framework.TestCase.assertEquals;

@RunWith(AndroidJUnit4.class)
public class ViewPagerTest {

    @Rule
    public ActivityScenarioRule<MainActivity> testRule = new ActivityScenarioRule<>(MainActivity.class);

    @Before
    public void setup(){
        Intents.init();
    }

    @After
    public void cleanUp(){
        Intents.release();
    }

    /**
     * Test that CameraFragment is displayed at startup
     */
    @Test
    public void CameraFragmentTest() throws InterruptedException {
        Thread.sleep(THREAD_SLEEP_1S);
        onView(withId(R.id.cameraFragmentLayout)).check(matches(isCompletelyDisplayed()));
    }

    /**
     * Test that GalleryFragment is displayed when swiping left once
     */
    @Test
    public void GalleryFragmentTest() throws InterruptedException {
        onView(withId(R.id.viewPager)).perform(swipeRight());
        Thread.sleep(THREAD_SLEEP_1S);
        onView(ViewMatchers.withId(R.id.galleryFragmentLayout)).check(matches(isCompletelyDisplayed()));
    }

    /**
     * Test that MapFragment is displayed when swiping once right
     */
    @Test
    public void MapFragmentTest() throws InterruptedException {
        onView(withId(R.id.viewPager)).perform(swipeLeft());
        Thread.sleep(THREAD_SLEEP_1S);
        onView(ViewMatchers.withId(R.id.mapFragmentLayout)).check(matches(isCompletelyDisplayed()));
    }

    /**
     * Test that SettingsFragment is displayed when swiping twice left
     * There is no xml file for the settings so we must check using the position of the viewPager
     */
    @Test
    public void SettingsFragmentTest() throws InterruptedException {
        onView(withId(R.id.viewPager)).perform(swipeRight());
        onView(withId(R.id.viewPager)).perform(swipeRight());
        Thread.sleep(THREAD_SLEEP_1S);
        testRule.getScenario().onActivity(activity -> assertEquals(SETTINGS_FRAGMENT_INDEX, activity.getCurrentPagerItem()));
    }

    /**
     * Test that SocialFragment is displayed when swiping twice right
     */
    @Test
    public void SocialFragmentTest() throws InterruptedException {
        onView(withId(R.id.viewPager)).perform(swipeLeft());
        onView(withId(R.id.viewPager)).perform(swipeLeft());
        Thread.sleep(THREAD_SLEEP_1S);
        onView(ViewMatchers.withId(R.id.socialFragmentLayout)).check(matches(isCompletelyDisplayed()));
    }
}
