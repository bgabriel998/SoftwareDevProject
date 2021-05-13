package ch.epfl.sdp.peakar.user.challenge.goal;

import ch.epfl.sdp.peakar.user.challenge.Challenge;

/**
 * Interface for a challenge with a goal to accomplish.
 */
public interface GoalChallenge extends Challenge {

    /**
     * Check if the authenticated user meet the requirements to claim the victory of the challenge.
     * Returns true if the user meet the requirements, false otherwise.
     */
    boolean meetRequirements();
}
