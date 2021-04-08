package com.github.giommok.softwaredevproject;

import android.net.Uri;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.firebase.auth.FirebaseAuth;


import com.google.firebase.database.DatabaseReference;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class AccountTest {

    /* Set up the environment */
    @BeforeClass
    public static void init() throws InterruptedException {
        /* Make sure that mock users are not on the database before the tests*/
        Database.refRoot.child(Database.CHILD_USERS).child("null").removeValue();
        Thread.sleep(1500);

        /* Make sure no user is signed in before a test */
        FirebaseAuth.getInstance().signOut();
        Account.getAccount().synchronizeUserProfile();
        Thread.sleep(1500);
    }

    /* Make sure that mock users are not on the database after a test */
    @After
    public void removeTestUsers() throws InterruptedException {
        Database.refRoot.child(Database.CHILD_USERS).child("null").removeValue();
        Thread.sleep(1500);
    }


    /**
     * Testing the output are not null but actual strings with no account
     */
    @Test
    public void noAccountTest() {
        Account account = Account.getAccount();

        assertFalse(account.isSignedIn());
        assertEquals(account.getDisplayName(), "null");
        assertEquals(account.getEmail(), "null");
        assertEquals(account.getId(), "null");
        assertEquals(account.getProviderId(), "null");
        assertEquals(account.getPhotoUrl(), Uri.EMPTY);
    }

    /**
     * Testing that synchronizeUsername works fine
     * @throws InterruptedException
     */
    @Test
    public void synchronizeUsernameTest() throws InterruptedException {
        Database.setChild(Database.CHILD_USERS+"/null", Arrays.asList(Database.CHILD_USERNAME), Arrays.asList("username@Test"));
        Thread.sleep(1500);
        Account account = Account.getAccount();
        // The uid used by synchronize username will be "null", the default value returned by getId() method.
        account.synchronizeUserProfile();
        Thread.sleep(1500);
        assertEquals("username@Test", account.getUsername());
        // Now it will test that if no data is present it produces a "null" username
        Database.refRoot.child(Database.CHILD_USERS).child("null").removeValue();
        Thread.sleep(2000);
        assertEquals("null",account.getUsername());
    }

    /**
     * Test the set and get user score method. Check if the given input is effectively
     * written to the database
     */
    @Test
    public void setAndGetUserScoreTest() throws InterruptedException{
        Database.setChild(Database.CHILD_USERS+"/null", Collections.singletonList(Database.CHILD_USERNAME), Collections.singletonList("usernameTest4"));

        Thread.sleep(1000);
        Account account = Account.getAccount();
        account.setUserScore(200000);
        Thread.sleep(1000);
        assertEquals(account.getUserScore(),200000);


    }

    /**
     * test user score synchronization between database and account class
     */
    @Test
    public void synchronizeUserScoreTest()throws InterruptedException{
        // To be sure that null user does not exists
        Database.refRoot.child(Database.CHILD_USERS).child("null").removeValue();
        Thread.sleep(1000);
        Database.setChild(Database.CHILD_USERS+"/null", Collections.singletonList(Database.CHILD_USERNAME), Collections.singletonList("usernameTest3"));

        //Set user score to zero
        Account account = Account.getAccount();
        DatabaseReference refAdd = Database.refRoot.child(Database.CHILD_USERS);
        refAdd.child(account.getId()).child(Database.CHILD_SCORE).setValue(0);
        Thread.sleep(1000);

        account.synchronizeUserProfile();
        refAdd.child(account.getId()).child(Database.CHILD_SCORE).setValue(200000);

        Thread.sleep(1000);

        assertEquals(200000,account.getUserScore());

    }


    /**
     * Test that the set and get discovered country high points effectively writes and retrieves
     * data to/from the database
     */
    @Test
    public void setAndGetDiscoveredCountryHighPoints()throws InterruptedException{
        Database.setChild(Database.CHILD_USERS+"/null", Collections.singletonList(Database.CHILD_USERNAME), Collections.singletonList("usernameTest4"));
        Thread.sleep(1000);
        Account account = Account.getAccount();
        CountryHighPoint newEntry = new CountryHighPoint("France","Mont Blanc",4810);
        account.setDiscoveredCountryHighPoint(newEntry);
        Thread.sleep(1000);
        assertEquals(newEntry.toString(), account.getDiscoveredCountryHighPoint().get("France").toString());
    }


    /**
     * Tests the synchronization of discovered country high points
     * @throws InterruptedException
     */
    @Test
    public void synchronizeDiscoveredCountryHighPointsTest() throws InterruptedException{
        Database.setChild(Database.CHILD_USERS + "/null", Collections.singletonList(Database.CHILD_USERNAME), Collections.singletonList("usernameTest4"));
        Thread.sleep(1000);
        Account account = Account.getAccount();
        CountryHighPoint newEntry = new CountryHighPoint("France","Mont Blanc",4810);
        //Add sync call back
        account.synchronizeUserProfile();

        Thread.sleep(1000);
        //Set value to the database manually
        DatabaseReference refAdd = Database.refRoot.child(Database.CHILD_USERS);
        refAdd.child("null").child(Database.CHILD_COUNTRY_HIGH_POINT).push().setValue(newEntry);

        Thread.sleep(1000);
        String s1 = account.getDiscoveredCountryHighPoint().get("France").toString();
        String s2 = newEntry.toString();
        assertEquals(s2,s1);
    }

    @Test
    public void synchronizeDiscoveredHeights() throws InterruptedException {
        Database.setChild(Database.CHILD_USERS + "/null", Collections.singletonList(Database.CHILD_USERNAME), Collections.singletonList("usernameTest4"));
        Thread.sleep(1000);
        Account account = Account.getAccount();
        //Add sync call back
        account.synchronizeUserProfile();

        Thread.sleep(1000);
        //Set value to the database manually
        Integer entry1 = ScoringConstants.BADGE_1st_4000_M_PEAK;
        Integer entry2 = ScoringConstants.BADGE_1st_3000_M_PEAK;
        DatabaseReference refAdd = Database.refRoot.child(Database.CHILD_USERS);

        Database.setChildObject(Database.CHILD_USERS +  "null" +
                Database.CHILD_DISCOVERED_PEAKS_HEIGHTS,Collections.singletonList(entry1));
        Database.setChildObject(Database.CHILD_USERS +  "null" +
                Database.CHILD_DISCOVERED_PEAKS_HEIGHTS,Collections.singletonList(entry2));

        Thread.sleep(1000);
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
