package com.github.bgabriel998.softwaredevproject;

import android.app.Activity;

import androidx.test.espresso.DataInteraction;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.intent.matcher.IntentMatchers;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.Locale;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.github.bgabriel998.softwaredevproject.UITestHelper.withDrawable;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertSame;

public class GalleryActivityTest {

    @Rule
    public ActivityScenarioRule<GalleryActivity> testRule = new ActivityScenarioRule<>(GalleryActivity.class);

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

    /* Test that the toolbar title is set as expected */
    @Test
    public void TestToolbarTitle(){
        String TOOLBAR_TITLE = "Gallery";
        ViewInteraction greetingText = Espresso.onView(withId(R.id.toolbarTitle));
        greetingText.check(matches(withText(TOOLBAR_TITLE)));
    }

    /* Test that the activity finishes when the toolbar back button is pressed. */
    @Test
    public void TestToolbarBackButton(){
        onView(withId(R.id.toolbarBackButton)).perform(click());
        assertSame(testRule.getScenario().getResult().getResultCode(), Activity.RESULT_CANCELED);
    }

    /* Test to scroll trough elements in recycler view and press last item, to see correct intent is sent */
    @Test
    public void TestPressLastItem(){
        // TODO Redo test when actually using database.
        onView(withId(R.id.gallery_recyclerview))
                .perform(RecyclerViewActions.actionOnItemAtPosition(19,
                        click()));
        intended(allOf(IntentMatchers.hasComponent(ImageActivity.class.getName()),
                IntentMatchers.hasExtra("image", R.drawable.temp_camera)));
    }
}
