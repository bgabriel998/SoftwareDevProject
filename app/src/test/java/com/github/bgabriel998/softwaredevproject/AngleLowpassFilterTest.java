package com.github.bgabriel998.softwaredevproject;

import com.github.bgabriel998.softwaredevproject.utils.AngleLowpassFilter;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AngleLowpassFilterTest {

    @Test
    public void lowpassFilterTest() {
        AngleLowpassFilter lowpassFilter = new AngleLowpassFilter();

        lowpassFilter.add(0);
        assertEquals(0, lowpassFilter.average(), 0.01);

        lowpassFilter.add((float) Math.PI);
        assertEquals(Math.PI/2, lowpassFilter.average(), 0.01);
    }

    @Test
    public void lowpassFilterFullTest() {
        AngleLowpassFilter lowpassFilter = new AngleLowpassFilter();
        lowpassFilter.add(0);
        double sumSin = Math.sin(0);
        double sumCos = Math.sin(0);
        assertEquals(Math.atan2(sumSin, sumCos), lowpassFilter.average(), 0.01);

        lowpassFilter.add((float) Math.PI);
        sumSin += Math.sin(Math.PI);
        sumCos += Math.sin(Math.PI);
        assertEquals(Math.atan2(sumSin/2, sumCos/2), lowpassFilter.average(), 0.01);

        lowpassFilter.add((float) Math.PI/2);
        sumSin += Math.sin(Math.PI/2);
        sumCos += Math.sin(Math.PI/2);
        assertEquals(Math.atan2(sumSin/3, sumCos/3), lowpassFilter.average(), 0.01);

        lowpassFilter.add((float) Math.PI/4);
        sumSin += Math.sin(Math.PI/4);
        sumCos += Math.sin(Math.PI/4);
        assertEquals(Math.atan2(sumSin/4, sumCos/4), lowpassFilter.average(), 0.01);

        lowpassFilter.add((float) Math.PI/6);
        sumSin += Math.sin(Math.PI/6);
        sumCos += Math.sin(Math.PI/6);
        assertEquals(Math.atan2(sumSin/5, sumCos/5), lowpassFilter.average(), 0.01);

        lowpassFilter.add((float) Math.PI * 2);
        sumSin += Math.sin(Math.PI/4);
        sumCos += Math.sin(Math.PI/4);
        sumSin -= Math.sin(0);
        sumCos -= Math.sin(0);
        assertEquals(Math.atan2(sumSin/5, sumCos/5), lowpassFilter.average(), 0.01);
    }
}
