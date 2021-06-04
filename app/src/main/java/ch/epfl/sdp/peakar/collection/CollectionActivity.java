package ch.epfl.sdp.peakar.collection;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;

import ch.epfl.sdp.peakar.R;
import ch.epfl.sdp.peakar.user.score.ScoringConstants;
import ch.epfl.sdp.peakar.user.services.AuthAccount;
import ch.epfl.sdp.peakar.user.services.AuthService;
import ch.epfl.sdp.peakar.user.services.FirebaseAuthService;
import ch.epfl.sdp.peakar.utils.ToolbarHandler;

public class CollectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection);
        ToolbarHandler.SetupToolbar(this, getString(R.string.toolbar_collections));

        // Load the list only if there is a logged user
        if(FirebaseAuthService.getInstance().getAuthAccount() != null) fillCollectionList();
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
        AuthAccount account = AuthService.getInstance().getAuthAccount();
        ArrayList<CollectedItem> collectedItems = ((ArrayList<CollectedItem>)
                (account.getDiscoveredPeaks().stream().map(poi-> new CollectedItem(poi.getName(),
                (int)(poi.getAltitude()* ScoringConstants.PEAK_FACTOR),
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