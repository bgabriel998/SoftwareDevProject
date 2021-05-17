package ch.epfl.sdp.peakar;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

import ch.epfl.sdp.peakar.user.challenge.Challenge;
import ch.epfl.sdp.peakar.user.friends.FriendItem;
import ch.epfl.sdp.peakar.user.services.AuthService;

public class ChallengeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge);


        FriendItem friend1 = new FriendItem(AuthService.getInstance().getAuthAccount().getFriends().get(0).getUid());
        List<Challenge> firend1Challenges = friend1.getChallenges();
        if(firend1Challenges.size() !=0 ){
            Challenge challenge = firend1Challenges.get(0); // Get first challenge and enroll
            challenge.join();
        }

        Log.v("","");
    }
}