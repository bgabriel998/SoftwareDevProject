package ch.epfl.sdp.peakar.user;

import android.net.Uri;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.google.firebase.auth.FirebaseAuth;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ch.epfl.sdp.peakar.database.Database;
import ch.epfl.sdp.peakar.database.DatabaseReference;
import ch.epfl.sdp.peakar.points.CountryHighPoint;
import ch.epfl.sdp.peakar.user.outcome.ProfileOutcome;
import ch.epfl.sdp.peakar.user.score.ScoringConstants;
import ch.epfl.sdp.peakar.user.services.AuthAccount;
import ch.epfl.sdp.peakar.user.services.AuthService;
import ch.epfl.sdp.peakar.user.services.FirebaseAuthService;

import static ch.epfl.sdp.peakar.database.DatabaseTest.databaseRefRoot;
import static ch.epfl.sdp.peakar.utils.TestingConstants.SHORT_SLEEP_TIME;
import static ch.epfl.sdp.peakar.utils.TestingConstants.USER_SCORE;
import static ch.epfl.sdp.peakar.utils.UserTestHelper.registerAuthUser;
import static ch.epfl.sdp.peakar.utils.UserTestHelper.removeAuthUser;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class AuthAccountTest {
    private static String user1;
    private static String user2;

    /* Set up the environment */
    @BeforeClass
    public static void init() {
        /* Make sure no user is signed in before tests */
        AuthService.getInstance().signOut(InstrumentationRegistry.getInstrumentation().getTargetContext());


        /* Create a new one */
        registerAuthUser();

        user1 = ("test" + AuthService.getInstance().getID()).substring(0, AuthAccount.NAME_MAX_LENGTH - 1);
        user2 = ("test" + AuthService.getInstance().getID()).substring(0, AuthAccount.NAME_MAX_LENGTH - 2);
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
            AuthService.getInstance().signOut(InstrumentationRegistry.getInstrumentation().getTargetContext());
            registerAuthUser();
        }
        else FirebaseAuthService.getInstance().forceRetrieveData();
    }

    /* Make sure that mock users are not on the database after a test */
    @After
    public void removeTestUsers() throws InterruptedException {
        databaseRefRoot.child(Database.CHILD_USERS).child(AuthService.getInstance().getID()).removeValue();
        databaseRefRoot.child(Database.CHILD_USERS).child(user2).removeValue();
        Thread.sleep(SHORT_SLEEP_TIME);
    }

    /**
     * Testing that account is null
     */
    @Test
    public void noAccountTest() {
        // Assert EMPTY uri before sign out as the anonymous user has not a photo
        assertEquals(Uri.EMPTY, AuthService.getInstance().getPhotoUrl());

        removeAuthUser();
        AuthService.getInstance().signOut(InstrumentationRegistry.getInstrumentation().getTargetContext());

        assertNull(AuthService.getInstance().getAuthAccount());
        // Assert EMPTY uri after sign out as no user is signed in
        assertEquals(Uri.EMPTY, AuthService.getInstance().getPhotoUrl());
    }

    /**
     * Testing that account is null
     */
    @Test
    public void noRegisteredTest() {
        assertNotNull(AuthService.getInstance().getAuthAccount());
        assertEquals(AuthAccount.USERNAME_BEFORE_REGISTRATION, AuthService.getInstance().getAuthAccount().getUsername());
    }

    /**
     * Testing that changeUsername works fine
     */
    @Test
    public void changeUsernameTest() {
        /* Get account */
        AuthAccount account = AuthService.getInstance().getAuthAccount();

        // Assert basic username
        assertEquals(AuthAccount.USERNAME_BEFORE_REGISTRATION, AuthService.getInstance().getAuthAccount().getUsername());
        
        // Add username
        account.changeUsername(user1);
        assertEquals(user1, account.getUsername());
    }

    /**
     * Testing that addFriend works fine
     */
    @Test
    public void addFriendTest() {
        /* Get account */
        AuthAccount account = AuthService.getInstance().getAuthAccount();

        /* Add friend remotely */
        databaseRefRoot.child(Database.CHILD_USERS).child(user2).child(Database.CHILD_USERNAME).setValue(user2);

        // Test if no friend is present
        assertTrue(account.getFriends().isEmpty());

        // Test if a friend is present
        ProfileOutcome outcome = account.addFriend(user2);
        assertEquals(ProfileOutcome.FRIEND_ADDED, outcome);
        assertEquals(user2, account.getFriends().get(0).getUid());

        // Test if the friend is removed
        account.removeFriend(user2);
        assertTrue(account.getFriends().isEmpty());
    }

    /**
     * Test the set and get user score method. Check if the given input is effectively
     * written to the database
     */
    @Test
    public void setAndGetScoreTest() {
        /* Get account */
        AuthAccount account = AuthService.getInstance().getAuthAccount();

        account.setScore(USER_SCORE);
        assertEquals(USER_SCORE, account.getScore());
    }

    /**
     * Testing that retrieve data works fine
     */
    @Test
    public void synchronizeFriendsTest() {
        /* Get account */
        AuthAccount account = AuthService.getInstance().getAuthAccount();

        databaseRefRoot.child(Database.CHILD_USERS).child(AuthService.getInstance().getID()).child(Database.CHILD_USERNAME).setValue(user1);
        databaseRefRoot.child(Database.CHILD_USERS).child(user2).child(Database.CHILD_USERNAME).setValue(user2);

        // Test if no friend is present
        assertTrue(account.getFriends().isEmpty());

        // Test if a friend is present
        databaseRefRoot.child(Database.CHILD_USERS).child(AuthService.getInstance().getID()).child(Database.CHILD_FRIENDS).child(user2).setValue("");
        FirebaseAuthService.getInstance().forceRetrieveData();
        assertEquals(user2, account.getFriends().get(0).getUid());
    }

    /**
     * Testing that retrieve data works fine
     */
    @Test
    public void synchronizeUserScoreTest() {
        /* Get account */
        AuthAccount account = AuthService.getInstance().getAuthAccount();

        databaseRefRoot.child(Database.CHILD_USERS).child(AuthService.getInstance().getID()).child(Database.CHILD_USERNAME).setValue(user1);

        //Set user score to USER_SCORE
        DatabaseReference refAdd = databaseRefRoot.child(Database.CHILD_USERS);
        refAdd.child(AuthService.getInstance().getID()).child(Database.CHILD_SCORE).setValue(USER_SCORE);

        FirebaseAuthService.getInstance().forceRetrieveData();
        assertEquals(USER_SCORE, account.getScore());
    }


    /**
     * Test that the set and get discovered country high points works
     */
    @Test
    public void setAndGetDiscoveredCountryHighPoints() {
        /* Get account */
        AuthAccount account = AuthService.getInstance().getAuthAccount();

        CountryHighPoint newEntry = new CountryHighPoint("France","Mont Blanc",4810);
        account.setDiscoveredCountryHighPoint(newEntry);
        assertEquals(newEntry.toString(), account.getDiscoveredCountryHighPoint().get("France").toString());
    }


    /**
     * Tests the synchronization of discovered country high points
     */
    @Test
    public void synchronizeDiscoveredCountryHighPointsTest() {
        /* Get account */
        AuthAccount account = AuthService.getInstance().getAuthAccount();

        databaseRefRoot.child(Database.CHILD_USERS).child(AuthService.getInstance().getID()).child(Database.CHILD_USERNAME).setValue(user1);
        CountryHighPoint newEntry = new CountryHighPoint("France","Mont Blanc",4810);

        //Set value to the database manually
        DatabaseReference refAdd = databaseRefRoot.child(Database.CHILD_USERS);
        refAdd.child(AuthService.getInstance().getID()).child(Database.CHILD_COUNTRY_HIGH_POINT).push().setValue(newEntry);

        FirebaseAuthService.getInstance().forceRetrieveData();
        String s1 = account.getDiscoveredCountryHighPoint().get("France").toString();
        String s2 = newEntry.toString();
        assertEquals(s2, s1);
    }

    /**
     * Test the synchronization of discovered heights
     */
    @Test
    public void synchronizeDiscoveredHeights() {
        /* Get account */
        AuthAccount account = AuthService.getInstance().getAuthAccount();


        databaseRefRoot.child(Database.CHILD_USERS).child(AuthService.getInstance().getID()).child(Database.CHILD_USERNAME).setValue(user1);

        //Set value to the database manually
        Integer entry1 = ScoringConstants.BADGE_1st_4000_M_PEAK;
        Integer entry2 = ScoringConstants.BADGE_1st_3000_M_PEAK;

        databaseRefRoot.child(Database.CHILD_USERS).child(AuthService.getInstance().getID()).child(Database.CHILD_DISCOVERED_PEAKS_HEIGHTS).push().setValue(Collections.singletonList(entry1));
        databaseRefRoot.child(Database.CHILD_USERS).child(AuthService.getInstance().getID()).child(Database.CHILD_DISCOVERED_PEAKS_HEIGHTS).push().setValue(Collections.singletonList(entry2));

        FirebaseAuthService.getInstance().forceRetrieveData();
        assertTrue(account.getDiscoveredPeakHeights().contains(entry1) && account.getDiscoveredPeakHeights().contains(entry2));
    }
    
    /**
     * Testing that checkUsernameValidity recognizes valid strings and rejects invalid strings
     */
    @Test
    public void isValidTest() {
        List<String> invalidStrings = Arrays.asList("", null, " ", "ab", "@@@@", "1.Z" ,"....", "aaaaaaaaaaaaaaaa");
        List<String> validStrings = Arrays.asList("abc", "null", "123", "ab_", "____", "aaaaaaaaaaaaaaa");
        for(String s: validStrings) assertTrue(s + " is not valid.", AuthAccount.checkUsernameValidity(s));
        for(String s: invalidStrings) assertFalse(s + " is valid.", AuthAccount.checkUsernameValidity(s));
    }

}
