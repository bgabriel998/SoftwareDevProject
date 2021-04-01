package com.github.bgabriel998.softwaredevproject;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.data.model.User;
import com.github.giommok.softwaredevproject.CacheEntry;
import com.github.giommok.softwaredevproject.Database;
import com.github.giommok.softwaredevproject.DatabaseEntry;
import com.github.giommok.softwaredevproject.FirebaseAccount;
import com.github.giommok.softwaredevproject.ScoringConstants;
import com.github.giommok.softwaredevproject.UserScore;
import com.github.ravifrancesco.softwaredevproject.POIPoint;
import com.google.firebase.database.DatabaseReference;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.Arrays;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;


public class Button2Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_button2);


        FirebaseAccount firebaseAccount = FirebaseAccount.getAccount();
        UserScore userScore = new UserScore(this, firebaseAccount);


        GeoPoint geoPoint_1 = new GeoPoint(45.8325,6.8641666666667,4810);
        POIPoint point_1 = new POIPoint(geoPoint_1);
        point_1.setName("Mont Blanc - Monte Bianco");

        GeoPoint geoPoint_2 = new GeoPoint(45.86355980599387, 6.951348205683087,4013);
        POIPoint point_2 = new POIPoint(geoPoint_2);
        point_2.setName("Dent du Geant");

        GeoPoint geoPoint_3 = new GeoPoint(45.891667, 6.907222,3673);
        POIPoint point_3 = new POIPoint(geoPoint_3);
        point_3.setName("Aiguille du plan");

        GeoPoint geoPoint_4 = new GeoPoint(47.421111, 10.985278,2962);
        POIPoint point_4 = new POIPoint(geoPoint_4);
        point_4.setName("Zugspitze");

        ArrayList<POIPoint> inputArrayList = new ArrayList<POIPoint>();
        //inputArrayList.add(point_2);
        //inputArrayList.add(point_1);
        //inputArrayList.add(point_3);
        inputArrayList.add(point_4);

        userScore.updateUserScore(inputArrayList);

    }
}
