package ch.epfl.sdp.peakar.user.profile;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

import ch.epfl.sdp.peakar.R;
import ch.epfl.sdp.peakar.collection.NewCollectedItem;
import ch.epfl.sdp.peakar.collection.NewCollectionListAdapter;
import ch.epfl.sdp.peakar.points.POIPoint;
import ch.epfl.sdp.peakar.user.outcome.ProfileOutcome;
import ch.epfl.sdp.peakar.user.services.AuthService;
import ch.epfl.sdp.peakar.user.services.OtherAccount;
import ch.epfl.sdp.peakar.utils.StatusBarHandler;
import ch.epfl.sdp.peakar.utils.UIUtils;

import static ch.epfl.sdp.peakar.utils.UIUtils.setTintColor;

/**
 * TODO Rename to remove new part.
 */
public class NewProfileActivity extends AppCompatActivity {
    public final static String AUTH_INTENT = "isAuth";
    public final static String OTHER_INTENT = "otherId";

    private boolean isAuthProfile;

    private OtherAccount otherAccount = null;


    private View selectedCollected = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the starting intent
        Intent startingIntent = getIntent();
        isAuthProfile = startingIntent.getBooleanExtra(AUTH_INTENT, false);

        if(!isAuthProfile) {
            String otherId = startingIntent.getStringExtra(OTHER_INTENT);
            new Thread(() -> {
                otherAccount = OtherAccount.getInstance(otherId);
                runOnUiThread(() -> {
                    setContentView(R.layout.activity_new_profile);

                    StatusBarHandler.StatusBarTransparent(this);

                    hideUI(false, true);
                    setupProfile(otherAccount.getUsername(), (int)otherAccount.getScore());

                    fillListView();
                });

            }).start();
        } else {
            setContentView(R.layout.activity_new_profile);

            StatusBarHandler.StatusBarTransparent(this);

            hideUI(true, false);
            setupProfile(AuthService.getInstance().getAuthAccount().getUsername(), (int)AuthService.getInstance().getAuthAccount().getScore());

            fillListView();
        }

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

        updateProfileImage();
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
            setTintColor(v, R.color.DarkGreen);
        }
    }

    /**
     * Fill list view.
     * TODO show correct points, correct date and correct country
     */
    private void fillListView() {
        ArrayList<NewCollectedItem> items = new ArrayList<>();
        for(POIPoint discoveredPeak: AuthService.getInstance().getAuthAccount().getDiscoveredPeaks()) {
            NewCollectedItem newCollectedItem = new NewCollectedItem(
                    discoveredPeak.getName(),
                    0,
                    (int)discoveredPeak.getAltitude(),
                    AuthService.getInstance().getAuthAccount().getDiscoveredCountryHighPoint().containsValue(discoveredPeak.getName()),
                    (float)discoveredPeak.getLongitude(),
                    (float)discoveredPeak.getLatitude(),
                    "2000-01-01");
            items.add(newCollectedItem);
        }

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

    /**
     * On sign out button click
     */
    public void signOutButton(View view) {
        AuthService.getInstance().signOut(this);
        finish();
    }

    /**
     * On change username button click
     */
    public void changeUsernameButton(View view) {
        // Hide username
        findViewById(R.id.profile_username).setVisibility(View.GONE);

        // Show edit text
        EditText changeUsername = findViewById(R.id.profile_username_edit);
        changeUsername.setVisibility(View.VISIBLE);
        changeUsername.requestFocus();

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(changeUsername, InputMethodManager.SHOW_IMPLICIT);

        // Change listener so next click will result in a username change
        findViewById(R.id.profile_change_username).setOnClickListener(this::confirmUsernameButton);
    }

    /**
     * On confirm username change button click
     */
    public void confirmUsernameButton(View view) {
        EditText usernameEdit = ((EditText)findViewById(R.id.profile_username_edit));
        hideKeyboard();
        String newUsername = usernameEdit.getText().toString();
        usernameEdit.getText().clear();

        // Start a new thread that will handle the process
        Thread changeThread = new Thread(){
            @Override
            public void run() {
                // Change the username and wait for the task to end
                ProfileOutcome result = AuthService.getInstance().getAuthAccount().changeUsername(newUsername);

                // Update the view on the UI thread
                runOnUiThread(() -> {
                    ((EditText)findViewById(R.id.profile_username_edit)).getText().clear();

                    // If username has changed, update the username text view
                    if(result == ProfileOutcome.USERNAME_CHANGED || result == ProfileOutcome.USERNAME_REGISTERED) ((TextView)findViewById(R.id.profile_username)).setText(AuthService.getInstance().getAuthAccount().getUsername());

                    // Display the message
                    Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), result.getMessage(), Snackbar.LENGTH_LONG);
                    snackbar.show();

                    // Hide username
                    findViewById(R.id.profile_username_edit).setVisibility(View.GONE);

                    // Show edit text
                    findViewById(R.id.profile_username).setVisibility(View.VISIBLE);

                    // Change listener so next click will result in a username chance
                    findViewById(R.id.profile_change_username).setOnClickListener(v -> changeUsernameButton(v));
                });
            }
        };
        changeThread.start();
    }

    /**
     * Update the image of the profile.
     */
    private void updateProfileImage() {
        Uri  profileImageUrl = isAuthProfile ? AuthService.getInstance().getPhotoUrl() : otherAccount.getPhotoUrl();
        if(profileImageUrl == Uri.EMPTY) return;
        Glide.with(this)
                .load(profileImageUrl)
                .circleCrop()
                .into((ImageView)findViewById(R.id.profile_picture));
    }
}
