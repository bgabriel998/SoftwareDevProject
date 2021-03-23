package com.github.giommok.softwaredevproject;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

class DatabaseException extends RuntimeException {
    DatabaseException() {
        super("Exception thrown by the Database class");
    }
}

public class Database {
    // This is a reference to the database root
    public static final DatabaseReference refRoot = FirebaseDatabase.getInstance("https://peakar-default-rtdb.europe-west1.firebasedatabase.app/").getReference();

    /**
     * Sets the childKeys fields of the desired database path with the given values
     * @param path      The path to the fields to be modified
     * @param childKeys    A list of strings containing the child keys where to put the new values
     * @param values    A list of strings containing the values to be added
     */
    public static void setChild(String path, List<String> childKeys, List<String> values) {

        // Moving to the correct child position
        DatabaseReference refAdd = refRoot.child(path);

        // If the names of the values to be updated and the values themselves have different sizes, the operation cannot be done
        if(childKeys.size() != values.size()) throw new DatabaseException();

        // Updating the child keys with the given values
        for(int i = 0; i < childKeys.size(); i++) {
            refAdd.child(childKeys.get(i)).setValue(values.get(i));
        }
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
