package ch.epfl.sdp.peakar.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import ch.epfl.sdp.peakar.R;
import ch.epfl.sdp.peakar.database.Database;
import ch.epfl.sdp.peakar.general.MainActivity;
import ch.epfl.sdp.peakar.social.RemoteSocialList;
import ch.epfl.sdp.peakar.social.SocialItem;
import ch.epfl.sdp.peakar.social.SocialListAdapter;
import ch.epfl.sdp.peakar.user.outcome.ProfileOutcome;
import ch.epfl.sdp.peakar.user.profile.ProfileActivity;
import ch.epfl.sdp.peakar.user.services.AuthService;
import ch.epfl.sdp.peakar.user.services.OtherAccount;

import static ch.epfl.sdp.peakar.general.MainActivity.lastFragmentIndex;
import static ch.epfl.sdp.peakar.utils.MenuBarHandlerFragments.updateSelectedIcon;
import static ch.epfl.sdp.peakar.utils.MainPagerAdapter.SETTINGS_FRAGMENT_INDEX;
import static ch.epfl.sdp.peakar.utils.MainPagerAdapter.SOCIAL_FRAGMENT_INDEX;
import static ch.epfl.sdp.peakar.utils.StatusBarHandlerFragments.StatusBarLightGrey;
import static ch.epfl.sdp.peakar.utils.TopBarHandlerFragments.setupGreyTopBar;
import static ch.epfl.sdp.peakar.utils.TopBarHandlerFragments.setupSwitch;

/**
 * A simple {@link Fragment} subclass that represents the social section.
 * Use the {@link SocialFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SocialFragment extends Fragment {

    private boolean returnToFragment;
    private final static List<SocialItem> globalSocialItems = Collections.synchronizedList(new ArrayList<>());
    private final static List<SocialItem> friendSocialItems = Collections.synchronizedList(new ArrayList<>());
    private ListView listView;
    private SocialListAdapter globalAdapter;
    private SocialListAdapter friendsAdapter;
    private View emptyFriendsView;
    private ConstraintLayout container;
    private SharedPreferences sharedPreferences;
    private String authUserID;

    /**
     * Constructor for the CameraPreview
     * Is required to be empty for the fragments
     */
    public SocialFragment() {}

    /**
     * Use this factory method to create a new instance of
     * this fragment.
     *
     * @return A new instance of fragment SocialFragment.
     */
    public static SocialFragment newInstance() {
        return new SocialFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        returnToFragment = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_social, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        container = (ConstraintLayout) view;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
    }

    @Override
    public void onResume() {
        super.onResume();
        requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        if(lastFragmentIndex.contains(SOCIAL_FRAGMENT_INDEX)) {
            lastFragmentIndex.remove((Object)SOCIAL_FRAGMENT_INDEX);
        }
        lastFragmentIndex.push(SOCIAL_FRAGMENT_INDEX);
        if(returnToFragment){
            reloadFragment();
            // If the user is not the same, reload the friends list
            if(AuthService.getInstance().getID() == null || !AuthService.getInstance().getID().equals(authUserID)) {
                SocialListAdapter oldFriendAdapter = friendsAdapter;
                authUserID = AuthService.getInstance().getID();
                friendsAdapter = setupFriendsListAdapter();

                // If the list that is shown is the list of friends, immediately update the view
                if(listView.getAdapter().equals(oldFriendAdapter)){
                    setFriendsView();
                }
            }
        }
        else{
            initFragment();
            returnToFragment = true;
        }
    }

    /**
     * Reloads the fragment
     */
    private void reloadFragment() {
        updateSelectedIcon(this);
        StatusBarLightGrey(this);
        setupGreyTopBar(this);
        setupSwitch(this, getString(R.string.switch_all), getString(R.string.switch_friends),
                (switchView, friendsChecked) -> {
                    if (friendsChecked) setFriendsView();
                    else setGlobalView();
                });

        if(sharedPreferences.getBoolean(getString(R.string.offline_mode_key), false)){
            Snackbar snackbar = Snackbar.make(container.findViewById(R.id.socialFragmentLayout),
                    getString(R.string.internet_connection_needed_social), Snackbar.LENGTH_LONG);
            snackbar.setAction("Settings", v -> {
                MainActivity activity = (MainActivity) getActivity();
                assert activity != null;
                activity.setCurrentPagerItem(SETTINGS_FRAGMENT_INDEX);
            });
            snackbar.setActionTextColor(requireContext().getColor(R.color.BabyBlue));
            snackbar.show();
        }
    }

    /**
     * Initialises the fragment
     */
    private void initFragment() {

        listView = container.findViewById(R.id.social_list);
        emptyFriendsView = container.findViewById(R.id.social_empty_friends);

        reloadFragment();

        globalAdapter = setupGlobalListAdapter();
        friendsAdapter = setupFriendsListAdapter();


        setGlobalView();

        // Add listener for the filter to the search bar
        EditText searchBar = container.findViewById(R.id.social_search_bar);
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
    }


    /**
     * Sets up the global list adapter, by syncing the database with a static list
     * @return a list adapter synced with the database displaying all users.
     */
    private SocialListAdapter setupGlobalListAdapter() {
        SocialListAdapter globalAdapter = new SocialListAdapter(getContext(),
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
        friendSocialItems.clear();
        SocialListAdapter friendsAdapter = new SocialListAdapter(getContext(),
                R.layout.social_profile_item,
                friendSocialItems,
                Collections.synchronizedList(new ArrayList<>()));
        friendsAdapter.setNotifyOnChange(false);
        if(AuthService.getInstance().getAuthAccount() != null) {
            authUserID = AuthService.getInstance().getID();
            RemoteSocialList.synchronizeFriends(friendSocialItems, friendsAdapter);
        }
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
            Snackbar snackbar = Snackbar.make(container.findViewById(R.id.social_list), ProfileOutcome.FAIL.getMessage(), Snackbar.LENGTH_LONG);
            snackbar.show();
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(R.layout.progress);
        Dialog loadingDialog = builder.create();
        loadingDialog.show();
        loadingDialog.setCanceledOnTouchOutside(false);
        loadingDialog.setCancelable(false);

        new Thread(() -> {
            // Load the account
            OtherAccount.getInstance(item.getUid());

            requireActivity().runOnUiThread(() -> {
                loadingDialog.dismiss();

                Intent intent = new Intent(requireContext(), ProfileActivity.class);
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
            intent.putExtra(ProfileActivity.AUTH_INTENT, true);
        } else {
            intent.putExtra(ProfileActivity.AUTH_INTENT, false);
            intent.putExtra(ProfileActivity.OTHER_INTENT, item.getUid());
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