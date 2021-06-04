package ch.epfl.sdp.peakar.user;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.DataInteraction;
import androidx.test.espresso.Espresso;
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

import ch.epfl.sdp.peakar.R;
import ch.epfl.sdp.peakar.collection.NewCollectedItem;
import ch.epfl.sdp.peakar.database.Database;
import ch.epfl.sdp.peakar.database.DatabaseReference;
import ch.epfl.sdp.peakar.points.POIPoint;
<<<<<<< HEAD
import ch.epfl.sdp.peakar.user.profile.NewProfileActivity;
=======
import ch.epfl.sdp.peakar.social.SocialActivity;
import ch.epfl.sdp.peakar.user.profile.ProfileActivity;
>>>>>>> b0c68852dfefa9f6d64cb68a6f72f8e049fd1334
import ch.epfl.sdp.peakar.user.services.AuthAccount;
import ch.epfl.sdp.peakar.user.services.AuthService;
import ch.epfl.sdp.peakar.user.services.FirebaseAuthService;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static ch.epfl.sdp.peakar.database.DatabaseTest.databaseRefRoot;
import static ch.epfl.sdp.peakar.utils.UserTestHelper.registerAuthUser;
import static ch.epfl.sdp.peakar.utils.UserTestHelper.removeAuthUser;
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
import static ch.epfl.sdp.peakar.utils.UITestHelper.withDrawable;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertSame;

public class OtherProfileActivityTest {
    private static String user2;
    private static final int user2score = 100;

    private static Intent getOtherIntent() {
        registerAuthUser();
        DatabaseReference dbRef2 = Database.getInstance().getReference().child(Database.CHILD_USERS).child(user2);
        dbRef2.child(Database.CHILD_USERNAME).setValue(user2);
        dbRef2.child(Database.CHILD_SCORE).setValue(user2score);
        Intent intent = new Intent(InstrumentationRegistry.getInstrumentation().getTargetContext(), ProfileActivity.class);
        intent.putExtra(ProfileActivity.AUTH_INTENT, false);
        intent.putExtra(ProfileActivity.OTHER_INTENT, user2);

        // Prepare the account
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

        ArrayList<POIPoint> inputArrayList = new ArrayList<>();
        inputArrayList.add(point_1);
        inputArrayList.add(point_2);
        inputArrayList.add(point_3);
        inputArrayList.add(point_4);

        final DatabaseReference discoveredRef = Database.getInstance().getReference().child(Database.CHILD_USERS).child(user2).child(Database.CHILD_DISCOVERED_PEAKS);
        for(POIPoint poiPoint: inputArrayList) {
            discoveredRef.push().setValue(poiPoint);
        }

        return intent;
    }

    /* Set up the environment */
    @BeforeClass
    public static void init() {
        /* Make sure no user is signed in before tests */
        AuthService.getInstance().signOut(InstrumentationRegistry.getInstrumentation().getTargetContext());


        /* Create a new one */
        registerAuthUser();

        String user1 = (BASIC_USERNAME + AuthService.getInstance().getID()).substring(0, AuthAccount.NAME_MAX_LENGTH - 1);
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
    public ActivityScenarioRule<NewProfileActivity> testRule = new ActivityScenarioRule<>(getOtherIntent());

    /* Test that when another account profile is loaded with but no user is authenticated, the display is correct */
    @Test
    public void withNoAuthUserTest() {
        removeAuthUser();
        testRule.getScenario().recreate();

        try {
            Thread.sleep(LONG_SLEEP_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Espresso.onView(withId(R.id.profile_username)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
        Espresso.onView(withId(R.id.profile_username_edit)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
        Espresso.onView(withId(R.id.profile_add_friend)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
        Espresso.onView(withId(R.id.profile_remove_friend)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
        Espresso.onView(withId(R.id.profile_friend)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
        Espresso.onView(withId(R.id.profile_sign_out)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
    }

    /* Test that when another account profile is loaded with an auth user, the display is correct */
    @Test
    public void withAuthUserTest() {
        try {
            Thread.sleep(LONG_SLEEP_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Espresso.onView(withId(R.id.profile_username)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
        Espresso.onView(withId(R.id.profile_username_edit)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
        Espresso.onView(withId(R.id.profile_add_friend)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
        Espresso.onView(withId(R.id.profile_remove_friend)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
        Espresso.onView(withId(R.id.profile_friend)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
        Espresso.onView(withId(R.id.profile_sign_out)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
    }

    /* Test that the peaks collected by another user are displayed correctly */
    @Test
    public void otherCollectedPeaks() {
        try {
            Thread.sleep(LONG_SLEEP_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Check correct order
        DataInteraction interaction = onData(instanceOf(NewCollectedItem.class));

        interaction.atPosition(0).onChildView(withId(R.id.collected_name)).check(matches(withText(MONT_BLANC_NAME)));
        interaction.atPosition(1).onChildView(withId(R.id.collected_name)).check(matches(withText(DENT_DU_GEANT_NAME)));
        interaction.atPosition(2).onChildView(withId(R.id.collected_name)).check(matches(withText(AIGUILLE_DU_PLAN_NAME)));
        interaction.atPosition(3).onChildView(withId(R.id.collected_name)).check(matches(withText(POINTE_DE_LAPAZ_NAME)));

        interaction.atPosition(0).onChildView(withId(R.id.collected_country)).check(matches(withDrawable(R.drawable.country_france)));
        interaction.atPosition(1).onChildView(withId(R.id.collected_country)).check(matches(withDrawable(R.drawable.country_france)));
        interaction.atPosition(2).onChildView(withId(R.id.collected_country)).check(matches(withDrawable(R.drawable.country_france)));
        interaction.atPosition(3).onChildView(withId(R.id.collected_country)).check(matches(withDrawable(R.drawable.country_france)));
    }

    /* Test that the other user can be added as a friend */
    @Test
    public void addFriendTest() {
        String addedString = user2 + " " + ApplicationProvider.getApplicationContext().getResources().getString(R.string.friend_added);

        try {
            Thread.sleep(LONG_SLEEP_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        onView(withId(R.id.profile_add_friend)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
        onView(withId(R.id.profile_add_friend)).perform(click());

        try {
            Thread.sleep(LONG_SLEEP_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        onView(withText(addedString)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
        onView(withId(R.id.profile_remove_friend)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
    }

    /* Test that the other user can be removed from the friends */
    @Test
    public void removeFriendTest() {
        String addedString = user2 + " " + ApplicationProvider.getApplicationContext().getResources().getString(R.string.friend_removed);
        AuthService.getInstance().getAuthAccount().addFriend(user2);
        try {
            Thread.sleep(LONG_SLEEP_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        FirebaseAuthService.getInstance().forceRetrieveData();
        testRule.getScenario().recreate();

        try {
            Thread.sleep(LONG_SLEEP_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(withId(R.id.profile_remove_friend)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
        onView(withId(R.id.profile_remove_friend)).perform(click());

        try {
            Thread.sleep(LONG_SLEEP_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(withText(addedString)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
        onView(withId(R.id.profile_add_friend)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
    }

    /* Test that when you click on the social activity button, social activity is started */
    @Test
    public void socialActivityButtonTest() {
        onView(withId(R.id.profile_friend)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
        onView(withId(R.id.profile_friend)).perform(click());
        assertSame(testRule.getScenario().getResult().getResultCode(), Activity.RESULT_CANCELED);
    }
}