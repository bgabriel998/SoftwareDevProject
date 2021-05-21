package ch.epfl.sdp.peakar.user.challenge;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Interface of a basic challenge.
 */
public interface Challenge {
    long AWARDED_POINTS_PER_USER = 100;
    String FOUNDER = "founder";
    String JOINED = "joined";

    String WINNER = "winner";
    String LOSER = "loser";

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
     * @return the challenge finishing Date
     */
    LocalDateTime getFinishDateTime();


    /**
     * Set challenge finish date time
     * @param finishDateTime challenge finish date time
     */
    void setFinishDateTime(LocalDateTime finishDateTime);

    /**
     * @return the challenge start Date
     */
    LocalDateTime getStartDateTime();

    /**
     * Set challenge start time
     * @param startDateTime challenge start time
     */
    void setStartDateTime(LocalDateTime startDateTime);


    /**
     * @return the challenge creation Date
     */
    LocalDateTime getCreationDateTime();


    /**
     * @return the challenge finishing Date
     */
    int getDurationInDays();

    /**
     * @return the challenge status
     */
    int getStatus();

    /**
     * Change challenge status
     */
    void setStatus(ChallengeStatus challengeStatus);


    /**
     * Get the points that will be assigned to the winner after the challenge ends.
     */
    long getPoints();

    /**
     * Make the authenticated user join a new challenge.
     */
    ChallengeOutcome join();

}
