package com.github.bgabriel998.softwaredevproject;

import android.content.Context;
import android.util.Log;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.intent.Intents;
import androidx.test.rule.ActivityTestRule;

import com.github.giommok.softwaredevproject.Account;
import com.github.giommok.softwaredevproject.CountryHighPoint;
import com.github.giommok.softwaredevproject.Database;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.theories.Theories;
import org.osmdroid.api.IMapController;
import org.osmdroid.api.IMapView;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class OSMMapTest {
    //Constants
    private static final float TILE_SCALING_FACTOR = 1.5f;

    @Rule
    public ActivityTestRule<MapActivity> activityTestRule = new ActivityTestRule<>(MapActivity.class);


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


        //Write country high point to the database
        Database.setChild(Database.CHILD_USERS+"/null", Collections.singletonList(Database.CHILD_USERNAME), Collections.singletonList("usernameTest4"));
        Thread.sleep(1000);
        Account account = Account.getAccount();
        CountryHighPoint newEntry = new CountryHighPoint("France","Mont Blanc",4810);
        account.setDiscoveredCountryHighPoint(newEntry);
        Thread.sleep(1000);
        assertEquals(newEntry.toString(), account.getDiscoveredCountryHighPoint().get("France").toString());

        //Sync the database and the user profile

        //Init map

        //Set Markers

        //Check amount of maker with getOverlays()


    }


}
