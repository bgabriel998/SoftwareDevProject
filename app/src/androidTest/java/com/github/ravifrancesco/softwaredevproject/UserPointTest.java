package com.github.ravifrancesco.softwaredevproject;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ServiceTestRule;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import java.util.Random;
import java.util.concurrent.TimeoutException;


public class UserPointTest {

    @Test
    public void getLocationTest() {

        Context mContext = InstrumentationRegistry.getInstrumentation().getContext();

        UserPoint userPoint = new UserPoint(mContext);

        // check if customLocation is off
        Assert.assertFalse(userPoint.isCustomLocation());

        // if the gps tracker is able to get the location, the test passes, otherwise
        // it will check if the default location is returned correctly
        if (userPoint.canGetLocation()) {
            return;
        } else {
            Assert.assertEquals(27.988056, userPoint.getLatitude(), 0);
            Assert.assertEquals(86.925278, userPoint.getLongitude(), 0);
            Assert.assertEquals(8848.86, userPoint.getAltitude(), 0);
            Assert.assertEquals(0.0, userPoint.getAccuracy(), 0);
        }

    }

    @Test
    public void setLocationTest() throws TimeoutException {

        Context mContext = InstrumentationRegistry.getInstrumentation().getContext();

        UserPoint userPoint = new UserPoint(mContext);

        Random r = new Random();

        double lat = r.nextDouble();
        double lon = r.nextDouble();
        double alt = r.nextDouble();
        double acc = r.nextDouble();

        // set the location to random location
        userPoint.setLocation(lat,lon,alt,acc);

        // check if the location is set correctly
        Assert.assertEquals(lat, userPoint.getLatitude(), 0);
        Assert.assertEquals(lon, userPoint.getLongitude(), 0);
        Assert.assertEquals(alt, userPoint.getAltitude(), 0);
        Assert.assertEquals(acc, userPoint.getAccuracy(), 0);
        // check if customLocation is enabled
        Assert.assertTrue(userPoint.isCustomLocation());

        // re enables real location
        userPoint.switchToRealLocation();

        // if the gps tracker is able to get the location, the test passes, otherwise
        // it will check if the default location is returned correctly
        if (!userPoint.canGetLocation()) {
            Assert.assertEquals(27.988056, userPoint.getLatitude(), 0);
            Assert.assertEquals(86.925278, userPoint.getLongitude(), 0);
            Assert.assertEquals(8848.86, userPoint.getAltitude(), 0);
            Assert.assertEquals(0.0, userPoint.getAccuracy(), 0);
        }

        // check if customLocation is off
        Assert.assertFalse(userPoint.isCustomLocation());

    }

}
