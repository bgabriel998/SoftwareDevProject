package ch.epfl.sdp.peakar.user.challenge;

import android.net.Uri;

import java.time.LocalDateTime;
import java.util.HashMap;

import ch.epfl.sdp.peakar.user.challenge.goal.RemotePointsChallenge;
import ch.epfl.sdp.peakar.user.services.AuthService;

/**
 * Item holding all information regarding challenge
 */
public class NewChallengeItem {
    private final boolean isAuthAccountFounder;
    private final RemotePointsChallenge remotePointsChallenge;

    /**
     * Class constructor. Creates new challenge Item
     * @param remotePointsChallenge challenge instance
     * @param isAuthAccountFounder boolean that tells if the auth account user is the founder of the challenge
     */
    public NewChallengeItem(RemotePointsChallenge remotePointsChallenge, boolean isAuthAccountFounder) {
        this.isAuthAccountFounder = isAuthAccountFounder;
        this.remotePointsChallenge = remotePointsChallenge;
    }

    /**
     * @return challenge name
     */
    public String getName() {
        return remotePointsChallenge.getChallengeName();
    }


    /**
     *
     * @return boolean that tells if the auth account user is the founder of the challenge
     */
    public boolean isAuthAccountFounder(){return isAuthAccountFounder;}

    /**
     * @return founder profile picture URI
     */
    public Uri getFounderURI(){return remotePointsChallenge.getFounderUri();}

    /**
     * @return challenge status
     */
    public int getStatus() {
        return remotePointsChallenge.getStatus();
    }

    /**
     * @return number of enrolled users in the challenge
     */
    public int getNumberOfParticipants() {
        return remotePointsChallenge.getUsers().size();
    }

    /**
     * @return challenge start time (LocalDateTime)
     */
    public LocalDateTime getStartDateTime() {
        return remotePointsChallenge.getStartDateTime();
    }

    /**
     * @return challenge finish time (LocalDateTime)
     */
    public LocalDateTime getEndDateTime() {
        return remotePointsChallenge.getFinishDateTime();
    }

    /**
     * @return challenge ranking HashMap <UID, points>
     */
    public HashMap<String, Integer> getChallengeRanking(){return remotePointsChallenge.getChallengeRanking();}

    /**
     * @return challenge enrolled users HashMap <UID, Username>
     */
    public HashMap<String, String> getEnrolledUsers(){return remotePointsChallenge.getChallengeUserNames();}

    /**
     * @return challenge instance
     */
    public RemotePointsChallenge getRemotePointsChallenge(){ return remotePointsChallenge;}

    /**
     * @return true if the auth account is enrolled in the challenge
     */
    public boolean isAuthAccountEnrolled(){
        return remotePointsChallenge.getUsers().contains(AuthService.getInstance().getID());
    }
}
