package ch.epfl.sdp.peakar.collection;

import android.app.Activity;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.DataInteraction;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.intent.matcher.IntentMatchers;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import ch.epfl.sdp.peakar.R;
import ch.epfl.sdp.peakar.database.Database;
import ch.epfl.sdp.peakar.points.POIPoint;
import ch.epfl.sdp.peakar.user.auth.Authentication;
import ch.epfl.sdp.peakar.user.auth.FirebaseAuthentication;
import ch.epfl.sdp.peakar.user.score.UserScore;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static ch.epfl.sdp.peakar.user.AccountTest.BASIC_USERNAME;
import static ch.epfl.sdp.peakar.user.AccountTest.LONG_SLEEP_TIME;
import static ch.epfl.sdp.peakar.user.AccountTest.SHORT_SLEEP_TIME;
import static ch.epfl.sdp.peakar.user.AccountTest.registerAuthUser;
import static ch.epfl.sdp.peakar.user.AccountTest.removeAuthUser;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;


@RunWith(AndroidJUnit4.class)
public class CollectionActivityTest {
    private static final String MONT_BLANC_NAME = "Mont Blanc - Monte Bianco";
    private static final String DENT_DU_GEANT_NAME = "Dent du Geant";
    private static final String POINTE_DE_LAPAZ_NAME = "Pointe de Lapaz";
    private static final String AIGUILLE_DU_PLAN = "Aiguille du Plan";

    @Rule
    public ActivityScenarioRule<CollectionActivity> testRule = new ActivityScenarioRule<>(CollectionActivity.class);

    @BeforeClass
    public static void setupUserAccount() throws InterruptedException {
        /* Make sure no user is signed in before a test */
        Authentication.getInstance().signOut(InstrumentationRegistry.getInstrumentation().getTargetContext());

        /* Create a new one */
        registerAuthUser();

        //Write peaks to the database
        GeoPoint geoPoint_1 = new GeoPoint(45.8325,6.8641666666667,4810);
        POIPoint point_1 = new POIPoint(geoPoint_1);
        point_1.setName(MONT_BLANC_NAME);

        GeoPoint geoPoint_2 = new GeoPoint(45.86355980599387, 6.951348205683087,4013);
        POIPoint point_2 = new POIPoint(geoPoint_2);
        point_2.setName(DENT_DU_GEANT_NAME);

        GeoPoint geoPoint_3 = new GeoPoint(45.891667, 6.907222,3673);
        POIPoint point_3 = new POIPoint(geoPoint_3);
        point_3.setName(AIGUILLE_DU_PLAN);

        GeoPoint geoPoint_4 = new GeoPoint(45.920774986207014, 6.812914656881065,3660);
        POIPoint point_4 = new POIPoint(geoPoint_4);
        point_4.setName(POINTE_DE_LAPAZ_NAME);

        ArrayList<POIPoint> inputArrayList = new ArrayList<POIPoint>();
        inputArrayList.add(point_2);
        inputArrayList.add(point_1);
        inputArrayList.add(point_3);
        inputArrayList.add(point_4);

        //Put every POI in database + in the cache
        UserScore userScore = new UserScore(ApplicationProvider.getApplicationContext());
        userScore.updateUserScoreAndDiscoveredPeaks(inputArrayList);
        Thread.sleep(LONG_SLEEP_TIME);

        FirebaseAuthentication.getInstance().forceRetrieveData();
    }

    /* Clean environment */
    @AfterClass
    public static void end() {
        Database.refRoot.child(Database.CHILD_USERS).child(Authentication.getInstance().getID()).removeValue();
        removeAuthUser();
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

    /* Test that the toolbar title is set as expected */
    @Test
    public void TestToolbarTitle(){
        String TOOLBAR_TITLE = "Collections";
        ViewInteraction greetingText = Espresso.onView(ViewMatchers.withId(R.id.toolbarTitle));
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
    public void TestContentOfListView() throws InterruptedException {
        //Write peaks to the database

        DataInteraction interaction =  onData(instanceOf(CollectedItem.class));

        Thread.sleep(LONG_SLEEP_TIME * 2);
        for (int i = 0; i < 4; i++){
            DataInteraction listItem = interaction.atPosition(i);
            DataInteraction dataInteraction = listItem.onChildView(withId(R.id.collected_name));

            switch(i){
                case 0:
                    dataInteraction.check(matches(withText(MONT_BLANC_NAME)));
                    break;
                case 1:
                    dataInteraction.check(matches(withText(DENT_DU_GEANT_NAME)));
                    break;
                case 2:
                    dataInteraction.check(matches(withText(AIGUILLE_DU_PLAN)));
                    break;
                case 3:
                    dataInteraction.check(matches(withText(POINTE_DE_LAPAZ_NAME)));
                    break;
            }
        }
    }

    @Test
    public void TestPressCollected() throws InterruptedException {
        // Item at pos 10 looks like this
        CollectedItem correctItem = new CollectedItem(
                AIGUILLE_DU_PLAN,
                367300,
                3673,
                6.907222f,
                45.891667f);

        // Get Item at pos 10 and click.
        DataInteraction listItem = onData(instanceOf(CollectedItem.class)).atPosition(2);
        listItem.perform(ViewActions.click());

        Thread.sleep(SHORT_SLEEP_TIME);

        // Catch intent, and check information
        intended(allOf(IntentMatchers.hasComponent(MountainActivity.class.getName()),
                        IntentMatchers.hasExtra("name", correctItem.getName()),
                        IntentMatchers.hasExtra("points", correctItem.getPoints()),
                        IntentMatchers.hasExtra("height", correctItem.getHeight()),
                        IntentMatchers.hasExtra("longitude", correctItem.getLongitude()),
                        IntentMatchers.hasExtra("latitude", correctItem.getLatitude())));
    }
}
