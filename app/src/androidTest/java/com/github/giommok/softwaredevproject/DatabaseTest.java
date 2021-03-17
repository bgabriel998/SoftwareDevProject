package com.github.giommok.softwaredevproject;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.google.firebase.FirebaseApp;

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


    @BeforeClass
    public static void init() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        FirebaseApp.initializeApp(context);
    }


    @Test
    public void isPresentTest() throws InterruptedException {

        Database.isPresent("users", "email", "dota2>lol", Assert::fail, () -> assertTrue("Correct behavior", true));
        Database.isPresent("users", "username", "usernameTest", () -> assertTrue("Correct behavior", true),  Assert::fail);
        Thread.sleep(2000);
    }


    @Test
    public void setChildTest() throws InterruptedException {
        Database.isPresent("users", "username", "testingValue", Assert::fail, () -> assertTrue("Correct behavior", true));
        Thread.sleep(1000);
        Database.setChild("users/test", Arrays.asList("username"), Arrays.asList("testingValue"));
        Thread.sleep(1000);
        Database.isPresent("users", "username", "testingValue", () -> assertTrue("Correct behavior", true), Assert::fail);
        Thread.sleep(1000);
        Database.setChild("users/test", Arrays.asList("username"), Arrays.asList("usernameTest"));
        Thread.sleep(1000);
    }

    @Test(expected = DatabaseException.class)
    public void differentSizes_SetChildTest() throws InterruptedException {
        Database.setChild("users/notexists", Arrays.asList("username", "error"), Arrays.asList("testingValue"));
        Thread.sleep(1000);
    }


}
