package com.github.ravifrancesco.softwaredevproject;


import android.app.Service;
import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.MediumTest;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;


@RunWith(AndroidJUnit4.class)
@MediumTest
public class GPSTrackerTest {

    @Test
    public void testGPSTracker() throws TimeoutException {

        Context mContext = InstrumentationRegistry.getInstrumentation().getContext();

        UserPoint userPoint = new UserPoint(mContext);
        GPSTracker mGpsTracker = new GPSTracker(mContext, userPoint);

        // if the gps tracker is able to get the location, the test passes, otherwise
        // it will check if the default location is returned correctly
        if (mGpsTracker.canGetLocation()) {
            return;
        } else {
            assertEquals(27.988056, mGpsTracker.getLatitude(), 0);
            assertEquals(86.925278, mGpsTracker.getLongitude(), 0);
            assertEquals(8848.86, mGpsTracker.getAltitude(), 0);
            assertEquals(0.0, mGpsTracker.getAccuracy(), 0);
        }

    }


}
