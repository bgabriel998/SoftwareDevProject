package ch.epfl.sdp.peakar.user.friends;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import ch.epfl.sdp.peakar.R;
import ch.epfl.sdp.peakar.user.services.AuthService;
import ch.epfl.sdp.peakar.utils.ToolbarHandler;

import java.util.Locale;

public class FriendItemActivity extends AppCompatActivity {
    private String UId;
    private String username;
    public static final String INTENT_EXTRA_NAME = "FRIEND_REMOVED";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_friend_item);

        Intent intent = getIntent();
        setupInformation(intent);

        ToolbarHandler.SetupToolbar(this, username);
    }

    /**
     * Setup the information on the friend activity from data of intent.
     * @param intent given intent
     */
    private void setupInformation(Intent intent){
        // Points
        TextView pointText = findViewById(R.id.pointText);
        pointText.setText(String.format(Locale.getDefault(), " %d",
                intent.getIntExtra("points", 0)));
        UId = intent.getStringExtra("uid");
        username = intent.getStringExtra("username");
    }

    /**
     * Remove a friend when the remove friend icon button is pressed.
     */
    public void removeFriendButton(View view) {
        // Initialize intent extra string
        String INTENT_EXTRA_VALUE = username + " " + getResources().getString(R.string.friend_removed);
        Log.d("FRIEND REMOVED", "removeFriendButton: " + INTENT_EXTRA_VALUE);

        // Remove the friend
        AuthService.getInstance().getAuthAccount().removeFriend(UId);

        // Put extra to the intent so a message can be shown in friends activity
        Intent intent = new Intent(this, FriendsActivity.class);
        intent.putExtra(INTENT_EXTRA_NAME, INTENT_EXTRA_VALUE);

        // Get back to the friends activity
        startActivity(intent);

        // Destroy this activity
        finish();
    }
}
