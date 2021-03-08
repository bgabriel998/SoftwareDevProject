package com.github.bgabriel998.softwaredevproject;

import org.junit.jupiter.api.Test;
import org.osmdroid.bonuspack.location.POI;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;

import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;


public class GeonamesHandlerUnitTest {
    @Test
    public void init_handler_correct() {
        GeonamesHandler handler = new GeonamesHandler("bgabrie1");
        assertNotNull(handler);
    }

    @Test
    public void init_handler_incorrect() {
        assertThrows(IllegalArgumentException.class, () -> {
            GeonamesHandler handler = new GeonamesHandler("");
        });
    }

    @Test
    public void handler_getPOI_incorrect() {
        assertThrows(IllegalArgumentException.class, () -> {
            GeonamesHandler handler = new GeonamesHandler("bgabrie1");
            handler.getPOI(null);
        });
    }

   /* @Test
    public void handler_getPOI_correct() {
        GeonamesHandler handler = new GeonamesHandler("bgabrie1");
        GeoPoint point = new GeoPoint(45.9258378624377, 6.878492964884342);
        ArrayList<POI> result = handler.getPOI(point);
        assertThat(result.size(), greaterThan(10));
    }

   /* @Test
    public void handler_filterPOI_correct() {
        GeonamesHandler handler = new GeonamesHandler("bgabrie1");
        GeoPoint location = new GeoPoint(45.9258378624377, 6.878492964884342);
        ArrayList<POI> queryResult = handler.getPOI(location);
        ArrayList<POI> filtered = handler.filterPOI(queryResult);
        for(POI point : filtered){
            assertEquals(point.mCategory,"mountain");
        }
    }*/

}
