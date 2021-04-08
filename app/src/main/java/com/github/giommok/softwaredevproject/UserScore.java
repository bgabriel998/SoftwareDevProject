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
