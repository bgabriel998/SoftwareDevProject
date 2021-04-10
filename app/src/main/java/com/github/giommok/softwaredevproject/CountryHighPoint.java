package com.github.giommok.softwaredevproject;


public class CountryHighPoint {


    private String countryName;
    private String countryHighPoint; //Country highest peak
    private long highPointHeight; // Country highest peak height

    /**
     * Class constructor -> initialises attributes to null/zero
     */
    public CountryHighPoint(){
        this.countryName = null;
        this.countryHighPoint = null;
        this.highPointHeight = 0;
    }
    /**
     * Class constructor -> initialises attributes
     * @param countryHighPoint Country highest peak name
     * @param highPointHeight Country highest peak height
     */
    public CountryHighPoint(String countryHighPoint, long highPointHeight){
        this.countryHighPoint = countryHighPoint;
        this.highPointHeight = highPointHeight;
    }

    /**
     * Class constructor -> initialises attributes
     * @param countryName country name
     * @param countryHighPoint name of the highest peak in the country
     * @param highPointHeight height of the highest peak in the country
     */
    public CountryHighPoint(String countryName, String countryHighPoint, long highPointHeight){
        this.countryHighPoint = countryHighPoint;
        this.highPointHeight = highPointHeight;
        this.countryName = countryName;
    }

    /**
     * @return country Name
     */
    public String getCountryName() {
        return countryName;
    }

    /**
     * set country name
     * @param countryName name of the country
     */
    public void setCountryName(String countryName) {
        this.countryName = countryName;
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


    /**
     * Convert object to a string listing all attributes and their values
     * @return String containing objects attributes and values
     */
    public String toString(){
        return countryName + ": " + countryHighPoint + " -> " + highPointHeight + " m";
    }

}
