package ch.epfl.sdp.peakar.collection;

/**
 * Item holding all information regarding a collected peak.
 */
public class NewCollectedItem implements Comparable< NewCollectedItem >{
    private final String name;
    private final int points;
    private final int height;
    private final boolean topInCountry;
    private final float longitude;
    private final float latitude;
    private final String date;
    private final String country;

    /**
     * Constructor
     * @param name of collected.
     * @param points given by collected.
     * @param height of collected.
     * @param topInCountry true if collected is highest in the country.
     * @param longitude of collected.
     * @param latitude of collected.
     * @param date item got collected.
     * @param country of collected.
     */
    public NewCollectedItem(String name,int points, int height, boolean topInCountry,
                            float longitude, float latitude, String date, String country) {
        this.name = name;
        this.points = points;
        this.height = height;
        this.topInCountry = topInCountry;
        this.longitude = longitude;
        this.latitude = latitude;
        this.date = date;
        this.country = country;
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

    /**
     * @return true if collected is top in country
     */
    public boolean isTopInCountry() {
        return topInCountry;
    }

    /**
     * @return collected height
     */
    public int getHeight() {
        return height;
    }

    /**
     * @return collected longitude
     */
    public float getLongitude() {
        return longitude;
    }

    /**
     * @return collected latitude
     */
    public float getLatitude() {
        return latitude;
    }

    /**
     * @return collected date
     */
    public String getDate() {
        return date;
    }

    /**
     * @return collected country
     */
    public String getCountry() {
        return country;
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
}

