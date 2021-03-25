package com.github.bgabriel998.softwaredevproject;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.data.model.User;
import com.github.giommok.softwaredevproject.ScoringConstants;
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

        UserScore userScore = new UserScore(this,99);
        long newScore = userScore.computeUserScore(inputArrayList);

        Button button = findViewById(R.id.compute_user_score_button);
        TextView tv = findViewById(R.id.scoreDetail);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long expectedUserScore = (long)(point_1.getAltitude() * ScoringConstants.PEAK_FACTOR +
                        point_2.getAltitude() * ScoringConstants.PEAK_FACTOR +
                        point_3.getAltitude() * ScoringConstants.PEAK_FACTOR +
                        point_4.getAltitude() * ScoringConstants.PEAK_FACTOR)+
                        ScoringConstants.BONUS_COUNTRY_TALLEST_PEAK;
                tv.setText("Current Score : " + expectedUserScore);
            }
        });



    }
}
