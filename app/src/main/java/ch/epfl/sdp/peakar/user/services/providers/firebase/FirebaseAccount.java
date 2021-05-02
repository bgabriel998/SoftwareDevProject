package ch.epfl.sdp.peakar.user.services.providers.firebase;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import ch.epfl.sdp.peakar.database.Database;
import ch.epfl.sdp.peakar.general.remote.RemoteResource;
import ch.epfl.sdp.peakar.points.CountryHighPoint;
import ch.epfl.sdp.peakar.points.POIPoint;
import ch.epfl.sdp.peakar.general.remote.RemoteOutcome;
import ch.epfl.sdp.peakar.user.services.Account;
import ch.epfl.sdp.peakar.user.friends.FriendItem;
import ch.epfl.sdp.peakar.user.outcome.AddFriendOutcome;
import ch.epfl.sdp.peakar.user.outcome.UsernameChoiceOutcome;

/**
 * This class extends Account to handle operations on a Firebase Realtime Database
 */
public class FirebaseAccount extends Account implements RemoteResource {
    private static String currentID = null;
    private static FirebaseAccount instance = null;
    private final DatabaseReference dbRefUser;

    /* Attributes saved also on DB */

    private String username = Account.USERNAME_BEFORE_REGISTRATION;
    private long score = 0;
    private HashSet<POIPoint> discoveredPeaks = new HashSet<>();
    private HashMap<String, CountryHighPoint> discoveredCountryHighPoint = new HashMap<>();
    private HashSet<Integer> discoveredPeakHeights = new HashSet<>();
    private List<FriendItem> friends = new ArrayList<>();
    private DataSnapshot data;

    /**
     * Create a new account instance.
     * @param newID ID of the authenticated user
     */
    private FirebaseAccount(String newID) {
        dbRefUser = Database.refRoot.child(Database.CHILD_USERS +  newID);
        currentID = newID;
    }

    protected static FirebaseAccount getInstance(String authID) {
        // If the auth ID has not changed, return the same instance
        if(authID.equals(currentID)) return instance;

        Log.d("ACCOUNT", "getInstance: creating a new instance");
        // Otherwise, create a new instance. On completion, return it
        instance = new FirebaseAccount(authID);
        return instance;
    }

    @Override
    public RemoteOutcome retrieveData() {

        /* Initialize attributes */
        username = Account.USERNAME_BEFORE_REGISTRATION;
        score = 0;
        discoveredPeaks = new HashSet<>();
        discoveredCountryHighPoint = new HashMap<>();
        discoveredPeakHeights = new HashSet<>();
        friends = new ArrayList<>();

        Task<DataSnapshot> retrieveTask = dbRefUser.get();

        try {
            // Wait for task to finish
            Tasks.await(retrieveTask);

            // Get the obtained data
            DataSnapshot data = retrieveTask.getResult();
            assert data != null;

            // If there is no data on DB, return such outcome
            if(!data.exists()) return RemoteOutcome.NOT_FOUND;

            // Otherwise, update the attributes with retrieve data
            this.data = data;
            loadData();
            return RemoteOutcome.FOUND;

        } catch (Exception e) {
            Log.d("AUTH", "retrieveData: failed " + e.getMessage());
            return RemoteOutcome.FAIL;
        }
    }

    @Override
    public void loadData() {
        // Load username
        username = Optional.ofNullable(data.child(Database.CHILD_USERNAME).getValue(String.class)).orElse(USERNAME_BEFORE_REGISTRATION);

        // Load score
        score = Optional.ofNullable(data.child(Database.CHILD_SCORE).getValue(long.class)).orElse(0L);

        // Load discovered peaks
        loadPeaks(data.child(Database.CHILD_DISCOVERED_PEAKS));

        // Load discovered country high points
        loadCountryHighPoints(data.child(Database.CHILD_COUNTRY_HIGH_POINT));

        // Load discovered heights
        loadHeights(data.child(Database.CHILD_DISCOVERED_PEAKS_HEIGHTS));

        // Load friends
        loadFriends(data.child(Database.CHILD_FRIENDS));

    }

    /**
     * Load discovered peaks.
     */
    private void loadPeaks(DataSnapshot data) {
        for (DataSnapshot peak : data.getChildren()) {
            POIPoint newPeak = new POIPoint(peak.child(Database.CHILD_ATTRIBUTE_PEAK_NAME).getValue(String.class),
                    Optional.ofNullable(peak.child(Database.CHILD_ATTRIBUTE_PEAK_LATITUDE).getValue(Double.class)).orElse(0.0),
                    Optional.ofNullable(peak.child(Database.CHILD_ATTRIBUTE_PEAK_LONGITUDE).getValue(Double.class)).orElse(0.0),
                    Optional.ofNullable(peak.child(Database.CHILD_ATTRIBUTE_PEAK_ALTITUDE).getValue(Long.class)).orElse(0L));

            // Add the peak
            discoveredPeaks.add(newPeak);
        }
    }

