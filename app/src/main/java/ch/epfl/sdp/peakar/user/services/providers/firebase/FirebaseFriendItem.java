package ch.epfl.sdp.peakar.user.services.providers.firebase;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import ch.epfl.sdp.peakar.database.Database;
import ch.epfl.sdp.peakar.general.remote.RemoteOutcome;
import ch.epfl.sdp.peakar.general.remote.RemoteResource;
import ch.epfl.sdp.peakar.user.friends.FriendItem;

public class FirebaseFriendItem extends FriendItem implements RemoteResource {
    private DatabaseReference dbRef;
    private ValueEventListener itemListener;

    /**
     * Constructor
     *
     * @param uid of user
     */
    public FirebaseFriendItem(String uid) {
        super(uid);
        retrieveData();
    }

    /**
     * Remove the listener of a friend item
     */
    public void removeListener() {
        dbRef.removeEventListener(itemListener);
    }

    @Override
    public RemoteOutcome retrieveData() {
        dbRef = Database.refRoot.child(Database.CHILD_USERS).child(super.getUid());

        // Create the listener
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

        // Add the listener that will load data whenever something changes
        dbRef.addValueEventListener(itemListener);
        return null;
    }
}
