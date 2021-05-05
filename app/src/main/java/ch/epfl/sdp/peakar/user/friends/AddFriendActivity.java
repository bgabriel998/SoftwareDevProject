package ch.epfl.sdp.peakar.user.friends;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

import ch.epfl.sdp.peakar.R;
import ch.epfl.sdp.peakar.user.outcome.ProfileOutcome;
import ch.epfl.sdp.peakar.user.services.AuthService;
import ch.epfl.sdp.peakar.utils.ToolbarHandler;

public class AddFriendActivity extends AppCompatActivity {

    private static final String  TOOLBAR_TITLE = "Add friends";

    // INTENT CONSTANT
    public static final String INTENT_EXTRA_NAME = "FRIEND_ADDED";

    // VIEW REFERENCES
    private View editTextFriend;

    AuthService authService = AuthService.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);

        // Set up view variables
        editTextFriend = findViewById(R.id.editTextFriend);

        // Setup the toolbar
        ToolbarHandler.SetupToolbar(this, TOOLBAR_TITLE);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    /**
     * On submit of add friend UI button click
     * @param view
     */
    public void submitFriendButton(View view) {
        String friend = ((EditText)editTextFriend).getText().toString();

        // Start a new thread that will handle the process
        Thread addThread = new Thread(){
            @Override
            public void run() {
                // Add the friend and wait for the task to end
                ProfileOutcome result = authService.getAuthAccount().addFriend(friend);

                // Update the view on the UI thread
                runOnUiThread(() -> {

                    // Clear the text
                    ((EditText)editTextFriend).getText().clear();

                    // If the friend has been added, get back to profile activity
                    if(result == ProfileOutcome.FRIEND_ADDED) {
                        // Prepare the intent extra value
                        String INTENT_EXTRA_VALUE = friend + " " + getResources().getString(result.getMessage());

                        // Get the intent that started the activity
                        Intent intent = getIntent();

                        // Put extra to the intent so a message can be shown in profile activity
                        intent.putExtra(INTENT_EXTRA_NAME, INTENT_EXTRA_VALUE);
                        setResult(RESULT_OK, intent);
                        ((EditText)editTextFriend).getText().clear();

                        // Destroy this activity
                        finish();
                    }
                    else {
                        // Display the message
                        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), result.getMessage(), Snackbar.LENGTH_LONG);
                        snackbar.show();
                    }
                });
            }
        };
        addThread.start();
    }
}