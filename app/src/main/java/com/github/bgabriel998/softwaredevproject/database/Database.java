package com.github.bgabriel998.softwaredevproject.database;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class Database {

    public static final String CHILD_USERS = "users/";
    public static final String CHILD_FRIENDS = "/friends/";
    public static final String CHILD_EMAIL = "email";
    public static final String CHILD_DISCOVERED_PEAKS = "DiscoveredPeaks";
    public static final String CHILD_DISCOVERED_PEAKS_HEIGHTS = "DiscoveredHeights";
    public static final String CHILD_USERNAME = "username";
    public static final String CHILD_SCORE = "score";
    public static final String CHILD_COUNTRY_HIGH_POINT = "CountryHighPoint";
    public static final String CHILD_COUNTRY_HIGH_POINT_NAME = "countryHighPoint";
    public static final String CHILD_ATTRIBUTE_HIGH_POINT_HEIGHT = "highPointHeight";
    public static final String CHILD_ATTRIBUTE_COUNTRY_NAME = "countryName";
    public static final String CHILD_ATTRIBUTE_PEAK_NAME = "name";
    public static final String CHILD_ATTRIBUTE_PEAK_LATITUDE = "latitude";
    public static final String CHILD_ATTRIBUTE_PEAK_LONGITUDE = "longitude";
    public static final String CHILD_ATTRIBUTE_PEAK_ALTITUDE = "altitude";
    // This is a reference to the database root
    public static final DatabaseReference refRoot = FirebaseDatabase.getInstance("https://peakar-default-rtdb.europe-west1.firebasedatabase.app/").getReference();

    /**
     * Sets the childKeys fields of the desired database path with the given values
     * @param path      The path to the fields to be modified
     * @param childKeys    A list of strings containing the child keys where to put the new values
     * @param values    A list of strings containing the values to be added
     */
    public static void setChild(String path, List<String> childKeys, List<Object> values) {

        // Moving to the correct child position
        DatabaseReference refAdd = refRoot.child(path);

        // If the names of the values to be updated and the values themselves have different sizes, the operation cannot be done
        if(childKeys.size() != values.size()) throw new RuntimeException("Exception thrown by the Database class");

        // Updating the child keys with the given values
        for(int i = 0; i < childKeys.size(); i++) {
            refAdd.child(childKeys.get(i)).setValue(values.get(i));
        }
    }


    /**
     * Sets an array of values to the given child at the given path in the DB
     * @param path The path to the field to be modified
     * @param values list of values to add
     */
    public static void setChildObjectList(String path,  ArrayList<Object> values){
        // Moving to the correct child position
        DatabaseReference refAdd = refRoot.child(path);

        //Iterate over all child and put values
        for(Object val : values)
            refAdd.push().setValue(val);
    }


    /**
     * Sets an array of values to the given child at the given path in the DB
     * @param path The path to the fields to be modified
     * @param value object to add
     */
    public static void setChildObject(String path,  Object value){
        // Moving to the correct child position
        DatabaseReference refAdd = refRoot.child(path);

        //Put value
        refAdd.push().setValue(value);
    }

    /**
     * This methods checks if in the selected path exists a key with a specified value in a specified field.
     * If it exists, ifTrue is run. Otherwise, ifFalse is run.
     * @param path
     * @param field The field where to look for the value
     * @param value The value to look for
     * @param ifTrue What to do if the value is already present
     * @param ifFalse What to do if the value does not exists
     */
    public static void isPresent(String path, String field, String value, Runnable ifTrue, Runnable ifFalse) {
        refRoot.child(path).orderByChild(field).equalTo(value).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    ifTrue.run();
                }
                else {
                    ifFalse.run();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
}