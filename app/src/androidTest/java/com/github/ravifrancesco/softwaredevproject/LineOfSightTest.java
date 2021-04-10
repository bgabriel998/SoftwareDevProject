package com.github.ravifrancesco.softwaredevproject;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Assert;
import org.junit.Test;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

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
        point1.setName("point1");
        point2.setName("point2");
        visiblePoints.add(point1);
        visiblePoints.add(point2);

        // points that should not be visible
        POIPoint point3 = new POIPoint(new GeoPoint(27.951538, 86.928781, 6400));
        POIPoint point4 = new POIPoint(new GeoPoint(27.987947, 86.933671, 8000));
        point3.setName("point3");
        point4.setName("point4");


        pointsToCheck.add(point1);
        pointsToCheck.add(point2);
        pointsToCheck.add(point3);
        pointsToCheck.add(point4);

        Assert.assertEquals(new HashSet<POIPoint>(lineOfSight.getVisiblePoints(pointsToCheck)), new HashSet<POIPoint>(visiblePoints));

    }

    @Test
    public void getVisiblePointsLabeledTest() {

        Context mContext = ApplicationProvider.getApplicationContext();

        // setting location near everest peak
        UserPoint userPoint = new UserPoint(mContext);
        userPoint.setLocation(28.000490, 86.921267,7500, 0);

        LineOfSight lineOfSight = new LineOfSight(userPoint);

        // PoiPoints to check
        List<POIPoint> pointsToCheck = new ArrayList<>();
        POIPoint point1 = new POIPoint(new GeoPoint(28.011581, 86.907036, 6200));
        POIPoint point2 = new POIPoint(new GeoPoint(28.017394, 86.922196, 7000));
        point1.setName("point1");
        point2.setName("point2");
        // points that should not be visible
        POIPoint point3 = new POIPoint(new GeoPoint(27.951538, 86.928781, 6400));
        POIPoint point4 = new POIPoint(new GeoPoint(27.987947, 86.933671, 8000));
        point3.setName("point3");
        point4.setName("point4");

        pointsToCheck.add(point1);
        pointsToCheck.add(point2);
        pointsToCheck.add(point3);
        pointsToCheck.add(point4);

        Map<POIPoint, Boolean> labeledPOIPoints = new HashMap<>();
        labeledPOIPoints.put(point1, true);
        labeledPOIPoints.put(point2, true);
        labeledPOIPoints.put(point3, false);
        labeledPOIPoints.put(point4, false);

        Assert.assertEquals(labeledPOIPoints, lineOfSight.getVisiblePointsLabeled(pointsToCheck));

    }

}
