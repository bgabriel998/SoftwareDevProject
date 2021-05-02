package ch.epfl.sdp.peakar.user;

import android.app.Activity;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.DataInteraction;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.intent.matcher.IntentMatchers;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.platform.app.InstrumentationRegistry;

import ch.epfl.sdp.peakar.R;
import ch.epfl.sdp.peakar.database.Database;
import ch.epfl.sdp.peakar.user.auth.Account;
import ch.epfl.sdp.peakar.user.auth.Authentication;
import ch.epfl.sdp.peakar.user.auth.FirebaseAuthentication;
import ch.epfl.sdp.peakar.user.friends.FriendItem;
import ch.epfl.sdp.peakar.user.friends.FriendItemActivity;
import ch.epfl.sdp.peakar.user.friends.FriendsActivity;
import com.google.firebase.auth.FirebaseAuth;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static ch.epfl.sdp.peakar.user.AccountTest.BASIC_USERNAME;
import static ch.epfl.sdp.peakar.user.AccountTest.SHORT_SLEEP_TIME;
import static ch.epfl.sdp.peakar.user.AccountTest.registerAuthUser;
import static ch.epfl.sdp.peakar.user.AccountTest.removeAuthUser;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

public class FriendsActivityTest {
    private static String username;
    private final static int FRIENDS_SIZE = 20;

    /* Set up the environment */
    @BeforeClass
    public static void init() {
        /* Make sure no user is signed in before tests */
        Authentication.getInstance().signOut(InstrumentationRegistry.getInstrumentation().getTargetContext());


        /* Create a new one */
        registerAuthUser();

        username = (BASIC_USERNAME + Authentication.getInstance().getID()).substring(0, Account.NAME_MAX_LENGTH - 4);
    }

    /* Clean environment */
    @AfterClass
    public static void end() {
        removeAuthUser();
    }

    /* Make sure that an account is signed in and as new before each test */
    @Before
    public void createTestUser() {
        if(FirebaseAuth.getInstance().getCurrentUser() == null) {
            Authentication.getInstance().signOut(InstrumentationRegistry.getInstrumentation().getTargetContext());
            registerAuthUser();
        }
        else {
            FirebaseAuthentication.getInstance().forceRetrieveData();
        }
    }

    /* Make sure that mock users are not on the database after a test */
    @After
    public void removeTestUsers() throws InterruptedException {
        Database.refRoot.child(Database.CHILD_USERS).child(Authentication.getInstance().getID()).removeValue();
        for(int i=0; i < FRIENDS_SIZE; i++) {
            Database.refRoot.child(Database.CHILD_USERS).child(username + ((i < 10) ? "0" + i : i)).removeValue();
        }
        Thread.sleep(SHORT_SLEEP_TIME);
    }

    @Rule
    public ActivityScenarioRule<FriendsActivity> testRule = new ActivityScenarioRule<>(FriendsActivity.class);

