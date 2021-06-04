package ch.epfl.sdp.peakar.user.profile;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import ch.epfl.sdp.peakar.R;
import ch.epfl.sdp.peakar.collection.NewCollectedItem;
import ch.epfl.sdp.peakar.collection.NewCollectionListAdapter;
import ch.epfl.sdp.peakar.database.Database;
import ch.epfl.sdp.peakar.points.POIPoint;
import ch.epfl.sdp.peakar.user.challenge.ChallengeItem;
import ch.epfl.sdp.peakar.user.challenge.ChallengeListAdapter;
import ch.epfl.sdp.peakar.user.challenge.goal.RemotePointsChallenge;
import ch.epfl.sdp.peakar.user.outcome.ProfileOutcome;
import ch.epfl.sdp.peakar.user.score.ScoringConstants;
import ch.epfl.sdp.peakar.user.services.Account;
import ch.epfl.sdp.peakar.user.services.AuthService;
import ch.epfl.sdp.peakar.user.services.OtherAccount;
import ch.epfl.sdp.peakar.utils.StatusBarHandler;
import ch.epfl.sdp.peakar.utils.UIUtils;

import static ch.epfl.sdp.peakar.utils.POIPointsUtilities.getCountryFromCoordinates;
import static ch.epfl.sdp.peakar.utils.UIUtils.setTintColor;


public class ProfileActivity extends AppCompatActivity {
    public final static String AUTH_INTENT = "isAuth";
    public final static String OTHER_INTENT = "otherId";

