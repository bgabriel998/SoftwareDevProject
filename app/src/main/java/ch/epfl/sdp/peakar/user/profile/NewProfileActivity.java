package ch.epfl.sdp.peakar.user.profile;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;

import ch.epfl.sdp.peakar.R;
import ch.epfl.sdp.peakar.collection.NewCollectedItem;
import ch.epfl.sdp.peakar.collection.NewCollectionListAdapter;
import ch.epfl.sdp.peakar.utils.StatusBarHandler;

/**
 * TODO Rename to remove new part.
 */
public class NewProfileActivity extends AppCompatActivity {

    private View selectedCollected = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_profile);

        StatusBarHandler.StatusBarTransparent(this);

        //setupUser();
        //setupOtherUser(true);
        setupOtherUser(false);

        fillListView();
    }

    /**
     * Setup activity as it is the actual users profile.
     */
    private void setupUser(){
        // TODO Remove Social
        findViewById(R.id.profile_add_friend).setVisibility(View.INVISIBLE);
        findViewById(R.id.profile_remove_friend).setVisibility(View.INVISIBLE);
    }

    /**
     * Setup activity as an other users profile
     * @param friends boolean if the user is friend with the owner of this profile.
     */
    private void setupOtherUser(boolean friends) {
        findViewById(R.id.profile_sign_out).setVisibility(View.INVISIBLE);
        if (friends) {
            // TODO Set Social color
            findViewById(R.id.profile_add_friend).setVisibility(View.INVISIBLE);
        }
        else {
            // TODO Set Social color
            findViewById(R.id.profile_remove_friend).setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Fill list view.
     * TODO get list from DB?
     */
    private void fillListView() {
        ArrayList<NewCollectedItem> items = new ArrayList<>(Arrays.asList(
                new NewCollectedItem("Mont Blanc - Monte Bianco", 4810000, 4810,
                        true, 0, 0, "2000-01-01"),
                new NewCollectedItem("Diablerets", 1210000, 3210,
                        false, 0, 0, "2000-01-01"),
                new NewCollectedItem("Kebnekaise", 710000, 2097,
                        true, 0, 0, "2000-01-01")
        ));
        //ArrayList<NewCollectedItem> items = new ArrayList<>();

        ListView collectionListView = findViewById(R.id.profile_collection);
        NewCollectionListAdapter listAdapter = new NewCollectionListAdapter(this,
                R.layout.profile_collected_item,
                items);

        collectionListView.setAdapter(listAdapter);

        if (items.size() > 0) {
            findViewById(R.id.profile_empty_text).setVisibility(View.INVISIBLE);
        }
        collectionListView.setOnItemClickListener(collectionClicked);
    }

    /**
     * Called when an item is pressed. Expands the clicked item, unless already expanded.
     * Then it shrinks it.
     */
    private final AdapterView.OnItemClickListener collectionClicked = (parent, view, position, id) -> {
        expandSelected(false);
        if (view == selectedCollected) {
            selectedCollected = null;
        }
        else {
            selectedCollected = view;
            expandSelected(true);
        }
    };

    /**
     * Expand or shrink the selected collected view.
     * @param expand boolean if expand or shrink.
     */
    private void expandSelected(boolean expand) {
        if (selectedCollected != null) {
            selectedCollected.findViewById(R.id.collected_position).setVisibility(expand ? View.VISIBLE:View.GONE);
            selectedCollected.findViewById(R.id.collected_date).setVisibility(expand ? View.VISIBLE:View.GONE);
        }
    }
}