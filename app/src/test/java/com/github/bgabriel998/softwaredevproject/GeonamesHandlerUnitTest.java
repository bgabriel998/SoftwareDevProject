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

}