    private boolean isAuthProfile;
    private Account displayedAccount = null;
    private String otherId = null;
    private View selectedCollected = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the starting intent
        Intent startingIntent = getIntent();
        isAuthProfile = startingIntent.getBooleanExtra(AUTH_INTENT, false);
        if(!isAuthProfile) {
            otherId = startingIntent.getStringExtra(OTHER_INTENT);
            new Thread(() -> {
                displayedAccount = OtherAccount.getInstance(otherId);
                runOnUiThread(() -> {
                    setContentView(R.layout.activity_profile);

                    // Enable swipe gesture
                    SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swipe_refresh);
                    swipeRefreshLayout.setEnabled(true);
                    swipeRefreshLayout.setOnRefreshListener(() -> {
                        new Thread(() -> {
                            displayedAccount = OtherAccount.getNewInstance(otherId);
                            runOnUiThread(() -> {
                                setupProfile();
                                swipeRefreshLayout.setRefreshing(false);
                            });
                        }).start();
                    });


                    fillCollectedListView();
                    StatusBarHandler.StatusBarTransparent(this);
                    if(AuthService.getInstance().getAuthAccount() != null) hideUI(false, isFriend());
                    else {
                        hideUI(false, false);
                        hideFriendButtons();
                    }
                    setupProfile();
                });
            }).start();
        } else {
            setContentView(R.layout.activity_profile);

            // Disable swipe gesture
            SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swipe_refresh);
            swipeRefreshLayout.setEnabled(false);

            StatusBarHandler.StatusBarTransparent(this);

            hideUI(true, false);

            displayedAccount = AuthService.getInstance().getAuthAccount();
            setupProfile();
        }
    }

    /**
     * Setup profile view
     */
    private void setupProfile(){
        TextView usernameText = findViewById(R.id.profile_username);
        TextView pointsText = findViewById(R.id.profile_points);

        String profileText = getResources().getString(R.string.score_display,
                                             UIUtils.IntegerConvert(displayedAccount.getScore()));
        usernameText.setText(displayedAccount.getUsername());
        pointsText.setText(profileText);

        updateProfileImage();

        // Handle the list view
        if(isAuthProfile && AuthService.getInstance().getAuthAccount().getUsername().equals(Account.USERNAME_BEFORE_REGISTRATION)) {
            changeUsernameButton(null);
            // Set text view
            ((TextView)findViewById(R.id.profile_empty_text)).setText(R.string.empty_collection_not_registered);
        } else {
            fillCollectedListView();
        }
    }

    /**
     * Hide the correct views based on if the profile is the user itself
     * or if it is a friend or not of the user.
     * @param self true if it is the users profile.
     * @param friends true if the profile is friend with the user
     */
    private void hideUI(boolean self, boolean friends) {
        findViewById(R.id.profile_add_friend).setVisibility(self || friends ? View.GONE : View.VISIBLE);
        findViewById(R.id.profile_remove_friend).setVisibility(self || !friends ? View.GONE : View.VISIBLE);
        findViewById(R.id.profile_sign_out).setVisibility(self ? View.VISIBLE : View.GONE);
        findViewById(R.id.profile_change_username).setVisibility(self ? View.VISIBLE : View.GONE);

        ImageView v = findViewById(R.id.profile_friend);
        v.setVisibility(self ? View.INVISIBLE : View.VISIBLE);
        if (friends) {
            setTintColor(v, R.color.DarkGreen);
        }
    }

    /**
     * Hide the friends buttons
     */
    private void hideFriendButtons() {
        findViewById(R.id.profile_add_friend).setVisibility(View.GONE);
        findViewById(R.id.profile_remove_friend).setVisibility(View.GONE);
    }

    /**
     * Fill list view.
     */
    private void fillCollectedListView() {
        findViewById(R.id.add_challenge).setVisibility(View.GONE);
        // Show correct text if empty
        ((TextView)findViewById(R.id.profile_empty_text)).setText(R.string.empty_collection);

        new Thread(() -> {
            ArrayList<NewCollectedItem> items = new ArrayList<>();
            for(POIPoint discoveredPeak: displayedAccount.getDiscoveredPeaks()) {
                NewCollectedItem newCollectedItem = new NewCollectedItem(
                        discoveredPeak.getName(),
                        (int)(discoveredPeak.getAltitude() * ScoringConstants.PEAK_FACTOR),
                        (int)discoveredPeak.getAltitude(),
                        displayedAccount.getDiscoveredCountryHighPointNames().contains(discoveredPeak.getName()),
                        (float)discoveredPeak.getLongitude(),
                        (float)discoveredPeak.getLatitude(),
                        discoveredPeak.getDiscoveredDate(),
                        getCountryFromCoordinates(this, (float)discoveredPeak.getLatitude(),
                                (float)discoveredPeak.getLongitude()));
                items.add(newCollectedItem);
            }

            // Sort the items by the peak points
            items.sort(Comparator.comparing(NewCollectedItem::getPoints).reversed());

            runOnUiThread(() -> {
                ListView collectionListView = findViewById(R.id.profile_collection);
                NewCollectionListAdapter listAdapter = new NewCollectionListAdapter(this,
                        R.layout.profile_collected_item,
                        items);

                collectionListView.setAdapter(listAdapter);

                if (items.size() > 0) {
                    findViewById(R.id.profile_empty_text).setVisibility(View.INVISIBLE);
                }
                collectionListView.setOnItemClickListener(collectionClicked);
            });
        }).start();
    }

    /**
     * Setup the challenge list view.
     * Fill list with challenges the user is enrolled in
     */
    private void fillChallengeListView() {
        // Show correct text if empty
        if(AuthService.getInstance().getAuthAccount() != null){
            if(AuthService.getInstance().getAuthAccount().equals(displayedAccount))
                findViewById(R.id.add_challenge).setVisibility(View.VISIBLE);
        }

        ((TextView)findViewById(R.id.profile_empty_text)).setText(R.string.empty_collection);

        ArrayList<ChallengeItem> items = new ArrayList<>();
        List<RemotePointsChallenge> challengeList  =
                displayedAccount.getChallenges().stream().map( c -> (RemotePointsChallenge) c).collect(Collectors.toList());

        for(RemotePointsChallenge enrolledChallenge: challengeList) {
            ChallengeItem challengeItem;

            challengeItem = new ChallengeItem(
                    enrolledChallenge,
                    enrolledChallenge.getFounderID().equals(otherId),
                    AuthService.getInstance().getAuthAccount() != null
            );

            items.add(challengeItem);
        }

        ListView challengeListView = findViewById(R.id.profile_collection);
        ChallengeListAdapter listAdapter = new ChallengeListAdapter(this,
                R.layout.profile_challenge_item,
                items);
        challengeListView.setAdapter(listAdapter);
        challengeListView.setOnItemClickListener(challengeClicked);
    }

    /**
     * Called when an item is pressed. Expands the clicked item, unless already expanded.
     * Then it shrinks it.
     */
    private final AdapterView.OnItemClickListener challengeClicked = (parent, view, position, id) -> {
        ChallengeItem item = (ChallengeItem)parent.getItemAtPosition(position);
        expandSelectedChallenge(false,item.getNumberOfParticipants());
        if (view == selectedCollected) {
            selectedCollected = null;
        }
        else {
            selectedCollected = view;
            expandSelectedChallenge(true,item.getNumberOfParticipants());
        }
    };



    /**
     * Expand or shrink the selected collected view.
     * @param expand boolean if expand or shrink.
     */
    private void expandSelectedChallenge(boolean expand, int enrolledUsers) {
        if (selectedCollected != null && enrolledUsers != 1) {
            selectedCollected.findViewById(R.id.challenge_first_user).setVisibility(expand ? View.VISIBLE:View.GONE);
            selectedCollected.findViewById(R.id.challenge_second_user).setVisibility(expand ? View.VISIBLE:View.GONE);
            if(enrolledUsers>2)
                selectedCollected.findViewById(R.id.challenge_third_user).setVisibility(expand ? View.VISIBLE:View.GONE);
        }
    }



    /**
     * Called when an item is pressed. Expands the clicked item, unless already expanded.
     * Then it shrinks it.
     */
    private final AdapterView.OnItemClickListener collectionClicked = (parent, view, position, id) -> {
        expandSelectedCollection(false);
        if (view == selectedCollected) {
            selectedCollected = null;
        }
        else {
            selectedCollected = view;
            expandSelectedCollection(true);
        }
    };

    /**
     * Expand or shrink the selected collected view.
     * @param expand boolean if expand or shrink.
     */
    private void expandSelectedCollection(boolean expand) {
        if (selectedCollected != null) {
            selectedCollected.findViewById(R.id.collected_position).setVisibility(expand ? View.VISIBLE:View.GONE);
            selectedCollected.findViewById(R.id.collected_date).setVisibility(expand ? View.VISIBLE:View.GONE);
        }
    }

    /**
     * On sign out button click
     */
    public void signOutButton(View view) {
        new Thread(() -> {
            AuthService.getInstance().signOut(this);
            finish();
        }).start();
    }

    /**
     * On change username button click
     */
    public void changeUsernameButton(View view) {
        if(!Database.getInstance().isOnline()) {
            showErrorMessage();
            return;
        }

        // Hide username
        findViewById(R.id.profile_username).setVisibility(View.GONE);

        // Hide change button
        findViewById(R.id.profile_change_username).setVisibility(View.GONE);

        // Show edit text
        EditText changeUsername = findViewById(R.id.profile_username_edit);
        changeUsername.setVisibility(View.VISIBLE);
        changeUsername.requestFocus();

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(changeUsername, InputMethodManager.SHOW_IMPLICIT);

        changeUsername.setOnKeyListener((v, keyCode, event) -> {
            if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                    (keyCode == KeyEvent.KEYCODE_ENTER)) {
                confirmUsernameButton();
            }
            return true;
        });
    }

    /**
     * On confirm username change
     */
    public void confirmUsernameButton() {
        EditText usernameEdit = findViewById(R.id.profile_username_edit);
        String newUsername = usernameEdit.getText().toString();

        // Start a new thread that will handle the process
        Thread changeThread = new Thread(){
            @Override
            public void run() {
                // Change the username and wait for the task to end
                ProfileOutcome result = AuthService.getInstance().getAuthAccount().changeUsername(newUsername);

                // Update the view on the UI thread
                runOnUiThread(() -> {

                    // If username has changed, update the username text view
                    // If username has changed, hide the keyboard and update the username text view
                    if(result == ProfileOutcome.USERNAME_CHANGED || result == ProfileOutcome.USERNAME_REGISTERED) {
                        removeUsernameChangeUI();
                        ((TextView)findViewById(R.id.profile_username)).setText(AuthService.getInstance().getAuthAccount().getUsername());
                    }
                    if(result == ProfileOutcome.USERNAME_REGISTERED) fillCollectedListView();

                    // Display the message
                    Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), result.getMessage(), Snackbar.LENGTH_LONG);
                    snackbar.show();
                });
            }
        };
        changeThread.start();
    }
    /**
     * Hides the keyboard and updates the UI after the username was changed or cancelled only if the
     * user is already registered
     */
    private void removeUsernameChangeUI() {
        if (getCurrentFocus() != null && !AuthService.getInstance().getAuthAccount().getUsername().equals(Account.USERNAME_BEFORE_REGISTRATION)) {
            // Hide keyboard
            hideKeyboard();

            // Hide username edit
            findViewById(R.id.profile_username_edit).setVisibility(View.GONE);

            // Show username
            findViewById(R.id.profile_username).setVisibility(View.VISIBLE);

            // Show change button
            findViewById(R.id.profile_change_username).setVisibility(View.VISIBLE);

            // Clear edit text
            ((EditText)findViewById(R.id.profile_username_edit)).getText().clear();
        }
    }

    /**
     * Method to hide the keyboard
     */
    private void hideKeyboard() {
        Log.d("ProfileActivity", "hideKeyboard: triggered");
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        removeUsernameChangeUI();
        return super.dispatchTouchEvent(ev);
    }

    /**
     * Update the image of the profile.
     */
    private void updateProfileImage() {
        Uri profileImageUrl = isAuthProfile ? AuthService.getInstance().getPhotoUrl() : ((OtherAccount)displayedAccount).getPhotoUrl();
        if(profileImageUrl == Uri.EMPTY) return;
        Glide.with(this)
                .load(profileImageUrl)
                .circleCrop()
                .into((ImageView)findViewById(R.id.profile_picture));
    }


    /*Collection button callback*/
    public void collectionButton(View view) {
        fillCollectedListView();
    }

    /*challenge button callback*/
    public void challengeButton(View view) {
        fillChallengeListView();
    }


    /**
     * Callback method when + button is pressed.
     * Ask for a challenge Name
     * @param view challenge view
     */
    public void addChallengeButton(View view){
        if(!Database.getInstance().isOnline()) {
            showErrorMessage();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.enter_challenge_name));

        // Set up the input
        final EditText challengeNameInput = new EditText(this);
        challengeNameInput.getId();

        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        challengeNameInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE);
        builder.setView(challengeNameInput);

        // Set up the buttons
        builder.setPositiveButton(getResources().getString(R.string.ok_button), (dialog, which) -> {
            String retrievedText = challengeNameInput.getText().toString();
            //Check that input challenge name length is between 3 and 25
            if(retrievedText.length() < 3 || retrievedText.length() > 25){
                dialog.dismiss();
                Toast.makeText(this, getResources().getString(R.string.toast_challenge_name),Toast.LENGTH_SHORT).show();
                addChallengeButton(view);
            }
            else{
                setChallengeDuration(retrievedText);
            }
        });
        builder.setNegativeButton(getResources().getString(R.string.cancel_button), (dialog, which) -> dialog.cancel());
        builder.show();
    }

    /**
     * Ask user to input challenge duration
     * This method finalizes the challenge creation
     * @param challengeName challenge name selected in the previous step
     */
    private void setChallengeDuration(String challengeName) {
        // setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.choose_challenge_duration));

        String[] durations = getResources().getStringArray(R.array.challenge_duration);

        builder.setItems(durations, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    default:
                        return;
                    case 0:
                        RemotePointsChallenge.generateNewChallenge(AuthService.getInstance().getID(), challengeName, 1);
                        break;
                    case 1:
                        RemotePointsChallenge.generateNewChallenge(AuthService.getInstance().getID(), challengeName, 2);
                        break;
                    case 2:
                        RemotePointsChallenge.generateNewChallenge(AuthService.getInstance().getID(), challengeName, 5);
                        break;
                    case 3:
                        RemotePointsChallenge.generateNewChallenge(AuthService.getInstance().getID(), challengeName, 7);
                        break;
                    case 4:
                        RemotePointsChallenge.generateNewChallenge(AuthService.getInstance().getID(), challengeName, 14);
                        break;
                }
                //Refresh the list of challenges
                fillChallengeListView();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Check if the user of this profile is a friend of the authenticated user.
     * @return true of they are friends, false otherwise.
     */
    private boolean isFriend() {
        return AuthService.getInstance().getAuthAccount().getFriends().stream().anyMatch(x -> x.getUid().equals(((OtherAccount)displayedAccount).getUserID()));
    }

    /**
     * On add friend button click
     */
    public void addFriendButton(View view) {
        hideFriendButtons();

        if(!Database.getInstance().isOnline()) {
            showErrorMessage();
            hideUI(false, false);
            return;
        }

        // Start a new thread that will handle the process
        Thread addThread = new Thread(){
            @Override
            public void run() {
                // Add the friend and wait for the task to end
                ProfileOutcome addFriendOutcome = AuthService.getInstance().getAuthAccount().addFriend(((OtherAccount)displayedAccount).getUserID());

                // Update the view on the UI thread
                runOnUiThread(() -> {

                    String outcomeMessage = getResources().getString(addFriendOutcome.getMessage());

                    if(addFriendOutcome == ProfileOutcome.FRIEND_ADDED) {
                        // Update UI
                        hideUI(false, true);

                        outcomeMessage = displayedAccount.getUsername() + " " + outcomeMessage;
                    }

                    // Display the message
                    Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), outcomeMessage, Snackbar.LENGTH_LONG);
                    snackbar.show();
                });
            }
        };
        addThread.start();
    }

    /**
     * On remove friend button click
     */
    public void removeFriendButton(View view) {
        hideFriendButtons();

        if(!Database.getInstance().isOnline()) {
            showErrorMessage();
            hideUI(false, true);
            return;
        }

        // Start a new thread that will handle the process
        Thread removeThread = new Thread(){
            @Override
            public void run() {
                // Remove the friend and wait for the task to end
                AuthService.getInstance().getAuthAccount().removeFriend(((OtherAccount)displayedAccount).getUserID());

                // Update the view on the UI thread
                runOnUiThread(() -> {
                    // Update UI
                    hideUI(false, false);

                    String removedMessage = displayedAccount.getUsername() + " " + getResources().getString(R.string.friend_removed);
                    // Display the message
                    Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), removedMessage, Snackbar.LENGTH_LONG);
                    snackbar.show();
                });
            }
        };
        removeThread.start();
    }

    /**
     * On social activity button click
     */
    public void socialActivityButton(View view) {
        finish();
    }

    /**
     * Show an error message when the DB is not online.
     */
    private void showErrorMessage() {
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), ProfileOutcome.FAIL.getMessage(), Snackbar.LENGTH_LONG);
        snackbar.show();
    }
}
