package com.github.ravifrancesco.softwaredevproject;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.rule.ServiceTestRule;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import java.util.Random;
import java.util.concurrent.TimeoutException;


public class UserPointTest {

    @Rule
    public final ServiceTestRule serviceRule = new ServiceTestRule();

    @Test
    public void getLocationTest() throws TimeoutException {

        serviceRule.startService(
                new Intent(ApplicationProvider.getApplicationContext(),
                        UserPoint.class));

        Context mContext = ApplicationProvider.getApplicationContext();

        UserPoint userPoint = new UserPoint(mContext);

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

        serviceRule.startService(
                new Intent(ApplicationProvider.getApplicationContext(),
                        UserPoint.class));

        Context mContext = ApplicationProvider.getApplicationContext();

        UserPoint userPoint = new UserPoint(mContext);

        Random r = new Random();

        double lat = r.nextDouble();
        double lon = r.nextDouble();
        double alt = r.nextDouble();
        double acc = r.nextDouble();

        if (!userPoint.canGetLocation()) {
            Assert.assertEquals(27.988056, userPoint.getLatitude(), 0);
            Assert.assertEquals(86.925278, userPoint.getLongitude(), 0);
            Assert.assertEquals(8848.86, userPoint.getAltitude(), 0);
            Assert.assertEquals(0.0, userPoint.getAccuracy(), 0);
        }

        userPoint.setLocation(lat,lon,alt,acc);

        Assert.assertEquals(lat, userPoint.getLatitude(), 0);
        Assert.assertEquals(lon, userPoint.getLongitude(), 0);
        Assert.assertEquals(alt, userPoint.getAltitude(), 0);
        Assert.assertEquals(acc, userPoint.getAccuracy(), 0);

        userPoint.switchToRealLocation();

        if (!userPoint.canGetLocation()) {
            Assert.assertEquals(27.988056, userPoint.getLatitude(), 0);
            Assert.assertEquals(86.925278, userPoint.getLongitude(), 0);
            Assert.assertEquals(8848.86, userPoint.getAltitude(), 0);
            Assert.assertEquals(0.0, userPoint.getAccuracy(), 0);
        }

    }

}
