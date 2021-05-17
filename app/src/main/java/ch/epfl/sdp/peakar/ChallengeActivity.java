package ch.epfl.sdp.peakar;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import ch.epfl.sdp.peakar.user.challenge.Challenge;
import ch.epfl.sdp.peakar.user.services.AuthService;
import ch.epfl.sdp.peakar.user.services.OtherAccount;

public class ChallengeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge);


        //RemotePointsChallenge challenge = (RemotePointsChallenge) RemotePointsChallenge.generateNewChallenge(AuthService.getInstance().getID(),100L,7);

        OtherAccount account = OtherAccount.getInstance(AuthService.getInstance().getAuthAccount().getFriends().get(0).getUid());
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Challenge challenge = account.getChallenges().get(0);
        challenge.join();
        Log.v("","");
    }
}