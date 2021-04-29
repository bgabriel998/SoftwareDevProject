package com.github.bgabriel998.softwaredevproject.rankings;

import android.app.Activity;

import androidx.test.espresso.DataInteraction;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.github.bgabriel998.softwaredevproject.R;
import com.github.bgabriel998.softwaredevproject.database.Database;
import com.github.bgabriel998.softwaredevproject.rankings.RankingItem;
import com.github.bgabriel998.softwaredevproject.rankings.RankingsActivity;
import com.github.bgabriel998.softwaredevproject.user.account.Account;
import com.google.firebase.auth.FirebaseAuth;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.github.bgabriel998.softwaredevproject.UITestHelper.withBackgroundColor;
import static com.github.bgabriel998.softwaredevproject.UITestHelper.withTextColor;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class RankingsActivityTest {

    private static final Integer MAXIMUM_POINTS = Integer.MAX_VALUE;
    private static final String TESTING_USERNAME = "invalid@username";
    private static final List<Integer> mockPoints = IntStream.rangeClosed(MAXIMUM_POINTS-19, MAXIMUM_POINTS-1).boxed().collect(Collectors.toList());
    private static final List<Integer> mockPositions = IntStream.rangeClosed(2, 20).boxed().collect(Collectors.toList());

    /* Set up the environment */
    @BeforeClass
    public static void init() throws InterruptedException {
        Collections.reverse(mockPoints);
        /* Make sure that mock users are not on the database before the tests*/
        for(int i=0; i < mockPoints.size(); i++) {
            Database.refRoot.child("users").child("test" + mockPositions.get(i)).removeValue();
        }
        Database.refRoot.child("users").child("null").removeValue();
        Thread.sleep(1500);

        /* Make sure no user is signed in before a test */
        FirebaseAuth.getInstance().signOut();
        Account.getAccount().synchronizeUserProfile();
        Thread.sleep(1500);
    }

    @Rule
    public ActivityScenarioRule<RankingsActivity> testRule = new ActivityScenarioRule<>(RankingsActivity.class);

    /* Make sure that mock users are not on the database after a test */
    @After
    public void removeTestUsers() throws InterruptedException {
        for(int i=0; i < mockPoints.size(); i++) {
            Database.refRoot.child("users").child("test" + mockPositions.get(i)).removeValue();
        }
        Database.refRoot.child("users").child("null").removeValue();
        Thread.sleep(1500);
    }

    /* Test that the toolbar title is set as expected */
    @Test
    public void TestToolbarTitle(){
        String TOOLBAR_TITLE = "Rankings";
        ViewInteraction greetingText = onView(ViewMatchers.withId(R.id.toolbarTitle));
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

    /* Test that mock user sin list view are at correct places and contain correct data */
    @Test
    public void TestContentOfListView() throws InterruptedException {
        // Add the mock user on the database
        for(int i=0; i < mockPoints.size(); i++) {
            Database.setChild("users/test" + mockPositions.get(i), Arrays.asList("username", "score"), Arrays.asList(TESTING_USERNAME + mockPositions.get(i), mockPoints.get(i)));
        }
        Database.setChild("users/null", Arrays.asList("username", "score"), Arrays.asList(TESTING_USERNAME, MAXIMUM_POINTS));
        Thread.sleep(1500);

        // Check correct data
        DataInteraction interaction =  onData(instanceOf(RankingItem.class));
        for(int i=0; i <= mockPoints.size(); i++) {
            DataInteraction listItem = interaction.atPosition(i);

            listItem.onChildView(withId(R.id.ranking_item_position))
                    .check(matches(withText((i == 0 ? 1 : mockPositions.get(i-1)) + ".")));
            listItem.onChildView(withId(R.id.ranking_item_username))
                    .check(matches(withText(TESTING_USERNAME + (i == 0 ? "" : mockPositions.get(i-1)))));
            listItem.onChildView(withId(R.id.ranking_item_points))
                    .check(matches(withText(String.format("%d", MAXIMUM_POINTS - i))));
        }
    }

    /* Test that all elements colors in list view are correct */
    @Test
    public void TestColorOfListView() throws InterruptedException {
        // Add the mock user on the database
        for(int i=0; i < mockPoints.size(); i++) {
            Database.setChild("users/test" + mockPositions.get(i), Arrays.asList("username", "score"), Arrays.asList(TESTING_USERNAME + mockPositions.get(i), mockPoints.get(i)));
        }
        Database.setChild("users/null", Arrays.asList("username", "score"), Arrays.asList(TESTING_USERNAME, MAXIMUM_POINTS));
        Thread.sleep(1500);

        DataInteraction interaction =  onData(instanceOf(RankingItem.class));

        // Check correct colors on current fake user
        DataInteraction listItem = interaction.atPosition(0);
        listItem.onChildView(withId(R.id.ranking_item_container))
                .check(matches(withBackgroundColor(R.color.DarkGreen)));
        listItem.onChildView(withId(R.id.ranking_item_position))
                .check(matches(withTextColor(R.color.LightGrey)));
        listItem.onChildView(withId(R.id.ranking_item_username))
                .check(matches(withTextColor(R.color.LightGrey)));
        listItem.onChildView(withId(R.id.ranking_item_points))
                .check(matches(withTextColor(R.color.LightGrey)));

        // Check correct colors on other fake users
        for(int i=0; i < mockPoints.size(); i++) {
            listItem = interaction.atPosition(i+1);

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
