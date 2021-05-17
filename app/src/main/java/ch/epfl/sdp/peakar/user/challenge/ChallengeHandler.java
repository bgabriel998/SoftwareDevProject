package ch.epfl.sdp.peakar.user.challenge;

import android.annotation.SuppressLint;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import ch.epfl.sdp.peakar.user.services.AuthAccount;
import ch.epfl.sdp.peakar.user.services.AuthService;

/**
 * This class handles challenges that the user is enrolled in
 * Check finish time, handle rewards...
 */
public class ChallengeHandler {

    private static final ChallengeHandler challengeHandler = null;
    private static final AuthAccount userAccount = null;
    private static List<Timer> challengeExpirationList = null;
    /**
     * Challenge Handler constructor
     */
    @SuppressLint("NewApi")
    public ChallengeHandler(){
        AuthAccount account = AuthService.getInstance().getAuthAccount();
        if(account != null){
            initChallengeFinishTimeListener();
            challengeExpirationList = new ArrayList<Timer>();
        }
    }

    /**
     * @return challenge handler singleton
     */
    public ChallengeHandler getInstance(){
        if(challengeHandler == null){
            new ChallengeHandler();
        }
        return challengeHandler;
    }

    /**
     * Creates a listener for each event
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void initChallengeFinishTimeListener(){
        //retrieve challenges
        List<Challenge> challengeList = userAccount.getChallenges();
        //add listener for enrolled challenges
        for(Challenge challenge : challengeList){
            Timer expirationTimer = new Timer();
            Date finishDate = Date.from(challenge.getFinishDateTime().atZone(ZoneId.systemDefault()).toInstant());
            expirationTimer.schedule(new ChallengeExpirationTimeTask(challenge),finishDate);
            //Add timer to list of all timers
            challengeExpirationList.add(expirationTimer);
        }
    }


    /**
     * Class used to link a timed callback to each challenge.
     * The run method gets called when the challenge finishes
     */
    static class ChallengeExpirationTimeTask extends TimerTask{

        /* Reference to the challenge used in callback */
        private final Challenge challenge;

        /*Constructor*/
        public ChallengeExpirationTimeTask(Challenge challenge){
            this.challenge = challenge;
        }

        @Override
        public void run() {
            Log.d("CHALLENGE", "Timer expired on challenge:"+ challenge.getID());
            //TODO --> Trigger notification to user


        }
    }



}
