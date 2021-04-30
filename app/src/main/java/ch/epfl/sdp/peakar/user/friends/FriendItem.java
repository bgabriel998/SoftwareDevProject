package ch.epfl.sdp.peakar.user.friends;

import ch.epfl.sdp.peakar.database.Database;
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
     * @param username of user.
     * @param points user has.
     */
    public FriendItem(String uid, String username, int points) {
        this.uid = uid;
        this.username = username;
        this.points = points;
        this.dbRef = Database.refRoot.child(Database.CHILD_USERS + uid);
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
     * Check if a friend item has a specific username
     * @return true if the user has the target username, false otherwise
     */
    public boolean hasUsername(String username) {
        return this.username.equals(username);
    }
}