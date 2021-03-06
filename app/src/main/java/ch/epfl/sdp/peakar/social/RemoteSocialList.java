package ch.epfl.sdp.peakar.social;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import ch.epfl.sdp.peakar.database.Database;
import ch.epfl.sdp.peakar.database.DatabaseReference;
import ch.epfl.sdp.peakar.user.services.AuthService;

/**
 * This class is an helper class designed to provide <code>SocialActivity</code>
 * with a synchronized list.
 */
public class RemoteSocialList {

    /**
     * Synchronize the list given in input so it changes correctly on every DB change
     * and notifies the list adapter, displays all users.
     * @param socialItems list to synchronize.
     * @param listAdapter adapter to notify on changes.
     */
    public static void synchronizeGlobal(List<SocialItem> socialItems, SocialListAdapter listAdapter) {
        socialItems.clear();
        DatabaseReference dbRef = Database.getInstance().getReference().child(Database.CHILD_USERS);
        dbRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                String childUsername = getUsername(snapshot);

                if (!childUsername.isEmpty()) {
                    addSorted(socialItems,
                            new SocialItem(snapshot.getKey(), childUsername,
                                    getScore(snapshot), getProfileUrl(snapshot)),
                            listAdapter);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                String uid = snapshot.getKey();

                for (SocialItem item : socialItems) {
                    if (item.getUid().equals(uid)) {
                        modifyItem(item, snapshot);
                        sortList(socialItems, listAdapter);
                        return;
                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                String uid = snapshot.getKey();
                socialItems.removeIf(x -> x.getUid().equals(uid));
                listAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                sortList(socialItems, listAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("SocialActivity", "onCancelled: " + error.getDetails());
            }
        });
    }

    /**
     * Synchronize the list given in input so it changes correctly on every DB change
     * and notifies the list adapter, displays all friends.
     * @param socialItems list to synchronize.
     * @param listAdapter adapter to notify on changes.
     */
    public static void synchronizeFriends(List<SocialItem> socialItems, SocialListAdapter listAdapter) {
        socialItems.clear();
        DatabaseReference dbRef = Database.getInstance().getReference().child(Database.CHILD_USERS).child(AuthService.getInstance().getID()).child(Database.CHILD_FRIENDS);
        dbRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Log.d("RemoteFriendList", "onChildAdded: ");

                // Produce a new remote friend item
                RemoteFriendItem remoteFriendItem = new RemoteFriendItem(snapshot.getKey());

                // Add a listener to the remote friend item to keep the list synchronized
                remoteFriendItem.addListener(produceFriendItemListener(socialItems, listAdapter, remoteFriendItem));

                socialItems.add(remoteFriendItem);
                sortList(socialItems, listAdapter);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                // Cannot happen: child is just a user ID and user IDs NEVER change
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                Log.d("RemoteFriendList", "onChildRemoved: ");

                String uid = snapshot.getKey();
                Optional<SocialItem> friendItem = socialItems.stream().filter(x -> x.getUid().equals(uid)).findFirst();
                if(friendItem.isPresent()) {
                    RemoteFriendItem remoteFriendItem = (RemoteFriendItem)friendItem.get();
                    remoteFriendItem.removeListener();
                    socialItems.remove(remoteFriendItem);
                    listAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                // We are not interested in possible moves of the friend IDs
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("SocialActivity", "onCancelled: " + error.getDetails());
            }
        });
    }

    /**
     * Retrieve the username from a database snap shot.
     * @param snapshot the given snapshot
     * @return the username
     */
    private static String getUsername(@NonNull DataSnapshot snapshot) {
        String username = snapshot.child(Database.CHILD_USERNAME).getValue(String.class);
        return username == null ? "" : username;
    }

    /**
     * Retrieve the score from a database snap shot.
     * @param snapshot the given snapshot
     * @return the score or 0 if no score found
     */
    private static Long getScore(@NonNull DataSnapshot snapshot) {
        Long score = snapshot.child(Database.CHILD_SCORE).getValue(Long.class);
        return score == null ? 0L : score;
    }

    /**
     * Retrieve the url of the profile image from a database snap shot.
     * @param snapshot the given snapshot
     * @return the url of the image or <code>Uri.EMPTY</code> if no url found
     */
    private static Uri getProfileUrl(@NonNull DataSnapshot snapshot) {
        String urlString = snapshot.child(Database.CHILD_PHOTO_URL).getValue(String.class);
        return urlString == null || urlString.equals("") ? Uri.EMPTY : Uri.parse(urlString);
    }

    /**
     * Add a social item to the list while keeping it sorted
     * @param socialItems to add item to
     * @param item to add.
     */
    private static void addSorted(List<SocialItem> socialItems, SocialItem item, SocialListAdapter listAdapter) {
        socialItems.removeIf(x -> x.getUid().equals(item.getUid()));
        socialItems.add(item);
        sortList(socialItems, listAdapter);
    }

    /**
     * Sort a given list based on the social items score
     * @param socialItems to sort.
     */
    private static void sortList(List<SocialItem> socialItems,SocialListAdapter listAdapter) {
        socialItems.sort(Comparator.comparing(SocialItem::getScore).reversed());
        listAdapter.notifyDataSetChanged();
    }

    /**
     * Modify an existing social item with the data from the database snapshot.
     * @param socialItem item to modify.
     * @param dataSnapshot the given snapshot.
     */
    private static void modifyItem(SocialItem socialItem, DataSnapshot dataSnapshot) {
        socialItem.setUsername(getUsername(dataSnapshot));
        socialItem.setScore(getScore(dataSnapshot));
        socialItem.setProfileUrl(getProfileUrl(dataSnapshot));
    }

    /**
     * Produce a friend item listener that will keep the friend item synchronized and will notify the list adapter on changes.
     * @param socialItems list that contains the friends.
     * @param listAdapter adapter of the list.
     * @param socialItem item that will be synchronized by the listener.
     * @return a listener to the DB representation of the social item.
     */
    private static ValueEventListener produceFriendItemListener(List<SocialItem> socialItems, SocialListAdapter listAdapter, SocialItem socialItem) {
        // Create the listener
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("RemoteFriendList", "onDataChange: ");

                // Update the old social item in the list
                modifyItem(socialItem, snapshot);

                // Update and notify the list
                sortList(socialItems, listAdapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("SocialActivity", "onCancelled: " + error.getDetails());
            }
        };
    }
}
