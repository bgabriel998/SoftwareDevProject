package com.github.bgabriel998.softwaredevproject;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.intent.matcher.IntentMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class CollectionActivityTest {

    @Rule
    public ActivityScenarioRule<CollectionActivity> testRule = new ActivityScenarioRule<>(CollectionActivity.class);

    @Before
    public void setup(){
        Intents.init();
    }

    @After
    public void cleanUp(){
        Intents.release();
    }

    /* Test that the toolbar title is set as expected */
    @Test
    public void TestToolbarTitle(){
        String TOOLBAR_TITLE = "Collections";
        ViewInteraction greetingText = Espresso.onView(withId(R.id.toolbarTitle));
        greetingText.check(matches(withText(TOOLBAR_TITLE)));
    }

    /* Test that pressing the collected item the view changes to MountainActivity */
    @Test
    public void TestCollectedItem(){
        //Intents.init();
        ViewInteraction button = Espresso.onView(withId(R.id.collected));
        button.perform(ViewActions.click());
        // Catch intent
        intended(IntentMatchers.hasComponent(MountainActivity.class.getName()));
        //Intents.release();
    }
}