    /* Test that the toolbar title is set as expected */
    @Test
    public void TestToolbarTitle(){
        String TOOLBAR_TITLE = "Friends";
        ViewInteraction greetingText = Espresso.onView(ViewMatchers.withId(R.id.toolbarTitle));
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
        for(int i=0; i < FRIENDS_SIZE; i++) {
            Database.setChild(Database.CHILD_USERS + username + ((i < 10) ? "0" + i : i), Collections.singletonList(Database.CHILD_USERNAME), Collections.singletonList(username + i));
        }
        Database.setChild(Database.CHILD_USERS + Authentication.getInstance().getID(), Collections.singletonList(Database.CHILD_USERNAME), Collections.singletonList(username));
        Thread.sleep(AccountTest.LONG_SLEEP_TIME);

        // Add mock users to the friends
        for(int i=0; i < FRIENDS_SIZE; i++) {
            Database.setChild(Database.CHILD_USERS + Authentication.getInstance().getID() + Database.CHILD_FRIENDS, Collections.singletonList(username + ((i < 10) ? "0" + i : i)), Collections.singletonList(""));
        }
        Thread.sleep(AccountTest.LONG_SLEEP_TIME * 2);

        FirebaseAuthentication.getInstance().forceRetrieveData();
        Thread.sleep(AccountTest.LONG_SLEEP_TIME * 2);

        assertNotNull(Authentication.getInstance().getAuthAccount().getFriends());
        assertSame(20, Authentication.getInstance().getAuthAccount().getFriends().size());

        testRule.getScenario().recreate();
        Thread.sleep(AccountTest.LONG_SLEEP_TIME * 2);

        DataInteraction interaction =  onData(instanceOf(FriendItem.class));

        for (int i = 0; i < FRIENDS_SIZE; i++){
            DataInteraction listItem = interaction.atPosition(i);

            String usernameIteration = username + i;

            listItem.onChildView(withId(R.id.friend_username))
                    .check(matches(withText(usernameIteration)));
        }

        // Change friend username and points
        String newUsername = username + "new";
        int newScore = 20;
        Database.setChild(Database.CHILD_USERS + username + "00", Arrays.asList(Database.CHILD_USERNAME, Database.CHILD_SCORE), Arrays.asList(newUsername, newScore));
        Thread.sleep(AccountTest.SHORT_SLEEP_TIME * 2);

        // Check username has been updated
        interaction.atPosition(0).onChildView(withId(R.id.friend_username))
                .check(matches(withText(newUsername)));

    }

    /* Test press on friend item and test remove friend button */
    @Test
    public void FriendItemActivityTest() throws InterruptedException {
        // Set up the scenario
        // Add mock users
        String FRIEND_USER = username + "00";

        Database.setChild(Database.CHILD_USERS + FRIEND_USER, Collections.singletonList(Database.CHILD_USERNAME), Collections.singletonList(FRIEND_USER));
        Database.setChild(Database.CHILD_USERS + Authentication.getInstance().getID(), Collections.singletonList(Database.CHILD_USERNAME), Collections.singletonList(username));

        // Add mock user to the friends
        Database.setChild(Database.CHILD_USERS + Authentication.getInstance().getID() + Database.CHILD_FRIENDS, Collections.singletonList(FRIEND_USER), Collections.singletonList(""));
        Thread.sleep(AccountTest.LONG_SLEEP_TIME * 2);

        FirebaseAuthentication.getInstance().forceRetrieveData();
        Thread.sleep(AccountTest.LONG_SLEEP_TIME * 2);

        testRule.getScenario().recreate();
        Thread.sleep(AccountTest.LONG_SLEEP_TIME * 2);

        // Item at pos 0 looks like this
        FriendItem correctItem = new FriendItem(
                FRIEND_USER);

        Intents.init();

        // Get Item at pos 0 and click.
        DataInteraction listItem = onData(instanceOf(FriendItem.class)).atPosition(0);
        listItem.perform(ViewActions.click());
        Thread.sleep(AccountTest.SHORT_SLEEP_TIME);

        // Catch intent, and check information
        intended(allOf(IntentMatchers.hasComponent(FriendItemActivity.class.getName()),
                IntentMatchers.hasExtra("username", correctItem.getUsername()),
                IntentMatchers.hasExtra("points", correctItem.getPoints())));

        // Remove friend
        onView(withId(R.id.removeFriendButton)).perform(ViewActions.click());
        String removeMessage = FRIEND_USER + " " + ApplicationProvider.getApplicationContext().getResources().getString(R.string.friend_removed);

        // Catch intent, and check information
        intended(allOf(IntentMatchers.hasComponent(FriendsActivity.class.getName()),
                IntentMatchers.hasExtra(FriendItemActivity.INTENT_EXTRA_NAME, removeMessage)));

        // Check snack bar
        onView(withText(removeMessage)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));

        Intents.release();
    }
}