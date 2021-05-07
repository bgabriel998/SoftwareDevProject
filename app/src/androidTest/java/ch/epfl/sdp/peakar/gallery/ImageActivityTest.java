package ch.epfl.sdp.peakar.gallery;

import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import ch.epfl.sdp.peakar.R;
import ch.epfl.sdp.peakar.UITestHelper;
import ch.epfl.sdp.peakar.utils.StorageHandler;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static ch.epfl.sdp.peakar.UITestHelper.withScaleEqualTo;
import static ch.epfl.sdp.peakar.UITestHelper.withScaleGreaterThan;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class ImageActivityTest {

    private static final String imagePath = StorageHandler.getOutputDirectoryMedia(ApplicationProvider.getApplicationContext()) +
                                                "/TestImage";

    private static final String IMAGE_DESCRIPTION = "Gallery fullscreen image";
    private static final int ZOOM_PERCENT = 100;
    private static final int ZOOM_STEPS = 10;


    static Intent intent;
    static {
        intent = new Intent(ApplicationProvider.getApplicationContext(), ImageActivity.class);
        intent.putExtra(ImageActivity.IMAGE_PATH_INTENT, imagePath);
    }

    /* Fill gallery with a image */
    @BeforeClass
    public static void fillGallery(){
        UITestHelper.AddImageFile("TestImage");
    }

    /* Clear gallery */
    @AfterClass
    public static void removeGallery(){
        UITestHelper.ClearGallery();
    }

    @Rule
    public ActivityScenarioRule<ImageActivity> testRule = new ActivityScenarioRule<>(intent);

    /* Test that the image view has the same image as the intent is. */
    @Test
    public void TestImageView(){
        onView(withId(R.id.fullscreen_image)).check(matches(UITestHelper.withImagePath(imagePath)));
    }

    /* Test to zoom in and out and see that the zoomable image scale changes. */
    @Test
    public void TestZoomIn(){
        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        try {
            UiSelector imageSelector = new UiSelector().descriptionContains(IMAGE_DESCRIPTION);
            // Check scale before test
            onView(withId(R.id.fullscreen_image)).check(matches(withScaleEqualTo(1)));

            // Test zoom in
            device.findObject(imageSelector).pinchOut(ZOOM_PERCENT, ZOOM_STEPS);
            Thread.sleep(1000);
            onView(withId(R.id.fullscreen_image)).check(matches(withScaleGreaterThan(1)));

            // Test zoom out, twice to make sure it is completely out zoomed.
            device.findObject(imageSelector).pinchIn(ZOOM_PERCENT, ZOOM_STEPS);
            device.findObject(imageSelector).pinchIn(ZOOM_PERCENT, ZOOM_STEPS);
            Thread.sleep(1000);
            onView(withId(R.id.fullscreen_image)).check(matches(withScaleEqualTo(1)));
        }
        catch (UiObjectNotFoundException e) {
            e.printStackTrace();
            fail("Could not find image to zoom.");
        }
        catch (InterruptedException e) {
            e.printStackTrace();
            fail("thread sleep failed in zoom test.");
        }
    }
}
