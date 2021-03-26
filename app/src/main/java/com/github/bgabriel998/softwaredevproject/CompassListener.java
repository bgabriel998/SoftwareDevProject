package com.github.bgabriel998.softwaredevproject;

/**
 * Interface for the CompassListener
 */
public interface CompassListener {
    /**
     * Gets the headings in degree
     * @param heading horizontal heading
     * @param headingV vertical heading
     */
    void onNewHeading(float heading, float headingV);
}
