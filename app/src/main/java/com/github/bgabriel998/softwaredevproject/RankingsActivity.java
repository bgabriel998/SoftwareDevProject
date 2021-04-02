package com.github.bgabriel998.softwaredevproject;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class RankingsActivity extends AppCompatActivity {

    private static final String  TOOLBAR_TITLE = "Rankings";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
                getRankings());

        rankingsListView.setAdapter(listAdapter);
    }

    /**
     * TODO Get list from DB
     * @return array list of all ranking items.
     */
    private ArrayList<RankingItem> getRankings(){
        ArrayList<RankingItem> rankingItems = new ArrayList<>();
        for (int i = 0; i < 20; i++){
            rankingItems.add(new RankingItem(String.format("Username%d", i), 100 - i));
        }
        return rankingItems;
    }
}