package com.github.bgabriel998.softwaredevproject;

import android.Manifest;

import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.intent.matcher.IntentMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {
    @Rule
    public ActivityScenarioRule<MainActivity> testRule = new ActivityScenarioRule<>(MainActivity.class);
    @Rule
    public GrantPermissionRule grantCameraPermissionRule = GrantPermissionRule.grant(Manifest.permission.CAMERA);
    @Rule
    public GrantPermissionRule grantLocation1PermissionRule = GrantPermissionRule.grant(Manifest.permission.ACCESS_FINE_LOCATION);
    @Rule
    public GrantPermissionRule grantLocation2PermissionRule = GrantPermissionRule.grant(Manifest.permission.ACCESS_COARSE_LOCATION);

    @Before
    public void setup(){
        Intents.init();
    }

    @After
    public void cleanUp(){
        Intents.release();
    }

    @Test
    public void intentIsFiredWhenUserClicksOnButton1() {
        //Intents.init();

        onView(withId(R.id.button1)).perform(click());
        Intents.intended(IntentMatchers.hasComponent(Button1Activity.class.getName()));
        //Intents.release();
    }

    @Test
    public void intentIsFiredWhenUserClicksOnButton2() {
        //Intents.init();

        onView(withId(R.id.button2)).perform(click());
        Intents.intended(IntentMatchers.hasComponent(Button2Activity.class.getName()));
        //Intents.release();
    }

    @Test
    public void intentIsFiredWhenUserClicksOnButton3() {
        //Intents.init();

        onView(withId(R.id.button3)).perform(click());
        Intents.intended(IntentMatchers.hasComponent(Button3Activity.class.getName()));
        //Intents.release();
    }

    @Test
    public void intentIsFiredWhenUserClicksOnButton4() {
        //Intents.init();

        onView(withId(R.id.button4)).perform(click());
        Intents.intended(IntentMatchers.hasComponent(Button4Activity.class.getName()));
        //Intents.release();
    }

    @Test
    public void intentIsFiredWhenUserClicksOnButton5() {
        //Intents.init();

        onView(withId(R.id.button5)).perform(click());
        Intents.intended(IntentMatchers.hasComponent(Button5Activity.class.getName()));
        //Intents.release();
    }

    @Test
    public void intentIsFiredWhenUserClicksOnButton6() {
        //Intents.init();

        onView(withId(R.id.button6)).perform(click());
        Intents.intended(IntentMatchers.hasComponent(Button6Activity.class.getName()));
        //Intents.release();
    }

    @Test
    public void intentIsFiredWhenUserClicksOnButton7() {
        //Intents.init();

        onView(withId(R.id.button7)).perform(click());
        Intents.intended(IntentMatchers.hasComponent(Button7Activity.class.getName()));
        //Intents.release();
    }

    @Test
    public void intentIsFiredWhenUserClicksOnButton8() {
        //Intents.init();

        onView(withId(R.id.button8)).perform(click());
        Intents.intended(IntentMatchers.hasComponent(Button8Activity.class.getName()));
        //Intents.release();
    }
}

