package com.github.bgabriel998.softwaredevproject;

import android.app.Activity;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.firebase.ui.auth.AuthUI;
import com.github.giommok.softwaredevproject.Database;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;


import java.util.Arrays;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withHint;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class ProfileActivityTest {

    /* Set up the environment */
    @BeforeClass
    public static void init() throws InterruptedException {
        /* Make sure that mock users are not on the database before the tests*/
        Database.refRoot.child("users").child("test").removeValue();
        Database.refRoot.child("users").child("null").removeValue();

        /* Make sure no user is signed in before a test */
        FirebaseAuth.getInstance().signOut();
        Thread.sleep(1500);
    }

    @Rule
    public ActivityScenarioRule<ProfileActivity> testRule = new ActivityScenarioRule<>(ProfileActivity.class);

    /* Make sure that mock users are not on the database after a test */
    @After
    public void removeTestUsers() throws InterruptedException {
        Database.refRoot.child("users").child("test").removeValue();
        Database.refRoot.child("users").child("null").removeValue();
        Thread.sleep(1500);
    }

    /* Test that the toolbar title is set as expected */
    @Test
    public void toolbarTitleTest(){
        String TOOLBAR_TITLE = "Profile";
        ViewInteraction greetingText = Espresso.onView(withId(R.id.toolbarTitle));
        greetingText.check(matches(withText(TOOLBAR_TITLE)));
    }

    /* Test that the activity finishes when the toolbar back button is pressed. */
    @Test
    public void toolbarBackButtonTest(){
        onView(withId(R.id.toolbarBackButton)).perform(click());
        assertSame(testRule.getScenario().getResult().getResultCode(), Activity.RESULT_CANCELED);
    }

    /* Test that if no account is signed the sign in button is visible */
    @Test
    public void withNoAccountSignedTest() {
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
    public void usernameAlreadyPresentTest() throws InterruptedException {
        final String usedUsername = "i3gn4u34o";
        Database.setChild("users/test", Arrays.asList("username"), Arrays.asList(usedUsername));

        testRule.getScenario().onActivity(ProfileActivity::setUsernameChoiceUI);
        onView(withId(R.id.editTextUsername)).perform(typeText(usedUsername));
        Espresso.closeSoftKeyboard();
        onView(withId(R.id.submitUsernameButton)).perform(click());
        Thread.sleep(2000);
        onView(withText(R.string.already_existing_username)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
        Thread.sleep(2000);
        Espresso.onView(withId(R.id.signInButton)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
    }

    /* Test that if the username has changed the correct message is displayed */
    /* The account created is then removed */
    @Test
    public void registerUserTest() throws InterruptedException {
        final String username = "i3gn4u34o";

        testRule.getScenario().onActivity(ProfileActivity::setUsernameChoiceUI);
        onView(withId(R.id.editTextUsername)).perform(typeText(username));
        Espresso.closeSoftKeyboard();
        onView(withId(R.id.submitUsernameButton)).perform(click());
        Thread.sleep(2000);
        onView(withText(R.string.available_username)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
        Thread.sleep(2000);
        Espresso.onView(withId(R.id.signInButton)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
    }

    /* Test that if the username chooses his current username the correct message is displayed */
    @Test
    public void chosenCurrentUsernameTest() throws InterruptedException {
        final String username = "null";

        testRule.getScenario().onActivity(ProfileActivity::setUsernameChoiceUI);
        onView(withId(R.id.editTextUsername)).perform(typeText(username));
        Espresso.closeSoftKeyboard();
        onView(withId(R.id.submitUsernameButton)).perform(click());
        Thread.sleep(2000);
        onView(withText(R.string.current_username)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
        Thread.sleep(2000);
        Espresso.onView(withId(R.id.signInButton)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
    }

    /* Test that if the username chooses an invalid username the correct message is displayed */
    @Test
    public void isNotValidTest() throws InterruptedException {
        final String username = "";

        testRule.getScenario().onActivity(ProfileActivity::setUsernameChoiceUI);
        onView(withId(R.id.editTextUsername)).perform(typeText(username));
        Espresso.closeSoftKeyboard();
        onView(withId(R.id.submitUsernameButton)).perform(click());
        Thread.sleep(2000);
        onView(withText(R.string.invalid_username)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
        Thread.sleep(2000);
        Espresso.onView(withId(R.id.signInButton)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
    }

    /* Test that UI is displayed correctly when change username button is pressed. */
    @Test
    public void changeUsernameButtonTest() throws InterruptedException {
        testRule.getScenario().onActivity(ProfileActivity::setLoggedUI);
        onView(withId(R.id.changeUsernameButton)).perform(click());
        Thread.sleep(2000);
        Espresso.onView(withId(R.id.submitUsernameButton)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
    }

    /* Test that UI is displayed correctly when sign out button is pressed. */
    @Test
    public void signOutButtonTest() throws InterruptedException {
        testRule.getScenario().onActivity(ProfileActivity::setLoggedUI);
        onView(withId(R.id.signOutButton)).perform(click());
        Thread.sleep(2000);
        Espresso.onView(withId(R.id.signInButton)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
    }
}
