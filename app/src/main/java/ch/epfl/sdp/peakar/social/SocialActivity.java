package ch.epfl.sdp.peakar.social;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ch.epfl.sdp.peakar.R;
import ch.epfl.sdp.peakar.user.friends.FriendItem;
import ch.epfl.sdp.peakar.user.services.AuthService;
import ch.epfl.sdp.peakar.utils.MenuBarHandler;

import static ch.epfl.sdp.peakar.utils.StatusBarHandler.StatusBarLightGrey;
import static ch.epfl.sdp.peakar.utils.TopBarHandler.setupGreyTopBar;
import static ch.epfl.sdp.peakar.utils.TopBarHandler.setupSwitch;

public class SocialActivity extends AppCompatActivity {

    private final static List<SocialItem> globalSocialItems = Collections.synchronizedList(new ArrayList<>());
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
    }

    /**
     * Sets up the global list adapter, by syncing the database with a static list
     * @return a list adapter synced with the database displaying all users.
     */
    private SocialListAdapter setupGlobalListAdapter() {
        SocialListAdapter globalAdapter = new SocialListAdapter(this,
                R.layout.social_profile_item,
                globalSocialItems);
        globalAdapter.setNotifyOnChange(false);
        RemoteSocialList.synchronizeGlobal(globalSocialItems, globalAdapter);
        return globalAdapter;
    }

    /**
     * Sets up the friends list adapter, by syncing the database with a static list
     * TODO Create SYNC.
     * @return a list adapter synced with the database displaying all friends.
     */
    private SocialListAdapter setupFriendsListAdapter() {
        List<SocialItem> friendsList = new ArrayList<>();
        if (AuthService.getInstance().getAuthAccount() != null) {
            for (FriendItem friendItem : AuthService.getInstance().getAuthAccount().getFriends()) {
                friendsList.add(new SocialItem(friendItem.getUid(), friendItem.getUsername(),
                        friendItem.getPoints(), Uri.EMPTY));
            }
        }

        return new SocialListAdapter(this,
                R.layout.social_profile_item,
                friendsList);
    }

    /**
     * Set the view in global mode, showing all users.
     */
    private void setGlobalView() {
        listView.setAdapter(globalAdapter);
        emptyFriendsView.setVisibility(View.GONE);
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
    }
}
