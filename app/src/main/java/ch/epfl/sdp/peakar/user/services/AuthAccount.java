package ch.epfl.sdp.peakar.user.services;

import java.util.ArrayList;
import java.util.stream.Collectors;

import ch.epfl.sdp.peakar.points.CountryHighPoint;
import ch.epfl.sdp.peakar.points.POIPoint;
import ch.epfl.sdp.peakar.user.friends.FriendItem;
import ch.epfl.sdp.peakar.user.outcome.ProfileOutcome;

/**
 * This class represents the local account model of an authenticated user.
 * It extends Account in order to represents possible changes in the account model.
 * For each database provider:
 * 1. extend this class implementing RemoteResource interface to handle the interaction with the specific Database.
 * 2. implement the modifiers methods that need interaction with the specific Database.
 */
public abstract class AuthAccount extends Account {
    /**
     * Get an authenticated account instance.
     * @param userID id of the user.
     */
    protected static AuthAccount getInstance(String userID) {
        // As the account firebase is the only one we use, return an instance of that.
        return RemoteAuthAccount.getInstance(userID);
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
    public ProfileOutcome changeUsername(String newUsername) {
        String oldUsername = accountData.getUsername();
        accountData.setUsername(newUsername);

        if(oldUsername.equals(USERNAME_BEFORE_REGISTRATION)) return ProfileOutcome.USERNAME_REGISTERED;
        return ProfileOutcome.USERNAME_CHANGED;
    }

    /**
     * Set the score of the authenticated user.
     */
    public void setScore(long newScore) {
        accountData.setScore(newScore);
    }

    /**
     * Add a friend of the authenticated user.
     * @param friendUsername friend's username.
     * @return outcome of the process.
     */
    public abstract ProfileOutcome addFriend(String friendUsername);

    /**
     * Add a <code>friendItem</code> to the list of friend items of the user.
     * @param friendItem <code>friendItem</code> corresponding to a new friend.
     */
    protected void addFriend(FriendItem friendItem) {
        accountData.addFriend(friendItem);
    }

    /**
     * Remove a friend of the authenticated user.
     * @param friendID friend's ID
     */
    public void removeFriend(String friendID) {
        accountData.removeFriend(friendID);
    }

    /**
     * Add entry to the list of discovered Country High points.
     * @param entry new country highest point discovered.
     */
    public void setDiscoveredCountryHighPoint(CountryHighPoint entry) {
        if(!getDiscoveredCountryHighPoint().containsKey(entry.getCountryName())) {
            accountData.addDiscoveredCountryHighPoint(entry.getCountryName(), entry);
        }
    }

    /**
     * Add new Height badge to the local Hashset and to the database
     * Avoid duplicates.
     * @param badge height badge (see ScoringConstants.java)
     */
    public void setDiscoveredPeakHeights(int badge) {
        accountData.addDiscoveredPeakHeight(badge);
    }

    /**
     * Append the list of new discovered peaks.
     * WARNING : On DB, the list given to the method filter out all peaks already present.
     */
    public void setDiscoveredPeaks(ArrayList<POIPoint> newDiscoveredPeaks) {
        newDiscoveredPeaks.forEach(x -> accountData.addPeak(x));
    }


    /* UTILITY METHODS */


    /**
     * Filter the list of discovered peaks. If the peak in the list is already contained in the Hashset
     * the peak gets dropped from the list.
     * Updates the HashSet with the new values
     * @param unfilteredDiscoveredPeaks unfiltered list of peaks (coming directly from AR activity)
     * @return list of POI after filtering
     */
    public ArrayList<POIPoint> filterNewDiscoveredPeaks(ArrayList<POIPoint> unfilteredDiscoveredPeaks){
        ArrayList<POIPoint> resultList = unfilteredDiscoveredPeaks.stream().filter(newPeak -> !accountData.getDiscoveredPeaks().contains(newPeak)).collect(Collectors.toCollection(ArrayList::new));
        // Update the list of discovered peaks (local HashSet)
        resultList.forEach(x -> accountData.addPeak(x));
        return resultList;
    }
}
