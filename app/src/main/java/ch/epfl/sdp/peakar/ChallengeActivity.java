package ch.epfl.sdp.peakar;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import ch.epfl.sdp.peakar.user.challenge.ChallengeHandler;

public class ChallengeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge);


        //RemotePointsChallenge.generateNewChallenge(AuthService.getInstance().getID(),1000,7);

        ChallengeHandler.getInstance();

/*
        final OtherAccount[] account = {null};
        Thread thread = new Thread(){
            public void run(){
                account[0] = OtherAccount.getInstance(AuthService.getInstance().getAuthAccount().getFriends().get(0).getUid());
            }
        };
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        //OtherAccount account = OtherAccount.getInstance(AuthService.getInstance().getAuthAccount().getFriends().get(0).getUid());
        if(account[0].getChallenges().size() != 0){
            Challenge challenge = account[0].getChallenges().get(0);
            challenge.join();
        }
*/
        Log.v("","");
    }
}