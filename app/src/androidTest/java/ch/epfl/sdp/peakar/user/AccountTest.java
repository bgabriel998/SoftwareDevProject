package ch.epfl.sdp.peakar.user;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import ch.epfl.sdp.peakar.database.Database;
import ch.epfl.sdp.peakar.points.CountryHighPoint;
import ch.epfl.sdp.peakar.user.services.Account;
import ch.epfl.sdp.peakar.user.outcome.ProfileOutcome;
import ch.epfl.sdp.peakar.user.services.AuthService;
import ch.epfl.sdp.peakar.user.services.providers.firebase.FirebaseAuthService;
import ch.epfl.sdp.peakar.user.score.ScoringConstants;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static ch.epfl.sdp.peakar.TestingConstants.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class AccountTest {
    private static String user1;
    private static String user2;

    // Helper method, register a new test user using a certain random offset given as input
    public static void registerAuthUser() {
        // Generate user
        AuthService.getInstance().authAnonymously();
    }

    // Helper method, delete the current user
    public static void removeAuthUser() {
        Task<Void> dbTask = Database.refRoot.child(Database.CHILD_USERS).child(AuthService.getInstance().getID()).removeValue();
        Task<Void> fbTask = null;

        FirebaseUser oldUser = FirebaseAuth.getInstance().getCurrentUser();
        AuthService.getInstance().signOut(InstrumentationRegistry.getInstrumentation().getTargetContext());
        if(oldUser != null) fbTask = oldUser.delete();

        try {
            Tasks.await(dbTask);
            if(oldUser!=null) {
                Tasks.await(fbTask);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* Set up the environment */
    @BeforeClass
    public static void init() {
        /* Make sure no user is signed in before tests */
        AuthService.getInstance().signOut(InstrumentationRegistry.getInstrumentation().getTargetContext());


        /* Create a new one */
        registerAuthUser();

        user1 = ("test" + AuthService.getInstance().getID()).substring(0, Account.NAME_MAX_LENGTH - 1);
        user2 = ("test" + AuthService.getInstance().getID()).substring(0, Account.NAME_MAX_LENGTH - 2);
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
        Database.refRoot.child(Database.CHILD_USERS).child(AuthService.getInstance().getID()).removeValue();
        Database.refRoot.child(Database.CHILD_USERS).child(user2).removeValue();
        Thread.sleep(SHORT_SLEEP_TIME);
    }

    /**
     * Testing that account is null
     */
    @Test
    public void noAccountTest() {
        removeAuthUser();
        AuthService.getInstance().signOut(InstrumentationRegistry.getInstrumentation().getTargetContext());

        assertNull(AuthService.getInstance().getAuthAccount());
    }

    /**
     * Testing that account is null
     */
    @Test
    public void noRegisteredTest() {
        assertNotNull(AuthService.getInstance().getAuthAccount());
        assertEquals(Account.USERNAME_BEFORE_REGISTRATION, AuthService.getInstance().getAuthAccount().getUsername());
    }

    /**
     * Testing that changeUsername works fine
     */
    @Test
    public void changeUsernameTest() {
        /* Get account */
        Account account = AuthService.getInstance().getAuthAccount();

        // Assert basic username
        assertEquals(Account.USERNAME_BEFORE_REGISTRATION, AuthService.getInstance().getAuthAccount().getUsername());
        
        // Add username
        account.changeUsername(user1);
        assertEquals(user1, account.getUsername());
    }

    /**
     * Testing that addFriend works fine
     * @throws InterruptedException
     */
    @Test
    public void addFriendTest() throws InterruptedException {
        /* Get account */
        Account account = AuthService.getInstance().getAuthAccount();

        /* Add friend remotely */
        Database.setChild(Database.CHILD_USERS + user2, Collections.singletonList(Database.CHILD_USERNAME), Collections.singletonList(user2));
        Thread.sleep(LONG_SLEEP_TIME);

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
        Account account = AuthService.getInstance().getAuthAccount();

        account.setScore(USER_SCORE);
        assertEquals(USER_SCORE, account.getScore());
    }

    /**
     * Testing that retrieve data works fine
     * @throws InterruptedException
     */
    @Test
    public void synchronizeFriendsTest() throws InterruptedException {
        /* Get account */
        Account account = AuthService.getInstance().getAuthAccount();

        Database.setChild(Database.CHILD_USERS + AuthService.getInstance().getID(), Collections.singletonList(Database.CHILD_USERNAME), Collections.singletonList(user1));
        Database.setChild(Database.CHILD_USERS + user2, Collections.singletonList(Database.CHILD_USERNAME), Collections.singletonList(user2));
        Thread.sleep(SHORT_SLEEP_TIME);

        // Test if no friend is present
        assertTrue(account.getFriends().isEmpty());

        // Test if a friend is present
        Database.setChild(Database.CHILD_USERS + AuthService.getInstance().getID() + Database.CHILD_FRIENDS, Collections.singletonList(user2), Collections.singletonList(""));
        FirebaseAuthService.getInstance().forceRetrieveData();
        assertEquals(user2, account.getFriends().get(0).getUid());
    }

    /**
     * Testing that retrieve data works fine
     */
    @Test
    public void synchronizeUserScoreTest()throws InterruptedException{
        /* Get account */
        Account account = AuthService.getInstance().getAuthAccount();

        Database.setChild(Database.CHILD_USERS + AuthService.getInstance().getID(), Collections.singletonList(Database.CHILD_USERNAME), Collections.singletonList(user1));

        //Set user score to USER_SCORE
        DatabaseReference refAdd = Database.refRoot.child(Database.CHILD_USERS);
        refAdd.child(AuthService.getInstance().getID()).child(Database.CHILD_SCORE).setValue(USER_SCORE);
        Thread.sleep(SHORT_SLEEP_TIME);

        FirebaseAuthService.getInstance().forceRetrieveData();
        assertEquals(USER_SCORE, account.getScore());
    }


    /**
     * Test that the set and get discovered country high points works
     */
    @Test
    public void setAndGetDiscoveredCountryHighPoints() {
        /* Get account */
        Account account = AuthService.getInstance().getAuthAccount();

        CountryHighPoint newEntry = new CountryHighPoint("France","Mont Blanc",4810);
        account.setDiscoveredCountryHighPoint(newEntry);
        assertEquals(newEntry.toString(), account.getDiscoveredCountryHighPoint().get("France").toString());
    }


    /**
     * Tests the synchronization of discovered country high points
     * @throws InterruptedException
     */
    @Test
    public void synchronizeDiscoveredCountryHighPointsTest() throws InterruptedException{
        /* Get account */
        Account account = AuthService.getInstance().getAuthAccount();

        Database.setChild(Database.CHILD_USERS +  AuthService.getInstance().getID(), Collections.singletonList(Database.CHILD_USERNAME), Collections.singletonList(user1));
        CountryHighPoint newEntry = new CountryHighPoint("France","Mont Blanc",4810);

        //Set value to the database manually
        DatabaseReference refAdd = Database.refRoot.child(Database.CHILD_USERS);
        refAdd.child(AuthService.getInstance().getID()).child(Database.CHILD_COUNTRY_HIGH_POINT).push().setValue(newEntry);
        Thread.sleep(SHORT_SLEEP_TIME);

        FirebaseAuthService.getInstance().forceRetrieveData();
        String s1 = account.getDiscoveredCountryHighPoint().get("France").toString();
        String s2 = newEntry.toString();
        assertEquals(s2, s1);
    }

    /**
     * Test the synchronization of discovered heights
     * @throws InterruptedException
     */
    @Test
    public void synchronizeDiscoveredHeights() throws InterruptedException {
        /* Get account */
        Account account = AuthService.getInstance().getAuthAccount();

        Database.setChild(Database.CHILD_USERS +  AuthService.getInstance().getID(), Collections.singletonList(Database.CHILD_USERNAME), Collections.singletonList(user1));
        Thread.sleep(SHORT_SLEEP_TIME);

        //Set value to the database manually
        Integer entry1 = ScoringConstants.BADGE_1st_4000_M_PEAK;
        Integer entry2 = ScoringConstants.BADGE_1st_3000_M_PEAK;

        Database.setChildObject(Database.CHILD_USERS +   AuthService.getInstance().getID() + "/" +
                Database.CHILD_DISCOVERED_PEAKS_HEIGHTS,Collections.singletonList(entry1));
        Database.setChildObject(Database.CHILD_USERS +   AuthService.getInstance().getID() + "/" +
                Database.CHILD_DISCOVERED_PEAKS_HEIGHTS,Collections.singletonList(entry2));
        Thread.sleep(SHORT_SLEEP_TIME);

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
        for(String s: validStrings) assertTrue(s + " is not valid.", Account.checkUsernameValidity(s));
        for(String s: invalidStrings) assertFalse(s + " is valid.", Account.checkUsernameValidity(s));
    }

}
