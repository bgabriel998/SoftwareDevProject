package ch.epfl.sdp.peakar.social;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import ch.epfl.sdp.peakar.R;
import ch.epfl.sdp.peakar.database.Database;
import ch.epfl.sdp.peakar.user.outcome.ProfileOutcome;
import ch.epfl.sdp.peakar.user.profile.NewProfileActivity;
import ch.epfl.sdp.peakar.user.services.AuthService;
import ch.epfl.sdp.peakar.user.services.OtherAccount;
import ch.epfl.sdp.peakar.utils.MenuBarHandler;
import ch.epfl.sdp.peakar.utils.OnSwipeTouchListener;

import static ch.epfl.sdp.peakar.utils.StatusBarHandler.StatusBarLightGrey;
import static ch.epfl.sdp.peakar.utils.TopBarHandler.setupGreyTopBar;
import static ch.epfl.sdp.peakar.utils.TopBarHandler.setupSwitch;

public class SocialActivity extends AppCompatActivity {

    private final static List<SocialItem> globalSocialItems = Collections.synchronizedList(new ArrayList<>());
    private final static List<SocialItem> friendSocialItems = Collections.synchronizedList(new ArrayList<>());
    private ListView listView;
    private SocialListAdapter globalAdapter;
    private SocialListAdapter friendsAdapter;
    private View emptyFriendsView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_social);

        listView = findViewById(R.id.social_list);
        emptyFriendsView = findViewById(R.id.social_empty_friends);

        StatusBarLightGrey(this);
        setupGreyTopBar(this);
        MenuBarHandler.setup(this);

        globalAdapter = setupGlobalListAdapter();
        friendsAdapter = setupFriendsListAdapter();

        setupSwitch(this, getString(R.string.switch_all), getString(R.string.switch_friends),
                (switchView, friendsChecked) -> {
                    if (friendsChecked) setFriendsView();
                    else setGlobalView();
                });

        setGlobalView();

        // Add listener for the filter to the search bar
        EditText searchBar = findViewById(R.id.social_search_bar);
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable arg0) {
                // After some text is typed, filter the lists
                String text = searchBar.getText().toString().toLowerCase(Locale.getDefault());
                globalAdapter.filter(text);
                friendsAdapter.filter(text);
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1,
                                          int arg2, int arg3) {
            }

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2,
                                      int arg3) {
            }
        });

        findViewById(R.id.social_list).setOnTouchListener(new OnSwipeTouchListener(this));
    }

    /**
     * Sets up the global list adapter, by syncing the database with a static list
     * @return a list adapter synced with the database displaying all users.
     */
    private SocialListAdapter setupGlobalListAdapter() {
        SocialListAdapter globalAdapter = new SocialListAdapter(this,
                R.layout.social_profile_item,
                globalSocialItems,
                Collections.synchronizedList(new ArrayList<>()));
        globalAdapter.setNotifyOnChange(false);
        RemoteSocialList.synchronizeGlobal(globalSocialItems, globalAdapter);
        return globalAdapter;
    }

    /**
     * Sets up the friends list adapter, by syncing the database with a static list
     * @return a list adapter synced with the database displaying all friends.
     */
    private SocialListAdapter setupFriendsListAdapter() {
        SocialListAdapter friendsAdapter = new SocialListAdapter(this,
                R.layout.social_profile_item,
                friendSocialItems,
                Collections.synchronizedList(new ArrayList<>()));
        friendsAdapter.setNotifyOnChange(false);
        if(AuthService.getInstance().getAuthAccount() != null) RemoteSocialList.synchronizeFriends(friendSocialItems, friendsAdapter);
        return friendsAdapter;
    }

    /**
     * Set the view in global mode, showing all users.
     */
    private void setGlobalView() {
        listView.setAdapter(globalAdapter);
        emptyFriendsView.setVisibility(View.GONE);
        // Add listener for clicking on item
        listView.setOnItemClickListener((parent, view, position, id) ->
                switchToProfileActivity((SocialItem) listView.getItemAtPosition(position)));
    }

    /**
     * Set the view in global mode, showing all friends.
     */
    private void setFriendsView() {
        listView.setAdapter(friendsAdapter);
        if (friendsAdapter.isEmpty()) {
            emptyFriendsView.setVisibility(View.VISIBLE);
        }
        else {
            emptyFriendsView.setVisibility(View.GONE);
        }
        // Add listener for clicking on item
        listView.setOnItemClickListener((parent, view, position, id) ->
                switchToProfileActivity((SocialItem) listView.getItemAtPosition(position)));
    }

    /**
     * Changes to ProfileActivity of the selected user and providing intent with information
     * from the item that was clicked.
     * @param item the given item.
     */
    public void switchToProfileActivity(SocialItem item) {
        if(!Database.getInstance().isOnline()) {
            Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), ProfileOutcome.FAIL.getMessage(), Snackbar.LENGTH_LONG);
            snackbar.show();
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(R.layout.progress);
        Dialog loadingDialog = builder.create();
        loadingDialog.show();

        new Thread(() -> {
            // Load the account
            OtherAccount.getInstance(item.getUid());

            runOnUiThread(() -> {
                loadingDialog.dismiss();

                Intent intent = new Intent(this, NewProfileActivity.class);
                fillIntent(intent, item);
                startActivity(intent);
            });
        }).start();

    }

    /**
     * Fills intent with information from item
     * @param intent to fill
     * @param item the given item
     */
    private void fillIntent(Intent intent, SocialItem item) {
        if(AuthService.getInstance().getAuthAccount() != null && AuthService.getInstance().getID().equals(item.getUid())) {
            intent.putExtra(NewProfileActivity.AUTH_INTENT, true);
        } else {
            intent.putExtra(NewProfileActivity.AUTH_INTENT, false);
            intent.putExtra(NewProfileActivity.OTHER_INTENT, item.getUid());
        }
    }

    /**
     * Get the global rank of a social item.
     * @param item item to look for in the global rankings.
     * @return rank of the social item or zero if social items was not found.
     */
    public static int getGlobalRank(SocialItem item) {
        Optional<SocialItem> socialItem = globalSocialItems.stream().filter(x -> x.getUid().equals(item.getUid())).findFirst();
        int index = socialItem.map(globalSocialItems::indexOf).orElse(-1);
        return index + 1;
    }
}
