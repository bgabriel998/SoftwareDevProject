package com.github.giommok.softwaredevproject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import com.github.ravifrancesco.softwaredevproject.POIPoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

public class UserScore {


    private HashMap<String,CacheEntry> databaseCache = null;
    private DataBaseHelper dataBaseHelper = null;
    private final FirebaseAccount fireBaseAccount;

    private final Context mContext;


    private static final int COUNTRY_HIGHEST_PEAK_DETECTION_TOLERANCE = 20;

    /**
     * initialises countryHighPoint database
     * initialises cache
     */
    public UserScore(Context context, FirebaseAccount fireBaseAccount){
        this.mContext = context;
        this.fireBaseAccount = fireBaseAccount;

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
            if(comparePeakNameWithCacheData(countryInfo, poiPoint) && !countryHighPointAlreadyDiscovered(poiPoint,country)) {
                retValue += ScoringConstants.BONUS_COUNTRY_TALLEST_PEAK;
                countryInfo.setCountryName(country);
                fireBaseAccount.setDiscoveredCountryHighPoint(countryInfo);
            }
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
        return (countryInfo.getCountryHighPoint().contains(poiPoint.getName()) ||
                poiPoint.getName().contains(countryInfo.getCountryHighPoint()))
                && Math.abs(poiPoint.getAltitude() - countryInfo.getHighPointHeight()) < COUNTRY_HIGHEST_PEAK_DETECTION_TOLERANCE;
    }

    /**
     * Check in the database if the user has already discovered this country
     * highest point
     * @param poiPoint new discovered peak
     * @param country country in which the new discovered peak is located
     * @return true if the
     */
    private boolean countryHighPointAlreadyDiscovered(POIPoint poiPoint, String country){
        HashMap<String,CacheEntry> countryHighPointDiscovered = fireBaseAccount.getDiscoveredCountryHighPoint();
        if(countryHighPointDiscovered == null) return false;
        if(countryHighPointDiscovered.containsKey(country))
            return countryHighPointDiscovered.get(country).getCountryHighPoint().contains(poiPoint.getName());
        return false;
    }

    /**
     * getDataFromCache try to get country HighPoint from cache. If entry is not present
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
    private long computeUserScore(ArrayList<POIPoint> scannedPeaks){
        //Get previous score from user database
        long userScore = fireBaseAccount.getUserScore();
        //Foreach loop over all scanned peaks
        for(POIPoint poiPoint : scannedPeaks){
            /*Add classical amount of points*/
            userScore += poiPoint.getAltitude() * ScoringConstants.PEAK_FACTOR;
            userScore += computeUserBonus(poiPoint);
        }
        return userScore;
    }



    /**
     * Updates user score in the database
     */
    public void updateUserScore(ArrayList<POIPoint> scannedPeaks){
        long userScore = computeUserScore(scannedPeaks);
        fireBaseAccount.setUserScore(userScore);
    }


    /**
     * Function that queries the user database. This function returns which
     * height range has already been discovered by the user
     * (1000m, 2000m, 3000m, 4000m, 5000m, 6000m, 7000m and 8000m peaks)
     * @return Array list of integers containing the above listed tags
     */
    private ArrayList<Integer> retrieveDiscoveredHeights(){
        //TODO : move this method to firebase account class and implement
        //TODO : functionality
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
