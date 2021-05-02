package ch.epfl.sdp.peakar.user.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import ch.epfl.sdp.peakar.points.CountryHighPoint;
import ch.epfl.sdp.peakar.points.POIPoint;
import ch.epfl.sdp.peakar.user.outcome.AddFriendOutcome;
import ch.epfl.sdp.peakar.user.services.providers.firebase.FirebaseAccount;
import ch.epfl.sdp.peakar.user.outcome.UsernameChoiceOutcome;
import ch.epfl.sdp.peakar.user.friends.FriendItem;

/**
 * This class describes the local behaviour of the user regardless of the Database provider.
 * For each database provider, extend this class implementing RemoteResource interface to handle the interaction with the specific Database.
 */
public abstract class Account {

    /* CONSTANTS */
    public static int NAME_MAX_LENGTH = 15;
    public static int NAME_MIN_LENGTH = 3;
    public static String USERNAME_BEFORE_REGISTRATION = "@";

    /**
     * Get an account instance.
     * WARNING: you SHOULD NOT call this method. To get the account reference you should call .
     * @param authID
     */
    protected static Account getInstance(String authID) {
        // As the account firebase is the only one we use, return an instance of that.
        return FirebaseAccount.getInstance(authID);
    }

    /* GETTERS */

    /**
     * Get the username of the authenticated user.
     */
    public abstract String getUsername();

    /**
     * Get the score of the authenticated user.
     */
    public abstract long getScore();

    /**
     * Get the country high points discovered by the authenticated user.
     */
    public abstract HashMap<String, CountryHighPoint> getDiscoveredCountryHighPoint();

    /**
     * Return only the names of the discovered country high points
     * as a list of strings
     * @return list of peak names
     */
    public abstract List<String> getDiscoveredCountryHighPointNames();

    /**
     * Get the list containing all height badges
     */
    public abstract HashSet<Integer> getDiscoveredPeakHeights();

    /**
     * Get the peaks discovered by the authenticated user.
     */
    public abstract HashSet<POIPoint> getDiscoveredPeaks();

    /**
     * Get the friends of the the authenticated user.
     */
    public abstract List<FriendItem> getFriends();

    /**
     * Check if the current authenticated user is registered. If so, retrieve the user data.
     */
    public abstract boolean isRegistered();


    /* SETTERS */


    /**
     * Change the username of the authenticated user.
     * @param newUsername new username to use.
     * @return outcome of the process.
     */
    public abstract UsernameChoiceOutcome changeUsername(String newUsername);

    /**
     * Set the score of the authenticated user.
     */
    public abstract void setScore(long newScore);

    /**
     * Add a friend of the authenticated user.
     * @param friendUsername friend's username.
     * @return outcome of the process.
     */
    public abstract AddFriendOutcome addFriend(String friendUsername);

    /**
     * Remove a friend of the authenticated user.
     * @param friendID friend's ID
     */
    public abstract void removeFriend(String friendID);

    /**
     * Add entry to the list of discovered Country High points.
     * @param entry new country highest point discovered.
     */
    public abstract void setDiscoveredCountryHighPoint(CountryHighPoint entry);

    /**
     * Add new Height badge to the local Hashset and to the database
     * Avoid duplicates.
     * @param badge height badge (see ScoringConstants.java)
     */
    public abstract void setDiscoveredPeakHeights(int badge);

    /**
     * Append the list of new discovered peaks to the database.
     * WARNING : The list given to the method filter out all peaks already in the database
     */
    public abstract void setDiscoveredPeaks(ArrayList<POIPoint> newDiscoveredPeaks);

    /**
     * Filter the list of discovered peaks. If the peak in the list is already contained in the Hashset
     * the peak gets dropped from the list.
     * Updates the HashSet with the new values
     * @param unfilteredDiscoveredPeaks unfiltered list of peaks (coming directly from AR activity)
     * @return list of POI after filtering
     */
    public abstract ArrayList<POIPoint> filterNewDiscoveredPeaks(ArrayList<POIPoint> unfilteredDiscoveredPeaks);


    /* STATIC UTILITY METHODS */

    /**
     * Check if the username chosen by the user is valid.
     * @param username inserted by the user.
     * @return true if and only if the username is valid.
     */
    public static Boolean checkUsernameValidity(String username) {
        return username != null && username.matches("\\w*") && username.length() >= NAME_MIN_LENGTH && username.length() <= NAME_MAX_LENGTH;
    }

}
