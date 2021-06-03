package ch.epfl.sdp.peakar.general;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.intent.matcher.IntentMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicReference;

import ch.epfl.sdp.peakar.R;
import ch.epfl.sdp.peakar.database.Database;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static ch.epfl.sdp.peakar.utils.MyPagerAdapter.SETTINGS_FRAGMENT_INDEX;
import static ch.epfl.sdp.peakar.utils.TestingConstants.THREAD_SLEEP_1S;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class SettingsActivityTest{

    @Rule
    public ActivityScenarioRule<MainActivity> testRule = new ActivityScenarioRule<>(MainActivity.class);
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


    /* Create intent */
    @Before
    public void setup() throws InterruptedException {
        Intents.init();
        testRule.getScenario().onActivity(activity -> activity.setCurrentPagerItem(SETTINGS_FRAGMENT_INDEX));
        Thread.sleep(2000);
    }

    /* Release Intent */
    @After
    public void cleanUp(){
        Intents.release();
        Database.getInstance().setOnlineMode();
    }

    /*Test discovery distance button*/
    @Test
    public void TestDiscoveryDistanceButton() throws InterruptedException {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        activity = getActivity(testRule);
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
        activity = getActivity(testRule);
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
        Boolean startVal = prefs.getBoolean(context.getResources().getString(R.string.night_mode_key), false);
        onView(withText(context.getResources().getString(R.string.night_mode_title))).perform(click());
        Boolean endVal = prefs.getBoolean(context.getResources().getString(R.string.night_mode_key), false);
        assertThat(startVal, not(is(endVal)));
    }

    /*Test offline mode button click*/
    @Test
    public void TestOfflineModeButton() throws InterruptedException {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        activity = getActivity(testRule);
        //Get selected value
        Boolean startVal = prefs.getBoolean(activity.getResources().getString(R.string.offline_mode_key), false);

        onView(withId(androidx.preference.R.id.recycler_view))
                .perform(RecyclerViewActions.actionOnItem(hasDescendant(withText(activity.getResources().getString(R.string.offline_mode_title))),
                        click()));

        Boolean endVal = prefs.getBoolean(activity.getResources().getString(R.string.offline_mode_key), false);
        assertThat(startVal, not(is(endVal)));
        Thread.sleep(THREAD_SLEEP_1S);
        if (endVal) {
            intended(IntentMatchers.hasComponent(SettingsMapActivity.class.getName()));
        }
    }

    /*Test language selection menu */
    @Test
    public void TestLanguageButton() throws InterruptedException {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        activity = getActivity(testRule);

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
        //Let context retrieves stability point
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
        Boolean startVal = prefs.getBoolean(context.getResources().getString(R.string.disable_caching_key), false);
        onView(withId(androidx.preference.R.id.recycler_view))
                .perform(RecyclerViewActions.actionOnItem(hasDescendant(withText(context.getResources().getString(R.string.caching_title))),
                        click()));
        Boolean endVal = prefs.getBoolean(context.getResources().getString(R.string.disable_caching_key), false);
        assertThat(startVal, not(is(endVal)));
    }

    /*Test preference toggle developer options*/
    @Test
    public void TestEnableDeveloperOptions(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        String devOptionsKey = context.getResources().getString(R.string.devOptions_key);
        //Get selected value
        boolean startVal = sharedPreferences.getBoolean(devOptionsKey, false);
        onView(withId(androidx.preference.R.id.recycler_view))
                .perform(RecyclerViewActions.actionOnItem(hasDescendant(withText(context.getResources().getString(R.string.display_dev_options))),
                        click()));
        boolean endVal = sharedPreferences.getBoolean(devOptionsKey, false);
        assertThat(startVal, not(is(endVal)));
    }

    /*Test preference toggle filter POIs*/
    @Test
    public void TestToggleFilterPOIs(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        String fillterPOIsKey = context.getResources().getString(R.string.filterPOIs_key);
        //Get selected value
        Boolean startVal = sharedPreferences.getBoolean(fillterPOIsKey, false);
        onView(withId(androidx.preference.R.id.recycler_view))
                .perform(RecyclerViewActions.actionOnItem(hasDescendant(withText(context.getResources().getString(R.string.filter_pois_title))),
                        click()));
        Boolean endVal = sharedPreferences.getBoolean(fillterPOIsKey, false);
        assertThat(startVal, not(is(endVal)));
    }

    /*Test select displayed mountain menu*/
    @Test
    public void TestSelectDisplayedMountains() throws InterruptedException {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        String displayPOIsKey = context.getResources().getString(R.string.displayPOIs_key);
        //Get selected value
        String startString = prefs.getString(displayPOIsKey, "");

        //Open selector window
        onView(withId(androidx.preference.R.id.recycler_view))
                .perform(RecyclerViewActions.actionOnItem(hasDescendant(withText(context.getResources().getString(R.string.display_pois_title))),
                        click()));
        Thread.sleep(THREAD_SLEEP_1S);
        String[] entries = context.getResources().getStringArray(R.array.displayPOIs_entries);
        //Select the opposite preference
        if(startString.equals(context.getResources().getStringArray(R.array.displayPOIs_values)[1]))
            onView(withText(entries[0])).perform(click());
        else
            onView(withText(entries[1])).perform(click());

        //Check that the selection happened
        String endString = prefs.getString(displayPOIsKey, "");
        assertThat(startString, not(is(endString)));
    }


    /*Gets the activity for testing*/
    private <T extends Activity> T getActivity(ActivityScenarioRule<T> activityScenarioRule) {
        AtomicReference<T> activityRef = new AtomicReference<>();
        activityScenarioRule.getScenario().onActivity(activityRef::set);
        return activityRef.get();
    }
}
