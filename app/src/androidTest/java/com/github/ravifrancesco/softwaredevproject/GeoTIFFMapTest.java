package com.github.ravifrancesco.softwaredevproject;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Looper;
import android.util.Log;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ServiceTestRule;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import java.util.concurrent.TimeoutException;

public class GeoTIFFMapTest {

    @Test
    public void geoTIFFConstructorTest() throws TimeoutException {

        Context mContext = InstrumentationRegistry.getInstrumentation().getContext();

        UserPoint userPoint = new UserPoint(mContext);

        // testing if the constructor works correctly
        GeoTIFFMap geoTIFFMap = new GeoTIFFMap(userPoint);

        Assert.assertTrue(true);

    }

    @Test
    public void getTopographyMapBitmap() throws TimeoutException, InterruptedException {

        Context mContext = InstrumentationRegistry.getInstrumentation().getContext();

        // setting everest location
        UserPoint userPoint = new UserPoint(mContext);
        userPoint.setLocation(GPSTracker.DEFAULT_LAT, GPSTracker.DEFAULT_LON,GPSTracker.DEFAULT_ALT, GPSTracker.DEFAULT_ACC);

        GeoTIFFMap geoTIFFMap = new GeoTIFFMap(userPoint);

        // checking if there are no errors retrieving the image
        Bitmap topographyMapBitmap = geoTIFFMap.getTopographyMapBitmap();
        Assert.assertFalse(topographyMapBitmap == null);

    }

    @Test
    public void updateMapTest() throws TimeoutException {

        Context mContext = InstrumentationRegistry.getInstrumentation().getContext();

        UserPoint userPoint = new UserPoint(mContext);
        userPoint.setLocation(GPSTracker.DEFAULT_LAT, GPSTracker.DEFAULT_LON,GPSTracker.DEFAULT_ALT, GPSTracker.DEFAULT_ACC);

        GeoTIFFMap geoTIFFMap = new GeoTIFFMap(userPoint);

        // getting the map for the everest location
        Bitmap topographyMapBitmapA = geoTIFFMap.getTopographyMapBitmap();

        // setting the location to < GeoTIFFMap.MINIMUM_DISTANCE_FOR_UPDATE
        userPoint.setLocation(27.994601, 86.933163, 0,0);

        // checking if the map returned is the same
        Bitmap topographyMapBitmapB = geoTIFFMap.getTopographyMapBitmap();
        Assert.assertFalse(topographyMapBitmapA.sameAs(topographyMapBitmapB));

    }

}
