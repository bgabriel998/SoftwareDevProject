package ch.epfl.sdp.peakar.user;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.intent.matcher.IntentMatchers;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import ch.epfl.sdp.peakar.R;
import ch.epfl.sdp.peakar.database.Database;
import ch.epfl.sdp.peakar.user.friends.AddFriendActivity;
import ch.epfl.sdp.peakar.user.friends.FriendsActivity;
import ch.epfl.sdp.peakar.user.profile.NewProfileActivity;
import ch.epfl.sdp.peakar.user.profile.ProfileActivity;
import ch.epfl.sdp.peakar.user.services.AuthAccount;
import ch.epfl.sdp.peakar.user.services.AuthService;
import ch.epfl.sdp.peakar.user.services.FirebaseAuthService;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withHint;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static ch.epfl.sdp.peakar.database.DatabaseTest.databaseRefRoot;
import static ch.epfl.sdp.peakar.user.AuthAccountTest.registerAuthUser;
import static ch.epfl.sdp.peakar.user.AuthAccountTest.removeAuthUser;
import static ch.epfl.sdp.peakar.utils.TestingConstants.BASIC_USERNAME;
import static ch.epfl.sdp.peakar.utils.TestingConstants.LONG_SLEEP_TIME;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

public class NewProfileActivityTest {
    private static String user1;
    private static String user2;

    private static Intent getAuthIntent() {
        registerAuthUser();
        Intent intent = new Intent(InstrumentationRegistry.getInstrumentation().getTargetContext(), NewProfileActivity.class);
        intent.putExtra(NewProfileActivity.AUTH_INTENT, true);
        return intent;
    }

    /* Set up the environment */
    @BeforeClass
    public static void init() {
        /* Make sure no user is signed in before tests */
        AuthService.getInstance().signOut(InstrumentationRegistry.getInstrumentation().getTargetContext());


        /* Create a new one */
        registerAuthUser();

        user1 = (BASIC_USERNAME + AuthService.getInstance().getID()).substring(0, AuthAccount.NAME_MAX_LENGTH - 1);
        user2 = (BASIC_USERNAME + AuthService.getInstance().getID()).substring(0, AuthAccount.NAME_MAX_LENGTH - 2);
    }

    /* Clean environment */
    @AfterClass
    public static void end() {
        removeTestUsers();
        removeAuthUser();
    }

    /* Make sure that an account is signed in and as new before each test */
    @Before
    public void createTestUser() {
        registerAuthUser();
        removeTestUsers();
    }

    /* Make sure that mock users are not on the database after a test */
    public static void removeTestUsers() {
        databaseRefRoot.child(Database.CHILD_USERS).child(AuthService.getInstance().getID()).removeValue();
        databaseRefRoot.child(Database.CHILD_USERS).child(user2).removeValue();
    }

    /* Create Intent */
    @Before
    public void createIntent(){
        Intents.init();
    }

    /* Release Intent */
    @After
    public void releaseIntent(){
        if(AuthService.getInstance().getAuthAccount() != null) Log.d("ProfileActivityTest", "username after test: " + AuthService.getInstance().getAuthAccount().getUsername());
        Intents.release();
    }

    @Rule
    public ActivityScenarioRule testRule = new ActivityScenarioRule<>(getAuthIntent());

    /* Test that the message inviting the user to write the username in the correct box is correct */
    @Test
    public void changeUsernameTextTest() {
        onView(withId(R.id.profile_change_username)).perform(click());
        ViewInteraction changeUsernameText = Espresso.onView(withId(R.id.profile_username_edit));
        changeUsernameText.check(matches(withHint(R.string.insert_username_button)));
    }

    /* Test that if the username is already present the correct message is displayed */
    @Test
    public void usernameAlreadyPresentTest() {
        databaseRefRoot.child(Database.CHILD_USERS).child(user2).child(Database.CHILD_USERNAME).setValue(user2);
        onView(withId(R.id.profile_change_username)).perform(click());
        onView(withId(R.id.profile_username_edit)).perform(typeText(user2));
        onView(withId(R.id.profile_change_username)).perform(click());
        Espresso.closeSoftKeyboard();
        try {
            Thread.sleep(LONG_SLEEP_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        onView(withText(R.string.already_existing_username)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
    }

    /* Test that if the username has changed the correct message is displayed */
    /* The account created is then removed */
    @Test
    public void registerUserTest() {
        databaseRefRoot.child(Database.CHILD_USERS).child(AuthService.getInstance().getID()).removeValue();
        FirebaseAuthService.getInstance().forceRetrieveData();
        Log.d("ProfileActivityTest", "registerUserTest: username before " + AuthService.getInstance().getAuthAccount().getUsername());
        onView(withId(R.id.profile_change_username)).perform(click());
        onView(withId(R.id.profile_username_edit)).perform(typeText(user1));
        Espresso.closeSoftKeyboard();
        onView(withId(R.id.profile_change_username)).perform(click());
        try {
            Thread.sleep(LONG_SLEEP_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        onView(withText(R.string.registered_username)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
        onView(withId(R.id.profile_username_edit)).perform(typeText(user2));
        Espresso.closeSoftKeyboard();
        onView(withId(R.id.profile_change_username)).perform(click());
        try {
            Thread.sleep(LONG_SLEEP_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        onView(withText(R.string.available_username)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
    }

    /* Test that if the username chooses his current username the correct message is displayed */
    @Test
    public void chosenCurrentUsernameTest() {
        onView(withId(R.id.profile_change_username)).perform(click());
        onView(withId(R.id.profile_username_edit)).perform(typeText("null"));
        Espresso.closeSoftKeyboard();
        onView(withId(R.id.profile_change_username)).perform(click());
        onView(withId(R.id.profile_change_username)).perform(click());
        onView(withId(R.id.profile_username_edit)).perform(typeText("null"));
        Espresso.closeSoftKeyboard();
        onView(withId(R.id.profile_change_username)).perform(click());
        try {
            Thread.sleep(LONG_SLEEP_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        onView(withText(R.string.current_username)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
    }

    /* Test that if the user chooses an invalid username the correct message is displayed */
    @Test
    public void isNotValidTest() {
        final String usernameTest = "";

        onView(withId(R.id.profile_change_username)).perform(click());
        onView(withId(R.id.profile_username_edit)).perform(typeText(usernameTest));
        Espresso.closeSoftKeyboard();
        onView(withId(R.id.profile_change_username)).perform(click());
        try {
            Thread.sleep(LONG_SLEEP_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        onView(withText(R.string.invalid_username)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
    }

    /* Test that UI is displayed correctly when change username button is pressed. */
    @Test
    public void changeUsernameButtonTest() {
        databaseRefRoot.child(Database.CHILD_USERS).child(AuthService.getInstance().getID()).child(Database.CHILD_USERNAME).setValue(user1);
        FirebaseAuthService.getInstance().forceRetrieveData();
        onView(withId(R.id.profile_change_username)).perform(click());
        Espresso.onView(withId(R.id.profile_username)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
        Espresso.onView(withId(R.id.profile_username_edit)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
    }

    /* Test that UI is displayed correctly when sign out button is pressed. */
    @Test
    public void signOutButtonTest() {
        databaseRefRoot.child(Database.CHILD_USERS).child(AuthService.getInstance().getID()).child(Database.CHILD_USERNAME).setValue(user1);
        FirebaseAuthService.getInstance().forceRetrieveData();
        testRule.getScenario().recreate();

        removeAuthUser();
        onView(withId(R.id.profile_sign_out)).perform(click());
        assertNotSame(Lifecycle.State.RESUMED, testRule.getScenario().getState());
    }
}
