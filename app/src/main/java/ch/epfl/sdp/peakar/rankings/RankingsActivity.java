package ch.epfl.sdp.peakar.rankings;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.client.Firebase;
import com.firebase.client.Query;
import com.firebase.ui.FirebaseListAdapter;
import com.google.firebase.FirebaseApp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ch.epfl.sdp.peakar.R;
import ch.epfl.sdp.peakar.user.services.providers.firebase.FirebaseRankingsList;
import ch.epfl.sdp.peakar.utils.ToolbarHandler;

public class RankingsActivity extends AppCompatActivity {

    private static final String  TOOLBAR_TITLE = "Rankings";
    private final static List<RankingItem> rankingItems = Collections.synchronizedList(new ArrayList<>());

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
                rankingItems);

        listAdapter.setNotifyOnChange(true);

        // Start rankings synchronization
        FirebaseRankingsList.synchronizeRankings(rankingItems, listAdapter);

        rankingsListView.setAdapter(listAdapter);
    }
}