package com.github.giommok.softwaredevproject;

import android.content.Context;
import android.net.Uri;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.Mockito.*;
import org.mockito.junit.MockitoJUnitRunner;


import java.util.Arrays;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class AccountTest {

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
