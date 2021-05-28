package ch.epfl.sdp.peakar.user.challenge.goal;

import android.net.Uri;

import androidx.annotation.Nullable;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

import ch.epfl.sdp.peakar.user.challenge.Challenge;
import ch.epfl.sdp.peakar.user.challenge.ChallengeStatus;
import ch.epfl.sdp.peakar.user.services.AuthService;

/**
 * Challenge in which the goal is to have the maximum amount of points
 * before the end of the challenge
 */
public abstract class PointsChallenge implements Challenge {
    private final String id;
    private final String founderID;
    private final String challengeName;
    private final List<String> users;
    private final int durationInDays;
    private final Uri founderUri;
    private int status;
    private final LocalDateTime creationDateTime;
    private LocalDateTime startDateTime;
    private LocalDateTime finishDateTime;
    private final HashMap<String, Integer> challengeRanking;
    private final HashMap<String, String> userIDUsername;

    /**
     * Create a new Points Challenge.
     * @param id unique identifier of the challenge.
     * @param founderID Id of the founder
     * @param challengeName challenge name.
     * @param users users who joined the challenge.
     * @param creationDateTime challenge creation date
     * @param startDateTime challenge start date
     * @param finishDateTime challenge finish date
     * @param durationInDays challenge duration (given in days)
     * @param founderUri URI of the founder profile picture
     * @param status challenge status
     * @param challengeRanking current user ranking
     * @param userIDUsername map between user ID and user names
     */
    public PointsChallenge(String id, String founderID, Uri founderUri,
                           String challengeName, List<String> users, int status,
                           LocalDateTime creationDateTime, int durationInDays,
                           LocalDateTime startDateTime, LocalDateTime finishDateTime,
                           @Nullable HashMap<String, Integer> challengeRanking,
                           @Nullable HashMap<String,String> userIDUsername) {
        this.id = id;
        this.founderID = founderID;
        this.founderUri = founderUri;
        this.challengeName = challengeName;
        this.users = users;
        this.startDateTime = startDateTime;
        this.finishDateTime = finishDateTime;
        this.status = status;
        this.creationDateTime = creationDateTime;
        this.durationInDays = durationInDays;
        this.challengeRanking = challengeRanking;
        this.userIDUsername = userIDUsername;
    }

    @Override
    public String getID() {
        return id;
    }


    @Override
    public HashMap<String,Integer> getChallengeRanking(){
        return challengeRanking;
    }

    @Override
    public HashMap<String,String> getChallengeUserNames(){
        return userIDUsername;
    }

    @Override
    public List<String> getUsers() {
        return users;
    }


    @Override
    public LocalDateTime getStartDateTime() {return startDateTime;}

    @Override
    public void setStartDateTime(LocalDateTime startDateTime){
        this.startDateTime = startDateTime;
    }

    @Override
    public LocalDateTime getFinishDateTime() {return finishDateTime;}

    @Override
    public void setFinishDateTime(LocalDateTime finishDateTime){
        this.finishDateTime = finishDateTime;
    }

    @Override
    public LocalDateTime getCreationDateTime() {return creationDateTime;}

    @Override
    public int getDurationInDays() {return durationInDays;}

    @Override
    public int getStatus() {return status;}

    @Override
    public String getChallengeName(){return challengeName;}

    @Override
    public String getFounderID(){return founderID;}

    @Override
    public Uri getFounderUri(){return founderUri;}

    @Override
    public void setStatus(ChallengeStatus challengeStatus){
        status = challengeStatus.getValue();
    }

    @Override
    public void join(){
        users.add(AuthService.getInstance().getID());
    }
}
