package ch.epfl.sdp.peakar.user;

import android.app.Activity;
import android.util.Log;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.intent.matcher.IntentMatchers;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.platform.app.InstrumentationRegistry;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;

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
import ch.epfl.sdp.peakar.user.profile.ProfileActivity;
import ch.epfl.sdp.peakar.user.services.AuthAccount;
import ch.epfl.sdp.peakar.user.services.AuthService;
import ch.epfl.sdp.peakar.user.services.providers.firebase.FirebaseAuthService;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withHint;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static ch.epfl.sdp.peakar.TestingConstants.BASIC_USERNAME;
import static ch.epfl.sdp.peakar.TestingConstants.LONG_SLEEP_TIME;
import static ch.epfl.sdp.peakar.user.AuthAccountTest.registerAuthUser;
import static ch.epfl.sdp.peakar.user.AuthAccountTest.removeAuthUser;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

public class ProfileActivityTest {
    private static String user1;
    private static String user2;

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
        Task<Void> task1 = Database.refRoot.child(Database.CHILD_USERS).child(AuthService.getInstance().getID()).removeValue();
        Task<Void> task2 = Database.refRoot.child(Database.CHILD_USERS).child(user2).removeValue();
        try {
            Tasks.await(task1);
            Tasks.await(task2);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
    public ActivityScenarioRule<ProfileActivity> testRule = new ActivityScenarioRule<>(ProfileActivity.class);

    /* Test that the toolbar title is set as expected */
    @Test
    public void toolbarTitleTest(){
        String TOOLBAR_TITLE = "Profile";
        ViewInteraction greetingText = Espresso.onView(ViewMatchers.withId(R.id.toolbarTitle));
        greetingText.check(matches(withText(TOOLBAR_TITLE)));
    }

    /* Test that the activity finishes when the toolbar back button is pressed. */
    @Test
    public void TestToolbarBackButton(){
        onView(withId(R.id.toolbarBackButton)).perform(click());
        try {
            Thread.sleep(LONG_SLEEP_TIME);
            assertSame(testRule.getScenario().getResult().getResultCode(), Activity.RESULT_CANCELED);
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail("TestToolbarBackButton failed");
        }
    }

    /* Test that if no account is signed the sign in button is visible */
    @Test
    public void withNoAccountSignedTest() {
        removeAuthUser();
        testRule.getScenario().recreate();

        Espresso.onView(withId(R.id.signInButton)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
        Espresso.onView(withId(R.id.signOutButton)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
        Espresso.onView(withId(R.id.changeUsernameButton)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
        Espresso.onView(withId(R.id.submitUsernameButton)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
        Espresso.onView(withId(R.id.editTextUsername)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
    }

    /* Test that the message inviting the user to write the username in the correct box is correct */
    @Test
    public void changeUsernameTextTest() {
        ViewInteraction changeUsernameText = Espresso.onView(withId(R.id.editTextUsername));
        changeUsernameText.check(matches(withHint(R.string.insert_username_button)));
    }

    /* Test that the username choice UI is correct */
    @Test
    public void changeUsernameUITest() {
        testRule.getScenario().onActivity(ProfileActivity::setUsernameChoiceUI);
        Espresso.onView(withId(R.id.signInButton)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
        Espresso.onView(withId(R.id.signOutButton)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
        Espresso.onView(withId(R.id.changeUsernameButton)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
        Espresso.onView(withId(R.id.submitUsernameButton)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
        Espresso.onView(withId(R.id.editTextUsername)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
    }

    /* Test that if the username is already present the correct message is displayed */
    @Test
    public void usernameAlreadyPresentTest() {
        Task<Void> task1 = Database.refRoot.child(Database.CHILD_USERS).child(user2).child(Database.CHILD_USERNAME).setValue(user2);
        try {
            Tasks.await(task1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        testRule.getScenario().onActivity(ProfileActivity::setUsernameChoiceUI);
        onView(withId(R.id.editTextUsername)).perform(typeText(user2));
        Espresso.closeSoftKeyboard();
        onView(withId(R.id.submitUsernameButton)).perform(click());
        try {
            Thread.sleep(LONG_SLEEP_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        onView(withText(R.string.already_existing_username)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
        Espresso.onView(withId(R.id.signInButton)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
    }

    /* Test that if the username has changed the correct message is displayed */
    /* The account created is then removed */
    @Test
    public void registerUserTest() {
        Task<Void> task1 = Database.refRoot.child(Database.CHILD_USERS).child(AuthService.getInstance().getID()).removeValue();
        try {
            Tasks.await(task1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        FirebaseAuthService.getInstance().forceRetrieveData();
        Log.d("ProfileActivityTest", "registerUserTest: username before " + AuthService.getInstance().getAuthAccount().getUsername());
        testRule.getScenario().onActivity(ProfileActivity::setUsernameChoiceUI);
        onView(withId(R.id.editTextUsername)).perform(typeText(user1));
        Espresso.closeSoftKeyboard();
        onView(withId(R.id.submitUsernameButton)).perform(click());
        try {
            Thread.sleep(LONG_SLEEP_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        onView(withText(R.string.registered_username)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
        testRule.getScenario().onActivity(ProfileActivity::setUsernameChoiceUI);
        onView(withId(R.id.editTextUsername)).perform(typeText(user2));
        Espresso.closeSoftKeyboard();
        onView(withId(R.id.submitUsernameButton)).perform(click());
        try {
            Thread.sleep(LONG_SLEEP_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        onView(withText(R.string.available_username)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
        Espresso.onView(withId(R.id.submitUsernameButton)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
    }

    /* Test that if the username chooses his current username the correct message is displayed */
    @Test
    public void chosenCurrentUsernameTest() {
        testRule.getScenario().onActivity(ProfileActivity::setUsernameChoiceUI);
        onView(withId(R.id.editTextUsername)).perform(typeText("null"));
        Espresso.closeSoftKeyboard();
        onView(withId(R.id.submitUsernameButton)).perform(click());
        testRule.getScenario().onActivity(ProfileActivity::setUsernameChoiceUI);
        onView(withId(R.id.editTextUsername)).perform(typeText("null"));
        Espresso.closeSoftKeyboard();
        onView(withId(R.id.submitUsernameButton)).perform(click());
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

        testRule.getScenario().onActivity(ProfileActivity::setUsernameChoiceUI);
        onView(withId(R.id.editTextUsername)).perform(typeText(usernameTest));
        Espresso.closeSoftKeyboard();
        onView(withId(R.id.submitUsernameButton)).perform(click());
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
        Task<Void> task1 = Database.refRoot.child(Database.CHILD_USERS).child(AuthService.getInstance().getID()).child(Database.CHILD_USERNAME).setValue(user1);
        try {
            Tasks.await(task1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        FirebaseAuthService.getInstance().forceRetrieveData();
        testRule.getScenario().recreate();

        onView(withId(R.id.changeUsernameButton)).perform(click());
        Espresso.onView(withId(R.id.submitUsernameButton)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
    }

    /* Test that the message inviting the user to write the username in the correct box is correct */
    @Test
    public void addFriendTextTest() {

        Task<Void> task1 = Database.refRoot.child(Database.CHILD_USERS).child(AuthService.getInstance().getID()).child(Database.CHILD_USERNAME).setValue(user1);
        try {
            Tasks.await(task1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        FirebaseAuthService.getInstance().forceRetrieveData();
        testRule.getScenario().recreate();

        onView(withId(R.id.addFriendButton)).perform(click());
        ViewInteraction addFriendText = Espresso.onView(withId(R.id.editTextFriend));
        addFriendText.check(matches(withHint(R.string.insert_friend_button)));
    }

    /* Test that if the friend is already present the correct message is displayed */
    @Test
    public void friendAlreadyPresentTest() {
        Task<Void> task1 = Database.refRoot.child(Database.CHILD_USERS).child(AuthService.getInstance().getID()).child(Database.CHILD_USERNAME).setValue(user1);
        Task<Void> task2 = Database.refRoot.child(Database.CHILD_USERS).child(user2).child(Database.CHILD_USERNAME).setValue(user2);
        Task<Void> task3 = Database.refRoot.child(Database.CHILD_USERS).child(AuthService.getInstance().getID()).child(Database.CHILD_FRIENDS).child(user2).setValue("");
        try {
            Tasks.await(task1);
            Tasks.await(task2);
            Tasks.await(task3);
        } catch (Exception e) {
            e.printStackTrace();
        }
        FirebaseAuthService.getInstance().forceRetrieveData();
        testRule.getScenario().recreate();

        onView(withId(R.id.addFriendButton)).perform(click());
        onView(withId(R.id.editTextFriend)).perform(typeText(user2));
        Espresso.closeSoftKeyboard();
        onView(withId(R.id.submitFriendButton)).perform(click());
        try {
            Thread.sleep(LONG_SLEEP_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        onView(withText(R.string.friend_already_added)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
    }

    /* Test that if the friend has been added the correct message is displayed */
    @Test
    public void addFriendTest() {
        final String added_message = user2 + " " + ApplicationProvider.getApplicationContext().getResources().getString(R.string.friend_added);
        Task<Void> task1 = Database.refRoot.child(Database.CHILD_USERS).child(AuthService.getInstance().getID()).child(Database.CHILD_USERNAME).setValue(user1);
        Task<Void> task2 = Database.refRoot.child(Database.CHILD_USERS).child(user2).child(Database.CHILD_USERNAME).setValue(user2);
        try {
            Tasks.await(task1);
            Tasks.await(task2);
        } catch (Exception e) {
            e.printStackTrace();
        }
        FirebaseAuthService.getInstance().forceRetrieveData();
        testRule.getScenario().recreate();
        onView(withId(R.id.addFriendButton)).perform(click());
        onView(withId(R.id.editTextFriend)).perform(typeText(user2));
        Espresso.closeSoftKeyboard();
        onView(withId(R.id.submitFriendButton)).perform(click());
        try {
            Thread.sleep(LONG_SLEEP_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        onView(withText(added_message)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
    }

    /* Test that if the friend username is the current username the correct message is displayed */
    @Test
    public void addFriendCurrentUsernameTest() {
        Task<Void> task1 = Database.refRoot.child(Database.CHILD_USERS).child(AuthService.getInstance().getID()).child(Database.CHILD_USERNAME).setValue(user1);
        try {
            Tasks.await(task1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        FirebaseAuthService.getInstance().forceRetrieveData();
        testRule.getScenario().recreate();

        onView(withId(R.id.addFriendButton)).perform(click());
        onView(withId(R.id.editTextFriend)).perform(typeText(user1));
        Espresso.closeSoftKeyboard();
        onView(withId(R.id.submitFriendButton)).perform(click());
        try {
            Thread.sleep(LONG_SLEEP_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        onView(withText(R.string.add_current_username)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
    }

    /* Test that if the friend username is not present the correct message is displayed */
    @Test
    public void addNoExistingUser() {
        Task<Void> task1 = Database.refRoot.child(Database.CHILD_USERS).child(AuthService.getInstance().getID()).child(Database.CHILD_USERNAME).setValue(user1);
        try {
            Tasks.await(task1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        FirebaseAuthService.getInstance().forceRetrieveData();
        testRule.getScenario().recreate();

        onView(withId(R.id.addFriendButton)).perform(click());
        onView(withId(R.id.editTextFriend)).perform(typeText(user2));
        Espresso.closeSoftKeyboard();
        onView(withId(R.id.submitFriendButton)).perform(click());
        try {
            Thread.sleep(LONG_SLEEP_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        onView(withText(R.string.friend_not_present_db)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
    }

    /* Test that if the user chooses an invalid friend's username the correct message is displayed */
    @Test
    public void friendIsNotValidTest() {
        final String username = "";

        Task<Void> task1 = Database.refRoot.child(Database.CHILD_USERS).child(AuthService.getInstance().getID()).child(Database.CHILD_USERNAME).setValue(user1);
        try {
            Tasks.await(task1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        FirebaseAuthService.getInstance().forceRetrieveData();
        testRule.getScenario().recreate();

        onView(withId(R.id.addFriendButton)).perform(click());
        onView(withId(R.id.editTextFriend)).perform(typeText(username));
        Espresso.closeSoftKeyboard();
        onView(withId(R.id.submitFriendButton)).perform(click());
        try {
            Thread.sleep(LONG_SLEEP_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        onView(withText(R.string.invalid_username)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
    }

    /* Test that UI is displayed correctly when sign out button is pressed. */
    @Test
    public void signOutButtonTest() {
        Task<Void> task1 = Database.refRoot.child(Database.CHILD_USERS).child(AuthService.getInstance().getID()).child(Database.CHILD_USERNAME).setValue(user1);
        try {
            Tasks.await(task1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        FirebaseAuthService.getInstance().forceRetrieveData();
        testRule.getScenario().recreate();

        removeAuthUser();
        onView(withId(R.id.signOutButton)).perform(click());
        Espresso.onView(withId(R.id.signInButton)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));

        onView(withId(R.id.signInButton)).perform(click());
        intended(IntentMatchers.hasComponent(com.google.android.gms.auth.api.signin.internal.SignInHubActivity.class.getName()));
    }

    /* Test that FriendsActivity is started on button click */
    @Test
    public void friendsButtonTest() {
        Task<Void> task1 = Database.refRoot.child(Database.CHILD_USERS).child(AuthService.getInstance().getID()).child(Database.CHILD_USERNAME).setValue(user1);
        try {
            Tasks.await(task1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        FirebaseAuthService.getInstance().forceRetrieveData();
        testRule.getScenario().recreate();

        onView(withId(R.id.friendsButton)).perform(click());
        intended(IntentMatchers.hasComponent(FriendsActivity.class.getName()));
    }

    /* Test that AddFriendActivity is started on button click */
    @Test
    public void addFriendButtonTest() {
        Task<Void> task1 = Database.refRoot.child(Database.CHILD_USERS).child(AuthService.getInstance().getID()).child(Database.CHILD_USERNAME).setValue(user1);
        try {
            Tasks.await(task1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        FirebaseAuthService.getInstance().forceRetrieveData();
        testRule.getScenario().recreate();

        onView(withId(R.id.addFriendButton)).perform(click());
        intended(IntentMatchers.hasComponent(AddFriendActivity.class.getName()));
    }
}
