package com.github.giommok.softwaredevproject;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.github.bgabriel998.softwaredevproject.FriendItem;
import com.github.ravifrancesco.softwaredevproject.POIPoint;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;


/* Singleton class containing the only possible account connected */
public class FirebaseAccount implements Account {

    private static String username = "null";
    private static long score = 0;
    private static FirebaseAccount account = null;
    /*local list of friends */
    private static List<FriendItem> friends = new ArrayList<>();
    private static DatabaseReference dbRefUser = Database.refRoot.child("users/null");
    /*local list of discovered country highest point : key -> country name */
    private static HashMap<String, CountryHighPoint> discoveredCountryHighPoint = new HashMap<String, CountryHighPoint>();
    /*local list of discovered height ranges 1000,2000,3000 m etc. */
    private static HashSet<Integer> discoveredPeakHeights = new HashSet<>();
    /*local list of discovered Peaks*/
    private static HashSet<POIPoint> discoveredPeaks = new HashSet<>();

    /*Listener -> callback called when the data in the database has changed for the given user*/
    private static final ValueEventListener userListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            if(snapshot.hasChild(Database.CHILD_DISCOVERED_PEAKS))
                syncGetDiscoveredPeaksFromProfile(snapshot.child(Database.CHILD_DISCOVERED_PEAKS));
            else discoveredPeaks = new HashSet<>();
            if(snapshot.hasChild(Database.CHILD_USERNAME))
                syncGetUsernameFromProfile(snapshot.child(Database.CHILD_USERNAME).getValue(String.class));
            else username = "null";
            syncFriendsFromProfile(snapshot.child(Database.CHILD_FRIENDS));
            if(snapshot.hasChild(Database.CHILD_SCORE))
                syncGetUserScoreFromProfile(snapshot.child(Database.CHILD_SCORE).getValue(long.class));
            else score = 0;
            if(snapshot.hasChild(Database.CHILD_COUNTRY_HIGH_POINT))
                syncGetCountryHighPointsFromProfile(snapshot.child(Database.CHILD_COUNTRY_HIGH_POINT));
            else discoveredCountryHighPoint = new HashMap<String, CountryHighPoint>();
            if(snapshot.hasChild(Database.CHILD_DISCOVERED_PEAKS_HEIGHTS))
                syncGetDiscoveredHeight(snapshot.child(Database.CHILD_DISCOVERED_PEAKS_HEIGHTS));
            else discoveredPeakHeights = new HashSet<>();
            Log.d("CALLBACK FIREBASE","END of the execution");
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
        Database.setChild(Database.CHILD_USERS +  getId() + "/",
                Collections.singletonList(Database.CHILD_SCORE),
                Collections.singletonList(score));
    }

    @Override
    public long getUserScore() {
        return score;
    }

    @Override
    public void setDiscoveredCountryHighPoint(CountryHighPoint entry){
        if(!discoveredCountryHighPoint.containsKey(entry.getCountryName())) {
            //Put entry in the account local copy
            discoveredCountryHighPoint.put(entry.getCountryName(), entry);
            //Put entry to the database
            Database.setChildObject(Database.CHILD_USERS + getId() + "/"+
                    Database.CHILD_COUNTRY_HIGH_POINT,entry);
        }
    }

    @Override
    public HashMap<String, CountryHighPoint> getDiscoveredCountryHighPoint(){
        return discoveredCountryHighPoint;
    }

    /**
     * Return only the names of the discovered country high points
     * as a list of strings
     * @return list of peak names
     */
    public List<String> getDiscoveredCountryHighPointNames(){
        List<String> retList = new ArrayList<>();
        for (Map.Entry<String, CountryHighPoint> highPoint : discoveredCountryHighPoint.entrySet()) {
            retList.add(highPoint.getValue().getCountryHighPoint());
        }
        return retList;
    }


    @Override
    public void setDiscoveredPeakHeights(int badge){
        if(!discoveredPeakHeights.contains(badge)){
            discoveredPeakHeights.add(badge);
            Database.setChildObject(Database.CHILD_USERS +  getId() + "/"+
                    Database.CHILD_DISCOVERED_PEAKS_HEIGHTS,Collections.singletonList(badge));
        }
    }

    @Override
    public HashSet<Integer> getDiscoveredPeakHeights(){
        return discoveredPeakHeights;
    }


    @Override
    public void setDiscoveredPeaks(ArrayList<POIPoint> newDiscoveredPeaks){
        Database.setChildObjectList(Database.CHILD_USERS + getId() + "/" +
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
        dbRefUser = Database.refRoot.child(Database.CHILD_USERS+  getId());
        dbRefUser.addValueEventListener(userListener);
        Log.d("FirebaseAccount","User profile callback created successfully");
    }


    /**
     * Get the list of discovered peak from the datasnapshot from database.
     * The list is automatically appended to the discoveredPeaks HashSet
     * @param discoveredPeaksFromHashMap snapshot from the database user child
     */
    private static void syncGetDiscoveredPeaksFromProfile(DataSnapshot discoveredPeaksFromHashMap){
        HashMap<String, HashMap<String, String>> entries = (HashMap<String, HashMap<String, String>>) discoveredPeaksFromHashMap.getValue();

        for (Map.Entry<String, HashMap<String, String>> entry : entries.entrySet()) {
            Object value = entry.getValue();
            POIPoint peak = new POIPoint(((HashMap<String, String>) value).get(Database.CHILD_ATTRIBUTE_PEAK_NAME),
                    ((HashMap<String, Double>) value).get(Database.CHILD_ATTRIBUTE_PEAK_LATITUDE),
                    ((HashMap<String, Double>) value).get(Database.CHILD_ATTRIBUTE_PEAK_LONGITUDE),
                    ((HashMap<String, Long>) value).get(Database.CHILD_ATTRIBUTE_PEAK_ALTITUDE));

            //Check if the peak is already contain to avoid duplicate creation
            discoveredPeaks.add(peak);
        }
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
     * Get the list of discovered country highPoints from the datasnapshot from database.
     * The list is automatically appended to the discoveredCountryHighPoint HashSet
     * @param countryHighPointFromHashMap snapshot from the database userchild
     */
    private static void syncGetCountryHighPointsFromProfile(DataSnapshot countryHighPointFromHashMap){

        HashMap<String, HashMap<String, String>> entries = (HashMap<String, HashMap<String, String>>) countryHighPointFromHashMap.getValue();

        //Iterate over the data snapshot to re-create objects
        for (Map.Entry<String, HashMap<String, String>> entry : entries.entrySet()) {
            Object value = entry.getValue();
            CountryHighPoint countryHighPoint = new CountryHighPoint(((HashMap<String, String>) value).get(Database.CHILD_ATTRIBUTE_COUNTRY_NAME),
                    ((HashMap<String, String>) value).get(Database.CHILD_COUNTRY_HIGH_POINT_NAME),
                    ((HashMap<String, Long>) value).get(Database.CHILD_ATTRIBUTE_HIGH_POINT_HEIGHT));
            String countryName = ((HashMap<String, String>) value).get(Database.CHILD_ATTRIBUTE_COUNTRY_NAME);
            //Check if the country high point is already in the list to avoid duplicate
            if(!discoveredCountryHighPoint.containsKey(countryName))
                discoveredCountryHighPoint.put(((HashMap<String, String>) value).get(Database.CHILD_ATTRIBUTE_COUNTRY_NAME), countryHighPoint);
        }
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
            discoveredPeakHeights.add((int)retrievedVal);
        }
    }

    /**
     * Synchronize the list of friends of the user.
     * The list is automatically recreated from an empty one each time a change happens in the DB child.
     * If the parent is empty, an empty list will be created.
     * @param parent snapshot from the database userchild
     */
    private static void syncFriendsFromProfile(DataSnapshot parent) {
        long parentSize = parent.getChildrenCount();
        // If parent is empty
        if(parentSize == 0) {
            friends = new ArrayList<>();
        }
        // If parent is not empty
        List<FriendItem> tempFriends = new CopyOnWriteArrayList<>();
        for (DataSnapshot child : parent.getChildren()) {
            String uidFriend = child.getKey();
            DatabaseReference childRef = Database.refRoot.child(Database.CHILD_USERS + uidFriend);
            childRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String usernameFriend = snapshot.child("username").getValue(String.class);
                    Integer scoreFriend = snapshot.child("score").getValue(Integer.class);
                    tempFriends.add(new FriendItem(uidFriend, usernameFriend, scoreFriend == null ? 0 : scoreFriend));
                    if(parentSize == tempFriends.size()) {
                        // Copy all the elements in an ArrayList
                        List<FriendItem> newFriends = new ArrayList<>();
                        newFriends.addAll(tempFriends);
                        // Once the update is done, change the friends object reference
                        friends = newFriends;

                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }
    }
}