package com.github.bgabriel998.softwaredevproject;

import android.Manifest;
import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.Intents;
import androidx.test.rule.GrantPermissionRule;

import com.github.ravifrancesco.softwaredevproject.Point;
import com.github.ravifrancesco.softwaredevproject.UserPoint;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static java.lang.Double.NaN;
import static org.junit.Assert.assertEquals;

public class ComputePOIPointsTest {

    @Rule
    public GrantPermissionRule grantCameraPermissionRule = GrantPermissionRule.grant(Manifest.permission.CAMERA);
    private UserPoint userPoint;

    @Before
    public void setup(){
        Intents.init();
        Context context = ApplicationProvider.getApplicationContext();
        userPoint = new UserPoint(context);
        userPoint.update();
    }

    @After
    public void cleanUp(){
        Intents.release();
    }

    /**
     * Compute the horizontal bearing between the userPoint and 4 points each one degree to the
     * north, east, south and west
     */
    @Test
    public void computeHorizontalBearing(){
        Point northPoint = new Point(userPoint.getLatitude() + 1, userPoint.getLongitude(), userPoint.getAltitude());
        Point eastPoint = new Point(userPoint.getLatitude(), userPoint.getLongitude() + 1, userPoint.getAltitude());
        Point southPoint = new Point(userPoint.getLatitude() - 1, userPoint.getLongitude(), userPoint.getAltitude());
        Point westPoint = new Point(userPoint.getLatitude(), userPoint.getLongitude() - 1, userPoint.getAltitude());

        double horizontalBearing = ComputePOIPoints.getHorizontalBearing(userPoint, northPoint);
        assertEquals(0, horizontalBearing, 1);
        horizontalBearing = ComputePOIPoints.getHorizontalBearing(userPoint, eastPoint);
        assertEquals(90, horizontalBearing, 1);
        horizontalBearing = ComputePOIPoints.getHorizontalBearing(userPoint, southPoint);
        assertEquals(180, horizontalBearing, 1);
        horizontalBearing = ComputePOIPoints.getHorizontalBearing(userPoint, westPoint);
        assertEquals(270, horizontalBearing, 1);
    }

    /**
     * Compute the vertical bearing between the userPoint and 4 points above, below, beside and the same point
     */
    @Test
    public void computeVerticalBearing(){
        Point above = new Point(userPoint.getLatitude(), userPoint.getLongitude(), userPoint.getAltitude() + 1000);
        Point below = new Point(userPoint.getLatitude(), userPoint.getLongitude(), 0);
        Point sameHeight = new Point(userPoint.getLatitude()+0.1, userPoint.getLongitude(), userPoint.getAltitude());
        Point samePoint = new Point(userPoint.getLatitude(), userPoint.getLongitude(), userPoint.getAltitude());

        double verticalBearing = ComputePOIPoints.getVerticalBearing(userPoint, above);
        assertEquals(180, verticalBearing, 1);

        verticalBearing = ComputePOIPoints.getVerticalBearing(userPoint, below);
        assertEquals(0, verticalBearing, 1);

        verticalBearing = ComputePOIPoints.getVerticalBearing(userPoint, sameHeight);
        assertEquals(90, verticalBearing, 1);

        verticalBearing = ComputePOIPoints.getVerticalBearing(userPoint, samePoint);
        assertEquals(NaN, verticalBearing, 1);
    }
}
