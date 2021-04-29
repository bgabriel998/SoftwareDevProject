package ch.epfl.sdp.peakar.gallery;

import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;

import com.github.bgabriel998.softwaredevproject.R;
<<<<<<< HEAD:app/src/androidTest/java/ch/epfl/sdp/peakar/gallery/ImageActivityTest.java
import ch.epfl.sdp.peakar.camera.CameraActivity;
=======
import com.github.bgabriel998.softwaredevproject.camera.CameraActivity;
>>>>>>> 5586ab2e5fa6a9c8e544aa316b8536f4986d3438:app/src/androidTest/java/com/github/bgabriel998/softwaredevproject/gallery/ImageActivityTest.java

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import ch.epfl.sdp.peakar.UITestHelper;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
<<<<<<< HEAD:app/src/androidTest/java/ch/epfl/sdp/peakar/gallery/ImageActivityTest.java
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertSame;
=======
import static com.github.bgabriel998.softwaredevproject.UITestHelper.AddImageFile;
import static com.github.bgabriel998.softwaredevproject.UITestHelper.ClearGallery;
import static com.github.bgabriel998.softwaredevproject.UITestHelper.withImagePath;
import static com.github.bgabriel998.softwaredevproject.UITestHelper.withScaleEqualTo;
import static com.github.bgabriel998.softwaredevproject.UITestHelper.withScaleGreaterThan;
>>>>>>> 5586ab2e5fa6a9c8e544aa316b8536f4986d3438:app/src/androidTest/java/com/github/bgabriel998/softwaredevproject/gallery/ImageActivityTest.java
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class ImageActivityTest {

    private static final String imagePath = CameraActivity.getOutputDirectory(ApplicationProvider.getApplicationContext()) +
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
