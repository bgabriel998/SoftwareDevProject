package ch.epfl.sdp.peakar.user.profile;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

import ch.epfl.sdp.peakar.R;
import ch.epfl.sdp.peakar.collection.NewCollectedItem;
import ch.epfl.sdp.peakar.collection.NewCollectionListAdapter;
import ch.epfl.sdp.peakar.utils.StatusBarHandler;
import ch.epfl.sdp.peakar.utils.UIUtils;

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

        hideUI(false, false);
        setupProfile("Alexander", 4978);

        fillListView();
    }

    /**
     * Setup profile text
     * @param username  of profile
     * @param points of profile
     */
    private void setupProfile(String username, int points){
        TextView usernameText = findViewById(R.id.profile_username);
        TextView pointsText = findViewById(R.id.profile_points);

        String profileText = getResources().getString(R.string.score_display,
                                             UIUtils.IntegerConvert(points));
        usernameText.setText(username);
        pointsText.setText(profileText);
    }

    /**
     * Hide the correct views based on if the profile is the user itself
     * or if it is a friend or not of the user.
     * @param self true if it is the users profile.
     * @param friends true if the profile is friend with the user
     */
    private void hideUI(boolean self, boolean friends) {
        findViewById(R.id.profile_add_friend).setVisibility(self || friends ? View.INVISIBLE : View.VISIBLE);
        findViewById(R.id.profile_remove_friend).setVisibility(self || !friends ? View.INVISIBLE : View.VISIBLE);
        findViewById(R.id.profile_sign_out).setVisibility(self ? View.VISIBLE : View.INVISIBLE);
        findViewById(R.id.profile_change_username).setVisibility(self ? View.VISIBLE : View.INVISIBLE);

        ImageView v = findViewById(R.id.profile_friend);
        v.setVisibility(self ? View.INVISIBLE : View.VISIBLE);
        if (friends) {
            Drawable d = v.getDrawable();
            d.setColorFilter(
                    new PorterDuffColorFilter(getColor(R.color.DarkGreen),
                            PorterDuff.Mode.SRC_ATOP));
            v.setImageDrawable(d);
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