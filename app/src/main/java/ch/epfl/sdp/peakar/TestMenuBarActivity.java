package ch.epfl.sdp.peakar;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

/**Empty activity to create tests for menu bar.
 * TODO: Remove when integrating with actual UI.
 */
public class TestMenuBarActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_menu_bar);

        //MenuBarHandler.setup(this);
    }
}