package com.github.giommok.softwaredevproject;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.google.firebase.FirebaseApp;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;


import java.util.Arrays;

@RunWith(AndroidJUnit4.class)
public class DatabaseTest {
    private static Context context;

    /* Set up the environment */
    @BeforeClass
    public static void init() throws InterruptedException {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        FirebaseApp.initializeApp(context);

        /* Make sure that mock users are not on the database before the tests*/
        Database.refRoot.child("users").child("null").removeValue();
        Thread.sleep(1500);
    }

    /* Make sure that mock users are not on the database after a test */
    @After
    public void removeTestUsers() throws InterruptedException {
        Database.refRoot.child("users").child("null").removeValue();
        Thread.sleep(1500);
    }

    /* Test that isPresent method works */
    @Test
    public void isPresentTest() throws InterruptedException {
        Database.setChild("users/null", Arrays.asList("username"), Arrays.asList("username@Test"));
        Thread.sleep(1500);
        Database.isPresent("users", "email", "dota2>lol", Assert::fail, () -> assertTrue("Correct behavior", true));
        Database.isPresent("users", "username", "username@Test", () -> assertTrue("Correct behavior", true),  Assert::fail);
        Thread.sleep(1500);
    }

    /* Test that setChild method works */
    @Test
    public void setChildTest() throws InterruptedException {
        Database.isPresent("users", "username", "testing@Value", Assert::fail, () -> assertTrue("Correct behavior", true));
        Thread.sleep(1500);
        Database.setChild("users/null", Arrays.asList("username"), Arrays.asList("testing@Value"));
        Thread.sleep(1500);
        Database.isPresent("users", "username", "testing@Value", () -> assertTrue("Correct behavior", true), Assert::fail);
        Thread.sleep(1500);
    }

    /* Test that exception is thrown if different list sizes are provided */
    @Test(expected = DatabaseException.class)
    public void differentSizes_SetChildTest() throws InterruptedException {
        Database.setChild("users/null", Arrays.asList("username", "error"), Arrays.asList("testingValue"));
        Thread.sleep(1000);
    }
}
