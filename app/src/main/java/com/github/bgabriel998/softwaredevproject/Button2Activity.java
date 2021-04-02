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
    }
}
