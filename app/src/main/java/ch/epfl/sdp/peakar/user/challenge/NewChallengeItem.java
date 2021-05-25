package ch.epfl.sdp.peakar.user.challenge;

import androidx.annotation.Nullable;

import java.time.LocalDateTime;
import java.util.HashMap;

/**
 * Item holding all information regarding challenge
 */
public class NewChallengeItem {
    private final String name;
    private final String founderID;
    private final int status;
    private final int numberOfParticipants;
    private final LocalDateTime startDateTime;
    private final LocalDateTime endDateTime;
    private final HashMap<String, Integer> challengeRanking;
    private final HashMap<String, String> enrolledUsers;

    /**
     *  @param name challenge name (name of the owner + challenge)
     * @param numberOfParticipants number of enrolled users
     * @param startDateTime start time
     * @param endDateTime end time
     */
    public NewChallengeItem(String name, String founderID, int status, int numberOfParticipants,
                            @Nullable LocalDateTime startDateTime, @Nullable LocalDateTime endDateTime
                            , @Nullable HashMap<String, Integer> challengeRanking,
                            @Nullable HashMap<String,String> enrolledUsers) {
        this.name = name;
        this.founderID = founderID;
        this.status = status;
        this.numberOfParticipants = numberOfParticipants;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.challengeRanking = challengeRanking;
        this.enrolledUsers = enrolledUsers;
    }

    public String getName() {
        return name;
    }

    public String getFounderID(){return founderID;}

    public int getStatus() {
        return status;
    }

    public int getNumberOfParticipants() {
        return numberOfParticipants;
    }

    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    public LocalDateTime getEndDateTime() {
        return endDateTime;
    }

    public HashMap<String, Integer> getChallengeRanking(){return challengeRanking;}

    public HashMap<String, String> getEnrolledUsers(){return enrolledUsers;}
}
