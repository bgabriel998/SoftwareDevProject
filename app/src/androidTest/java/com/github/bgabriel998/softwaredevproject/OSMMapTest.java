package com.github.bgabriel998.softwaredevproject;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.rule.ActivityTestRule;

import com.github.giommok.softwaredevproject.Database;
import com.github.giommok.softwaredevproject.FirebaseAccount;
import com.github.giommok.softwaredevproject.UserScore;
import com.github.ravifrancesco.softwaredevproject.POIPoint;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class OSMMapTest {
    //Constants
    private static final float TILE_SCALING_FACTOR = 1.5f;
    private static final String MONT_BLANC_NAME = "Mont Blanc - Monte Bianco";
    private static final String DENT_DU_GEANT_NAME = "Dent du Geant";
    private static final String POINTE_DE_LAPAZ_NAME = "Pointe de Lapaz";
    private static final String AIGUILLE_DU_PLAN = "Aiguille du Plan";

    @Rule
    public ActivityScenarioRule<MapActivity> activityActivityScenarioRule = new ActivityScenarioRule<>(MapActivity.class);


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
    public void osmMapConstructorTest() throws NoSuchFieldException, IllegalAccessException {
        Context context = ApplicationProvider.getApplicationContext();
        Assert.assertNotNull(context);
        OSMMap osmMap = MapActivity.osmMap;
        MapView mapView = osmMap.getMapView();
        //Check map initialization
        assertEquals(TILE_SCALING_FACTOR, mapView.getTilesScaleFactor(),0.0f);
    }


    @Test
    public void displayUserLocationTest() throws InterruptedException {
        Context context = ApplicationProvider.getApplicationContext();
        Assert.assertNotNull(context);
        OSMMap osmMap = MapActivity.osmMap;
        MapView mapView = osmMap.getMapView();
        List<Overlay> overlayList = osmMap.getMapView().getOverlays();
        for(Overlay overlay : overlayList){
            if(overlay instanceof MyLocationNewOverlay){
                Thread.sleep(4000);
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
        FirebaseAccount account = FirebaseAccount.getAccount();
        UserScore userScore = new UserScore(ApplicationProvider.getApplicationContext(),account);
        userScore.updateUserScoreAndDiscoveredPeaks(inputArrayList);

        //Init map
        Context context = ApplicationProvider.getApplicationContext();
        Assert.assertNotNull(context);
        OSMMap osmMap = MapActivity.osmMap;
        MapView mapView = osmMap.getMapView();


        //The following lines initializes a handler and a looper
        //Those are needed here to handle the zooming animation caused by
        //the setMarkersForDiscoveredPeaks method
        Looper.prepare();
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                osmMap.setMarkersForDiscoveredPeaks(account, true);
            }
        }, 100);
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
                    case AIGUILLE_DU_PLAN:
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

        //Remove created child
        Database.refRoot.child(Database.CHILD_USERS).child("null").removeValue();
    }



}
