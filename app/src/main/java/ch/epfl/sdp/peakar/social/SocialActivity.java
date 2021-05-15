package ch.epfl.sdp.peakar.social;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ch.epfl.sdp.peakar.R;
import ch.epfl.sdp.peakar.utils.MenuBarHandler;

import static ch.epfl.sdp.peakar.utils.StatusBarHandler.StatusBarLightGrey;
import static ch.epfl.sdp.peakar.utils.TopBarHandler.setupGreyTopBar;

public class SocialActivity extends AppCompatActivity {

    private final static List<SocialItem> globalSocialItems = Collections.synchronizedList(new ArrayList<>());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_social);

        StatusBarLightGrey(this);
        setupGreyTopBar(this);
        MenuBarHandler.setup(this);

        ListView collectionListView = findViewById(R.id.social_list);

        SocialListAdapter globalListAdapter = new SocialListAdapter(this,
                R.layout.social_profile_item,
                globalSocialItems);

        globalListAdapter.setNotifyOnChange(false);

        // Start rankings synchronization
        RemoteSocialList.synchronizeGlobal(globalSocialItems, globalListAdapter);

        collectionListView.setAdapter(globalListAdapter);
    }
}