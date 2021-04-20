package com.github.bgabriel998.softwaredevproject;

import android.app.Activity;
import android.view.View;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.DataInteraction;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.intent.matcher.IntentMatchers;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.platform.app.InstrumentationRegistry;

import com.github.giommok.softwaredevproject.Account;
import com.github.giommok.softwaredevproject.Database;
import com.google.firebase.auth.FirebaseAuth;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.swipeDown;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayingAtLeast;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

public class FriendsActivityTest {
    private final static int FRIENDS_SIZE = 20;

    /* Set up the environment */
    @BeforeClass
    public static void init() throws InterruptedException {
        /* Make sure that mock users are not on the database before the tests*/
        for(int i=0; i < FRIENDS_SIZE; i++) {
            Database.refRoot.child("users").child("test" + i).removeValue();
        }
        Database.refRoot.child("users").child("null").removeValue();
        Thread.sleep(1500);

        /* Make sure no user is signed in before a test */
        FirebaseAuth.getInstance().signOut();
        Account.getAccount().synchronizeUserProfile();
        Thread.sleep(1500);
    }

    /* Make sure that mock users are not on the database after a test */
    @After
    public void removeTestUsers() throws InterruptedException {
        for(int i=0; i < FRIENDS_SIZE; i++) {
            Database.refRoot.child("users").child("test" + i).removeValue();
        }
        Database.refRoot.child("users").child("null").removeValue();
        Thread.sleep(1500);
    }

    @Rule
    public ActivityScenarioRule<FriendsActivity> testRule = new ActivityScenarioRule<>(FriendsActivity.class);

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
        String TOOLBAR_TITLE = "Friends";
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

    /* Test that all elements in list view are at correct place and contains correct data */
    @Test
    public void TestContentOfListView() throws InterruptedException {
        // Set up the scenario
        // Add mock users
        String TESTING_USERNAME = "username@Test";
        for(int i=0; i < FRIENDS_SIZE; i++) {
            Database.setChild(Database.CHILD_USERS + "test" + i, Collections.singletonList(Database.CHILD_USERNAME), Collections.singletonList(TESTING_USERNAME + i));
        }
        Database.setChild(Database.CHILD_USERS + "null", Collections.singletonList(Database.CHILD_USERNAME), Collections.singletonList(TESTING_USERNAME));
        Thread.sleep(1500);

        // Add mock users to the friends
        for(int i=0; i < FRIENDS_SIZE; i++) {
            Database.setChild(Database.CHILD_USERS + "null" + Database.CHILD_FRIENDS, Collections.singletonList("test" + i), Collections.singletonList(""));
        }
        Thread.sleep(1500);

        testRule.getScenario().recreate();

        DataInteraction interaction =  onData(instanceOf(FriendItem.class));

        for (int i = 0; i < FRIENDS_SIZE; i++){
            DataInteraction listItem = interaction.atPosition(i);

            String username = TESTING_USERNAME + i;

            listItem.onChildView(withId(R.id.friend_username))
                    .check(matches(withText(username)));
        }

        // Change friend username and points
        String newUsername = TESTING_USERNAME + "new";
        int newScore = 20;
        Database.setChild(Database.CHILD_USERS + "test0", Collections.singletonList(Database.CHILD_USERNAME), Collections.singletonList(newUsername));
        Database.setChild(Database.CHILD_USERS + "test0", Collections.singletonList(Database.CHILD_SCORE), Collections.singletonList(newScore));
        Thread.sleep(1500);

        // Check username has been updated
        interaction.atPosition(0).onChildView(withId(R.id.friend_username))
                .check(matches(withText(newUsername)));

    }

    /* Test press on friend item and test remove friend button */
    @Test
    public void FriendItemActivityTest() throws InterruptedException {
        // Set up the scenario
        // Add mock users
        String TESTING_USERNAME = "username@Test";
        String TESTING_UID = "null";
        String FRIEND_USERNAME = TESTING_USERNAME + 0;
        String FRIEND_UID = "test0";

        Database.setChild(Database.CHILD_USERS + FRIEND_UID, Collections.singletonList(Database.CHILD_USERNAME), Collections.singletonList(FRIEND_USERNAME));
        Database.setChild(Database.CHILD_USERS + TESTING_UID, Collections.singletonList(Database.CHILD_USERNAME), Collections.singletonList(TESTING_USERNAME));
        Thread.sleep(1500);

        // Add mock user to the friends
        Database.setChild(Database.CHILD_USERS + TESTING_UID + Database.CHILD_FRIENDS, Collections.singletonList(FRIEND_UID), Collections.singletonList(""));
        Thread.sleep(1500);

        testRule.getScenario().recreate();
        Thread.sleep(500);

        // Item at pos 0 looks like this
        FriendItem correctItem = new FriendItem(
                FRIEND_UID,
                FRIEND_USERNAME,
                0);

        // Get Item at pos 0 and click.
        DataInteraction listItem = onData(instanceOf(FriendItem.class)).atPosition(0);
        listItem.perform(ViewActions.click());
        Thread.sleep(500);

        // Catch intent, and check information
        intended(allOf(IntentMatchers.hasComponent(FriendItemActivity.class.getName()),
                IntentMatchers.hasExtra("username", correctItem.getUsername()),
                IntentMatchers.hasExtra("points", correctItem.getPoints())));

        // Remove friend
        onView(withId(R.id.removeFriendButton)).perform(ViewActions.click());
        String removeMessage = FRIEND_USERNAME + " " + ApplicationProvider.getApplicationContext().getResources().getString(R.string.friend_removed);

        // Catch intent, and check information
        intended(allOf(IntentMatchers.hasComponent(FriendsActivity.class.getName()),
                IntentMatchers.hasExtra(FriendItemActivity.INTENT_EXTRA_NAME, removeMessage)));

        // Check snack bar
        onView(withText(removeMessage)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
    }
}
