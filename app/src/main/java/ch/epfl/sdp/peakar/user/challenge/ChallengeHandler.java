package ch.epfl.sdp.peakar.user.challenge;

import android.annotation.SuppressLint;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import ch.epfl.sdp.peakar.user.challenge.goal.RemotePointsChallenge;
import ch.epfl.sdp.peakar.user.services.AuthAccount;
import ch.epfl.sdp.peakar.user.services.AuthService;

/**
 * This class handles challenges that the user is enrolled in
 * Check finish time, handle rewards...
 */
public class ChallengeHandler {

    private static AuthAccount userAccount = null;
    private static ArrayList<Timer> challengeExpirationList = null;

    /**
     * challenge handler singleton
     */
    @SuppressLint("NewApi")
    public static void init(){
        userAccount = AuthService.getInstance().getAuthAccount();
        challengeExpirationList = new ArrayList<Timer>();

        // Replace the account and stop the old timers
        userAccount = AuthService.getInstance().getAuthAccount();
        challengeExpirationList.forEach(x -> {
            x.cancel();
            x.purge();
        });
        challengeExpirationList.clear();

        // Set up a new listener
        initChallengeFinishTimeListener();

    }

    /**
     * Creates a listener for each event
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private static void initChallengeFinishTimeListener(){
        //retrieve challenges
        List<Challenge> challengeList = userAccount.getChallenges();
        //add listener for enrolled challenges
        for(Challenge challenge : challengeList){
            Timer expirationTimer = new Timer();
            Date finishDate = Date.from(challenge.getFinishDateTime().atZone(ZoneId.systemDefault()).toInstant());
            expirationTimer.schedule(new ChallengeExpirationTimerTask((RemotePointsChallenge)challenge,expirationTimer),finishDate);
            //Add timer to list of all timers
            challengeExpirationList.add(expirationTimer);
        }
    }


    /**
     * Class used to link a timed callback to each challenge.
     * The run method gets called when the challenge finishes
     */
    private static class ChallengeExpirationTimerTask extends TimerTask{

        /* Reference to the challenge used in callback */
        private final RemotePointsChallenge challenge;
        private final Timer timer;

        /**
         * Class constructor
         * @param challenge reference to the current challenge
         * @param timer timer that issues when challenge is over
         */
        public ChallengeExpirationTimerTask(RemotePointsChallenge challenge, Timer timer){
            this.challenge = challenge;
            this.timer = timer;
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void run() {
            //Get the reward that currently logged user has retrieved
            int reward = challenge.endChallenge();

            //TODO --> DISPLAY POP UP TO SHOW
            // THAT CHALLENGE IS FINISHED ->
            // use reward to show to the user how many points he gained
            // if the user loses the challenge reward = 0

            //Clean up ChallengeTimerTask
            challengeExpirationList.remove(timer);
            timer.cancel();
            timer.purge();
        }
    }
}
