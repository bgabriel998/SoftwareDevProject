package ch.epfl.sdp.peakar.user.challenge.goal;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import ch.epfl.sdp.peakar.database.Database;
import ch.epfl.sdp.peakar.database.DatabaseReference;
import ch.epfl.sdp.peakar.user.challenge.Challenge;
import ch.epfl.sdp.peakar.user.challenge.ChallengeOutcome;
import ch.epfl.sdp.peakar.user.challenge.ChallengeStatus;
import ch.epfl.sdp.peakar.user.score.ScoringConstants;
import ch.epfl.sdp.peakar.user.services.AuthService;
import ch.epfl.sdp.peakar.user.services.OtherAccount;

/**
 * Concrete implementation of <code>PointsChallenge</code> using Database interaction.
 */
public class RemotePointsChallenge extends PointsChallenge {

    /**
     * Constructor for a new concrete points challenge. This constructor needs to be called to retrieve an existing challenge, not to create a new one.
     * @param id unique identifier of the challenge.
     * @param founderID Id of the founder
     * @param challengeName challenge name.
     * @param users users who joined the challenge.
     * @param userIDPointsMap current user ranking
     * @param userIDUsernameMap map between user ID and user names
     */
    public RemotePointsChallenge(String id, String founderID, Uri founderUri , String challengeName, List<String> users, int status,
                                 LocalDateTime creationDateTime, int durationInDays,
                                 LocalDateTime startDateTime, LocalDateTime finishDateTime,
                                 @Nullable HashMap<String,Integer> userIDPointsMap,
                                 @Nullable HashMap<String,String> userIDUsernameMap) {
        super(id, founderID, founderUri, challengeName, users, status, creationDateTime, durationInDays, startDateTime, finishDateTime, userIDPointsMap,userIDUsernameMap);


    }

    /**
     * Generate a new PointsChallenge.
     * @param founderID ID of the founder of the challenge.
     * @param durationInDays duration of the challenge in number of days
     */
    @SuppressLint("NewApi")
    public static Challenge generateNewChallenge(String founderID, String challengeName, int durationInDays) {
        // Create a new challenge remotely
        DatabaseReference challengeRef = Database.getInstance().getReference().child(Database.CHILD_CHALLENGES).push();
        // Get ID of new challenge
        String id = challengeRef.getKey();
        assert id != null;
        // Add users
        List<String> users = new ArrayList<>(Collections.singleton(founderID));
        challengeRef.child(Database.CHILD_USERS).child(founderID).setValue(FOUNDER);

        //Set name
        challengeRef.child(Database.CHILD_CHALLENGE_NAME).setValue(challengeName);

        //Set name
        challengeRef.child(Database.CHILD_CHALLENGE_FOUNDER).setValue(founderID);

        // Add start/finish times
        LocalDateTime creationTime = LocalDateTime.now();
        challengeRef.child(Database.CHILD_CHALLENGE_CREATION).setValue(creationTime.toString());
        challengeRef.child(Database.CHILD_CHALLENGE_DURATION).setValue(durationInDays);

        //Add challenge status
        challengeRef.child(Database.CHILD_CHALLENGE_STATUS).setValue(ChallengeStatus.PENDING.getValue());

        // Join the new challenge remotely + set value to current amount of points that the user has
        Database.getInstance().getReference().child(Database.CHILD_USERS).child(founderID).child(Database.CHILD_CHALLENGES).child(id).setValue(0);
        RemotePointsChallenge newChallenge = new RemotePointsChallenge(id,founderID, AuthService.getInstance().getPhotoUrl(), challengeName, users, ChallengeStatus.PENDING.getValue(),
                            creationTime, durationInDays,null,null, null, null);
        // Add locally if the founder is the authenticated user
        if(founderID.equals(AuthService.getInstance().getID())) AuthService.getInstance().getAuthAccount().getChallenges().add(newChallenge);
        return newChallenge;
    }


