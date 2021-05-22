package ch.epfl.sdp.peakar.social;

import com.google.firebase.database.ValueEventListener;

import ch.epfl.sdp.peakar.database.Database;
import ch.epfl.sdp.peakar.database.DatabaseReference;
import ch.epfl.sdp.peakar.social.SocialItem;

public class RemoteFriendItem extends SocialItem {
    private final DatabaseReference dbRef;
    private ValueEventListener itemListener;

    /**
     * Constructor
     *
     * @param uid of user
     */
    public RemoteFriendItem(String uid) {
        super(uid);
        dbRef = Database.getInstance().getReference().child(Database.CHILD_USERS).child(uid);
    }

    /**
     * Add the listener of a friend item
     */
    public void addListener(ValueEventListener itemListener) {
        this.itemListener = itemListener;
        dbRef.addValueEventListener(itemListener);
    }

    /**
     * Remove the listener of a friend item
     */
    public void removeListener() {
        if(itemListener != null) dbRef.removeEventListener(itemListener);
    }
}
