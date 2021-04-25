package com.github.bgabriel998.softwaredevproject;

import android.app.Activity;
import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.ViewInteraction;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

import org.junit.Rule;
import org.junit.Test;

import java.util.Locale;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.github.giommok.softwaredevproject.AccountTest.LONG_SLEEP_TIME;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

public class FriendItemActivityTest {

    private static final String testName = "TEST_NAME";
    private static final int testPoints = 100;

    static Intent intent;
    static {
        intent = new Intent(ApplicationProvider.getApplicationContext(), FriendItemActivity.class);
        intent.putExtra("username", testName);
        intent.putExtra("points", testPoints);
    }

    @Rule
    public ActivityScenarioRule<MountainActivity> testRule = new ActivityScenarioRule<>(intent);

    /* Test that the toolbar title is set as expected */
    @Test
    public void TestToolbarTitle(){
        ViewInteraction toolbarTitle = Espresso.onView(withId(R.id.toolbarTitle));
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
    public void TestPointsText() throws InterruptedException {
        Thread.sleep(LONG_SLEEP_TIME);
        ViewInteraction pointText = Espresso.onView(withId(R.id.pointText));
        pointText.check(matches(withText(String.format(Locale.getDefault(), " %d", testPoints))));
    }
}
