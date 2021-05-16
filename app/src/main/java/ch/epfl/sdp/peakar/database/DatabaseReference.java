package ch.epfl.sdp.peakar.database;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * This interface represents a Database Reference, i.e. a reference to a database child.
 */
public interface DatabaseReference {
    /**
     * Get the child with the given path from the current reference.
     */
    public DatabaseReference child(String pathString);

    /**
     * Get the database snapshot of the current reference.
     * Note that this method is a blocking method, meaning that it cannot be used on the UI thread.
     * You may want to run this method on a new thread and, after that, do something else on the UI thread.
     */
    public DatabaseSnapshot get();

    /**
     * Get the key of the current child.
     */
    public String getKey();

    /**
     * Set asynchronously a value to the current reference.
     */
    public Task<Void> setValueAsync(Object value);

    /**
     * Set a value to the current reference.
     * Note that this method is a blocking method, meaning that it cannot be used on the UI thread.
     * You may want to run this method on a new thread and, after that, do something else on the UI thread.
     */
    public void setValue(Object value);

    /**
     * Create a new child in the current path and return its reference.
     */
    public DatabaseReference push();

    /**
     * Removes asynchronously the current child from the database.
     */
    public Task<Void> removeValueAsync();

    /**
     * Removes the current child from the database.
     * Note that this method is a blocking method, meaning that it cannot be used on the UI thread.
     * You may want to run this method on a new thread and, after that, do something else on the UI thread.
     */
    public void removeValue();

    /**
     * Create a query in which children are ordered by the values of the specified path.
     */
    public DatabaseQuery orderByChild(String path);
    /**
     * Add a listener for changes in the data at this location. Each time time the data changes, your
     * listener will be called with an immutable snapshot of the data.
     */
    public ValueEventListener addValueEventListener(ValueEventListener valueEventListener);

    /**
     * Add a listener for child events occurring at this location. When child locations are added,
     * removed, changed, or moved, the listener will be triggered for the appropriate event.
     */
    public ChildEventListener addChildEventListener(ChildEventListener childEventListener);

    /**
     * Remove the given listener from the current reference.
     */
    public void removeEventListener(ValueEventListener valueEventListener);
}
