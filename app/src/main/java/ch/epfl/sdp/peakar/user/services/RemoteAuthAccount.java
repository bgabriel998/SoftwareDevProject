package ch.epfl.sdp.peakar.user.services;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import ch.epfl.sdp.peakar.database.Database;
import ch.epfl.sdp.peakar.database.DatabaseReference;
import ch.epfl.sdp.peakar.database.DatabaseSnapshot;
import ch.epfl.sdp.peakar.general.remote.RemoteOutcome;
import ch.epfl.sdp.peakar.general.remote.RemoteResource;
import ch.epfl.sdp.peakar.points.CountryHighPoint;
import ch.epfl.sdp.peakar.points.POIPoint;
import ch.epfl.sdp.peakar.social.SocialItem;
import ch.epfl.sdp.peakar.user.outcome.ProfileOutcome;
import ch.epfl.sdp.peakar.social.RemoteFriendItem;

/**
 * This class extends AuthAccount to handle operations on a Database
 */
public class RemoteAuthAccount extends AuthAccount implements RemoteResource {
    private static String currentID = null;
    private static RemoteAuthAccount instance = null;
    private static DatabaseReference dbRefUser = null;

    /**
     * Create a new account instance.
     * @param newID ID of the authenticated user
     */
    private RemoteAuthAccount(String newID) {
        dbRefUser = Database.getInstance().getReference().child(Database.CHILD_USERS + newID);
        currentID = newID;
    }

    /**
     * Get a Firebase account instance.
     * WARNING: you SHOULD NOT call this method. To get an account you should call <code>Account.getInstance(String authID)</code>.
     * @param authID id of the user.
     */
    protected static RemoteAuthAccount getInstance(String authID) {
        // If the auth ID has not changed, return the same instance
        if(authID.equals(currentID)) return instance;

        Log.d("ACCOUNT", "getInstance: creating a new instance");
        // Otherwise, create a new instance. On completion, return it
        instance = new RemoteAuthAccount(authID);
        return instance;
    }

    @Override
    public RemoteOutcome retrieveData() {
        AccountData newAccountData = new AccountData();
        RemoteOutcome remoteOutcome = RemoteAccountDataFactory.retrieveAccountData(newAccountData, dbRefUser);
        accountData = newAccountData;
        return remoteOutcome;
    }

    @Override
    public void init() {
        if(getUsername().equals(USERNAME_BEFORE_REGISTRATION)) {
            DatabaseSnapshot data = dbRefUser.get();
            assert data != null;

            // If the account is registered but is not loaded, load it
            if (data.exists()) retrieveData();
        }
    }

    /* SETTERS */

    @Override
    public ProfileOutcome changeUsername(String newUsername) {
        String username = getUsername();
        Log.d("Account", "changeUsername: current username = " + username);

        // If the username is not valid
        if(!AuthAccount.checkUsernameValidity(newUsername)) return ProfileOutcome.INVALID;

        // If the username is the current one
        if(newUsername.equals(username)) return ProfileOutcome.USERNAME_CURRENT;

        // Finally, check if it is available
        DatabaseSnapshot data = Database.getInstance().getReference().child(Database.CHILD_USERS).orderByChild(Database.CHILD_USERNAME).equalTo(newUsername).get();
        assert data != null;

        // Check if it was already used
        if(data.exists()) return ProfileOutcome.USERNAME_USED;

        // If not, register the new username asynchronously
        dbRefUser.child(Database.CHILD_USERNAME).setValue(newUsername);

        // If the user was not registered yet, register him by adding the missing fields on the DB.
        if(getUsername().equals(USERNAME_BEFORE_REGISTRATION)) {
            // Add the photo
            dbRefUser.child(Database.CHILD_PHOTO_URL).setValue(AuthService.getInstance().getPhotoUrl().toString());
            Log.d("Account", "changeUsername - addPhotoUrl: added");
        }

        // Change username locally
        return super.changeUsername(newUsername);
    }

    @Override
    public void setScore(long newScore) {
        // Set local value
        super.setScore(newScore);

        // Set remote value
        Database.getInstance().getReference().child(Database.CHILD_USERS).child(currentID).child(Database.CHILD_SCORE).setValue(newScore);
    }

    @Override
    public ProfileOutcome addFriend(String friendId) {
        // Add the friend remotely
        dbRefUser.child(Database.CHILD_FRIENDS).child(friendId).setValue("");

        // Create a friend item
        SocialItem newFriendItem = new RemoteFriendItem(friendId);

        // Add the friend locally
        addFriend(newFriendItem);

        return ProfileOutcome.FRIEND_ADDED;
    }

    @Override
    public void removeFriend(String friendID) {
        // Remove listener
        getFriends().stream().filter(x -> x.getUid().equals(friendID)).map(x -> (RemoteFriendItem)x).forEach(RemoteFriendItem::removeListener);

        // Remove friend
        dbRefUser.child(Database.CHILD_FRIENDS).child(friendID).removeValue();

        // Remove locally
        super.removeFriend(friendID);
    }

    @Override
    public void setDiscoveredCountryHighPoint(CountryHighPoint entry){
        if(!getDiscoveredCountryHighPoint().containsKey(entry.getCountryName())) {
            // Add remotely
            Database.getInstance().getReference().child(Database.CHILD_USERS).child(currentID).child(Database.CHILD_COUNTRY_HIGH_POINT).push().setValue(entry);

            // Add locally
            super.setDiscoveredCountryHighPoint(entry);
        }
    }

    @Override
    public void setDiscoveredPeakHeights(int badge) {
        if(!getDiscoveredPeakHeights().contains(badge)){
            // Add remotely
            Database.getInstance().getReference().child(Database.CHILD_USERS).child(currentID).child(Database.CHILD_DISCOVERED_PEAKS_HEIGHTS).push().setValue(badge);

            // Add locally
            super.setDiscoveredPeakHeights(badge);
        }
    }

    @Override
    public void setDiscoveredPeaks(ArrayList<POIPoint> newDiscoveredPeaks) {
        // Add remotely
        final List<POIPoint> addedList = new ArrayList<>(newDiscoveredPeaks);
        final DatabaseReference discoveredRef = Database.getInstance().getReference().child(Database.CHILD_USERS).child(currentID).child(Database.CHILD_DISCOVERED_PEAKS);
        for(POIPoint poiPoint: addedList) {
            discoveredRef.push().setValue(poiPoint);
        }

        // Add locally
        super.setDiscoveredPeaks(newDiscoveredPeaks);
    }
}
