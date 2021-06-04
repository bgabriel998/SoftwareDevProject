package ch.epfl.sdp.peakar.database.providers.firebase;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import ch.epfl.sdp.peakar.database.DatabaseQuery;
import ch.epfl.sdp.peakar.database.DatabaseReference;
import ch.epfl.sdp.peakar.database.DatabaseSnapshot;

/**
 * This is a concrete implementation of DatabaseReference.
 * Its implementation makes use of Firebase API, so needs to be modified if database provider is changed.
 */
public class FirebaseDatabaseReference implements DatabaseReference {
    // FIREBASE CONSTANTS
    public static final String DATABASE_ADDRESS = "https://peakar-default-rtdb.europe-west1.firebasedatabase.app/";

    // FIREBASE REFERENCE
    private final com.google.firebase.database.DatabaseReference firebaseReference;

    /**
     * Constructor of a reference pointing to the root of the database.

     */
    public FirebaseDatabaseReference(){
        firebaseReference = FirebaseDatabase.getInstance(DATABASE_ADDRESS).getReference();
    }

    /**
     * Constructor of a reference given the Firebase Reference.
     */
    private FirebaseDatabaseReference(com.google.firebase.database.DatabaseReference firebaseReference){
        this.firebaseReference = firebaseReference;
    }

    @Override
    public DatabaseReference child(String pathString) {
        return new FirebaseDatabaseReference(firebaseReference.child(pathString));
    }

    @Override
    public DatabaseSnapshot get() {
        Task<DataSnapshot> getTask = firebaseReference.get();
        try {
            Log.d("FirebaseDatabaseReference", "get reference: " + firebaseReference.toString());
            Tasks.await(getTask);
            Log.d("FirebaseDatabaseReference", "get: completed");
            return new FirebaseDatabaseSnapshot(getTask.getResult());
        } catch(Exception e) {
            e.printStackTrace();
            e.getCause();
            throw new RuntimeException("DBReference: error getting data snapshot");
        }
    }

    @Override
    public String getKey() {
        return firebaseReference.getKey();
    }

    @Override
    public Task<Void> setValueAsync(Object value) {
        return firebaseReference.setValue(value);
    }

    @Override
    public void setValue(Object value) {
        Task<Void> setTask = firebaseReference.setValue(value);
        try {
            Tasks.await(setTask);
        } catch(Exception e) {
            e.printStackTrace();
            e.getCause();
        }
    }

    @Override
    public DatabaseReference push() {
        return new FirebaseDatabaseReference(firebaseReference.push());
    }

    @Override
    public Task<Void> removeValueAsync() {
        return firebaseReference.removeValue();
    }

    @Override
    public void removeValue() {
        Task<Void> removeTask = firebaseReference.removeValue();
        try {
            Tasks.await(removeTask);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public DatabaseQuery orderByChild(String path) {
        return new FirebaseDatabaseQuery(firebaseReference.orderByChild(path));
    }

    @Override
    public ValueEventListener addValueEventListener(ValueEventListener valueEventListener) {
        return firebaseReference.addValueEventListener(valueEventListener);
    }

    @Override
    public ChildEventListener addChildEventListener(ChildEventListener childEventListener) {
        return firebaseReference.addChildEventListener(childEventListener);
    }

    @Override
    public void removeEventListener(ValueEventListener valueEventListener) {
        firebaseReference.removeEventListener(valueEventListener);
    }
}
