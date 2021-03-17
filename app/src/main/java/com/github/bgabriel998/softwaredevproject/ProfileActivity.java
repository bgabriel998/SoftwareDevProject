package com.github.bgabriel998.softwaredevproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

public class ProfileActivity extends AppCompatActivity {

    private static final String  TOOLBAR_TITLE = "Profile";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ToolbarHandler.SetupToolbar(this, TOOLBAR_TITLE);
    }

    /* Changes view to SignInActivity */
    public void signOutButton(View view) {
        Intent intent = new Intent(this, SignInActivity.class);
        startActivity(intent);
    }
}