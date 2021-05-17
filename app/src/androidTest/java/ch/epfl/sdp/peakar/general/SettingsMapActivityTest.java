package ch.epfl.sdp.peakar.general;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osmdroid.tileprovider.MapTileProviderBase;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.io.IOException;

import ch.epfl.sdp.peakar.R;
import ch.epfl.sdp.peakar.utils.OfflineContentContainer;
import ch.epfl.sdp.peakar.utils.StorageHandler;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static ch.epfl.sdp.peakar.utils.TestingConstants.THREAD_SLEEP_5S;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class SettingsMapActivityTest {

    private MapView mapView;

    @Rule
    public ActivityScenarioRule<SettingsMapActivity> testRule = new ActivityScenarioRule<>(SettingsMapActivity.class);

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

    /* Test that the "Download" button appears after adding a pin to the map */
    @Test
    public void TestMapLongPress(){
        // checks that button and loading bar is not visible
        onView((withId((R.id.downloadButton))))
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.INVISIBLE)));
        onView((withId((R.id.downloadButton))))
                .check(matches(not(isDisplayed())));
        onView((withId((R.id.loadingView))))
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
        onView((withId((R.id.loadingView))));
        // performs long press
        ViewInteraction view = onView(withId(R.id.settingsMapView));
        view.perform(ViewActions.longClick());
        // check if download button is visible and displayed
        onView((withId((R.id.downloadButton))))
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
        onView((withId((R.id.downloadButton))))
                .check(matches(isDisplayed()));
        // check that the loading view is not visible and not displayed
        onView((withId((R.id.loadingView))))
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
        onView((withId((R.id.loadingView))));
    }

    /* Test that the overlays are set correctly before and after a long press) */
    @Test
    public void TestMapLongPressOverlays() {
        // checks that there are no overlays
        testRule.getScenario().onActivity(a -> {
            MapView mapView = a.findViewById(R.id.settingsMapView);
            Assert.assertEquals(2,mapView.getOverlays().size());
        });
        // performs long press
        ViewInteraction view = onView(withId(R.id.settingsMapView));
        view.perform(ViewActions.longClick());
        // check if ok button is visible and displayed
        testRule.getScenario().onActivity(a -> {
            MapView mapView = a.findViewById(R.id.settingsMapView);
            Assert.assertEquals(4,mapView.getOverlays().size());
        });
    }

    /* Test that pressing the back button finish the activity and resets the offline mode value */
    @Test
    public void testBackButton() {
        ViewInteraction button = onView(withId(R.id.toolbarBackButton));
        button.perform(ViewActions.click());
        try {
            Thread.sleep(1000);
            assertSame(testRule.getScenario().getResult().getResultCode(), Activity.RESULT_CANCELED);
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail("TestBackButton failed");
        }

        Context ctx = ApplicationProvider.getApplicationContext();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        // check that the offline mode value has been reset
        Assert.assertFalse(prefs.getBoolean(ctx.getResources().getString(R.string.offline_mode_key), false));

    }

    /* Tests that the activity is terminated after download button is pressed. */
    @Test
    public void downloadButtonPressed() {
        // performs long press
        ViewInteraction mapView = onView(withId(R.id.settingsMapView));
        mapView.perform(ViewActions.longClick());
        // press the download button
        ViewInteraction view = onView(withId(R.id.downloadButton));
        view.perform(ViewActions.click());
        // Check if activity is stopped
        try {
            Thread.sleep(1000);
            assertSame(testRule.getScenario().getResult().getResultCode(), Activity.RESULT_CANCELED);
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail("TestDownloadButton failed");
        }

    }

    /* checks if the offline content is saved after the ok button is pressed */
    @Test
    public void saveJsonTest() throws InterruptedException, IOException {
        // performs long press
        ViewInteraction mapView = onView(withId(R.id.settingsMapView));
        mapView.perform(ViewActions.longClick());
        // press the download button
        ViewInteraction view = onView(withId(R.id.downloadButton));
        view.perform(ViewActions.click());
        // Check if the file exists
        Thread.sleep(15000);
        OfflineContentContainer savedData = StorageHandler.readOfflineContentContainer(ApplicationProvider.getApplicationContext());
        Assert.assertNotNull(savedData);
        Assert.assertNotNull(savedData.boundingBox);
        Assert.assertNotNull(savedData.POIPoints);
        Assert.assertNotNull(savedData.topography);
    }

    /*Test switch between normal map and satellite view*/
    @Test
    public void pressSatelliteNormalMapButton() {
        testRule.getScenario().onActivity(activity -> mapView = activity.findViewById(R.id.settingsMapView));

        //Originally the map is set to default
        //A press on the button will change the map tile for satellite

        //MapView mapView = osmMap.getMapView();
        MapTileProviderBase tileProviderBase = mapView.getTileProvider();
        assertEquals("Mapnik", tileProviderBase.getTileSource().name());
        //Click on button
        ViewInteraction button = onView(withId(R.id.changeMapTile));
        button.perform(click());
        //Check that the provider has changed
        tileProviderBase = mapView.getTileProvider();
        assertEquals("ARCGisOnline", tileProviderBase.getTileSource().name());
    }

    /* Check that a press on "zoom on user location button effectively zooms on user loc"*/
    @Test
    public void pressZoomOnUserLocationButton() throws InterruptedException {
        testRule.getScenario().onActivity(activity -> mapView = activity.findViewById(R.id.settingsMapView));
        //Get map center at start
        GeoPoint geoPointStart = (GeoPoint) mapView.getMapCenter();
        //Wait 5 Sec for the provider to get the location
        Thread.sleep(THREAD_SLEEP_5S);

        ViewInteraction button = onView(withId(R.id.zoomOnUserLocation));
        button.perform(click());
        //Get center of the map after zoom
        GeoPoint geoPointEnd = (GeoPoint)  mapView.getMapCenter();
        //Compare two map centers
        assertThat(geoPointStart, is(not(geoPointEnd)));
    }
}
