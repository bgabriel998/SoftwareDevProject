package com.github.bgabriel998.softwaredevproject;

/**
 * Item holding name of collected,
 * collected points, collected height and position.
 * Used to fill collection list.
 */
public class CollectedItem {
    private String name;
    private int points;
    private int height;
    private float longitude;
    private float latitude;

    /**
     * Constructor
     * @param name of collected
     * @param points given by collected
     * @param height of collected
     * @param longitude of collected
     * @param latitude of collected
     */
    public CollectedItem(String name, int points, int height, float longitude, float latitude) {
        this.name = name;
        this.points = points;
        this.height = height;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public String getName() {
        return name;
    }

    public int getPoints() {
        return points;
    }

    public int getHeight() {
        return height;
    }

    public float getLongitude() {
        return longitude;
    }

    public float getLatitude() {
        return latitude;
    }
}
