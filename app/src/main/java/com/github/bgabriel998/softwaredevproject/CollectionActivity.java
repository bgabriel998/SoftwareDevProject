package com.github.bgabriel998.softwaredevproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;

import java.util.ArrayList;

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
     * TODO Get list from DB
     * @return array list of all collected items.
     */
    private ArrayList<CollectedItem> getCollection(){
        ArrayList<CollectedItem> collectedItems = new ArrayList<>();

        collectedItems.add(new CollectedItem("TEST_Mountain", 15, 1000, 30, 60));
        collectedItems.add(new CollectedItem("TEST_Mountain2", 12, 800, 75, 82));

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
        intent.putExtra("name", item.name);
        intent.putExtra("points", item.points);
        intent.putExtra("height", item.height);
        intent.putExtra("longitude", item.longitude);
        intent.putExtra("latitude", item.latitude);
    }
}