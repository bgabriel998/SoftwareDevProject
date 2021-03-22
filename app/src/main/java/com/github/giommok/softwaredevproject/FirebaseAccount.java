package com.github.giommok.softwaredevproject;

import android.content.Context;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.util.Log;

import com.github.ravifrancesco.softwaredevproject.POIPoint;
import com.github.ravifrancesco.softwaredevproject.UserPoint;
import com.google.firebase.auth.FirebaseAuth;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


/* Singleton class containing the only possible account connected */
public class FirebaseAccount implements Account {
    private static FirebaseAccount account = null;
    private static DataBaseHelper dataBaseHelper = null;
    private static long userScore = 0;
    private static Context mContext = null;



    public static FirebaseAccount getAccount(Context context){
        mContext = context;
        if (account == null)
            account = new FirebaseAccount();


        if(dataBaseHelper == null){
            dataBaseHelper = new DataBaseHelper(context);
            try {
                dataBaseHelper.openDataBase();
            }
            catch (Exception e){
                Log.e("EXCEPTION",e.toString());
            }

        }
        long height = dataBaseHelper.queryHighestPeakHeight("Monaco");
        String name = dataBaseHelper.queryHighestPeakName("Monaco");
        Log.v("Debug","Monaco :" + name+" " + height+ "m");



        return account;
    }


    @Override
    public boolean isSignedIn() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return false;
        return true;
    }

    @Override
    public String getProviderId() {
        if(isSignedIn()) return FirebaseAuth.getInstance().getCurrentUser().getProviderId();
        return "null";
    }

    @Override
    public String getDisplayName() {
        if(isSignedIn()) return FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        return "null";
    }

    @Override
    public String getEmail() {
        if(isSignedIn()) return FirebaseAuth.getInstance().getCurrentUser().getEmail();
        return "null";
    }

    @Override
    public String getId() {
        if(isSignedIn()) return FirebaseAuth.getInstance().getCurrentUser().getUid();
        return "null";
    }

    @Override
    public Uri getPhotoUrl() {
        if(isSignedIn()) return FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl();
        return Uri.EMPTY;
    }

    public void updateUserScore(ArrayList<POIPoint> scannedPeaks){

        //Foreach loop over all scanned peaks
        for(POIPoint poiPoint : scannedPeaks){
            /*Add classical amount of points*/
            userScore += poiPoint.getAltitude() * ScoringConstants.PEAK_FACTOR;
            /*Search country*/
            String country = getCountryFromCoordinates(poiPoint.getLatitude(), poiPoint.getLongitude());
            if(country != null){
                Log.v("updateUserScore","Country "+ country);
                Log.v("","");
            }
        }





        /**/
    }

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
