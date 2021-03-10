package com.github.bgabriel998.softwaredevproject;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.osmdroid.bonuspack.location.POI;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.github.bgabriel998.softwaredevproject", appContext.getPackageName());
    }

    @Test
    public void testGetSurroundingPeaks(){
        GeonamesHandler handler = new GeonamesHandler("bgabrie1");
        GeoPoint point = new GeoPoint(45.9258378624377, 6.878492964884342);
        ArrayList<POI> result = null;
        try {
            result = handler.getSurroundingPeaks(point);
            assertThat(result.size(), greaterThan(10));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}