package com.github.giommok.softwaredevproject;

import android.net.Uri;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class AccountTest {

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
        Database.setChild("users/null", Arrays.asList("username"), Arrays.asList("usernameTest3"));
        Thread.sleep(1000);
        Account account = Account.getAccount();
        // The uid used by synchronize username will be "null", the default value returned by getId() method.
        account.synchronizeUsername();
        Thread.sleep(1000);
        assertEquals(account.getUsername(), "usernameTest3");
        // Now it will test that if no data is present it produces a "null1" username
        Database.refRoot.child("users/null").child("username").removeValue();
        Thread.sleep(1000);
        assertEquals(account.getUsername(), "null1");
    }
}
