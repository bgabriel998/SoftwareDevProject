package com.github.giommok.softwaredevproject;

public class CacheEntry {

    private String countryHighPoint; //Country highest peak
    private long highPointHeight; // Country highest peak height

    /**
     * Class constructor -> initialises attributes
     * @param countryHighPoint Country highest peak name
     * @param highPointHeight Country highest peak height
     */
    public CacheEntry(String countryHighPoint,long highPointHeight){
        this.countryHighPoint = countryHighPoint;
        this.highPointHeight = highPointHeight;
    }

    /**
     * getCountryHighPoint
     * @return country high point name
     */
    public String getCountryHighPoint() {
        return countryHighPoint;
    }

    /**
     * Sets country high point name using input string
     * @param countryHighPoint Country highest peak
     */
    public void setCountryHighPoint(String countryHighPoint) {
        this.countryHighPoint = countryHighPoint;
    }

    /**
     * getHighPointHeight
     * @return country high point height
     */
    public long getHighPointHeight() {
        return highPointHeight;
    }

    /**
     * Sets country high point height using input string
     * @param highPointHeight country high point height
     */
    public void setHighPointHeight(long highPointHeight) {
        this.highPointHeight = highPointHeight;
    }


}
