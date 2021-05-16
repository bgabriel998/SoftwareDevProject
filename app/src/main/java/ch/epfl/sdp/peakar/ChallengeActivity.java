package ch.epfl.sdp.peakar;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

public class ChallengeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge);

        Log.v("","");
        //Challenge challenge = RemotePointsChallenge.generateNewChallenge(AuthService.getInstance().getID(),100L,7);

    }
}