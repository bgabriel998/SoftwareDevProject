package ch.epfl.sdp.peakar.user.services;

import android.net.Uri;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import ch.epfl.sdp.peakar.points.CountryHighPoint;
import ch.epfl.sdp.peakar.points.POIPoint;
import ch.epfl.sdp.peakar.user.challenge.Challenge;
import ch.epfl.sdp.peakar.user.friends.FriendItem;

/**
 * Class that represents the data content of an account.
 */
public class AccountData {
    private String username;
    private long score;
    private final HashSet<POIPoint> discoveredPeaks;
    private final HashMap<String, CountryHighPoint> discoveredCountryHighPoint;
    private final HashSet<Integer> discoveredPeakHeights;
    private final List<FriendItem> friends;
    private final List<Challenge> challenges;
    private Uri photoUrl;

    /**
     * Create an initial object with starting values.
     */
    public AccountData() {
        username = AuthAccount.USERNAME_BEFORE_REGISTRATION;
        score = 0;
        discoveredPeaks = new HashSet<>();
        discoveredCountryHighPoint = new HashMap<>();
        discoveredPeakHeights = new HashSet<>();
        friends = new ArrayList<>();
        challenges = new ArrayList<>();
        photoUrl = Uri.EMPTY;
    }

    /**
     * Get the username.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Set the username.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Get the score.
     */
    public long getScore() {
        return score;
    }

    /**
     * Set the score.
     */
    public void setScore(long score) {
        this.score = score;
    }

    /**
     * Get the discovered peaks.
     */
    public HashSet<POIPoint> getDiscoveredPeaks() {
        return discoveredPeaks;
    }

    /**
     * Add a discovered peak.
     */
    public void addPeak(POIPoint poiPoint) {
        discoveredPeaks.add(poiPoint);
    }

    /**
     * Get the discovered country high points.
     */
    public HashMap<String, CountryHighPoint> getDiscoveredCountryHighPoint() {
        return discoveredCountryHighPoint;
    }

    /**
     * Add a discovered country high point.
     */
    public void addDiscoveredCountryHighPoint(String countryName, CountryHighPoint countryHighPoint) {
        discoveredCountryHighPoint.put(countryName, countryHighPoint);
    }

    /**
     * Get the discovered peak heights.
     */
    public HashSet<Integer> getDiscoveredPeakHeights() {
        return discoveredPeakHeights;
    }

    /**
     * Add a discovered peak height.
     */
    public void addDiscoveredPeakHeight(int discoveredHeight) {
        discoveredPeakHeights.add(discoveredHeight);
    }

    /**
     * Get the friends.
     */
    public List<FriendItem> getFriends() {
        return friends;
    }

    /**
     * Add a friend.
     */
    public void addFriend(FriendItem friendItem) {
        friends.add(friendItem);
    }

    /**
     * Remove a friend.
     * @param friendID id of the friend to remove.
     */
    public void removeFriend(String friendID) {
        friends.removeIf(x -> x.hasID(friendID));
    }

    /**
     * Get the challenges.
     */
    public List<Challenge> getChallenges() {
        return challenges;
    }

    /**
     * Add a challenge.
     */
    public void addChallenge(Challenge challenge) {
        challenges.add(challenge);
    }

    /**
     * Get the photo url.
     */
    public Uri getPhotoUrl() {
        return photoUrl;
    }

    /**
     * Set the photo url.
     */
    public void setPhotoUrl(Uri photoUrl) {
        this.photoUrl = photoUrl;
    }
}
