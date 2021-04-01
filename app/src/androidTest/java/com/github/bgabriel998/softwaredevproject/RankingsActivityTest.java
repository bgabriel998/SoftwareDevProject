package com.github.bgabriel998.softwaredevproject;

import android.app.Activity;

import androidx.test.espresso.DataInteraction;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.matcher.BoundedMatcher;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.github.giommok.softwaredevproject.Database;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.github.bgabriel998.softwaredevproject.UITestHelper.withBackgroundColor;
import static com.github.bgabriel998.softwaredevproject.UITestHelper.withTextColor;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertSame;

@RunWith(AndroidJUnit4.class)
public class RankingsActivityTest {

    @Rule
    public ActivityScenarioRule<RankingsActivity> testRule = new ActivityScenarioRule<>(RankingsActivity.class);

    /* Make sure that mock users are not on the database after a test */
    @After
    public void removeTestUsers() throws InterruptedException {
        Database.refRoot.child("users").child("null").removeValue();
        Database.refRoot.child("users").child("test").removeValue();
        Thread.sleep(500);
    }

    /* Test that the toolbar title is set as expected */
    @Test
    public void TestToolbarTitle(){
        String TOOLBAR_TITLE = "Rankings";
        ViewInteraction greetingText = onView(withId(R.id.toolbarTitle));
        greetingText.check(matches(withText(TOOLBAR_TITLE)));
    }

    /* Test that the activity finishes when the toolbar back button is pressed. */
    @Test
    public void TestToolbarBackButton(){
        onView(withId(R.id.toolbarBackButton)).perform(click());
        assertSame(testRule.getScenario().getResult().getResultCode(), Activity.RESULT_CANCELED);
    }

    /* Test that a mock user in list view is at correct place and contains correct data */
    @Test
    public void TestContentOfListView() throws InterruptedException {
        // Set up the test
        final Integer MAXIMUM_POINTS = Integer.MAX_VALUE;
        final String TESTING_USERNAME = "invalid##username@";
        final Integer FIRST_POSITION = 1;

        // Add the mock user on the database
        Database.setChild("users/null", Arrays.asList("username", "score"), Arrays.asList(TESTING_USERNAME, MAXIMUM_POINTS));
        Thread.sleep(1000);
        DataInteraction interaction =  onData(instanceOf(RankingItem.class));
        DataInteraction listItem = interaction.atPosition(0);

        // Check correct data
        listItem.onChildView(withId(R.id.ranking_item_position))
                .check(matches(withText(String.format("%d.", FIRST_POSITION))));
        listItem.onChildView(withId(R.id.ranking_item_username))
                .check(matches(withText(TESTING_USERNAME)));
        listItem.onChildView(withId(R.id.ranking_item_points))
                .check(matches(withText(String.format("%d", MAXIMUM_POINTS))));
    }

    /* Test that all elements colors in list view */
    @Test
    public void TestColorOfListView() throws InterruptedException {
        // Set up the test
        final Integer MAXIMUM_POINTS = Integer.MAX_VALUE;
        final String TESTING_USERNAME = "invalid##username@";
        final Integer FIRST_POSITION = 0;

        // Add the mock users on the database
        Database.setChild("users/null", Arrays.asList("username", "score"), Arrays.asList(TESTING_USERNAME, MAXIMUM_POINTS));
        Database.setChild("users/test", Arrays.asList("username", "score"), Arrays.asList(TESTING_USERNAME, MAXIMUM_POINTS-1));
        Thread.sleep(1000);
        DataInteraction interaction =  onData(instanceOf(RankingItem.class));

        // Check correct colors on current fake user
        DataInteraction listItem = interaction.atPosition(FIRST_POSITION);
        listItem.onChildView(withId(R.id.ranking_item_container))
                .check(matches(withBackgroundColor(R.color.DarkGreen)));
        listItem.onChildView(withId(R.id.ranking_item_position))
                .check(matches(withTextColor(R.color.LightGrey)));
        listItem.onChildView(withId(R.id.ranking_item_username))
                .check(matches(withTextColor(R.color.LightGrey)));
        listItem.onChildView(withId(R.id.ranking_item_points))
                .check(matches(withTextColor(R.color.LightGrey)));

        // Check correct colors on other fake user
        listItem = interaction.atPosition(FIRST_POSITION+1);
        listItem.onChildView(withId(R.id.ranking_item_container))
                .check(matches(withBackgroundColor(R.color.LightGrey)));
        listItem.onChildView(withId(R.id.ranking_item_position))
                .check(matches(withTextColor(R.color.DarkGreen)));
        listItem.onChildView(withId(R.id.ranking_item_username))
                .check(matches(withTextColor(R.color.DarkGreen)));
        listItem.onChildView(withId(R.id.ranking_item_points))
                .check(matches(withTextColor(R.color.DarkGreen)));
    }
}
