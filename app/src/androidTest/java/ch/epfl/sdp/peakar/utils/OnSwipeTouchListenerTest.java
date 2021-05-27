package ch.epfl.sdp.peakar.utils;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.intent.matcher.IntentMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import ch.epfl.sdp.peakar.R;
import ch.epfl.sdp.peakar.camera.CameraActivity;
import ch.epfl.sdp.peakar.gallery.GalleryActivity;
import ch.epfl.sdp.peakar.general.SettingsActivity;
import ch.epfl.sdp.peakar.map.MapActivity;
import ch.epfl.sdp.peakar.social.SocialActivity;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static ch.epfl.sdp.peakar.utils.UITestHelper.leftToRightSwipe;
import static ch.epfl.sdp.peakar.utils.UITestHelper.rightToLeftSwipe;

public class OnSwipeTouchListenerTest {

    /* Create Intent */
    @Before
    public void setup() {
        Intents.init();
    }

    /* Release Intent */
    @After
    public void cleanUp() {
        Intents.release();
    }

    /* Tests that a swipe from right to left on the CameraActivity opens the MapActivity */
    @Test
    public void TestSwipleRightToLeftCamera() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), CameraActivity.class);
        ActivityScenario.launch(intent);
        onView(withId(R.id.cameraFragment)).perform(rightToLeftSwipe());
        intended(IntentMatchers.hasComponent(MapActivity.class.getName()));
    }

    /* Tests that a swipe from left to right on the CameraActivity opens the GalleryActivity */
    @Test
    public void TestSwipleLeftToRightCamera(){
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), CameraActivity.class);
        ActivityScenario.launch(intent);
        onView(withId(R.id.cameraFragment)).perform(leftToRightSwipe());
        intended(IntentMatchers.hasComponent(GalleryActivity.class.getName()));
    }

    /* Tests that a swipe from right to left on the GalleryActivity opens the CameraActivity */
    @Test
    public void TestSwipleRightToLeftGallery(){
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), GalleryActivity.class);
        ActivityScenario.launch(intent);
        onView(withId(R.id.gallery_recyclerview)).perform(rightToLeftSwipe());
        intended(IntentMatchers.hasComponent(CameraActivity.class.getName()));
    }

    /* Tests that a swipe from left to right on the GalleryActivity opens the SettingsActivity */
    @Test
    public void TestSwipleLeftToRightGallery(){
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), GalleryActivity.class);
        ActivityScenario.launch(intent);
        onView(withId(R.id.gallery_recyclerview)).perform(leftToRightSwipe());
        intended(IntentMatchers.hasComponent(SettingsActivity.class.getName()));
    }

    /* Tests that a swipe from left to right on the SocialActivity opens the MapActivity */
    @Test
    public void TestSwipleLeftToRightSocial(){
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), SocialActivity.class);
        ActivityScenario.launch(intent);
        onView(withId(R.id.social_list)).perform(leftToRightSwipe());
        intended(IntentMatchers.hasComponent(MapActivity.class.getName()));
    }

    //TODO Fix swipes on map

    /* Tests that a swipe from right to left on the MapActivity opens the SocialActivity */
    //@Test
    public void TestSwipleRightToLeftMap(){
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), MapActivity.class);
        ActivityScenario.launch(intent);
        onView(withId(R.id.map)).perform(rightToLeftSwipe());
        intended(IntentMatchers.hasComponent(SocialActivity.class.getName()));
    }

    /* Tests that a swipe from left to right on the MapActivity opens the CameraActivity */
    //@Test
    public void TestSwipleLeftToRightMap(){
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), MapActivity.class);
        ActivityScenario.launch(intent);
        onView(withId(R.id.map)).perform(leftToRightSwipe());
        intended(IntentMatchers.hasComponent(CameraActivity.class.getName()));
    }
}
