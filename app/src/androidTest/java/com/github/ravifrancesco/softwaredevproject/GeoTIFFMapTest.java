package com.github.ravifrancesco.softwaredevproject;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.rule.ServiceTestRule;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import java.util.concurrent.TimeoutException;

public class GeoTIFFMapTest {

    /*

    @Rule
    public final ServiceTestRule serviceRule = new ServiceTestRule();

    @Test
    public void geoTIFFConstructorTest() throws TimeoutException {
        serviceRule.startService(
                new Intent(ApplicationProvider.getApplicationContext(),
                        GPSTracker.class));

        Context mContext = ApplicationProvider.getApplicationContext();

        UserPoint userPoint = new UserPoint(mContext);

        // testing if the constructor works correctly
        GeoTIFFMap geoTIFFMap = new GeoTIFFMap(userPoint);

        Assert.assertTrue(true);

    }

    @Test
    public void getTopographyMapBitmap() throws TimeoutException {
        serviceRule.startService(
                new Intent(ApplicationProvider.getApplicationContext(),
                        GPSTracker.class));

        Context mContext = ApplicationProvider.getApplicationContext();

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
        serviceRule.startService(
                new Intent(ApplicationProvider.getApplicationContext(),
                        GPSTracker.class));

        Context mContext = ApplicationProvider.getApplicationContext();

        UserPoint userPoint = new UserPoint(mContext);
        userPoint.setLocation(GPSTracker.DEFAULT_LAT, GPSTracker.DEFAULT_LON,GPSTracker.DEFAULT_ALT, GPSTracker.DEFAULT_ACC);

        GeoTIFFMap geoTIFFMap = new GeoTIFFMap(userPoint);

        // getting the map for the everest location
        Bitmap topographyMapBitmapA = geoTIFFMap.getTopographyMapBitmap();

        // setting the location to < GeoTIFFMap.MINIMUM_DISTANCE_FOR_UPDATE
        userPoint.setLocation(27.994601, 86.933163, 0,0);

        // checking if the map returned is the same
        Bitmap topographyMapBitmapB = geoTIFFMap.getTopographyMapBitmap();
        Assert.assertTrue(topographyMapBitmapA.sameAs(topographyMapBitmapB));

    }

     */

}
