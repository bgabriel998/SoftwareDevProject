package ch.epfl.sdp.peakar.user.services;

import android.net.Uri;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import ch.epfl.sdp.peakar.points.CountryHighPoint;
import ch.epfl.sdp.peakar.points.POIPoint;
import ch.epfl.sdp.peakar.user.challenge.Challenge;
import ch.epfl.sdp.peakar.user.friends.FriendItem;

public class AccountData {
    private String username;
    private long score;
    private final HashSet<POIPoint> discoveredPeaks;
    private final HashMap<String, CountryHighPoint> discoveredCountryHighPoint;
    private final HashSet<Integer> discoveredPeakHeights;
    private final List<FriendItem> friends;
    private final List<Challenge> challenges;
    private Uri photoUrl;

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

    public String getUsername() {
        return username;
    }

    public long getScore() {
        return score;
    }

    public HashSet<POIPoint> getDiscoveredPeaks() {
        return discoveredPeaks;
    }

    public HashMap<String, CountryHighPoint> getDiscoveredCountryHighPoint() {
        return discoveredCountryHighPoint;
    }

    public HashSet<Integer> getDiscoveredPeakHeights() {
        return discoveredPeakHeights;
    }

    public List<FriendItem> getFriends() {
        return friends;
    }

    public List<Challenge> getChallenges() {
        return challenges;
    }

    public Uri getPhotoUrl() {
        return photoUrl;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setScore(long score) {
        this.score = score;
    }

    public void setPhotoUrl(Uri photoUrl) {
        this.photoUrl = photoUrl;
    }
}
