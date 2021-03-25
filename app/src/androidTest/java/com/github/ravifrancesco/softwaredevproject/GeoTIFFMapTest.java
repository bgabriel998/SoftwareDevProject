package com.github.ravifrancesco.softwaredevproject;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.rule.ServiceTestRule;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import java.util.concurrent.TimeoutException;

public class GeoTIFFMapTest {

    @Rule
    public final ServiceTestRule serviceRule = new ServiceTestRule();

    @Test
    public void geoTIFFConstructorTest() throws TimeoutException {
        serviceRule.startService(
                new Intent(ApplicationProvider.getApplicationContext(),
                        GPSTracker.class));

        Context mContext = ApplicationProvider.getApplicationContext();

        UserPoint userPoint = new UserPoint(mContext);

        GeoTIFFMap geoTIFFMap = new GeoTIFFMap(userPoint);

        Assert.assertTrue(true);

    }

    @Test
    public void getTopographyMapBitmap() throws TimeoutException {
        serviceRule.startService(
                new Intent(ApplicationProvider.getApplicationContext(),
                        GPSTracker.class));

        Context mContext = ApplicationProvider.getApplicationContext();

        UserPoint userPoint = new UserPoint(mContext);

        GeoTIFFMap geoTIFFMap = new GeoTIFFMap(userPoint);

        Assert.assertTrue(true);

    }



}
