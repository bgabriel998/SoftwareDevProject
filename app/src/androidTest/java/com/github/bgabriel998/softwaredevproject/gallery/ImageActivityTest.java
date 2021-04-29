package com.github.bgabriel998.softwaredevproject.gallery;

import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;

import com.github.bgabriel998.softwaredevproject.R;
import com.github.bgabriel998.softwaredevproject.camera.CameraActivity;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static com.github.bgabriel998.softwaredevproject.UITestHelper.AddImageFile;
import static com.github.bgabriel998.softwaredevproject.UITestHelper.ClearGallery;
import static com.github.bgabriel998.softwaredevproject.UITestHelper.withImagePath;
import static com.github.bgabriel998.softwaredevproject.UITestHelper.withScaleEqualTo;
import static com.github.bgabriel998.softwaredevproject.UITestHelper.withScaleGreaterThan;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class ImageActivityTest {

    private static final String imagePath = CameraActivity.getOutputDirectory(ApplicationProvider.getApplicationContext()) +
                                                "/TestImage";

    private static final String imageDescription = "Gallery fullscreen image";
    private static final int zoomPercent = 50;
    private static final int zoomSteps = 10;


    static Intent intent;
    static {
        intent = new Intent(ApplicationProvider.getApplicationContext(), ImageActivity.class);
        intent.putExtra(ImageActivity.IMAGE_PATH_INTENT, imagePath);
    }

    /* Fill gallery with a image */
    @BeforeClass
    public static void fillGallery(){
        AddImageFile("TestImage");
    }

    /* Clear gallery */
    @AfterClass
    public static void removeGallery(){
        ClearGallery();
    }

    @Rule
    public ActivityScenarioRule<ImageActivity> testRule = new ActivityScenarioRule<>(intent);

    /* Test that the image view has the same image as the intent is. */
    @Test
    public void TestImageView(){
        onView(withId(R.id.fullscreen_image)).check(matches(withImagePath(imagePath)));
    }

    /* Test to zoom in and out and see that the zoomable image scale changes. */
    @Test
    public void TestZoomIn(){
        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        try {
            UiSelector imageSelector = new UiSelector().descriptionContains(imageDescription);
            // Check scale before test
            onView(withId(R.id.fullscreen_image)).check(matches(withScaleEqualTo(1)));

            // Test zoom in
            device.findObject(imageSelector).pinchOut(zoomPercent,zoomSteps);
            Thread.sleep(1000);
            onView(withId(R.id.fullscreen_image)).check(matches(withScaleGreaterThan(1)));

            // Test zoom out
            device.findObject(imageSelector).pinchIn(2*zoomPercent,zoomSteps);
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
