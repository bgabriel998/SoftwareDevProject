package com.github.ravifrancesco.softwaredevproject;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;

import org.junit.Test;
import org.junit.runner.manipulation.Ordering;

import javax.crypto.spec.GCMParameterSpec;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class GPSTest {

    @Test
    public void testGetLocation() {

        Context mContext = mock(Context.class);

        UserPoint userPoint = new UserPoint(mContext);
        GPSTracker mGpsTracker = new GPSTracker(mContext, userPoint);


        assertEquals(27.988056, mGpsTracker.getLatitude(), 0);
        assertEquals(86.925278, mGpsTracker.getLongitude(), 0);
        assertEquals(8848.86, mGpsTracker.getAltitude(), 0);
        assertEquals(0.0, mGpsTracker.getAccuracy(), 0);

    }

}
