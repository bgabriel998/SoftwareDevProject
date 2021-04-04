package com.github.giommok.softwaredevproject;

import android.net.Uri;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class AccountTest {

    /* Set up the environment */
    @BeforeClass
    public static void init() throws InterruptedException {
        /* Make sure that mock users are not on the database before the tests*/
        Database.refRoot.child("users").child("null").removeValue();
        Thread.sleep(1500);

        /* Make sure no user is signed in before a test */
        FirebaseAuth.getInstance().signOut();
        Thread.sleep(1500);
    }

    /* Make sure that mock users are not on the database after a test */
    @After
    public void removeTestUsers() throws InterruptedException {
        Database.refRoot.child("users").child("null").removeValue();
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
        Database.setChild("users/null", Arrays.asList("username"), Arrays.asList("username@Test"));
        Thread.sleep(1500);
        Account account = Account.getAccount();
        // The uid used by synchronize username will be "null", the default value returned by getId() method.
        account.synchronizeUsername();
        Thread.sleep(1500);
        assertEquals(account.getUsername(), "username@Test");
        // Now it will test that if no data is present it produces a "null" username
        Database.refRoot.child("users").child("null").removeValue();
        Thread.sleep(1500);
        assertEquals(account.getUsername(), "null");
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
