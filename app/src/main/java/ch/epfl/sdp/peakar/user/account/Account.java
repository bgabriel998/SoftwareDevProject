package ch.epfl.sdp.peakar.user.account;

import android.net.Uri;

import ch.epfl.sdp.peakar.points.CountryHighPoint;
import ch.epfl.sdp.peakar.points.POIPoint;
import ch.epfl.sdp.peakar.user.friends.FriendItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public interface Account {
    int MAX_LENGHT = 15;
    int MIN_LENGTH = 3;

    /**
     * @return the reference to the user account
     */
    public static Account getAccount() {
        // as FirebaseAccount is the only account, getAccount will return FirebaseAccount.getAccount
        return FirebaseAccount.getAccount();
    }

    /**
     * @return true if the account is signed in and false otherwise
     */
    public boolean isSignedIn();

    /**
     * @return the provider of the Firebase account
     */
    public String getProviderId();

    /**
     * @return the main display name of the user or null if no account is signed in
     */
    public String getDisplayName();

    /**
     * @return the e-mail of the user or null if no account is signed in
     */
    public String getEmail();

    /**
     * @return the ID of the user or null if no account is signed in
     */
    public String getId();

    /**
     * @return the URL of the user profile image
     */
    public Uri getPhotoUrl();

    /**
     * @return the username of the current account or null if no account is signed in
     */
    public String getUsername();

    /**
     * Check if the username chosen by the user is valid.
     * @param username inserted by the user.
     * @return true if and only if the username is valid.
     */
    public static Boolean isValid(String username) {
        return username != null && username.matches("\\w*") && username.length() >= 3 && username.length() <= 15;
    }

    /**
     * set the user score in the database
     */
    public void setUserScore(long newScore);

    /**
     *
     * @return the score of the user or zero if no account is signed in
     */
    public long getUserScore();


    /**
     * Add entry to the list of discovered Country High points
     * @param entry new country highest point discovered
     */
    public void setDiscoveredCountryHighPoint(CountryHighPoint entry);


    /**
     * @return country high points discovered by the user
     */
    public HashMap<String, CountryHighPoint> getDiscoveredCountryHighPoint();

    /**
     * @return hashset of discovered peaks
     */
    public HashSet<POIPoint> getDiscoveredPeaks();

    /**
     * Append the list of new discovered peaks to the database.
     * WARNING : The list given to the method filter out all peaks already in the database
     */
    public void setDiscoveredPeaks(ArrayList<POIPoint> newDiscoveredPeaks);


    /**
     * Creates a callback to synchronize the whole user profile
     */
    public void synchronizeUserProfile();

    /**
     * Add new Height badge to the local Hashset and to the database
     * Avoid duplicates.
     * @param badge height badge (see ScoringConstants.java)
     */
    public void setDiscoveredPeakHeights(int badge);


    /**
     * @return list containing all height badges
     */
    public HashSet<Integer> getDiscoveredPeakHeights();

    /**
     * @return the friends of the current user or an empty list if the user has no friends
     */
    public List<FriendItem> getFriends();
}