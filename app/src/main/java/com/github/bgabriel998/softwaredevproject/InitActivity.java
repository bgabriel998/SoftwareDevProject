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

        FirebaseApp.initializeApp(this);

        ProgressBar progressBar = findViewById(R.id.progressBarInitActivity);
        progressBar.setVisibility(View.VISIBLE);
        initApp();

        //Opening the main menu
        //TODO MainActivity.class should be replaced by MainMenuActivity.class in the future
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);

    }


    /**
     * Init application global stuff before opening the main menu
     */
    private void initApp() {
        FirebaseAccount firebaseAccount = FirebaseAccount.getAccount();
        firebaseAccount.synchronizeUserProfile();
    }
}