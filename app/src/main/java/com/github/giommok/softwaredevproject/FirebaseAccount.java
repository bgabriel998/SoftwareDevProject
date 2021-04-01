package com.github.giommok.softwaredevproject;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.Future;


/* Singleton class containing the only possible account connected */
public class FirebaseAccount implements Account {

    private static String username = "null";
    private static long score = 0;
    private static FirebaseAccount account = null;
    private static HashMap<String,CacheEntry> discoveredCountryHighPoint = new HashMap<String,CacheEntry>();


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
    public void synchronizeUsername() {
        DatabaseReference dbRef = Database.refRoot.child("users/" + getId() + "/username");
        ValueEventListener usernameListener = new ValueEventListener() {
            @Override
            public synchronized void onDataChange(DataSnapshot dataSnapshot) {
                username = dataSnapshot.getValue(String.class);
                if(username == null) username = "null";
                Log.d("FireBase Data", "Username "+ username);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        dbRef.addValueEventListener(usernameListener);
    }

    @Override
    public void setUserScore(long newScore) {
        //set local value
        score = newScore;
        DatabaseReference refAdd = Database.refRoot.child("users/");
        refAdd.child(account.getId()).child("score").setValue(score);
    }


    @Override
    public long getUserScore() {
        return score;
    }

    @Override
    public void synchronizeUserScore() {
        DatabaseReference dbRef = Database.refRoot.child("users/" + getId() + "/score");
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try{
                    score = snapshot.getValue(long.class);
                }
                catch (Exception e){
                    Log.e("FireBase Data","Can't synchronize user score");
                }
                Log.v("FireBase Data","User score : " + score +"pts");
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void setDiscoveredCountryHighPoint(CacheEntry entry){
        if(!discoveredCountryHighPoint.containsKey(entry.getCountryName())) {
            //Put entry in the account local copy
            discoveredCountryHighPoint.put(entry.getCountryName(), entry);

            //Put entry in the database
            DatabaseReference refAdd = Database.refRoot.child("users/");
            refAdd.child(getId()).child("CountryHighPoint")
                    .push().setValue(entry);
        }
    }

    @Override
    public HashMap<String,CacheEntry> getDiscoveredCountryHighPoint(){
        return discoveredCountryHighPoint;
    }

    @Override
    public void synchronizeDiscoveredCountryHighPoints() {
        DatabaseReference dbRef = Database.refRoot.child("users/" + getId() + "/CountryHighPoint");
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //Get all entries from DataSnapshot
                try {
                    HashMap<String, HashMap<String, String>> entries = (HashMap<String, HashMap<String, String>>) snapshot.getValue();
                    //Re-init discoveredCountryHighPoint HashSet
                    discoveredCountryHighPoint = new HashMap<String, CacheEntry>();
                    //Iterate over all entries to convert to discoveredCountryHighPoint HashSet entries
                    for (Map.Entry<String, HashMap<String, String>> entry : entries.entrySet()) {
                        Object value = entry.getValue();
                        CacheEntry countryHighPoint = new CacheEntry(((HashMap<String, String>) value).get("countryName"),
                                ((HashMap<String, String>) value).get("countryHighPoint"),
                                ((HashMap<String, Long>) value).get("highPointHeight"));
                        discoveredCountryHighPoint.put(((HashMap<String, String>) value).get("countryName"), countryHighPoint);
                        Log.d("FireBase Data", countryHighPoint.toString());
                    }
                }
                catch(Exception e){
                    Log.e("FireBase Data","Can't synchronize user discovered country high points");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
