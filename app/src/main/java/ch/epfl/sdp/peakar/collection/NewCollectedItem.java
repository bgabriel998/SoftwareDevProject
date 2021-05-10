package ch.epfl.sdp.peakar.collection;

import java.util.Locale;

import ch.epfl.sdp.peakar.utils.UIUtils;

/**
 * Item holding all information regarding a collected peak.
 * TODO Rename to remove New part
 */
public class NewCollectedItem implements Comparable< NewCollectedItem >{
    private final String name;
    private final int points;
    private final int height;
    private final boolean topInCountry;
    private final float longitude;
    private final float latitude;
    private final String date;

    /**
     * Constructor
     * @param name of collected.
     * @param points given by collected.
     * @param height of collected.
     * @param topInCountry true if collected is highest in the country.
     * @param longitude of collected.
     * @param latitude of collected.
     * @param date item got collected.
     */
    public NewCollectedItem(String name,int points, int height, boolean topInCountry,
                            float longitude, float latitude, String date) {
        this.name = name;
        this.points = points;
        this.height = height;
        this.topInCountry = topInCountry;
        this.longitude = longitude;
        this.latitude = latitude;
        this.date = date;
    }

    /**
     * @return collected name.
     */
    public String getName() {
        return name;
    }

    /**
     * @return collected points.
     */
    public Integer getPoints() {
        return points;
    }

    public boolean isTopInCountry() {
        return topInCountry;
    }

    /**
     * Sort collection of CollectedItem by points
     * @param c other collectedItem
     * @return collected item with highest number of points
     */
    @Override
    public int compareTo(NewCollectedItem c){
        return this.getPoints().compareTo(c.getPoints());
    }

    /**
     * @return the points in text format how it should be displayed.
     */
    public String getPointsText() {
        return "Points: " + UIUtils.IntegerConvert(points);
    }

    /**
     * @return the height in text format how it should be displayed.
     */
    public String getHeightText() {
        return UIUtils.IntegerConvert(height) + "m";
    }

    /**
     * @return the position in text format how it should be displayed.
     */
    public String getPositionText() {
        return String.format(Locale.getDefault(), "Position: (%.2f, %.2f)", longitude, latitude);
    }

    /**
     * @return the date in text format how it should be displayed.
     */
    public String getDateText() {
        return "Date: " + date;
    }
}

