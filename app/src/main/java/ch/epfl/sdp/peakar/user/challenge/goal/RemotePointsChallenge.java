package ch.epfl.sdp.peakar.user.challenge.goal;

import android.annotation.SuppressLint;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ch.epfl.sdp.peakar.database.Database;
import ch.epfl.sdp.peakar.database.DatabaseReference;
import ch.epfl.sdp.peakar.database.DatabaseSnapshot;
import ch.epfl.sdp.peakar.user.challenge.Challenge;
import ch.epfl.sdp.peakar.user.challenge.ChallengeOutcome;
import ch.epfl.sdp.peakar.user.services.AuthService;

/**
 * Concrete implementation of <code>PointsChallenge</code> using Database interaction.
 */
public class RemotePointsChallenge extends PointsChallenge {

    /**
     * Constructor for a new concrete points challenge. This constructor needs to be called to retrieve an existing challenge, not to create a new one.
     * @param id unique identifier of the challenge.
     * @param users users who joined the challenge.
     * @param awardPoints points that will be awarded to the winner.
     * @param goalPoints score to be reached to win.
     */
    public RemotePointsChallenge(String id, List<String> users, long awardPoints, long goalPoints,
                                 LocalDateTime startDateTime, LocalDateTime finishDateTime) {
        super(id, users, awardPoints, goalPoints, startDateTime, finishDateTime);
    }

    /**
     * Generate a new PointsChallenge.
     * @param founderID ID of the founder of the challenge.
     * @param goalPoints score to be reached to win.
     * @param durationInDays duration of the challenge in number of days
     */
    @SuppressLint("NewApi")
    public static Challenge generateNewChallenge(String founderID, long goalPoints, int durationInDays) {
        // Create a new challenge remotely
        DatabaseReference challengeRef = Database.getInstance().getReference().child(Database.CHILD_CHALLENGES).push();
        // Get ID of new challenge
        String id = challengeRef.getKey();
        assert id != null;
        // Add users
        List<String> users = new ArrayList<>(Collections.singleton(founderID));
        challengeRef.child(Database.CHILD_USERS).child(founderID).setValue(FOUNDER);
        // Add prize
        challengeRef.child(Database.CHILD_CHALLENGE_GOAL).setValue(goalPoints);

        // Add start/finish times
        LocalDateTime startDateTime = LocalDateTime.now();
        LocalDateTime finishDateTime = startDateTime.plusDays(durationInDays);
        challengeRef.child(Database.CHILD_CHALLENGE_START).setValue(startDateTime);
        challengeRef.child(Database.CHILD_CHALLENGE_FINISH).setValue(finishDateTime);

        // Join the new challenge remotely
        Database.getInstance().getReference().child(Database.CHILD_USERS).child(founderID).child(Database.CHILD_CHALLENGES).child(id).setValue(Database.VALUE_POINTS_CHALLENGE);
        RemotePointsChallenge newChallenge = new RemotePointsChallenge(id, users, 0L, goalPoints,startDateTime,finishDateTime);
        // Add locally if the founder is the authenticated user
        if(founderID.equals(AuthService.getInstance().getID())) AuthService.getInstance().getAuthAccount().getChallenges().add(newChallenge);
        return newChallenge;
    }

    @Override
    public ChallengeOutcome join() {
        // If the authenticated user has already joined this challenge.
        if(getUsers().contains(AuthService.getInstance().getID())) return ChallengeOutcome.NOT_POSSIBLE;
        // Join remotely
        Database.getInstance().getReference().child(Database.CHILD_CHALLENGES).child(getID()).child(Database.CHILD_USERS).child(AuthService.getInstance().getID()).setValue(JOINED);
        // Add locally
        AuthService.getInstance().getAuthAccount().getChallenges().add(this);
        return super.join();
    }

    @Override
    public ChallengeOutcome claimVictory() {
        ChallengeOutcome localOutcome = super.claimVictory();

        // If still miss requirements, return it.
        if(localOutcome == ChallengeOutcome.AWARDED) {
            // Check if the challenge is still there
            DatabaseSnapshot data = Database.getInstance().getReference().child(Database.CHILD_CHALLENGES).child(getID()).get();
            assert data != null;
            // If the challenge is still there, the authenticated user is indeed the winner.
            if(data.exists()) {
                // Update local score
                long newScore = AuthService.getInstance().getAuthAccount().getScore() + getPoints();
                AuthService.getInstance().getAuthAccount().setScore(newScore);

                // Remove from the users
                getUsers().forEach(x -> Database.getInstance().getReference().child(Database.CHILD_USERS).child(x).child(Database.CHILD_CHALLENGES).child(getID()).removeValueAsync());

                // Remove from the challenges
                Database.getInstance().getReference().child(Database.CHILD_CHALLENGES).child(getID()).removeValueAsync();
            } else {
                localOutcome = ChallengeOutcome.ALREADY_OVER;
            }
        }

        return localOutcome;
    }
}
