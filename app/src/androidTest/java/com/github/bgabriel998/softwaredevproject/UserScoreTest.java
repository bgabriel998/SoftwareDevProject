package com.github.bgabriel998.softwaredevproject;

import android.content.Context;
import android.util.Log;

import androidx.test.core.app.ApplicationProvider;

import static org.junit.Assert.assertEquals;

import com.github.giommok.softwaredevproject.Database;
import com.github.giommok.softwaredevproject.FirebaseAccount;
import com.github.giommok.softwaredevproject.ScoringConstants;
import com.github.giommok.softwaredevproject.UserScore;
import com.github.ravifrancesco.softwaredevproject.POIPoint;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osmdroid.bonuspack.location.POI;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class UserScoreTest {

    private static final int USER_ID = 99;

    private static UserScore userScore;

    @BeforeClass
    public static void setup(){
        Context context = ApplicationProvider.getApplicationContext();
        Assert.assertNotNull(context);
        FirebaseAccount account = FirebaseAccount.getAccount();
        userScore = new UserScore(context,account);
    }

    /**
     * Test compute user bonus function
     * compute the amount of points given to the user with a given Array
     * List of POI
     */
    @Test
    public void testComputeUserBonus(){
        GeoPoint geoPoint_1 = new GeoPoint(45.8325,6.8641666666667,4810);
        POIPoint point_1 = new POIPoint(geoPoint_1);
        point_1.setName("Mont Blanc - Monte Bianco");

        GeoPoint geoPoint_2 = new GeoPoint(45.86355980599387, 6.951348205683087,4013);
        POIPoint point_2 = new POIPoint(geoPoint_2);
        point_2.setName("Dent du Geant");

        GeoPoint geoPoint_3 = new GeoPoint(45.891667, 6.907222,3673);
        POIPoint point_3 = new POIPoint(geoPoint_3);
        point_3.setName("Aiguille du plan");

        GeoPoint geoPoint_4 = new GeoPoint(45.920774986207014, 6.812914656881065,3660);
        POIPoint point_4 = new POIPoint(geoPoint_4);
        point_4.setName("Pointe de Lapaz");

        ArrayList<POIPoint> inputArrayList = new ArrayList<POIPoint>();
        inputArrayList.add(point_2);
        inputArrayList.add(point_1);
        inputArrayList.add(point_3);
        inputArrayList.add(point_4);


        userScore.updateUserScore(inputArrayList);
        long expectedUserScore = (long)(point_1.getAltitude() * ScoringConstants.PEAK_FACTOR +
                                point_2.getAltitude() * ScoringConstants.PEAK_FACTOR +
                                point_3.getAltitude() * ScoringConstants.PEAK_FACTOR +
                                point_4.getAltitude() * ScoringConstants.PEAK_FACTOR)+
                                ScoringConstants.BONUS_COUNTRY_TALLEST_PEAK;

        assertEquals(expectedUserScore,FirebaseAccount.getAccount().getUserScore());
        //remove child from database
        Database.refRoot.child("users").child("null").removeValue();
    }


}
