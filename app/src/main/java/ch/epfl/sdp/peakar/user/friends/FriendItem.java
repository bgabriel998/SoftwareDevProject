package ch.epfl.sdp.peakar.user.friends;

import androidx.annotation.NonNull;

import ch.epfl.sdp.peakar.database.Database;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

/**
 * Item holding user attributes
 * used to fill friends list.
 */
public class FriendItem {
    private final String uid;
    private String username;
    private int points;
    private final DatabaseReference dbRef;
    private ValueEventListener itemListener;

    /**
     * Constructor
     * @param uid of user
     */
    public FriendItem(String uid) {
        this.uid = uid;
        dbRef = Database.refRoot.child(Database.CHILD_USERS).child(uid);

        // Define the listener
        itemListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Integer newPoints = snapshot.child(Database.CHILD_SCORE).getValue(Integer.class);
                String newUsername = snapshot.child(Database.CHILD_USERNAME).getValue(String.class);
                if(newPoints != null) setPoints(newPoints);
                if(newUsername != null) setUsername(newUsername);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };

        // Add the listener
        dbRef.addValueEventListener(itemListener);
    }

    /**
     * Getter of the friend item username
     * @return username of the user
     */
    public String getUsername() {
        return username;
    }

    /**
     * Setter of the friend item username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Getter of the friend item user points
     * @return points of the user
     */
    public int getPoints() {
        return points;
    }

    /**
     * Setter of the friend item points
     */
    public void setPoints(int points) {
        this.points = points;
    }

    /**
     * Getter of the friend item UId
     */
    public String getUid() {
        return uid;
    }

    /**
     * Set the listener of a friend item that will update the fields on DB changes
     */
    public void setListener(ValueEventListener listener) {
        itemListener = listener;
        dbRef.addValueEventListener(itemListener);
    }

    /**
     * Remove the listener of a friend item
     */
    public void removeListener() {
        dbRef.removeEventListener(itemListener);
    }

    /**
     * Check if a friend item has a specific ID
     * @return true if the user has the target ID, false otherwise
     */
    public boolean hasID(String ID) {
        return this.uid.equals(ID);
    }
}