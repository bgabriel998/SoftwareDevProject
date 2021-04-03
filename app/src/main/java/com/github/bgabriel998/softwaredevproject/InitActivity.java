package com.github.bgabriel998.softwaredevproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.github.giommok.softwaredevproject.FirebaseAccount;
import com.google.firebase.FirebaseApp;

public class InitActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init);

        ProgressBar progressBar = findViewById(R.id.progressBarInitActivity);
        progressBar.setVisibility(View.VISIBLE);
        initApp();

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);

    }


    private void initApp() {
        FirebaseApp.initializeApp(this);
        FirebaseAccount firebaseAccount = FirebaseAccount.getAccount();

        firebaseAccount.synchronizeDiscoveredCountryHighPoints();
        firebaseAccount.synchronizeUserScore();
        firebaseAccount.synchronizeUsername();
    }
}