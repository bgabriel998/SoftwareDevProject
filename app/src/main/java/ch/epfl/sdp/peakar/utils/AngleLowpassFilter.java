package ch.epfl.sdp.peakar.utils;

import java.util.ArrayDeque;

/**
 * Lowpass filter for angles. Converts radians into sum of sine and cosine to overcome the problem
 * that angle values are cyclic. For example when the angles changes from 2*pi to 0 or from -pi to pi
 * See <a href="StackOverflow">https://stackoverflow.com/a/18911252</a>
 */
public class AngleLowpassFilter {
    //Defines the length of the queue
    private static final int LENGTH = 5;

    private float sumSin, sumCos;

    private final ArrayDeque<Float> queue = new ArrayDeque<>();

    /**
     * Adds an element to the queue and expresses the angle through their sine and cosine values
     * @param radians angle that gets added
     */
    public void add(float radians){
        sumSin += (float) Math.sin(radians);
        sumCos += (float) Math.cos(radians);
        queue.add(radians);

        //If the queue is greater then the defined length
        if(queue.size() > LENGTH){
            //remove the oldest element
            float old = queue.poll();
            //Substract their sine and cosine from the sine and cosine sum
            sumSin -= Math.sin(old);
            sumCos -= Math.cos(old);
        }
    }

    /**
     * Returns an average of the queue
     * @return average angle of the queue
     */
    public float average(){
        int size = queue.size();
        return (float) Math.atan2(sumSin/size, sumCos/size);
    }
}
