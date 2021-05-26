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

    public NewChallengeItem(RemotePointsChallenge remotePointsChallenge, boolean isAuthAccountFounder) {
        this.isAuthAccountFounder = isAuthAccountFounder;
        this.remotePointsChallenge = remotePointsChallenge;
    }

    public String getName() {
        return remotePointsChallenge.getChallengeName();
    }

    public String getFounderID(){return remotePointsChallenge.getFounderID();}

    public boolean isAuthAccountFounder(){return isAuthAccountFounder;}

    public Uri getFounderURI(){return remotePointsChallenge.getFounderUri();}

    public int getStatus() {
        return remotePointsChallenge.getStatus();
    }

    public int getNumberOfParticipants() {
        return remotePointsChallenge.getUsers().size();
    }

    public LocalDateTime getStartDateTime() {
        return remotePointsChallenge.getStartDateTime();
    }

    public LocalDateTime getEndDateTime() {
        return remotePointsChallenge.getFinishDateTime();
    }

    public HashMap<String, Integer> getChallengeRanking(){return remotePointsChallenge.getChallengeRanking();}

    public HashMap<String, String> getEnrolledUsers(){return remotePointsChallenge.getChallengeUserNames();}

    public RemotePointsChallenge getRemotePointsChallenge(){ return remotePointsChallenge;}

    public boolean isAuthAccountEnrolled(){
        return remotePointsChallenge.getUsers().contains(AuthService.getInstance().getID());
    }
}
