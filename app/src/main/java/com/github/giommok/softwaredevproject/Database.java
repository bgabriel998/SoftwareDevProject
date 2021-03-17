package com.github.giommok.softwaredevproject;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

class DatabaseException extends RuntimeException {
    DatabaseException() {
        super("Exception thrown by the Database class");
    }
}

public class Database {
    // This is a reference to the database root
    private static final DatabaseReference refRoot = FirebaseDatabase.getInstance("https://peakar-default-rtdb.europe-west1.firebasedatabase.app/").getReference();

    /**
     * This method
     * @param path      A list of strings containing the strings of the child keys from the root (excluded) to the one to be modified (included)
     * @param childKeys    A list of strings containing the child keys where to put the new values
     * @param values    A list of strings containing the values to be added
     */
    public static void setChild(List<String> path, List<String> childKeys, List<String> values) {
        DatabaseReference refAdd = refRoot;
        // If the names of the values to be updated and the values themselves have different sizes, the operation cannot be done
        if(childKeys.size() != values.size()) throw new DatabaseException();
        // Moving to the correct child position
        for(String child : path) {
            refAdd = refAdd.child(child);
        }
        // Updating the child keys with the given values
        for(int i = 0; i < childKeys.size(); i++) {
            refAdd.child(childKeys.get(i)).setValue(values.get(i));
        }
    }


}