    @SuppressLint("NewApi")
    @Override
    public ChallengeOutcome join() {
        // If the authenticated user has already joined this challenge.
        if(getUsers().contains(AuthService.getInstance().getID())) return ChallengeOutcome.NOT_POSSIBLE;

        // Join remotely
        Database.getInstance().getReference().child(Database.CHILD_CHALLENGES).child(getID()).child(Database.CHILD_USERS).child(AuthService.getInstance().getID()).setValue(JOINED);
        // Add locally
        AuthService.getInstance().getAuthAccount().getChallenges().add(this);

        /*Set start score */
        Database.getInstance().getReference().child(Database.CHILD_USERS).child(AuthService.getInstance().getID()).child(Database.CHILD_CHALLENGES)
                .child(getID()).setValue(AuthService.getInstance().getAuthAccount().getScore());

        //Check if challenge has started
        if(getStatus() == ChallengeStatus.PENDING.getValue()){
            //Start challenge status and start/stop (locally)
            setStatus(ChallengeStatus.ONGOING);
            setStartDateTime(LocalDateTime.now());
            setFinishDateTime(LocalDateTime.now().plusDays(getDurationInDays()));
            //Start challenge status and start/stop (on DB)
            Database.getInstance().getReference().child(Database.CHILD_CHALLENGES).child(getID()).child(Database.CHILD_CHALLENGE_START).setValue(getStartDateTime().toString());
            Database.getInstance().getReference().child(Database.CHILD_CHALLENGES).child(getID()).child(Database.CHILD_CHALLENGE_FINISH).setValue(getFinishDateTime().toString());
            Database.getInstance().getReference().child(Database.CHILD_CHALLENGES).child(getID()).child(Database.CHILD_CHALLENGE_STATUS).setValue(ChallengeStatus.ONGOING.getValue());


            //Initialize initial amount of points for the two challengers (owner + joiner)
            List<String> users = getUsers();
            for(String user : users){
                if(!user.equals(AuthService.getInstance().getID()))
                    Database.getInstance().getReference().child(Database.CHILD_USERS)
                        .child(user).child(Database.CHILD_CHALLENGES).child(getID()).setValue(OtherAccount.getInstance(user).getScore());

            }
        }
        return super.join();
    }


    /**
     * Challenge Timer expired
     * Give points to users
     * Remove challenge from database and from each users that have participated
     * @return number of points gained. If the user has not the most points among the users during
     * the challenge period, he gets 0 pts
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public int endChallenge(){
        int pointsGained = 0;

        if(getStatus() == ChallengeStatus.ONGOING.getValue()){
            //Set the status of the challenge to ENDED if it is still ONGOING
            setStatus(ChallengeStatus.ENDED);
            Database.getInstance().getReference().child(Database.CHILD_CHALLENGES).child(getID()).child(Database.CHILD_CHALLENGE_STATUS)
                    .setValue(ChallengeStatus.ENDED.getValue());
            //Search the winner among enrolled users
            List<String> enrolledUsers = getUsers();
            //Map <UID,Score>
            HashMap<String, Integer> scoreMap = getPointsGainedPerUser(enrolledUsers);
            //Find winner UID
            String winner = findWinner(scoreMap);
            //Reward winner with points
            rewardWinner(winner);
            //Remove winner -> enrolledUsers is now list of losers
            enrolledUsers.remove(winner);
            //Set winner / loser tags for each enrolled users in the DB
            modifyUserStatusInDB(winner, enrolledUsers);
        }

        //Check if authUser is winner or loser
        String authUser = Database.getInstance().getReference().child(Database.CHILD_CHALLENGES)
                .child(getID())
                .child(Database.CHILD_USERS).child(AuthService.getInstance()
                .getID()).get().getValue(String.class);
        if(authUser.equals(WINNER)){
            pointsGained = ScoringConstants.REWARD_FINISH_CHALLENGE;
        }

        //Check if the user was the last in the challenge -> if yes remove the
        //challenge from the DB
        List<String> users = getUsers();
        //Remove self from the list
        users.remove(AuthService.getInstance().getID());
        if(users.size() >= 1) {
            //Remove user from the /challenge/id/users list
            Database.getInstance().getReference().child(Database.CHILD_CHALLENGES)
                    .child(getID()).child(Database.CHILD_USERS)
                    .child(AuthService.getInstance().getID()).removeValueAsync();
        }
        else {
            //Remove completely the challenge if no user is left in the challenge
            Database.getInstance().getReference().child(Database.CHILD_CHALLENGES).child(getID()).removeValueAsync();
        }

        //Remove the challenge from the user section
        Database.getInstance().getReference().child(Database.CHILD_USERS)
                .child(AuthService.getInstance().getID()).child(Database.CHILD_CHALLENGES)
                .child(getID()).removeValue();

        return pointsGained;
    }

    /**
     * Check among the hashMap of enrolled user which one has the most points
     * @param enrolledUserScoreMap map between UID and score points gained during the challenge
     * @return UID of the user with the most points gained
     */
    public String findWinner(HashMap<String, Integer> enrolledUserScoreMap){
        Map.Entry<String, Integer> maxEntry = null;
        for(Map.Entry<String,Integer> entry : enrolledUserScoreMap.entrySet()){
            if(maxEntry == null || entry.getValue().compareTo(maxEntry.getValue())> 0)
                maxEntry = entry;
        }
        assert maxEntry != null;
        return maxEntry.getKey();
    }

