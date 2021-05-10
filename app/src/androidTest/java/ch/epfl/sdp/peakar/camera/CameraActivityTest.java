package ch.epfl.sdp.peakar.camera;

import android.app.Activity;
import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.intent.matcher.IntentMatchers;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import ch.epfl.sdp.peakar.R;
import ch.epfl.sdp.peakar.map.MapActivity;
import ch.epfl.sdp.peakar.points.ComputePOIPoints;

import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;


@RunWith(AndroidJUnit4.class)
public class CameraActivityTest {

    @Rule
    public ActivityScenarioRule<CameraActivity> testRule = new ActivityScenarioRule<>(CameraActivity.class);

    /* Setup environment */
    @BeforeClass
    public static void computePOIPointsInstance(){
        Context context = ApplicationProvider.getApplicationContext();
        ComputePOIPoints.getInstance(context);
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


    /* Test that pressing the map icon button changes view to MapActivity */
    @Test
    public void TestMapIconButton(){
        ViewInteraction button = Espresso.onView(ViewMatchers.withId(R.id.mapButton));
        button.perform(ViewActions.click());
        // Catch intent
        intended(IntentMatchers.hasComponent(MapActivity.class.getName()));
    }

    /* Test that pressing the back button finish the activity */
    @Test
    public void TestBackButton(){
        ViewInteraction button = Espresso.onView(withId(R.id.cameraBackButton));
        button.perform(ViewActions.click());
        try {
            Thread.sleep(1000);
            assertSame(testRule.getScenario().getResult().getResultCode(), Activity.RESULT_CANCELED);
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail("TestBackButton failed");
        }
    }
}
