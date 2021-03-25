package com.github.giommok.softwaredevproject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import com.github.ravifrancesco.softwaredevproject.POIPoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class UserScore {

    private ArrayList<String> discoveredCountryHighestPeaks = null;
    private ArrayList<Integer> discoveredHeights = null;
    private HashMap<String,CacheEntry> databaseCache = null;
    private DataBaseHelper dataBaseHelper = null;
    private final int userID;

    private final Context mContext;


    private static final int COUNTRY_HIGHEST_PEAK_DETECTION_TOLERANCE = 20;

    /**
     * initialises countryHighPoint database
     * initialises cache
     */
    public UserScore(Context context, int userID){
        this.mContext = context;
        this.userID = userID;
        discoveredCountryHighestPeaks = retrieveDiscoveredCountryHighPoints();
        discoveredHeights = retrieveDiscoveredHeights();

        databaseCache = new HashMap<String,CacheEntry>();
        if(dataBaseHelper == null){
            dataBaseHelper = new DataBaseHelper(context);
        }
    }

    /**
     * Compute user bonus.
     * If the user discovers the highest peak in a country
     * a bonus is added to the his score
     * @return bonus to add
     */
    private long computeUserBonus(POIPoint poiPoint){
        long retValue = 0;
        dataBaseHelper.openDataBase();
        /*Search country*/
        String country = getCountryFromCoordinates(poiPoint.getLatitude(), poiPoint.getLongitude());
        if(country != null){
            CacheEntry countryInfo = getDataFromCache(country);
            if (countryInfo == null) {
                //Value was not in the cache
                //Retrieve info to cache it
                countryInfo = new CacheEntry(dataBaseHelper.queryHighestPeakName(country),
                        dataBaseHelper.queryHighestPeakHeight(country));
                databaseCache.put(country,countryInfo);
            }
            //Check if the current peak is one country high point
            if(comparePeakNameWithCacheData(countryInfo, poiPoint))
                retValue += ScoringConstants.BONUS_COUNTRY_TALLEST_PEAK;
                updateUserHighestPeaksCountryDiscovered(countryInfo.getCountryHighPoint());
        }
        dataBaseHelper.close();
        return retValue;
    }

    /**
     * Compare the poi peak name with the given cache entry name
     * @param countryInfo cache entry containing highest point name from the same country as poiPoint
     * @param poiPoint peak retrieved with provider
     * @return true if the comparison matches
     */
    private boolean comparePeakNameWithCacheData(CacheEntry countryInfo, POIPoint poiPoint){
        if((countryInfo.getCountryHighPoint().contains(poiPoint.getName()) ||
                poiPoint.getName().contains(countryInfo.getCountryHighPoint()))
                && Math.abs(poiPoint.getAltitude() - countryInfo.getHighPointHeight()) < COUNTRY_HIGHEST_PEAK_DETECTION_TOLERANCE)
            return true;
        else return false;
    }

    /**
     * getDataFromCache try to get data from cache. If entry is not present
     * in the cache, the function returns null
     * @param country country name to get its highest point
     * @return CacheEntry containing
     */
    private CacheEntry getDataFromCache(String country){
        if(databaseCache.containsKey(country)){
            return databaseCache.get(country);
        }
        else return null;
    }


    /**
     * Computes the new user score given the list of freshly scanned peaks
     * @param scannedPeaks peaks the user scanned
     * @return new user Score
     */
    public long computeUserScore(ArrayList<POIPoint> scannedPeaks){
        //Get previous score from user database
        long userScore = retrieveUserScore();
        //Foreach loop over all scanned peaks
        for(POIPoint poiPoint : scannedPeaks){
            /*Add classical amount of points*/
            userScore += poiPoint.getAltitude() * ScoringConstants.PEAK_FACTOR;
            userScore += computeUserBonus(poiPoint);
        }
        return userScore;
    }

    /**
     * Retrieve previous score from user database
     * @return user score
     */
    public long retrieveUserScore(){
        //TODO : implement this method
        return 0;
    }


    /**
     * Updates user score in the database
     * @param userScore new user score
     */
    public void updateUserScore( long userScore){
        //TODO Update the user score in the database
    }

    /**
     * Updates the user database with the newly discovered highpoint
     * @param PeakName name of the peak that is the highest of the country
     */
    public void updateUserHighestPeaksCountryDiscovered(String PeakName){
        //TODO Update the user score in the database
    }


    /**
     * Function that queries the user database. This function returns which
     * country highest point the use has discovered
     * @return Array List containing country High Points Names
     */
    private ArrayList<String> retrieveDiscoveredCountryHighPoints(){
        //TODO Query the database and retrieve user info
        return null;
    }

    /**
     * Function that queries the user database. This function returns which
     * height range has already been discovered by the user
     * (1000m, 2000m, 3000m, 4000m, 5000m, 6000m, 7000m and 8000m peaks)
     * @return Array list of integers containing the above listed tags
     */
    private ArrayList<Integer> retrieveDiscoveredHeights(){
        //TODO Query the database and retrieve user info
        return null;
    }


    /**
     * Retrieve country using input latitude and longitude
     * @param latitude poiPoint latitude (peak latitude)
     * @param longitude poiPoint longitude (peak longitude)
     * @return Name of the country where the peak is located
     */
    private String getCountryFromCoordinates(double latitude,double longitude){
        Geocoder gcd = new Geocoder(mContext, Locale.forLanguageTag("en"));
        try {
            List<Address> addresses = gcd.getFromLocation(latitude, longitude, 1);
            if (addresses.size() > 0) {
                return addresses.get(0).getCountryName();
            }
        }
        catch(Exception e){
            Log.e("getCountryFromCoordinates", "Can't get country from coordinates");
        }
        return null;
    }


}
