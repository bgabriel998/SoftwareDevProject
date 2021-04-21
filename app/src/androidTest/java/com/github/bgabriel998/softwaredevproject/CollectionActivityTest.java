package com.github.bgabriel998.softwaredevproject;

import android.app.Activity;
import android.widget.TextView;

import androidx.test.espresso.DataInteraction;
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

import java.util.Locale;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class CollectionActivityTest {

    @Rule
    public ActivityScenarioRule<CollectionActivity> testRule = new ActivityScenarioRule<>(CollectionActivity.class);

    /* Create Intent */
    @Before
    public void setup(){
        Intents.init();
    }

    /* Release Intent */
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

    /* Test that the activity finishes when the toolbar back button is pressed. */
    @Test
    public void TestToolbarBackButton(){
        onView(withId(R.id.toolbarBackButton)).perform(click());
        try {
            Thread.sleep(1000);
            assertSame(testRule.getScenario().getResult().getResultCode(), Activity.RESULT_CANCELED);
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail("TestToolbarBackButton failed");
        }
    }

    /* Test that all elements in list view are at correct place and contains correct data */
    @Test
    public void TestContentOfListView(){
        // TODO Redo test when actually using database.
        DataInteraction interaction =  onData(instanceOf(CollectedItem.class));

        for (int i = 0; i < 20; i++){
            DataInteraction listItem = interaction.atPosition(i);

            String name = String.format(Locale.getDefault(),"Mont Blanc - Monte Bianco%d", i);
            String points = String.format("%d", 100 - i);

            listItem.onChildView(withId(R.id.collected_name))
                    .check(matches(withText(name)));
            listItem.onChildView(withId(R.id.collected_points))
                    .check(matches(withText(points)));
        }
    }

    @Test
    public void TestPressCollected() {
        // Item at pos 10 looks like this
        CollectedItem correctItem = new CollectedItem(
                String.format(Locale.getDefault(),"Mont Blanc - Monte Bianco%d", 10),
                100-10,
                1000-10,
                50+10,
                50-10);

        // Get Item at pos 10 and click.
        DataInteraction listItem = onData(instanceOf(CollectedItem.class)).atPosition(10);
        listItem.perform(ViewActions.click());

        // Catch intent, and check information
        intended(allOf(IntentMatchers.hasComponent(MountainActivity.class.getName()),
                        IntentMatchers.hasExtra("name", correctItem.getName()),
                        IntentMatchers.hasExtra("points", correctItem.getPoints()),
                        IntentMatchers.hasExtra("height", correctItem.getHeight()),
                        IntentMatchers.hasExtra("longitude", correctItem.getLongitude()),
                        IntentMatchers.hasExtra("latitude", correctItem.getLatitude())));
    }
}
