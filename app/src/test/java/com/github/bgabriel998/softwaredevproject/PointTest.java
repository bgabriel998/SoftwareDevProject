package com.github.bgabriel998.softwaredevproject;

import com.github.bgabriel998.softwaredevproject.map.Point;

import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;

public class PointTest {

    @Test
    public void zeroDistanceTest() {

        Point point1 = new Point(0.0, 0.0, 0.0);
        Point point2 = new Point(0.0, 0.0, 0.0);

        double expected = 0.0;
        double delta = 0.0;

        assertEquals(expected, point1.computeDistance(point2), delta);

    }

    @Test
    public void verticalDistanceTest() {

        Point point1 = new Point(0.0, 0.0, 1000.0);
        Point point2 = new Point(0.0, 0.0, 0.0);

        double expected = 1000.0;
        double delta = 0.0;

        assertEquals(expected, point1.computeDistance(point2), delta);

    }

    @Test
    public void commutativeDistanceTest() {

        Random rand = new Random();

        Point point1 = new Point(rand.nextDouble(), rand.nextDouble(), rand.nextDouble());
        Point point2 = new Point(rand.nextDouble(), rand.nextDouble(), rand.nextDouble());

        double delta = 0.0;

        assertEquals(point2.computeDistance(point1), point1.computeDistance(point2), delta);

    }

    @Test
    public void testDistance() {

        Point point1 = new Point(0.0, 0.0, 0.0);
        Point point2 = new Point(0.5, 0.5, 0.0);

        double expected = 79000.0;
        double delta = 500.0;

        assertEquals(expected, point1.computeDistance(point2), delta);

    }

}
