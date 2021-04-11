package com.github.bgabriel998.softwaredevproject;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.github.giommok.softwaredevproject.FirebaseAccount;
import com.github.giommok.softwaredevproject.UserScore;
import com.github.ravifrancesco.softwaredevproject.POIPoint;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;

public class Button2Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_button2);

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



        FirebaseAccount account = FirebaseAccount.getAccount();
        UserScore userScore = new UserScore(this,account);
        userScore.updateUserScoreAndDiscoveredPeaks(inputArrayList);



    }
}
