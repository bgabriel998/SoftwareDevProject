package com.github.giommok.softwaredevproject;

import android.net.Uri;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.firebase.auth.FirebaseAuth;
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
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class AccountTest {
    public final static String BASIC_USERNAME = "username@test";
    public final static int SHORT_SLEEP_TIME = 500;
    public final static int LONG_SLEEP_TIME = 1500;
    public final static int USER_SCORE = 200000;

    private static final int USER_OFFSET = new Random().nextInt();

    private static final String user1 = BASIC_USERNAME + USER_OFFSET;;
    private static final String user2 = BASIC_USERNAME + USER_OFFSET + 1;

    // Helper method, register a new test user using a certain random offset given as input
    public static void registerTestUser() {
        Account account = Account.getAccount();

        // Generate user
        FirebaseAuth.getInstance().signInAnonymously();
        try {
            Thread.sleep(LONG_SLEEP_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // Synchronize
        account.synchronizeUserProfile();
        try {
            Thread.sleep(LONG_SLEEP_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Helper method, delete the current user
    public static void removeTestUser() {
        // If no account is signed, stop
        if(!Account.getAccount().isSignedIn()) return;

        // Otherwise
        Database.refRoot.child(Database.CHILD_USERS).child(Account.getAccount().getId()).removeValue();
        FirebaseAuth.getInstance().getCurrentUser().delete();

        try {
            Thread.sleep(LONG_SLEEP_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Account.getAccount().synchronizeUserProfile();

        try {
            Thread.sleep(LONG_SLEEP_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /* Set up the environment */
    @BeforeClass
    public static void init() throws InterruptedException {
        /* Make sure no user is signed in before a test */
        FirebaseAuth.getInstance().signOut();
        Thread.sleep(SHORT_SLEEP_TIME);

        Account.getAccount().synchronizeUserProfile();
        Thread.sleep(SHORT_SLEEP_TIME);
    }

    /* Clean environment */
    @AfterClass
    public static void end() {
        removeTestUser();
    }

    /* Make sure that an account is signed in before each test */
    @Before
    public void createTestUser() {
        if(!Account.getAccount().isSignedIn()) registerTestUser();
    }

    /* Make sure that mock users are not on the database after a test */
    @After
    public void removeTestUsers() throws InterruptedException {
        Database.refRoot.child(Database.CHILD_USERS).child(Account.getAccount().getId()).removeValue();
        Database.refRoot.child(Database.CHILD_USERS).child(user2).removeValue();
        Thread.sleep(SHORT_SLEEP_TIME);
    }

    /**
     * Testing the output are not null but actual strings with no account
     */
    @Test
    public void noAccountTest() {
        removeTestUser();
        Account account = Account.getAccount();

        assertFalse(account.isSignedIn());
        assertTrue(account.getFriends().isEmpty());
        assertEquals("null", account.getDisplayName());
        assertEquals("null", account.getEmail());
        assertEquals("null", account.getId());
        assertEquals("null", account.getProviderId());
        assertEquals(Uri.EMPTY, account.getPhotoUrl());
    }

    /**
     * Testing that synchronizeUsername works fine
     * @throws InterruptedException
     */
    @Test
    public void synchronizeUsernameTest() throws InterruptedException {
        /* Get account */
        Account account = Account.getAccount();

        // Add username
        Database.setChild(Database.CHILD_USERS + account.getId(), Collections.singletonList(Database.CHILD_USERNAME), Collections.singletonList(user1));
        Thread.sleep(SHORT_SLEEP_TIME);


        // Check username
        Thread.sleep(SHORT_SLEEP_TIME);
        assertEquals(user1, account.getUsername());

        // Now it will test that if username is removed, it produces a "null" username
        Database.refRoot.child(Database.CHILD_USERS).child(account.getId()).removeValue();
        Thread.sleep(SHORT_SLEEP_TIME);
        assertEquals("null", account.getUsername());
    }

    /**
     * Testing that synchronizeFriends works fine
     * @throws InterruptedException
     */
    @Test
    public void synchronizeFriendsTest() throws InterruptedException {
        /* Get account */
        Account account = Account.getAccount();

        Database.setChild(Database.CHILD_USERS + account.getId(), Collections.singletonList(Database.CHILD_USERNAME), Collections.singletonList(user1));
        Database.setChild(Database.CHILD_USERS + user2, Collections.singletonList(Database.CHILD_USERNAME), Collections.singletonList(user2));
        Thread.sleep(SHORT_SLEEP_TIME);

        // Test if no friend is present
        assertTrue(account.getFriends().isEmpty());

        // Test if a friend is present
        Database.setChild(Database.CHILD_USERS + account.getId() + Database.CHILD_FRIENDS, Collections.singletonList(user2), Collections.singletonList(""));
        Thread.sleep(SHORT_SLEEP_TIME);
        assertEquals(user2, account.getFriends().get(0).getUsername());

        // Test if the friend is removed
        Database.refRoot.child(Database.CHILD_USERS + account.getId() + Database.CHILD_FRIENDS).removeValue();
        Thread.sleep(SHORT_SLEEP_TIME);
        assertTrue(account.getFriends().isEmpty());
    }

    /**
     * Test the set and get user score method. Check if the given input is effectively
     * written to the database
     */
    @Test
    public void setAndGetUserScoreTest() throws InterruptedException{
        /* Get account */
        Account account = Account.getAccount();

        Database.setChild(Database.CHILD_USERS + account.getId(), Collections.singletonList(Database.CHILD_USERNAME), Collections.singletonList(user1));
        Thread.sleep(SHORT_SLEEP_TIME);

        account.setUserScore(USER_SCORE);
        Thread.sleep(SHORT_SLEEP_TIME);
        assertEquals(USER_SCORE, account.getUserScore());
    }

    /**
     * test user score synchronization between database and account class
     */
    @Test
    public void synchronizeUserScoreTest()throws InterruptedException{
        /* Get account */
        Account account = Account.getAccount();
        Thread.sleep(SHORT_SLEEP_TIME);

        Database.setChild(Database.CHILD_USERS + account.getId(), Collections.singletonList(Database.CHILD_USERNAME), Collections.singletonList(user1));

        //Set user score to zero
        DatabaseReference refAdd = Database.refRoot.child(Database.CHILD_USERS);
        refAdd.child(account.getId()).child(Database.CHILD_SCORE).setValue(0);
        Thread.sleep(SHORT_SLEEP_TIME);


        refAdd.child(account.getId()).child(Database.CHILD_SCORE).setValue(USER_SCORE);
        Thread.sleep(SHORT_SLEEP_TIME);
        assertEquals(USER_SCORE, account.getUserScore());

    }


    /**
     * Test that the set and get discovered country high points effectively writes and retrieves
     * data to/from the database
     */
    @Test
    public void setAndGetDiscoveredCountryHighPoints()throws InterruptedException{
        /* Get account */
        Account account = Account.getAccount();
        Thread.sleep(SHORT_SLEEP_TIME);

        Database.setChild(Database.CHILD_USERS + account.getId(), Collections.singletonList(Database.CHILD_USERNAME), Collections.singletonList(user1));
        Thread.sleep(SHORT_SLEEP_TIME);

        CountryHighPoint newEntry = new CountryHighPoint("France","Mont Blanc",4810);
        account.setDiscoveredCountryHighPoint(newEntry);
        Thread.sleep(SHORT_SLEEP_TIME);

        assertEquals(newEntry.toString(), account.getDiscoveredCountryHighPoint().get("France").toString());
    }


    /**
     * Tests the synchronization of discovered country high points
     * @throws InterruptedException
     */
    @Test
    public void synchronizeDiscoveredCountryHighPointsTest() throws InterruptedException{
        /* Get account */
        Account account = Account.getAccount();
        Thread.sleep(SHORT_SLEEP_TIME);

        Database.setChild(Database.CHILD_USERS + account.getId(), Collections.singletonList(Database.CHILD_USERNAME), Collections.singletonList(user1));
        CountryHighPoint newEntry = new CountryHighPoint("France","Mont Blanc",4810);
        Thread.sleep(SHORT_SLEEP_TIME);

        //Set value to the database manually
        DatabaseReference refAdd = Database.refRoot.child(Database.CHILD_USERS);
        refAdd.child(account.getId()).child(Database.CHILD_COUNTRY_HIGH_POINT).push().setValue(newEntry);

        Thread.sleep(SHORT_SLEEP_TIME);
        String s1 = account.getDiscoveredCountryHighPoint().get("France").toString();
        String s2 = newEntry.toString();
        assertEquals(s2, s1);
    }

    @Test
    public void synchronizeDiscoveredHeights() throws InterruptedException {
        /* Get account */
        Account account = Account.getAccount();
        Thread.sleep(SHORT_SLEEP_TIME);

        Database.setChild(Database.CHILD_USERS + account.getId(), Collections.singletonList(Database.CHILD_USERNAME), Collections.singletonList(user1));
        Thread.sleep(SHORT_SLEEP_TIME);

        //Set value to the database manually
        Integer entry1 = ScoringConstants.BADGE_1st_4000_M_PEAK;
        Integer entry2 = ScoringConstants.BADGE_1st_3000_M_PEAK;

        Database.setChildObject(Database.CHILD_USERS +  account.getId() + "/" +
                Database.CHILD_DISCOVERED_PEAKS_HEIGHTS,Collections.singletonList(entry1));
        Database.setChildObject(Database.CHILD_USERS +  account.getId() + "/" +
                Database.CHILD_DISCOVERED_PEAKS_HEIGHTS,Collections.singletonList(entry2));

        Thread.sleep(SHORT_SLEEP_TIME);
        assertTrue(account.getDiscoveredPeakHeights().contains(entry1) && account.getDiscoveredPeakHeights().contains(entry2));
    }
    
    /**
     * Testing that isValid recognizes valid strings and rejects invalid strings
     */
    @Test
    public void isValidTest() {
        List<String> invalidStrings = Arrays.asList("", null, " ", "ab", "@@@@", "1.Z" ,"....", "aaaaaaaaaaaaaaaa");
        List<String> validStrings = Arrays.asList("abc", "null", "123", "ab_", "____", "aaaaaaaaaaaaaaa");
        for(String s: validStrings) assertTrue(s + " is not valid.", Account.isValid(s));
        for(String s: invalidStrings) assertFalse(s + " is valid.", Account.isValid(s));
    }

}
