package ch.epfl.sdp.peakar.user.challenge;

import android.net.Uri;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

/**
 * Interface of a basic challenge.
 */
public interface Challenge {
    String FOUNDER = "founder";
    String JOINED = "joined";

    String WINNER = "winner";
    String LOSER = "loser";

    /**
     * Get the unique identifier of the challenge.
     */
    String getID();


    /**
     * @return actual challenge ranking. Null if not applicable
     */
    HashMap<String,Integer> getChallengeRanking();

    /**
     * @return map between UID and username from users
     */
    HashMap<String,String> getChallengeUserNames();

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
     * @return challenge Name
     */
    String getChallengeName();

    /**
     * @return challenge founder UID
     */
    String getFounderID();

    /**
     * @return challenge founder profile pic uri
     */
    Uri getFounderUri();

    /**
     * Change challenge status
     */
    void setStatus(ChallengeStatus challengeStatus);

    /**
     * Make the authenticated user join a new challenge.
     */
    void join();

}
