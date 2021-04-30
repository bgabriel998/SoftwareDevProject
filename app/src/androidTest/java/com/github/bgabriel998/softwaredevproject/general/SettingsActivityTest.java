package com.github.bgabriel998.softwaredevproject.general;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.github.bgabriel998.softwaredevproject.R;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicReference;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class SettingsActivityTest{

    @Rule
    public ActivityScenarioRule<SettingsActivity> testRule = new ActivityScenarioRule<>(SettingsActivity.class);
    private Activity activity;

    private static Context context = null;
    /**
     * Reset all shared preferences
     */
    @BeforeClass
    public static void setupClass(){
        context = ApplicationProvider.getApplicationContext();
        SharedPreferences preferences = context.getSharedPreferences("measSys_preference",
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear().commit();
        preferences = context.getSharedPreferences("range_preference",
                Context.MODE_PRIVATE);
        editor = preferences.edit();
        editor.clear().commit();
        preferences = context.getSharedPreferences("language_preference",
                Context.MODE_PRIVATE);
        editor = preferences.edit();
        editor.clear().commit();
        preferences = context.getSharedPreferences("disable_caching",
                Context.MODE_PRIVATE);
        editor = preferences.edit();
        editor.clear().commit();
        preferences = context.getSharedPreferences("offline_mode_key",
                Context.MODE_PRIVATE);
        editor = preferences.edit();
        editor.clear().commit();
    }


    @Before
    public void init(){
        activity = getActivity(testRule);
    }



    /*Test discovery distance button*/
    @Test
    public void TestDiscoveryDistanceButton() throws InterruptedException {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        //Open selector window
        onView(withId(androidx.preference.R.id.recycler_view))
                .perform(RecyclerViewActions.actionOnItem(hasDescendant(withText(activity.getResources().getString(R.string.range_title))),
                        click()));
        Thread.sleep(1000);
        //select first option
        onView(withText(activity.getResources().getStringArray(R.array.range_entries)[0])).perform(click());
        //Get selected value
        String startString = prefs.getString(activity.getResources().getString(R.string.range_key), "");

        // TEST 10KM
        onView(withId(androidx.preference.R.id.recycler_view))
                .perform(RecyclerViewActions.actionOnItem(hasDescendant(withText(activity.getResources().getString(R.string.range_title))),
                        click()));
        Thread.sleep(1000);
        //select first option
        onView(withText(activity.getResources().getStringArray(R.array.range_entries)[1])).perform(click());

        //Check that the selection happened
        String endString = prefs.getString(activity.getResources().getString(R.string.range_key), "");
        assertThat(startString, not(is(endString)));

        startString = endString;

        // TEST 20KM
        onView(withId(androidx.preference.R.id.recycler_view))
                .perform(RecyclerViewActions.actionOnItem(hasDescendant(withText(activity.getResources().getString(R.string.range_title))),
                        click()));
        Thread.sleep(1000);
        //select first option
        onView(withText(activity.getResources().getStringArray(R.array.range_entries)[2])).perform(click());

        //Check that the selection happened
        endString = prefs.getString(activity.getResources().getString(R.string.range_key), "");
        assertThat(startString, not(is(endString)));

        startString = endString;

        // TEST 30KM
        onView(withId(androidx.preference.R.id.recycler_view))
                .perform(RecyclerViewActions.actionOnItem(hasDescendant(withText(activity.getResources().getString(R.string.range_title))),
                        click()));
        Thread.sleep(1000);
        //select first option
        onView(withText(activity.getResources().getStringArray(R.array.range_entries)[3])).perform(click());

        //Check that the selection happened
        endString = prefs.getString(activity.getResources().getString(R.string.range_key), "");
        assertThat(startString, not(is(endString)));

        startString = endString;

        // TEST 50KM
        onView(withId(androidx.preference.R.id.recycler_view))
                .perform(RecyclerViewActions.actionOnItem(hasDescendant(withText(activity.getResources().getString(R.string.range_title))),
                        click()));
        Thread.sleep(1000);
        //select first option
        onView(withText(activity.getResources().getStringArray(R.array.range_entries)[4])).perform(click());

        //Check that the selection happened
        endString = prefs.getString(activity.getResources().getString(R.string.range_key), "");
        assertThat(startString, not(is(endString)));
    }

    /*Test measuring system menu*/
    @Test
    public void TestMeasuringSystemButton() throws InterruptedException {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        //Get selected value
        String startString = prefs.getString(activity.getResources().getString(R.string.measSys_key), "");

        //Open selector window
        onView(withId(androidx.preference.R.id.recycler_view))
                .perform(RecyclerViewActions.actionOnItem(hasDescendant(withText(activity.getResources().getString(R.string.measSys_title))),
                        click()));
        Thread.sleep(1000);
        //Select the opposite preference
        if(startString.equals(activity.getResources().getStringArray(R.array.measSys_values)[1]))
            onView(withText(activity.getResources().getStringArray(R.array.measSys_entries)[0])).perform(click());
        else
            onView(withText(activity.getResources().getStringArray(R.array.measSys_entries)[1])).perform(click());

        //Check that the selection happened
        String endString = prefs.getString(activity.getResources().getString(R.string.measSys_key), "");
        assertThat(startString, not(is(endString)));
    }

    /*Test night mode button click */
    @Test
    public void TestNightModeButton(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        //Get selected value
        Boolean startVal = prefs.getBoolean(activity.getResources().getString(R.string.night_mode_key), false);
        onView(withText(activity.getResources().getString(R.string.night_mode_title))).perform(click());
        Boolean endVal = prefs.getBoolean(activity.getResources().getString(R.string.night_mode_key), false);
        assertThat(startVal, not(is(endVal)));
    }

    /*Test offline mode button click*/
    @Test
    public void TestOfflineModeButton(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        //Get selected value
        Boolean startVal = prefs.getBoolean(activity.getResources().getString(R.string.offline_mode_key), false);

        onView(withId(androidx.preference.R.id.recycler_view))
                .perform(RecyclerViewActions.actionOnItem(hasDescendant(withText(activity.getResources().getString(R.string.offline_mode_title))),
                        click()));

        Boolean endVal = prefs.getBoolean(activity.getResources().getString(R.string.offline_mode_key), false);
        assertThat(startVal, not(is(endVal)));
    }

    /*Test language selection menu */
    @Test
    public void TestLanguageButton() throws InterruptedException {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);


        //Open selector window
        onView(withId(androidx.preference.R.id.recycler_view))
                .perform(RecyclerViewActions.actionOnItem(hasDescendant(withText(activity.getResources().getString(R.string.language_title))),
                        click()));
        Thread.sleep(1000);
        //select first option
        onView(withText(activity.getResources().getStringArray(R.array.language_entries)[0])).perform(click());
        //Get selected value
        String startString = prefs.getString(activity.getResources().getString(R.string.language_key), "");


        onView(withId(androidx.preference.R.id.recycler_view))
                .perform(RecyclerViewActions.actionOnItem(hasDescendant(withText(activity.getResources().getString(R.string.language_title))),
                        click()));
        Thread.sleep(1000);
        //select first option
        onView(withText(activity.getResources().getStringArray(R.array.language_entries)[1])).perform(click());

        //Check that the selection happened
        String endString = prefs.getString(activity.getResources().getString(R.string.language_key), "");
        assertThat(startString, not(is(endString)));

        startString = endString;
        //Test GERMAN
        onView(withId(androidx.preference.R.id.recycler_view))
                .perform(RecyclerViewActions.actionOnItem(hasDescendant(withText(activity.getResources().getString(R.string.language_title))),
                        click()));
        Thread.sleep(1000);
        //select first option
        onView(withText(activity.getResources().getStringArray(R.array.language_entries)[2])).perform(click());

        //Check that the selection happened
        endString = prefs.getString(activity.getResources().getString(R.string.language_key), "");
        assertThat(startString, not(is(endString)));

        startString = endString;
        //Test SWEDISH
        onView(withId(androidx.preference.R.id.recycler_view))
                .perform(RecyclerViewActions.actionOnItem(hasDescendant(withText(activity.getResources().getString(R.string.language_title))),
                        click()));
        Thread.sleep(1000);
        //select first option
        onView(withText(activity.getResources().getStringArray(R.array.language_entries)[3])).perform(click());

        //Check that the selection happened
        endString = prefs.getString(activity.getResources().getString(R.string.language_key), "");
        assertThat(startString, not(is(endString)));

        //Open selector window
        onView(withId(androidx.preference.R.id.recycler_view))
                .perform(RecyclerViewActions.actionOnItem(hasDescendant(withText(activity.getResources().getString(R.string.language_title))),
                        click()));
        Thread.sleep(1000);
        //select first option
        onView(withText(activity.getResources().getStringArray(R.array.language_entries)[0])).perform(click());
        //Let activity retrieves stability point
    }

    public void TestToolbarTitle(){
        String TOOLBAR_TITLE = "Settings";
        ViewInteraction greetingText = Espresso.onView(withId(R.id.toolbarTitle));
        greetingText.check(matches(withText(TOOLBAR_TITLE)));
    }

    /*Test preference allow caching button click*/
    @Test
    public void TestAllowCachingButton(){


        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        //Get selected value
        Boolean startVal = prefs.getBoolean(activity.getResources().getString(R.string.disable_caching_key), false);
        onView(withId(androidx.preference.R.id.recycler_view))
                .perform(RecyclerViewActions.actionOnItem(hasDescendant(withText(activity.getResources().getString(R.string.caching_title))),
                        click()));
        Boolean endVal = prefs.getBoolean(activity.getResources().getString(R.string.disable_caching_key), false);
        assertThat(startVal, not(is(endVal)));
    }

    /*Gets the activity for testing*/
    private <T extends Activity> T getActivity(ActivityScenarioRule<T> activityScenarioRule) {
        AtomicReference<T> activityRef = new AtomicReference<>();
        activityScenarioRule.getScenario().onActivity(activityRef::set);
        return activityRef.get();
    }
}
