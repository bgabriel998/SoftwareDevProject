package ch.epfl.sdp.peakar.user.profile;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import ch.epfl.sdp.peakar.R;
import ch.epfl.sdp.peakar.utils.StatusBarHandler;

import static android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS;

public class NewProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_profile);

        StatusBarHandler.StatusBarTransparent(this);

        //setupUser();
        //setupOtherUser(true);
        setupOtherUser(false);

        //findViewById(R.id.profile_collection).setVisibility(View.INVISIBLE);
        findViewById(R.id.profile_empty_text).setVisibility(View.INVISIBLE);
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
}