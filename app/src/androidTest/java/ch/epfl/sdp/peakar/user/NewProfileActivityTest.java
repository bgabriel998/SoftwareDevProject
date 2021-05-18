package ch.epfl.sdp.peakar.user;

import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.ListView;

import androidx.lifecycle.Lifecycle;
import androidx.test.espresso.DataInteraction;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import ch.epfl.sdp.peakar.R;
import ch.epfl.sdp.peakar.collection.NewCollectedItem;
import ch.epfl.sdp.peakar.database.Database;
import ch.epfl.sdp.peakar.points.POIPoint;
import ch.epfl.sdp.peakar.rankings.RankingItem;
import ch.epfl.sdp.peakar.user.profile.NewProfileActivity;
import ch.epfl.sdp.peakar.user.score.UserScore;
import ch.epfl.sdp.peakar.user.services.AuthAccount;
import ch.epfl.sdp.peakar.user.services.AuthService;
import ch.epfl.sdp.peakar.user.services.FirebaseAuthService;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withHint;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static ch.epfl.sdp.peakar.database.DatabaseTest.databaseRefRoot;
import static ch.epfl.sdp.peakar.user.AuthAccountTest.registerAuthUser;
import static ch.epfl.sdp.peakar.user.AuthAccountTest.removeAuthUser;
import static ch.epfl.sdp.peakar.utils.TestingConstants.AIGUILLE_DU_PLAN_ALT;
import static ch.epfl.sdp.peakar.utils.TestingConstants.AIGUILLE_DU_PLAN_LAT;
import static ch.epfl.sdp.peakar.utils.TestingConstants.AIGUILLE_DU_PLAN_LONG;
import static ch.epfl.sdp.peakar.utils.TestingConstants.AIGUILLE_DU_PLAN_NAME;
import static ch.epfl.sdp.peakar.utils.TestingConstants.BASIC_USERNAME;
import static ch.epfl.sdp.peakar.utils.TestingConstants.DENT_DU_GEANT_ALT;
import static ch.epfl.sdp.peakar.utils.TestingConstants.DENT_DU_GEANT_LAT;
import static ch.epfl.sdp.peakar.utils.TestingConstants.DENT_DU_GEANT_LONG;
import static ch.epfl.sdp.peakar.utils.TestingConstants.DENT_DU_GEANT_NAME;
import static ch.epfl.sdp.peakar.utils.TestingConstants.LONG_SLEEP_TIME;
import static ch.epfl.sdp.peakar.utils.TestingConstants.MONT_BLANC_ALT;
import static ch.epfl.sdp.peakar.utils.TestingConstants.MONT_BLANC_LAT;
import static ch.epfl.sdp.peakar.utils.TestingConstants.MONT_BLANC_LONG;
import static ch.epfl.sdp.peakar.utils.TestingConstants.MONT_BLANC_NAME;
import static ch.epfl.sdp.peakar.utils.TestingConstants.POINTE_DE_LAPAZ_ALT;
import static ch.epfl.sdp.peakar.utils.TestingConstants.POINTE_DE_LAPAZ_LAT;
import static ch.epfl.sdp.peakar.utils.TestingConstants.POINTE_DE_LAPAZ_LONG;
import static ch.epfl.sdp.peakar.utils.TestingConstants.POINTE_DE_LAPAZ_NAME;
import static ch.epfl.sdp.peakar.utils.TestingConstants.THREAD_SLEEP_5S;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertNotSame;

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
        FirebaseAuthService.getInstance().forceRetrieveData();
        testRule.getScenario().recreate();
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
        ViewInteraction changeUsernameText = Espresso.onView(withId(R.id.profile_username_edit));
        changeUsernameText.check(matches(withHint(R.string.insert_username_button)));
    }

    /* Test that if the username is already present the correct message is displayed */
    @Test
    public void usernameAlreadyPresentTest() {
        databaseRefRoot.child(Database.CHILD_USERS).child(user2).child(Database.CHILD_USERNAME).setValue(user2);
        onView(withId(R.id.profile_username_edit)).perform(replaceText(user2));
        InstrumentationRegistry.getInstrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_ENTER);
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
        onView(withId(R.id.profile_username_edit)).perform(replaceText(user1));
        InstrumentationRegistry.getInstrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_ENTER);
        try {
            Thread.sleep(LONG_SLEEP_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        onView(withText(R.string.registered_username)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
        onView(withId(R.id.profile_change_username)).perform(click());
        onView(withId(R.id.profile_username_edit)).perform(replaceText(user2));
        InstrumentationRegistry.getInstrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_ENTER);
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
        onView(withId(R.id.profile_username_edit)).perform(replaceText(user1));
        InstrumentationRegistry.getInstrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_ENTER);
        try {
            Thread.sleep(LONG_SLEEP_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        onView(withId(R.id.profile_change_username)).perform(click());
        onView(withId(R.id.profile_username_edit)).perform(replaceText(user1));
        InstrumentationRegistry.getInstrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_ENTER);
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

        onView(withId(R.id.profile_username_edit)).perform(replaceText(usernameTest));
        InstrumentationRegistry.getInstrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_ENTER);
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
        testRule.getScenario().recreate();
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

    /* Test that when you are not registered and you try to press on the screen, the focus does not disappear */
    @Test
    public void pressOnScreenButNotRegistered() {
        onView(withId(R.id.profile_picture)).perform(click());
        Espresso.onView(withId(R.id.profile_username)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
        Espresso.onView(withId(R.id.profile_username_edit)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
    }

    /* Test that collected mountains are sorted according to their points */
    @Test
    public void sortedByPoints_CollectedItemsTest() {
        // Prepare the account
        databaseRefRoot.child(Database.CHILD_USERS).child(AuthService.getInstance().getID()).child(Database.CHILD_USERNAME).setValue(user1);
        UserScore userScore = new UserScore(InstrumentationRegistry.getInstrumentation().getTargetContext());

        GeoPoint geoPoint_1 = new GeoPoint(MONT_BLANC_LAT,MONT_BLANC_LONG,MONT_BLANC_ALT);
        POIPoint point_1 = new POIPoint(geoPoint_1);
        point_1.setName(MONT_BLANC_NAME);

        GeoPoint geoPoint_2 = new GeoPoint(DENT_DU_GEANT_LAT, DENT_DU_GEANT_LONG,DENT_DU_GEANT_ALT);
        POIPoint point_2 = new POIPoint(geoPoint_2);
        point_2.setName(DENT_DU_GEANT_NAME);

        GeoPoint geoPoint_3 = new GeoPoint(AIGUILLE_DU_PLAN_LAT, AIGUILLE_DU_PLAN_LONG,AIGUILLE_DU_PLAN_ALT);
        POIPoint point_3 = new POIPoint(geoPoint_3);
        point_3.setName(AIGUILLE_DU_PLAN_NAME);

        GeoPoint geoPoint_4 = new GeoPoint(POINTE_DE_LAPAZ_LAT, POINTE_DE_LAPAZ_LONG,POINTE_DE_LAPAZ_ALT);
        POIPoint point_4 = new POIPoint(geoPoint_4);
        point_4.setName(POINTE_DE_LAPAZ_NAME);

        ArrayList<POIPoint> inputArrayList = new ArrayList<POIPoint>();
        inputArrayList.add(point_1);
        inputArrayList.add(point_2);
        inputArrayList.add(point_3);
        inputArrayList.add(point_4);

        userScore.updateUserScoreAndDiscoveredPeaks(inputArrayList);

        // Update the account
        FirebaseAuthService.getInstance().forceRetrieveData();
        testRule.getScenario().recreate();

        try {
            Thread.sleep(THREAD_SLEEP_5S);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Check correct order
        DataInteraction interaction = onData(instanceOf(NewCollectedItem.class));

        try {
            Thread.sleep(THREAD_SLEEP_5S);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        interaction.atPosition(0).onChildView(withId(R.id.collected_name)).check(matches(withText(MONT_BLANC_NAME)));
        interaction.atPosition(1).onChildView(withId(R.id.collected_name)).check(matches(withText(DENT_DU_GEANT_NAME)));
        interaction.atPosition(2).onChildView(withId(R.id.collected_name)).check(matches(withText(AIGUILLE_DU_PLAN_NAME)));
        interaction.atPosition(3).onChildView(withId(R.id.collected_name)).check(matches(withText(POINTE_DE_LAPAZ_NAME)));
    }
}
