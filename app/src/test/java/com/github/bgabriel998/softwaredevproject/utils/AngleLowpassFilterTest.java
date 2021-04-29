package com.github.bgabriel998.softwaredevproject.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AngleLowpassFilterTest {
    private static final int QUEUE_LENGTH = 5;

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

        //Fill the queue of the filter with values from 0 until Pi with steps of PI/4
        for(int i=0; i<QUEUE_LENGTH; i++){
            //Increase everytime angle by PI/4
            lowpassFilter.add((float) (i*Math.PI/4));
        }
        assertEquals(Math.PI/2, lowpassFilter.average(), 0.01);

        //Add a few elements to check that the oldest elements are removed
        for(int i=0; i<QUEUE_LENGTH; i++){
            //Continue increasing angle by PI/4
            lowpassFilter.add((float) ((i+5)*Math.PI/4));
            //Average angle should move by PI/4 everytime incrementing the angle by PI/4
            if((i+3)*Math.PI/4 <= Math.PI){
                assertEquals((i+3)*Math.PI/4, lowpassFilter.average(), 0.01);
            }
            //AngleLowPassFilter returns values between -180° and 180°, so we need to check the negative
            //values when the result is greater then PI
            else{
                assertEquals((i-QUEUE_LENGTH)*Math.PI/4, lowpassFilter.average(), 0.01);
            }
        }
    }
}
