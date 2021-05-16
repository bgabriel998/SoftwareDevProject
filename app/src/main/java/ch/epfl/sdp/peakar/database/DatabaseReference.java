package ch.epfl.sdp.peakar.database;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * This class represents a Database Reference.
 * Its implementation makes use of Firebase API, so needs to be modified if database provider is changed.
 */
public class DatabaseReference {
    // FIREBASE CONSTANTS
    public static final String DATABASE_ADDRESS = "https://peakar-default-rtdb.europe-west1.firebasedatabase.app/";

    // FIREBASE REFERENCE
    public com.google.firebase.database.DatabaseReference firebaseReference;

    /**
     * Constructor of a reference pointing to the root of the database.
     */
    protected DatabaseReference(){
        firebaseReference = FirebaseDatabase.getInstance(DATABASE_ADDRESS).getReference();
    }

    /**
     * Constructor of a reference given the Firebase Reference.
     */
    private DatabaseReference(com.google.firebase.database.DatabaseReference firebaseReference){
        this.firebaseReference = firebaseReference;
    }

    /**
     * Get the child with the given path from the current reference.
     */
    public DatabaseReference child(String pathString) {
        return new DatabaseReference(firebaseReference.child(pathString));
    }

    /**
     * Get the database snapshot of the current reference.
     * Note that this method is a blocking method, meaning that it cannot be used on the UI thread.
     * You may want to run this method on a new thread and, after that, do something else on the UI thread.
     */
    public DatabaseSnapshot get() {
        Task<DataSnapshot> getTask = firebaseReference.get();
        try {
            Tasks.await(getTask);
            return new DatabaseSnapshot(getTask.getResult());
        } catch(Exception e) {
            e.printStackTrace();
            throw new RuntimeException("DBReference: error getting data snapshot");
        }
    }

    /**
     * Get the key of the current child.
     */
    public String getKey() {
        return firebaseReference.getKey();
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
    public DatabaseReference push() {
        return new DatabaseReference(firebaseReference.push());
    }

    /**
     * Removes asynchronously the current child from the database.
     */
    public Task<Void> removeValueAsync() {
        return firebaseReference.removeValue();
    }

    /**
     * Removes the current child from the database.
     * Note that this method is a blocking method, meaning that it cannot be used on the UI thread.
     * You may want to run this method on a new thread and, after that, do something else on the UI thread.
     */
    public void removeValue() {
        Task<Void> removeTask = firebaseReference.removeValue();
        try {
            Tasks.await(removeTask);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Create a query in which children are ordered by the values of the specified path.
     */
    public DatabaseQuery orderByChild(String path) {
        return new DatabaseQuery(firebaseReference.orderByChild(path));
    }

    /**
     * Add a listener for changes in the data at this location. Each time time the data changes, your
     * listener will be called with an immutable snapshot of the data.
     */
    public ValueEventListener addValueEventListener(ValueEventListener valueEventListener) {
        return firebaseReference.addValueEventListener(valueEventListener);
    }

    /**
     * Add a listener for child events occurring at this location. When child locations are added,
     * removed, changed, or moved, the listener will be triggered for the appropriate event.
     */
    public ChildEventListener addChildEventListener(ChildEventListener childEventListener) {
        return firebaseReference.addChildEventListener(childEventListener);
    }

    /**
     * Remove the given listener from the current reference.
     */
    public void removeEventListener(ValueEventListener valueEventListener) {
        firebaseReference.removeEventListener(valueEventListener);
    }
}