    /**
     * Update winner score in the DB
     * @param winnerUID winner user ID
     */
    private void rewardWinner(String winnerUID){
        int currentScore  = Database.getInstance().getReference()
                .child(Database.CHILD_USERS).child(winnerUID)
                .child(Database.CHILD_SCORE)
                .get().getValue(Integer.class);
        currentScore+= ScoringConstants.REWARD_FINISH_CHALLENGE;
        Database.getInstance().getReference()
                .child(Database.CHILD_USERS).child(winnerUID)
                .child(Database.CHILD_SCORE)
                .setValueAsync(currentScore);
    }

    /**
     * Retrieve points gained by each enrolled user during the challenge
     * @param enrolledUsers list of enrolled users
     * @return HashMap<String,Integer> with key = UID and value = points gained
     */
    public HashMap<String,Integer> getPointsGainedPerUser(List<String> enrolledUsers){
        HashMap<String, Integer> retScoreMap = new HashMap<>();
        for(String user : enrolledUsers){
            //Get user score at beginning of challenge
            int initialScore =  Optional.ofNullable(Database.getInstance().getReference()
                    .child(Database.CHILD_USERS).child(user)
                    .child(Database.CHILD_CHALLENGES).child(getID())
                    .get().getValue(Integer.class)).orElse(0);

            //Get current user score
            int currentScore = Optional.ofNullable(Database.getInstance().getReference()
                    .child(Database.CHILD_USERS).child(user)
                    .child(Database.CHILD_SCORE)
                    .get().getValue(Integer.class)).orElse(0);

            String username = Optional.ofNullable(Database.getInstance().getReference()
                    .child(Database.CHILD_USERS).child(user)
                    .child(Database.CHILD_USERNAME)
                    .get().getValue(String.class)).orElse("");

            //Compute the number of points gained during the challenge
            int pointsGainedInChallenge = currentScore - initialScore;
            retScoreMap.put(user,pointsGainedInChallenge);
        }
        return retScoreMap;
    }

    /**
     * Write to challenge for each user if he is winner or loser
     * @param winnerUID ID of user that won the challenge
     * @param losersUIDs IDs of users that loses the challenge
     */
    private void modifyUserStatusInDB(String winnerUID, List<String> losersUIDs){
        Database.getInstance().getReference().child(Database.CHILD_CHALLENGES).child(getID())
                .child(Database.CHILD_USERS).child(winnerUID).setValue(WINNER);
        for(String userID : losersUIDs){
            Database.getInstance().getReference().child(Database.CHILD_CHALLENGES).child(getID())
                    .child(Database.CHILD_USERS).child(userID).setValue(LOSER);
        }
    }
}
