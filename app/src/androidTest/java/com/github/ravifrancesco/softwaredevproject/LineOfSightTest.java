package com.github.ravifrancesco.softwaredevproject;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Assert;
import org.junit.Test;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class LineOfSightTest {

    @Test
    public void getVisiblePointsTest() {

        Context mContext = ApplicationProvider.getApplicationContext();

        // setting location near everest peak
        UserPoint userPoint = new UserPoint(mContext);
        userPoint.setLocation(28.000490, 86.921267,7500, 0);

        LineOfSight lineOfSight = new LineOfSight(userPoint);

        // PoiPoints to check
        List<POIPoint> pointsToCheck = new ArrayList<>();
        // POIPoints that should be visible
        List<POIPoint> visiblePoints = new ArrayList<>();
        POIPoint point1 = new POIPoint(new GeoPoint(28.011581, 86.907036, 6200));
        POIPoint point2 = new POIPoint(new GeoPoint(28.017394, 86.922196, 7000));
        visiblePoints.add(point1);
        visiblePoints.add(point2);

        // points that should not be visible
        POIPoint point3 = new POIPoint(new GeoPoint(27.951538, 86.928781, 6400));
        POIPoint point4 = new POIPoint(new GeoPoint(27.987947, 86.933671, 8000));

        pointsToCheck.add(point1);
        pointsToCheck.add(point2);
        pointsToCheck.add(point3);
        pointsToCheck.add(point4);

        Assert.assertEquals(new HashSet<POIPoint>(lineOfSight.getVisiblePoints(pointsToCheck)), new HashSet<POIPoint>(visiblePoints));

    }

}
