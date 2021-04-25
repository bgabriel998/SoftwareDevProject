package com.github.bgabriel998.softwaredevproject.gallery;

import android.app.Activity;
import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.ViewInteraction;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.github.bgabriel998.softwaredevproject.R;
import com.github.bgabriel998.softwaredevproject.camera.CameraActivity;
import com.github.bgabriel998.softwaredevproject.gallery.ImageActivity;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.github.bgabriel998.softwaredevproject.UITestHelper.AddImageFile;
import static com.github.bgabriel998.softwaredevproject.UITestHelper.ClearGallery;
import static com.github.bgabriel998.softwaredevproject.UITestHelper.withImagePath;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class ImageActivityTest {

    private static final int testImage = R.drawable.temp_diablerets;
    private static final String imagePath = CameraActivity.getOutputDirectory(ApplicationProvider.getApplicationContext()) +
                                                "/TestImage";

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

    /* Test that the toolbar title is set as expected */
    @Test
    public void TestToolbarTitle(){
        String TOOLBAR_TITLE = "Image";
        ViewInteraction greetingText = Espresso.onView(withId(R.id.toolbarTitle));
        greetingText.check(matches(withText(TOOLBAR_TITLE)));
    }

    /* Test that the activity finishes when the toolbar back button is pressed. */
    @Test
    public void TestToolbarBackButton(){
        onView(withId(R.id.toolbarBackButton)).perform(click());
        try {
            Thread.sleep(1000);
            assertSame(testRule.getScenario().getResult().getResultCode(), Activity.RESULT_CANCELED);
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail("TestToolbarBackButton failed");
        }
    }

    /* Test that the image view has the same image as the intent is. */
    @Test
    public void TestImageView(){
        onView(withId(R.id.fullscreen_image)).check(matches(withImagePath(imagePath)));
    }
}
