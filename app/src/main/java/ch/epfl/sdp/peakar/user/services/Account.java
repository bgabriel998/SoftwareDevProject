package ch.epfl.sdp.peakar.user.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import ch.epfl.sdp.peakar.points.CountryHighPoint;
import ch.epfl.sdp.peakar.points.POIPoint;
import ch.epfl.sdp.peakar.user.friends.FriendItem;
import ch.epfl.sdp.peakar.user.services.providers.firebase.FirebaseOtherAccount;

/**
 * This class describes the local behaviour of the account model regardless of the Database provider.
 * For each database provider:
 * 1. extend this class implementing RemoteResource interface to handle the interaction with the specific Database.
 * 2. implement the modifiers methods that need interaction with the specific Database.
 */
public abstract class Account {
    /* CONSTANTS */
    public static int NAME_MAX_LENGTH = 15;
    public static int NAME_MIN_LENGTH = 3;
    public static String USERNAME_BEFORE_REGISTRATION = "@";

    protected AccountData accountData;

    /* GETTERS */

    /**
     * Get the username of the authenticated user.
     */
    public String getUsername() {
        return accountData.getUsername();
    }

    /**
     * Get the score of the authenticated user.
     */
    public long getScore() {
        return accountData.getScore();
    }

    /**
     * Get the country high points discovered by the authenticated user.
     */
    public HashMap<String, CountryHighPoint> getDiscoveredCountryHighPoint() {
        return accountData.getDiscoveredCountryHighPoint();
    }

    /**
     * Return only the names of the discovered country high points
     * as a list of strings
     * @return list of peak names
     */
    public List<String> getDiscoveredCountryHighPointNames() {
        List<String> retList = new ArrayList<>();
        for (Map.Entry<String, CountryHighPoint> highPoint : accountData.getDiscoveredCountryHighPoint().entrySet()) {
            retList.add(highPoint.getValue().getCountryHighPoint());
        }
        return retList;
    }

    /**
     * Get the list containing all height badges discovered by the authenticated user.
     */
    public HashSet<Integer> getDiscoveredPeakHeights() {
        return accountData.getDiscoveredPeakHeights();
    }

    /**
     * Get the peaks discovered by the authenticated user.
     */
    public HashSet<POIPoint> getDiscoveredPeaks() {
        return accountData.getDiscoveredPeaks();
    }

    /**
     * Get the friends of the the authenticated user.
     */
    public List<FriendItem> getFriends() {
        return accountData.getFriends();
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
