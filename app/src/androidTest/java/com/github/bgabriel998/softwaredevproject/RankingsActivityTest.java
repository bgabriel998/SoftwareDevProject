package com.github.bgabriel998.softwaredevproject;

import android.app.Activity;

import androidx.test.espresso.DataInteraction;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.matcher.BoundedMatcher;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

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

    /* Test that all elements in list view are at correct place and contains correct data */
    @Test
    public void TestContentOfListView(){
        DataInteraction interaction =  onData(instanceOf(RankingItem.class));

        for (int i = 0; i < 20; i++){
            DataInteraction listItem = interaction.atPosition(i);

            String position = String.format("%d.", i+1);
            String username = String.format("Username%d", i);
            String points = String.format("%d", 100 - i);

            listItem.onChildView(withId(R.id.ranking_item_position))
                    .check(matches(withText(position)));
            listItem.onChildView(withId(R.id.ranking_item_username))
                    .check(matches(withText(username)));
            listItem.onChildView(withId(R.id.ranking_item_points))
                    .check(matches(withText(points)));
        }
    }

    /* Test that all elements colors in list view */
    @Test
    public void TestColorOfListView(){
        DataInteraction interaction =  onData(instanceOf(RankingItem.class));

        for (int i = 0; i < 20; i++){
            DataInteraction listItem = interaction.atPosition(i);

            // Test users own ranking item
            if (i == 2) {
                listItem.onChildView(withId(R.id.ranking_item_container))
                        .check(matches(withBackgroundColor(R.color.DarkGreen)));
                listItem.onChildView(withId(R.id.ranking_item_position))
                        .check(matches(withTextColor(R.color.LightGrey)));
                listItem.onChildView(withId(R.id.ranking_item_username))
                        .check(matches(withTextColor(R.color.LightGrey)));
                listItem.onChildView(withId(R.id.ranking_item_points))
                        .check(matches(withTextColor(R.color.LightGrey)));
            }
            // Test all other ranking items
            else {
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
    }
}
