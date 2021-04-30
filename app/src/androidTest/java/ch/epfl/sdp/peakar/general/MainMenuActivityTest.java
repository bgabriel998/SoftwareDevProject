package ch.epfl.sdp.peakar.general;

import android.Manifest;
import android.content.pm.PackageManager;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.intent.matcher.IntentMatchers;
import androidx.test.espresso.matcher.RootMatchers;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import ch.epfl.sdp.peakar.R;
import ch.epfl.sdp.peakar.camera.CameraActivity;
import ch.epfl.sdp.peakar.collection.CollectionActivity;
import ch.epfl.sdp.peakar.gallery.GalleryActivity;
import ch.epfl.sdp.peakar.rankings.RankingsActivity;
import ch.epfl.sdp.peakar.user.profile.ProfileActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class MainMenuActivityTest {

    @Rule
    public ActivityScenarioRule<MainMenuActivity> testRule = new ActivityScenarioRule<>(MainMenuActivity.class);

    @Rule
    public GrantPermissionRule grantCameraPermissionRule = GrantPermissionRule.grant(Manifest.permission.CAMERA);

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

    /* Test that pressing the profile button changes view to ProfileActivity */
    @Test
    public void TestProfileButton(){
        ViewInteraction button = onView(ViewMatchers.withId(R.id.profileButton));
        button.perform(click());
        // Catch intent
        intended(IntentMatchers.hasComponent(ProfileActivity.class.getName()));
    }

    /* Test that pressing the settings button changes view to SettingsActivity */
    @Test
    public void TestSettingsButton(){
        ViewInteraction button = onView(withId(R.id.settingsButton));
        button.perform(click());
        // Catch intent
        intended(IntentMatchers.hasComponent(SettingsActivity.class.getName()));
    }

    /* Test that pressing the camera button changes view to CameraActivity */
    @Test
    public void TestCameraButton(){
        ViewInteraction button = onView(withId(R.id.startCameraButton));
        button.perform(click());
        // Catch intent
        intended(IntentMatchers.hasComponent(CameraActivity.class.getName()));
    }

    /**
     * Test that the AlertDialog pops up and that the cameraPermission gets requested when the method
     * requestCameraPermission is executed.
     */
    @Test
    public void cameraPermissionRequested() throws NoSuchMethodException {
        //Call directly method that is called to request the camera permission
        Method method = MainMenuActivity.class.getDeclaredMethod("requestCameraPermission");
        method.setAccessible(true);
        ActivityScenario<MainMenuActivity> scenario = testRule.getScenario();
        scenario.onActivity(activity -> {
            try {
                method.invoke(activity);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        });
        onView(withText("Camera permission required!")).inRoot(RootMatchers.isDialog()).check(matches(isDisplayed()));
        //Button1 is postive button of alertDialog
        onView(withId(android.R.id.button1)).perform(click());
    }

    /**
     * Tests that when the camera permission gets granted and onRequestPermissionsResult is called,
     * the camera-preview opens without clicking on the button again.
     */
    @Test
    public void cameraPermissionGrantedAfterRequest() throws NoSuchMethodException {
        Method method = MainMenuActivity.class.getDeclaredMethod("onRequestPermissionsResult", int.class, String[].class, int[].class);
        method.setAccessible(true);
        ActivityScenario<MainMenuActivity> scenario = testRule.getScenario();
        scenario.onActivity(activity -> {
            try {
                int CAMERA_REQUEST_CODE = 100;
                String[] cameraPermission = new String[]{Manifest.permission.CAMERA};
                int[] grantResults = new int[]{PackageManager.PERMISSION_GRANTED};
                method.invoke(activity, CAMERA_REQUEST_CODE, cameraPermission, grantResults);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        });
        Intents.intended(IntentMatchers.hasComponent(CameraActivity.class.getName()));
    }

    /* Test that pressing the collection button changes view to CollectionActivity */
    @Test
    public void TestCollectionButton(){
        ViewInteraction button = onView(withId(R.id.collectionButton));
        button.perform(click());
        // Catch intent
        intended(IntentMatchers.hasComponent(CollectionActivity.class.getName()));
    }

    /* Test that pressing the ranking button changes view to RankingsActivity */
    @Test
    public void TestRankingsButton(){
        ViewInteraction button = onView(withId(R.id.rankingsButton));
        button.perform(click());
        // Catch intent
        intended(IntentMatchers.hasComponent(RankingsActivity.class.getName()));
    }

    /* Test that pressing the ranking button changes view to GalleryActivity */
    @Test
    public void TestGalleryButton(){
        ViewInteraction button = onView(withId(R.id.galleryButton));
        button.perform(click());
        // Catch intent
        intended(IntentMatchers.hasComponent(GalleryActivity.class.getName()));
    }
}
