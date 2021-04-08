package com.github.giommok.softwaredevproject;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.github.bgabriel998.softwaredevproject.FriendItem;
import com.github.bgabriel998.softwaredevproject.RankingItem;
import com.github.ravifrancesco.softwaredevproject.POIPoint;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import org.osmdroid.bonuspack.location.POI;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/* Singleton class containing the only possible account connected */
public class FirebaseAccount implements Account {

    private static String username = "null";
    private static long score = 0;
    private static ArrayList<FriendItem> friends = new ArrayList<>();
    private static FirebaseAccount account = null;
    DatabaseReference dbRefUser = Database.refRoot.child(Database.CHILD_USERS+ Database.FOLDER + getId());
    /*local list of discovered height ranges 1000,2000,3000 m etc. */
    private static HashSet<Integer> discoveredPeakHeights = new HashSet<>();
    /*local list of discovered Peaks*/
    private static HashSet<POIPoint> discoveredPeaks = new HashSet<>();

    /*Listener -> callback called when the data in the database has changed for the given user*/
    private static final ValueEventListener userListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            Log.w("SYNCH PROFILE USER", "onDataChange: called");
            if(snapshot.hasChild(Database.CHILD_USERNAME))
                syncGetUsernameFromProfile(snapshot.child(Database.CHILD_USERNAME).getValue(String.class));
            else username = "null";
            if(snapshot.hasChild(Database.CHILD_FRIENDS))
                syncFriendsFromProfile(snapshot.child(Database.CHILD_FRIENDS));
            else friends = new ArrayList<>();
            if(snapshot.hasChild(Database.CHILD_SCORE))
                syncGetUserScoreFromProfile(snapshot.child(Database.CHILD_SCORE).getValue(long.class));
            else score = 0;
            if(snapshot.hasChild(Database.CHILD_DISCOVERED_PEAKS_HEIGHTS))
                syncGetDiscoveredHeight(snapshot.child(Database.CHILD_DISCOVERED_PEAKS_HEIGHTS));
            else discoveredPeakHeights = new HashSet<>();

        }
        @Override
        public void onCancelled(@NonNull DatabaseError error) {
        }
    };


    public static FirebaseAccount getAccount() {
        if (account == null) {
            account = new FirebaseAccount();
        }
        return account;
    }


    @Override
    public boolean isSignedIn() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return false;
        return true;
    }

    @Override
    public String getProviderId() {
        if(isSignedIn()) return FirebaseAuth.getInstance().getCurrentUser().getProviderId();
        return "null";
    }

    @Override
    public String getDisplayName() {
        if(isSignedIn()) return FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        return "null";
    }

    @Override
    public String getEmail() {
        if(isSignedIn()) return FirebaseAuth.getInstance().getCurrentUser().getEmail();
        return "null";
    }

    @Override
    public String getId() {
        if(isSignedIn()) return FirebaseAuth.getInstance().getCurrentUser().getUid();
        return "null";
    }

    @Override
    public Uri getPhotoUrl() {
        if(isSignedIn()) return FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl();
        return Uri.EMPTY;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public List<FriendItem> getFriends() {
        return friends;
    }

    @Override
    public void setUserScore(long newScore) {
        //set local value
        score = newScore;
        Database.setChild(Database.CHILD_USERS + Database.FOLDER + getId(),
                Collections.singletonList(Database.CHILD_SCORE),
                Collections.singletonList(score));
    }

    @Override
    public long getUserScore() {
        return score;
    }


    @Override
    public void setDiscoveredPeakHeights(int badge){
        if(!discoveredPeakHeights.contains(badge)){
            discoveredPeakHeights.add(badge);
            Database.setChildObject(Database.CHILD_USERS + Database.FOLDER + getId(),
                    Database.CHILD_DISCOVERED_PEAKS_HEIGHTS,Collections.singletonList(badge));
        }
    }

    @Override
    public HashSet<Integer> getDiscoveredPeakHeights(){
        return discoveredPeakHeights;
    }


    @Override
    public void setDiscoveredPeaks(ArrayList<POIPoint> newDiscoveredPeaks){
        Database.setChildObjectList(Database.CHILD_USERS + Database.FOLDER +getId(),
                Database.CHILD_DISCOVERED_PEAKS,
                new ArrayList<Object>(newDiscoveredPeaks));

    }

    @Override
    public HashSet<POIPoint> getDiscoveredPeaks(){
        return discoveredPeaks;
    }

    /**
     * Filter the list of discovered peaks. If the peak in the list is already contained in the Hashset (so contained in the DB)
     * the peak gets dropped from the list.
     * Updates the HashSet with the new values
     * @param unfilteredDiscoveredPeaks unfiltered list of peaks (coming directly from AR activity)
     * @return list of POI after filtering
     */
    public ArrayList<POIPoint> filterNewDiscoveredPeaks(ArrayList<POIPoint> unfilteredDiscoveredPeaks){
        ArrayList<POIPoint> resultList = unfilteredDiscoveredPeaks.stream().filter(newPeak -> !discoveredPeaks.contains(newPeak)).collect(Collectors.toCollection(ArrayList::new));
        //Update the list of discovered peaks (local HashSet)
        discoveredPeaks.addAll(resultList);
        return resultList;
    }


    @Override
    public void synchronizeUserProfile(){
        dbRefUser.removeEventListener(userListener);
        dbRefUser = Database.refRoot.child(Database.CHILD_USERS+ Database.FOLDER + getId());
        dbRefUser.addValueEventListener(userListener);
        Log.d("FirebaseAccount","User profile callback created successfully");
    }


    /**
     * Get the username from the database
     * Assign the username attribute with its value
     * @param usernameFromHashMap username stored in the database
     */
    private static void syncGetUsernameFromProfile(String usernameFromHashMap){
        username = usernameFromHashMap;
    }

    /**
     * Get the user score from the database
     * Assign the score attribute with its value
     * @param scoreFromHashMap user score stored in the database
     */
    private static void syncGetUserScoreFromProfile(long scoreFromHashMap){
        score = scoreFromHashMap;
    }


    /**
     * Get the list of discovered peak heights (ranges)
     * The list is automatically appended to the discoveredHeights hashSet
     * @param discoveredHeightsFromHashMap snapshot from the database userchild
     */
    private static void syncGetDiscoveredHeight(DataSnapshot discoveredHeightsFromHashMap){
        HashMap<String, HashMap<String, String>> entries = (HashMap<String, HashMap<String, String>>) discoveredHeightsFromHashMap.getValue();
        for (Map.Entry<String, HashMap<String, String>> entry : entries.entrySet()){
            Object value = entry.getValue();
            long retrievedVal = ((ArrayList<Long>) value).get(0);
            if(!discoveredPeakHeights.contains(retrievedVal)) discoveredPeakHeights.add((int)retrievedVal);
        }
    }

    private static void syncFriendsFromProfile(DataSnapshot parent) {
        ArrayList<FriendItem> tempFriends = new ArrayList<>();
        for (DataSnapshot child : parent.getChildren()) {
            String uidFriend = child.getKey();
            Log.d("FRIENDS", "Updating friends. Current size = " + friends.size());
            DatabaseReference childRef = Database.refRoot.child(Database.CHILD_USERS + Database.FOLDER + uidFriend);
            childRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String usernameFriend = snapshot.child("username").getValue(String.class);
                    Integer scoreFriend = snapshot.child("score").getValue(Integer.class);
                    tempFriends.add(new FriendItem(uidFriend, usernameFriend, scoreFriend == null ? 0 : scoreFriend));
                    Log.d("FRIENDS", "Synch: Updated a friend item. New size = " + tempFriends.size());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }
        friends = tempFriends;
        Log.d("FRIENDS", "Friends updated. New size = " + friends.size());
    }
}