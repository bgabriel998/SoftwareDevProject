package com.github.bgabriel998.softwaredevproject;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.intent.matcher.IntentMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

@RunWith(AndroidJUnit4.class)
public class CameraActivityTest {

    @Rule
    public ActivityScenarioRule<CameraActivity> testRule = new ActivityScenarioRule<>(CameraActivity.class);

    /* Test that pressing the map icon button changes view to MapActivity */
    @Test
    public void TestMapIconButton(){
        Intents.init();
        ViewInteraction button = Espresso.onView(withId(R.id.mapButton));
        button.perform(ViewActions.click());
        // Catch intent
        intended(IntentMatchers.hasComponent(MapActivity.class.getName()));
        Intents.release();
    }
}