    /**
     * Load discovered country high points.
     */
    private void loadCountryHighPoints(DataSnapshot data) {
        for (DataSnapshot countryHighPoint : data.getChildren()) {
            // Get the country name
            String countryName = Optional.ofNullable(countryHighPoint.child(Database.CHILD_ATTRIBUTE_COUNTRY_NAME).getValue(String.class)).orElse("null");

            /// Get the high point
            CountryHighPoint newCountryHighPoint = new CountryHighPoint(countryName,
                    Optional.ofNullable(countryHighPoint.child(Database.CHILD_COUNTRY_HIGH_POINT_NAME).getValue(String.class)).orElse("null"),
                    Optional.ofNullable(countryHighPoint.child(Database.CHILD_ATTRIBUTE_HIGH_POINT_HEIGHT).getValue(Long.class)).orElse(0L));

            // Add the high point
            discoveredCountryHighPoint.put(countryName, newCountryHighPoint);
        }
    }

    /**
     * Load discovered heights
     */
    private void loadHeights(DataSnapshot data) {
        for (DataSnapshot heightEntry : data.getChildren()) {
            // Get the height
            int newHeight = Optional.ofNullable(heightEntry.child("0").getValue(Integer.class)).orElse(0);

            // Add the height
            discoveredPeakHeights.add(newHeight);
        }
    }

    private void loadFriends(DataSnapshot data) {
        for (DataSnapshot friendEntry : data.getChildren()) {
            // Get the friend ID
            String uidFriend = friendEntry.getKey();

            // Create a friend item
            FirebaseFriendItem newFriendItem = new FirebaseFriendItem(uidFriend);

            // Add the friend
            friends.add(newFriendItem);
        }
    }

    /* GETTERS */

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public long getScore() {
        return score;
    }

    @Override
    public HashMap<String, CountryHighPoint> getDiscoveredCountryHighPoint() {
        return discoveredCountryHighPoint;
    }

    @Override
    public List<String> getDiscoveredCountryHighPointNames() {
        List<String> retList = new ArrayList<>();
        for (Map.Entry<String, CountryHighPoint> highPoint : discoveredCountryHighPoint.entrySet()) {
            retList.add(highPoint.getValue().getCountryHighPoint());
        }
        return retList;
    }

    @Override
    public HashSet<Integer> getDiscoveredPeakHeights() {
        return discoveredPeakHeights;
    }

    @Override
    public HashSet<POIPoint> getDiscoveredPeaks() {
        return discoveredPeaks;
    }

    @Override
    public List<FriendItem> getFriends() {
        return friends;
    }

    @Override
    public boolean isRegistered() {
        if(!username.equals(USERNAME_BEFORE_REGISTRATION)) return true;
        Task<DataSnapshot> checkTask = dbRefUser.get();
        try {
            Tasks.await(checkTask);

            DataSnapshot data = checkTask.getResult();
            assert data != null;

            if(!data.exists()) return false;

            // If the account exists but is not loaded, load it
            retrieveData();

            return true;

        } catch (Exception e) {
            return false;
        }
    }

    /* SETTERS */

    @Override
    public UsernameChoiceOutcome changeUsername(String newUsername) {
        // If the username is not valid
        if(!Account.checkUsernameValidity(newUsername)) return UsernameChoiceOutcome.INVALID;

        // If the username is the current one
        if(newUsername.equals(username)) return UsernameChoiceOutcome.CURRENT;

        // Finally, check if it is available
        Task<DataSnapshot> checkTask = Database.refRoot.child(Database.CHILD_USERS).orderByChild(Database.CHILD_USERNAME).equalTo(newUsername).get();

        try {
            // Wait for task to finish
            Tasks.await(checkTask);

            // Get the obtained data
            DataSnapshot data = checkTask.getResult();
            assert data != null;

            // Check if it was already used
            if(data.exists()) return UsernameChoiceOutcome.USED;

            // If not, register the new username
            Task<Void> changeTask = dbRefUser.child(Database.CHILD_USERNAME).setValue(newUsername);

            try {
                // Wait for task to finish
                Tasks.await(changeTask);

                String oldUsername = username;
                username = newUsername;

                if(oldUsername.equals(USERNAME_BEFORE_REGISTRATION)) return UsernameChoiceOutcome.REGISTERED;
                return UsernameChoiceOutcome.CHANGED;

            } catch (Exception e) {
                Log.d("Account", "changeUsername - changeTask: failed");
                return UsernameChoiceOutcome.FAIL;
            }

        } catch (Exception e) {
            Log.d("Account", "changeUsername - checkTask: failed");
            return UsernameChoiceOutcome.FAIL;
        }

    }

