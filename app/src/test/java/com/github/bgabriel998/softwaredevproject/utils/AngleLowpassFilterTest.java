package com.github.bgabriel998.softwaredevproject.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AngleLowpassFilterTest {

    // Tests the average between 0 and PI/2
    @Test
    public void lowpassFilterTest() {
        AngleLowpassFilter lowpassFilter = new AngleLowpassFilter();

        lowpassFilter.add(0);
        assertEquals(0, lowpassFilter.average(), 0.01);

        lowpassFilter.add((float) Math.PI/2);
        assertEquals(Math.PI/4, lowpassFilter.average(), 0.01);
    }

    //Tests that the oldest element of the queue gets removed
    @Test
    public void lowpassFilterFullTest() {
        AngleLowpassFilter lowpassFilter = new AngleLowpassFilter();
        lowpassFilter.add(0);
        float sumSin = 0;
        float sumCos = 1;
        assertEquals(Math.atan2(sumSin, sumCos), lowpassFilter.average(), 0.01);

        lowpassFilter.add((float) Math.PI/2);
        sumSin += 1;
        sumCos += 0;
        assertEquals(Math.atan2(sumSin/2, sumCos/2), lowpassFilter.average(), 0.01);

        lowpassFilter.add((float) Math.PI/2);
        sumSin += Math.sin(Math.PI/2);
        sumCos += Math.cos(Math.PI/2);
        assertEquals(Math.atan2(sumSin/3, sumCos/3), lowpassFilter.average(), 0.01);

        lowpassFilter.add((float) Math.PI/4);
        sumSin += Math.sin(Math.PI/4);
        sumCos += Math.cos(Math.PI/4);
        assertEquals(Math.atan2(sumSin/4, sumCos/4), lowpassFilter.average(), 0.01);

        lowpassFilter.add((float) Math.PI/6);
        sumSin += Math.sin(Math.PI/6);
        sumCos += Math.cos(Math.PI/6);
        assertEquals(Math.atan2(sumSin/5, sumCos/5), lowpassFilter.average(), 0.01);

        lowpassFilter.add((float) Math.PI * 2);
        sumSin += Math.sin(Math.PI*2);
        sumCos += Math.cos(Math.PI*2);
        sumSin -= Math.sin(0);
        sumCos -= Math.cos(0);
        assertEquals(Math.atan2(sumSin/5, sumCos/5), lowpassFilter.average(), 0.01);
    }
}
