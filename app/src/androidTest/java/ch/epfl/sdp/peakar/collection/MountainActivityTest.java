package ch.epfl.sdp.peakar.collection;

import android.app.Activity;
import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Locale;

import ch.epfl.sdp.peakar.R;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class MountainActivityTest {

    private static final String testName = "TEST_NAME";
    private static final int testPoints = 100;
    private static final int testHeight = 1000;
    private static final float testLongitude = 40;
    private static final float testLatitude = 60;

    static Intent intent;
    static {
        intent = new Intent(ApplicationProvider.getApplicationContext(), MountainActivity.class);
        intent.putExtra("name", testName);
        intent.putExtra("points", testPoints);
        intent.putExtra("height", testHeight);
        intent.putExtra("longitude", testLongitude);
        intent.putExtra("latitude", testLatitude);
    }

    @Rule
    public ActivityScenarioRule<MountainActivity> testRule = new ActivityScenarioRule<>(intent);

    /* Test that the toolbar title is set as expected */
    @Test
    public void TestToolbarTitle(){
        ViewInteraction toolbarTitle = Espresso.onView(ViewMatchers.withId(R.id.toolbarTitle));
        toolbarTitle.check(matches(withText(testName)));
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

    /* Test that the points text is set as expected */
    @Test
    public void TestPointsText(){
        ViewInteraction pointText = Espresso.onView(withId(R.id.pointText));
        pointText.check(matches(withText(String.format(Locale.getDefault(), " %d", testPoints))));
    }

    /* Test that the height text is set as expected */
    @Test
    public void TestHeightText(){
        ViewInteraction heightText = Espresso.onView(withId(R.id.heightText));
        heightText.check(matches(withText(String.format(Locale.getDefault(), " %d", testHeight))));
    }

    /* Test that the position text is set as expected */
    @Test
    public void TestPositionText(){
        ViewInteraction positionText = Espresso.onView(withId(R.id.positionText));
        positionText.check(matches(withText(String.format(Locale.getDefault(), " (%.2f, %.2f)",
                                                        testLongitude, testLatitude))));
    }
}
