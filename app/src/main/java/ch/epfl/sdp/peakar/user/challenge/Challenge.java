package ch.epfl.sdp.peakar.user.challenge;

import java.util.Date;
import java.util.List;
import java.util.Set;

public interface Challenge {
    long AWARDED_POINTS_PER_USER = 100;
    String FOUNDER = "founder";
    String JOINED = "joined";

    /**
     * Get the unique identifier of the challenge.
     */
    String getID();

    /**
     * Get the users who joined the challenge.
     * @return the list of IDs of the users who joined this challenge.
     */
    List<String> getUsers();

    /**
     * Add a user to the challenge.
     */
    void addUser(String userID);

    /**
     * Get the points that will be assigned to the winner after the challenge ends.
     */
    long getPoints();

    /**
     * Make the authenticated user join a new challenge.
     */
    ChallengeOutcome join();

    /**
     * Claim the victory of the challenge.
     * The victory points will be assigned to the current authenticated user if the user meets the requirements.
     * If the reward has already been claimed, an exception will be thrown.
     * The remote challenge has to be removed after this method is performed.
     */
    ChallengeOutcome claimVictory();
}
