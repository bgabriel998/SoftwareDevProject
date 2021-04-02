package com.github.giommok.softwaredevproject;

import android.net.Uri;

import androidx.test.espresso.core.internal.deps.guava.cache.Cache;
import androidx.test.ext.junit.runners.AndroidJUnit4;


import com.google.firebase.database.DatabaseReference;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class AccountTest {

    /* Make sure that mock users are not on the database after a test */
    @After
    public void removeTestUsers() throws InterruptedException {
        Database.refRoot.child("users").child("null").removeValue();
        Thread.sleep(500);
    }











        //Remove test child






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
        Database.setChild("users/null", Arrays.asList("username"), Arrays.asList("username@Test"));
        Thread.sleep(1000);
        Account account = Account.getAccount();
        // The uid used by synchronize username will be "null", the default value returned by getId() method.
        account.synchronizeUsername();
        Thread.sleep(1000);
        assertEquals(account.getUsername(), "username@Test");
        // Now it will test that if no data is present it produces a "null" username
        Database.refRoot.child("users").child("null").removeValue();
        Thread.sleep(1000);
        assertEquals(account.getUsername(), "null");
    }

    /**
     * Test the set and get user score method. Check if the given input is effectively
     * written to the database
     */
    @Test
    public void setAndGetUserScoreTest() throws InterruptedException{
        Database.setChild("users/null", Arrays.asList("username"), Arrays.asList("username@Test"));

        Thread.sleep(1000);
        Account account = Account.getAccount();
        account.setUserScore(200000);
        Thread.sleep(1000);
        assertEquals(account.getUserScore(),200000);
        //Remove test child
    }

    /**
     * test user score synchronization between database and account class
     */
    @Test
    public void synchronizeUserScoreTest()throws InterruptedException{




        Database.setChild("users/null", Arrays.asList("username"), Arrays.asList("usernameTest3"));

        //Set user score to zero
        Account account = Account.getAccount();
        DatabaseReference refAdd = Database.refRoot.child("users/");
        refAdd.child(account.getId()).child("score").setValue(0);
        Thread.sleep(1000);

        account.synchronizeUserScore();
        refAdd.child(account.getId()).child("score").setValue(200000);

        Thread.sleep(4000);
        assertEquals(account.getUserScore(), 200000);

    }


    /**
     * Test that the set and get discovered country high points effectively writes and retrieves
     * data to/from the database
     */
    @Test
    public void setAndGetDiscoveredCountryHighPoints()throws InterruptedException{
        Database.setChild("users/null", Arrays.asList("username"), Arrays.asList("username@Test"));

        Thread.sleep(1000);
        Account account = Account.getAccount();
        CacheEntry newEntry = new CacheEntry("France","Mont Blanc",4810);
        account.setDiscoveredCountryHighPoint(newEntry);
        Thread.sleep(1000);
        assertEquals(account.getDiscoveredCountryHighPoint().get("France").toString(),newEntry.toString());
    }


    @Test
    public void synchronizeDiscoveredCountryHighPointsTest() throws InterruptedException{
        Database.setChild("users/null", Arrays.asList("username"), Arrays.asList("username@Test"));

        Thread.sleep(1000);
        Account account = Account.getAccount();
        CacheEntry newEntry = new CacheEntry("France","Mont Blanc",4810);
        //Add sync call back
        account.synchronizeDiscoveredCountryHighPoints();

        Thread.sleep(1000);
        //Set value to the database manually
        DatabaseReference refAdd = Database.refRoot.child("users/");
        refAdd.child("null").child("CountryHighPoint").push().setValue(newEntry);

        Thread.sleep(1000);
        String s1 = account.getDiscoveredCountryHighPoint().get("France").toString();
        String s2 = newEntry.toString();
        assertEquals(s1,s2);
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
