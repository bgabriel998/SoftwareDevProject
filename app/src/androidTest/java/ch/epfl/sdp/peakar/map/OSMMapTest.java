package ch.epfl.sdp.peakar.map;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.platform.app.InstrumentationRegistry;

import com.google.firebase.auth.FirebaseAuth;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.osmdroid.tileprovider.MapTileProviderBase;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.List;

import ch.epfl.sdp.peakar.R;
import ch.epfl.sdp.peakar.database.Database;
import ch.epfl.sdp.peakar.points.POIPoint;
import ch.epfl.sdp.peakar.user.score.UserScore;
import ch.epfl.sdp.peakar.user.services.AuthService;
import ch.epfl.sdp.peakar.user.services.FirebaseAuthService;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static ch.epfl.sdp.peakar.utils.TestingConstants.*;
import static ch.epfl.sdp.peakar.utils.UserTestHelper.registerAuthUser;
import static ch.epfl.sdp.peakar.utils.UserTestHelper.removeAuthUser;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class OSMMapTest {

    private MapView mapView;
    private OSMMap osmMap;

    @Rule
    public ActivityScenarioRule<MapActivity> testRule = new ActivityScenarioRule<>(MapActivity.class);

    /* Set up the environment */
    @BeforeClass
    public static void init() {
        /* Make sure no user is signed in before tests */
        AuthService.getInstance().signOut(InstrumentationRegistry.getInstrumentation().getTargetContext());

        /* Create a new one */
        registerAuthUser();
    }

    /* Clean environment */
    @AfterClass
    public static void end() {
        removeAuthUser();
    }

    /* Make sure that an account is signed in and as new before each test */
    @Before
    public void createTestUser() {
        if(FirebaseAuth.getInstance().getCurrentUser() == null) {
            AuthService.getInstance().signOut(InstrumentationRegistry.getInstrumentation().getTargetContext());
            registerAuthUser();
        }
        else {
            FirebaseAuthService.getInstance().forceRetrieveData();
        }
    }

    /* Make sure that mock users are not on the database after a test */
    @After
    public void removeTestUsers() throws InterruptedException {
        Database.getInstance().getReference().child(Database.CHILD_USERS).child(AuthService.getInstance().getID()).removeValue();
        Thread.sleep(SHORT_SLEEP_TIME);
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

    //Test creation of OSMmap object
    @Test
    public void osmMapConstructorTest() {
        Context context = ApplicationProvider.getApplicationContext();
        Assert.assertNotNull(context);
        testRule.getScenario().onActivity(activity -> mapView = activity.findViewById(R.id.map));
        //Check map initialization
        assertEquals(TILE_SCALING_FACTOR, mapView.getTilesScaleFactor(),0.0f);
    }


    @Test
    public void displayUserLocationTest() throws InterruptedException {
        Context context = ApplicationProvider.getApplicationContext();
        Assert.assertNotNull(context);
        testRule.getScenario().onActivity(activity -> mapView = activity.findViewById(R.id.map));

        List<Overlay> overlayList = mapView.getOverlays();
        for(Overlay overlay : overlayList){
            if(overlay instanceof MyLocationNewOverlay){
                Thread.sleep(6000);
                GeoPoint geoPoint = ((MyLocationNewOverlay) overlay).getMyLocation();
                assertNotNull(geoPoint);
                return;
            }
        }
        fail("fail to retrieve user location on OpenStreetMap activity");
    }

    @Test
    public void setMarkersForDiscoveredPeaksTest() throws InterruptedException {
        //Write peaks to the database
        GeoPoint geoPoint_1 = new GeoPoint(MONT_BLANC_LAT,MONT_BLANC_LONG,MONT_BLANC_ALT);
        POIPoint point_1 = new POIPoint(geoPoint_1);
        point_1.setName(MONT_BLANC_NAME);

        GeoPoint geoPoint_2 = new GeoPoint(DENT_DU_GEANT_LAT, DENT_DU_GEANT_LONG,DENT_DU_GEANT_ALT);
        POIPoint point_2 = new POIPoint(geoPoint_2);
        point_2.setName(DENT_DU_GEANT_NAME);

        GeoPoint geoPoint_3 = new GeoPoint(AIGUILLE_DU_PLAN_LAT, AIGUILLE_DU_PLAN_LONG,AIGUILLE_DU_PLAN_ALT);
        POIPoint point_3 = new POIPoint(geoPoint_3);
        point_3.setName(AIGUILLE_DU_PLAN_NAME);

        GeoPoint geoPoint_4 = new GeoPoint(POINTE_DE_LAPAZ_LAT, POINTE_DE_LAPAZ_LONG,POINTE_DE_LAPAZ_ALT);
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

        //Init map
        Context context = ApplicationProvider.getApplicationContext();
        Assert.assertNotNull(context);
        testRule.getScenario().onActivity(activity -> osmMap = activity.getOsmMap());


        //The following lines initializes a handler and a looper
        //Those are needed here to handle the zooming animation caused by
        //the setMarkersForDiscoveredPeaks method
        Looper.prepare();
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(() -> osmMap.setMarkersForDiscoveredPeaks(true), 100);
        //Wait for the handler to complete
        Thread.sleep(1000);

        //Initialize check variables
        boolean AiguilleDuPlanMarkerPresent = false;
        boolean MontBlancMarkerPresent = false;
        boolean PointeDeLapazMarkerPresent = false;
        boolean DentDuGeantMarkerPresent = false;

        //Get all overlays on map
        List<Overlay> overlayList =  osmMap.getMapView().getOverlays();
        for(Overlay overlay : overlayList) {
            if(overlay instanceof Marker){
                //Cast into marker
                Marker marker = (Marker) overlay;
                //Get the name of the marker (located in the 1st part of the title)
                switch (marker.getTitle().split("\n")[0]){
                    case MONT_BLANC_NAME:
                        MontBlancMarkerPresent = true;
                        break;
                    case AIGUILLE_DU_PLAN_NAME:
                        AiguilleDuPlanMarkerPresent = true;
                        break;
                    case DENT_DU_GEANT_NAME:
                        DentDuGeantMarkerPresent = true;
                        break;
                    case POINTE_DE_LAPAZ_NAME:
                        PointeDeLapazMarkerPresent = true;
                        break;
                }
            }
        }
        //Check that all expected markers are present
        Assert.assertTrue(MontBlancMarkerPresent);
        Assert.assertTrue(AiguilleDuPlanMarkerPresent);
        Assert.assertTrue(DentDuGeantMarkerPresent);
        Assert.assertTrue(PointeDeLapazMarkerPresent);
    }


    /*Test switch between normal map and satellite view*/
    @Test
    public void pressSatelliteNormalMapButton() {
        testRule.getScenario().onActivity(activity -> mapView = activity.findViewById(R.id.map));
        //Originally the map is set to default
        //A press on the button will change the map tile for satellite

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
        testRule.getScenario().onActivity(activity -> mapView = activity.findViewById(R.id.map));
        //Get map center at start
        GeoPoint geoPointStart = (GeoPoint) mapView.getMapCenter();
        //Wait 5 Sec for the provider to get the location
        Thread.sleep(5000);

        ViewInteraction button = onView(withId(R.id.zoomOnUserLocation));
        button.perform(click());
        //Get center of the map after zoom
        GeoPoint geoPointEnd = (GeoPoint)  mapView.getMapCenter();
        //Compare two map centers
        assertThat(geoPointStart, is(not(geoPointEnd)));
    }


}