    @Override
    public void setScore(long newScore) {
        // Set local value
        score = newScore;

        // Set remote value
        Database.setChild(Database.CHILD_USERS +  currentID,
                Collections.singletonList(Database.CHILD_SCORE),
                Collections.singletonList(score));
    }

    @Override
    public AddFriendOutcome addFriend(String friendUsername) {
        Log.d("ACCOUNT", "addFriend: current username: " + username);
        Log.d("ACCOUNT", "addFriend: friend username: " + friendUsername);

        // If the friend username is not valid
        if(!Account.checkUsernameValidity(friendUsername)) return AddFriendOutcome.INVALID;

        // If the friend username is the current one
        if(friendUsername.equals(username)) return AddFriendOutcome.CURRENT;

        // Finally, check if the user exists
        Task<DataSnapshot> checkExistenceTask = Database.refRoot.child(Database.CHILD_USERS).orderByChild(Database.CHILD_USERNAME).equalTo(friendUsername).get();

        try {
            Log.d("ACCOUNT", "addFriend - checkExistenceTask: ongoing");

            // Wait for the search to end
            Tasks.await(checkExistenceTask);

            // Get the obtained data
            DataSnapshot data = checkExistenceTask.getResult();
            assert data != null;

            // Check if the friend exists
            if(!data.exists()) return AddFriendOutcome.NOT_PRESENT;

            Log.d("ACCOUNT", "addFriend: friend is on DB");

            // Get the friend ID
            String tempID = null;
            for(DataSnapshot searchedUser : data.getChildren()) {
                tempID = searchedUser.getKey();
            }
            assert tempID != null;
            Log.d("ACCOUNT", "addFriend: FRIEND ID: " + tempID);

            final String friendID = tempID;

            // Check if the friend is already a user's friend
            if(friends.stream().anyMatch(x -> x.hasID(friendID))) return AddFriendOutcome.ALREADY_ADDED;

            Log.d("ACCOUNT", "addFriend: adding friend");
            Log.d("ACCOUNT", "addFriend: adding friend - current username: " + username);

            // Otherwise, add the friend remotely
            Task<Void> addTask = dbRefUser.child(Database.CHILD_FRIENDS).child(friendID).setValue("");

            // Wait for task to finish
            Tasks.await(addTask);

            // Create a friend item
            FirebaseFriendItem newFriendItem = new FirebaseFriendItem(friendID);

            // Add the friend locally
            friends.add(newFriendItem);

            return AddFriendOutcome.ADDED;
        } catch (Exception e) {
            Log.d("ACCOUNT", "addFriend - checkExistenceTask: failed");
            return AddFriendOutcome.FAIL;
        }
    }

    @Override
    public void removeFriend(String friendID) {
        // Remove listener
        friends.stream().filter(x -> x.hasID(friendID)).map(x -> (FirebaseFriendItem)x).forEach(FirebaseFriendItem::removeListener);

        // Remove remotely, in an asynchronous way as there is no need to retrieve the information for now
        new Thread(() -> {
            Task<Void> removeTask = dbRefUser.child(Database.CHILD_FRIENDS).child(friendID).removeValue();
        }).start();


        // Remove locally
        friends.removeIf(x -> x.hasID(friendID));
    }

    @Override
    public void setDiscoveredCountryHighPoint(CountryHighPoint entry){
        if(!discoveredCountryHighPoint.containsKey(entry.getCountryName())) {
            // Add locally
            discoveredCountryHighPoint.put(entry.getCountryName(), entry);

            // Add remotely
            Database.setChildObject(Database.CHILD_USERS + currentID + "/" +
                    Database.CHILD_COUNTRY_HIGH_POINT, entry);
        }
    }

    @Override
    public void setDiscoveredPeakHeights(int badge) {
        if(!discoveredPeakHeights.contains(badge)){

            // Add locally
            discoveredPeakHeights.add(badge);

            // Add remotely
            Database.setChildObject(Database.CHILD_USERS +  currentID + "/" +
                    Database.CHILD_DISCOVERED_PEAKS_HEIGHTS, Collections.singletonList(badge));
        }
    }

    @Override
    public void setDiscoveredPeaks(ArrayList<POIPoint> newDiscoveredPeaks) {
        // Add remotely
        Database.setChildObjectList(Database.CHILD_USERS + currentID + "/" +
                        Database.CHILD_DISCOVERED_PEAKS,
                new ArrayList<Object>(newDiscoveredPeaks));
    }


    @Override
    public ArrayList<POIPoint> filterNewDiscoveredPeaks(ArrayList<POIPoint> unfilteredDiscoveredPeaks){
        ArrayList<POIPoint> resultList = unfilteredDiscoveredPeaks.stream().filter(newPeak -> !discoveredPeaks.contains(newPeak)).collect(Collectors.toCollection(ArrayList::new));
        //Update the list of discovered peaks (local HashSet)
        discoveredPeaks.addAll(resultList);
        return resultList;
    }


}
