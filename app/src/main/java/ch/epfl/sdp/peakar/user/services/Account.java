package ch.epfl.sdp.peakar.user.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import ch.epfl.sdp.peakar.points.CountryHighPoint;
import ch.epfl.sdp.peakar.points.POIPoint;
import ch.epfl.sdp.peakar.user.friends.FriendItem;
import ch.epfl.sdp.peakar.user.outcome.ProfileOutcome;
import ch.epfl.sdp.peakar.user.services.providers.firebase.FirebaseAccount;

/**
 * This class describes the local behaviour of the account model regardless of the Database provider.
 * For each database provider:
 * 1. extend this class implementing RemoteResource interface to handle the interaction with the specific Database.
 * 2. implement the modifiers methods that need interaction with the specific Database.
 */
public abstract class Account {

    /* LOCAL ATTRIBUTES */
    protected String username = Account.USERNAME_BEFORE_REGISTRATION;
    protected long score = 0;
    protected HashSet<POIPoint> discoveredPeaks = new HashSet<>();
    protected HashMap<String, CountryHighPoint> discoveredCountryHighPoint = new HashMap<>();
    protected HashSet<Integer> discoveredPeakHeights = new HashSet<>();
    protected List<FriendItem> friends = new ArrayList<>();

    /* CONSTANTS */
    public static int NAME_MAX_LENGTH = 15;
    public static int NAME_MIN_LENGTH = 3;
    public static String USERNAME_BEFORE_REGISTRATION = "@";

    /**
     * Get an account instance.
     * WARNING: you SHOULD NOT call this method. To get the account reference you should call AuthInstance.getInstance().getAuthAccount().
     * @param authID id of the authenticated user.
     */
    protected static Account getInstance(String authID) {
        // As the account firebase is the only one we use, return an instance of that.
        return FirebaseAccount.getInstance(authID);
    }

    /* GETTERS */

    /**
     * Get the username of the authenticated user.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Get the score of the authenticated user.
     */
    public long getScore() {
        return score;
    }

    /**
     * Get the country high points discovered by the authenticated user.
     */
    public HashMap<String, CountryHighPoint> getDiscoveredCountryHighPoint() {
        return discoveredCountryHighPoint;
    }

    /**
     * Return only the names of the discovered country high points
     * as a list of strings
     * @return list of peak names
     */
    public List<String> getDiscoveredCountryHighPointNames() {
        List<String> retList = new ArrayList<>();
        for (Map.Entry<String, CountryHighPoint> highPoint : discoveredCountryHighPoint.entrySet()) {
            retList.add(highPoint.getValue().getCountryHighPoint());
        }
        return retList;
    }

    /**
     * Get the list containing all height badges discovered by the authenticated user.
     */
    public HashSet<Integer> getDiscoveredPeakHeights() {
        return discoveredPeakHeights;
    }

    /**
     * Get the peaks discovered by the authenticated user.
     */
    public HashSet<POIPoint> getDiscoveredPeaks() {
        return discoveredPeaks;
    }

    /**
     * Get the friends of the the authenticated user.
     */
    public List<FriendItem> getFriends() {
        return friends;
    }


    /* GETTER TO IMPLEMENT WITH DATABASE INTERACTION */


    /**
     * This method must be called on app opening if an account has been authenticated.
     * If so, check if the user is registered and in this case retrieve the user data.
     */
    public abstract void init();


    /* MODIFIERS TO IMPLEMENT WITH DATABASE INTERACTION */


    /**
     * Change the username of the authenticated user.
     * @param newUsername new username to use.
     * @return outcome of the process.
     */
    public abstract ProfileOutcome changeUsername(String newUsername);

    /**
     * Set the score of the authenticated user.
     */
    public abstract void setScore(long newScore);

    /**
     * Add a friend of the authenticated user.
     * @param friendUsername friend's username.
     * @return outcome of the process.
     */
    public abstract ProfileOutcome addFriend(String friendUsername);

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


    /* UTILITY METHODS */


    /**
     * Filter the list of discovered peaks. If the peak in the list is already contained in the Hashset
     * the peak gets dropped from the list.
     * Updates the HashSet with the new values
     * @param unfilteredDiscoveredPeaks unfiltered list of peaks (coming directly from AR activity)
     * @return list of POI after filtering
     */
    public ArrayList<POIPoint> filterNewDiscoveredPeaks(ArrayList<POIPoint> unfilteredDiscoveredPeaks){
        ArrayList<POIPoint> resultList = unfilteredDiscoveredPeaks.stream().filter(newPeak -> !discoveredPeaks.contains(newPeak)).collect(Collectors.toCollection(ArrayList::new));
        // Update the list of discovered peaks (local HashSet)
        discoveredPeaks.addAll(resultList);
        return resultList;
    }

    /**
     * Check if the username chosen by the user is valid.
     * @param username inserted by the user.
     * @return true if and only if the username is valid.
     */
    public static Boolean checkUsernameValidity(String username) {
        return username != null && username.matches("\\w*") && username.length() >= NAME_MIN_LENGTH && username.length() <= NAME_MAX_LENGTH;
    }

}
