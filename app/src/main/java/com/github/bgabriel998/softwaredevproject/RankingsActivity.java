package com.github.bgabriel998.softwaredevproject;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.github.giommok.softwaredevproject.Database;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RankingsActivity extends AppCompatActivity {

    private static final String  TOOLBAR_TITLE = "Rankings";
    private static ArrayList<RankingItem> rankingItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO MOVE TO MAIN MENU ACTIVITY
        FirebaseApp.initializeApp(this);
        synchronizeRanking();
        setContentView(R.layout.activity_rankings);

        ToolbarHandler.SetupToolbar(this, TOOLBAR_TITLE);

        fillRankingsList();
    }

    /**
     * Fetch ListView and setup it upp with a ranking item list adapter.
     */
    private void fillRankingsList(){
        ListView rankingsListView = findViewById(R.id.rankings_list_view);

        RankingListAdapter listAdapter = new RankingListAdapter(this,
                R.layout.ranking_item,
                rankingItems);


        rankingsListView.setAdapter(listAdapter);
    }

    /**
     * Update the ranking whenever the DB changes
     */

    public void synchronizeRanking() {
        DatabaseReference dbRef = Database.refRoot.child("users");
        ValueEventListener rankingListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot parent) {
                ArrayList<RankingItem> tempRankings =  new ArrayList<>();
                for (DataSnapshot child : parent.getChildren()) {
                    boolean found = false;
                    String uid = child.getKey() != null ? child.getKey() : "null";
                    String childUsername = child.child("username").getValue(String.class);
                    Integer childScore = child.child("score").getValue(Integer.class);
                    tempRankings.add(new RankingItem(uid, childUsername, childScore == null ? 0 : childScore));
                }
                Collections.reverse(tempRankings);
                rankingItems = tempRankings;
                fillRankingsList();
                Log.d("SYNCHRONIZE_RANKING", "onDataChange");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        dbRef.orderByChild("score").addValueEventListener(rankingListener);

    }

}