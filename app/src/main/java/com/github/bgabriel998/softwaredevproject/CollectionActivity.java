package com.github.bgabriel998.softwaredevproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.giommok.softwaredevproject.FirebaseAccount;
import com.github.giommok.softwaredevproject.ScoringConstants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;

public class CollectionActivity extends AppCompatActivity {

    private static final String  TOOLBAR_TITLE = "Collections";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection);
        ToolbarHandler.SetupToolbar(this, TOOLBAR_TITLE);

        fillCollectionList();
    }

    /**
     * Fetch ListView and setup it upp with a collection item list adapter.
     */
    private void fillCollectionList(){
        ListView collectionListView = findViewById(R.id.collection_list_view);

        CollectionListAdapter listAdapter = new CollectionListAdapter(this,
                R.layout.collected_item,
                getCollection());

        collectionListView.setAdapter(listAdapter);

        // Add listener for clicking on item
        collectionListView.setOnItemClickListener((parent, view, position, id) ->
                switchToCollectedActivity((CollectedItem)collectionListView.getItemAtPosition(position)));
    }

    /**
     * @return array list of all collected items.
     */
    private ArrayList<CollectedItem> getCollection(){
        FirebaseAccount account = FirebaseAccount.getAccount();
        ArrayList<CollectedItem> collectedItems = ((ArrayList<CollectedItem>)
                (account.getDiscoveredPeaks().stream().map(poi-> new CollectedItem(poi.getName(),
                (int)(poi.getAltitude()*ScoringConstants.PEAK_FACTOR),
                (int)(poi.getAltitude()), (float) poi.getLongitude(),
                (float) poi.getLatitude())).collect(Collectors.toList())));
        collectedItems.sort(Collections.reverseOrder());
        return collectedItems;
    }

    /**
     * Changes to mountain activity and providing intent with information
     * from the item that was clicked.
     * @param item the given item.
     */
    public void switchToCollectedActivity(CollectedItem item) {
        Intent intent = new Intent(this, MountainActivity.class);
        fillIntent(intent, item);
        startActivity(intent);
    }

    /**
     * Fills intent with information from item
     * @param intent to fill
     * @param item the given item
     */
    private void fillIntent(Intent intent, CollectedItem item) {
        intent.putExtra("name", item.getName());
        intent.putExtra("points", item.getPoints());
        intent.putExtra("height", item.getHeight());
        intent.putExtra("longitude", item.getLongitude());
        intent.putExtra("latitude", item.getLatitude());
    }
}