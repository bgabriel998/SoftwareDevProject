package ch.epfl.sdp.peakar.camera;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import ch.epfl.sdp.peakar.R;
import ch.epfl.sdp.peakar.general.MainActivity;
import ch.epfl.sdp.peakar.points.ComputePOIPoints;

import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static ch.epfl.sdp.peakar.utils.TestingConstants.SHORT_SLEEP_TIME;
import static org.junit.Assert.assertTrue;


@RunWith(AndroidJUnit4.class)
public class CameraFragmentTest {

    @Rule
    public ActivityScenarioRule<MainActivity> testRule = new ActivityScenarioRule<>(MainActivity.class);

    private static SharedPreferences sharedPreferences;
    private static Context context;

    /* Setup environment */
    @BeforeClass
    public static void computePOIPoints() {
        context = ApplicationProvider.getApplicationContext();
        ComputePOIPoints.getInstance(context);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear().commit();
    }

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

    /* Test that the compass is changed when clicking on the compass button */
    @Test
    public void TestChangeCompassButton() throws InterruptedException {
        String displayCompassKey = context.getResources().getString(R.string.displayCompass_key);
        sharedPreferences.edit().putBoolean(displayCompassKey, false).apply();

        ViewInteraction button = Espresso.onView(withId(R.id.compassMiniature));
        button.perform(ViewActions.click());

        Thread.sleep(SHORT_SLEEP_TIME);

        boolean displayCompass = sharedPreferences.getBoolean(displayCompassKey, false);
        assertTrue(displayCompass);
    }
}
