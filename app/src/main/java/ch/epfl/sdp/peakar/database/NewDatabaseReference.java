package ch.epfl.sdp.peakar.database;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * This class represents a Database Reference.
 * Its implementation makes use of Firebase API, so needs to be modified if database provider is changed.
 */
public class NewDatabaseReference {
    // FIREBASE CONSTANTS
    public static final String DATABASE_ADDRESS = "https://peakar-default-rtdb.europe-west1.firebasedatabase.app/";

    // FIREBASE REFERENCE
    public DatabaseReference firebaseReference;

    /**
     * Constructor of a reference pointing to the root of the database.
     */
    protected NewDatabaseReference(){
        firebaseReference = FirebaseDatabase.getInstance(DATABASE_ADDRESS).getReference();
    }

    /**
     * Constructor of a reference given the Firebase Reference.
     */
    private NewDatabaseReference(DatabaseReference firebaseReference){
        this.firebaseReference = firebaseReference;
    }

    /**
     * Get the child with the given path from the current reference.
     */
    public NewDatabaseReference child(String pathString) {
        return new NewDatabaseReference(firebaseReference.child(pathString));
    }

    /**
     * Get the database snapshot of the current reference.
     * Note that this method is a blocking method, meaning that it cannot be used on the UI thread.
     * You may want to run this method on a new thread and, after that, do something else on the UI thread.
     */
    public NewDatabaseSnapshot get() {
        Task<DataSnapshot> getTask = firebaseReference.get();
        try {
            Tasks.await(getTask);
            return new NewDatabaseSnapshot(getTask.getResult());
        } catch(Exception e) {
            e.printStackTrace();
            throw new RuntimeException("DBReference: error getting data snapshot");
        }
    }

    /**
     * Set asynchronously a value to the current reference.
     */
    public Task<Void> setValueAsync(Object value) {
        return firebaseReference.setValue(value);
    }

    /**
     * Set a value to the current reference.
     * Note that this method is a blocking method, meaning that it cannot be used on the UI thread.
     * You may want to run this method on a new thread and, after that, do something else on the UI thread.
     */
    public void setValue(Object value) {
        Task<Void> setTask = firebaseReference.setValue(value);
        try {
            Tasks.await(setTask);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Create a new child in the current path and return its reference.
     */
    public NewDatabaseReference push() {
        return new NewDatabaseReference(firebaseReference.push());
    }
}
