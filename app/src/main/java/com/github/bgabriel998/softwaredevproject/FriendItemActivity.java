package com.github.bgabriel998.softwaredevproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.github.giommok.softwaredevproject.Account;
import com.github.giommok.softwaredevproject.Database;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.Locale;

public class FriendItemActivity extends AppCompatActivity {
    private String UId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_friend_item);

        Intent intent = getIntent();
        setupInformation(intent);

        ToolbarHandler.SetupToolbar(this, intent.getStringExtra("username"));
    }

    /**
     * Setup the information on the mountain activity from data of intent.
     * @param intent given intent
     */
    private void setupInformation(Intent intent){
        // Points
        TextView pointText = findViewById(R.id.pointText);
        pointText.setText(String.format(Locale.getDefault(), " %d",
                intent.getIntExtra("points", -1)));
        UId = intent.getStringExtra("uid");
    }

    public void removeFriendButton(View view) {
        Account account = Account.getAccount();
        Database.refRoot.child(Database.CHILD_USERS + account.getId() + Database.CHILD_FRIENDS).child(UId).removeValue();
        Intent intent = new Intent(this, FriendsActivity.class);
        startActivity(intent);
        finish();
    }
}