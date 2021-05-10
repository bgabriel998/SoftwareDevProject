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

public class NewProfileActivity extends AppCompatActivity {

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

    private void setupUser(){
        // TODO Remove Social
        findViewById(R.id.profile_add_friend).setVisibility(View.INVISIBLE);
        findViewById(R.id.profile_remove_friend).setVisibility(View.INVISIBLE);
    }

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

    private final AdapterView.OnItemClickListener collectionClicked = (parent, view, position, id) -> {
        view.findViewById(R.id.collected_position).setVisibility(View.VISIBLE);
        view.findViewById(R.id.collected_date).setVisibility(View.VISIBLE);
    };
}