package ch.epfl.sdp.peakar.user.services.providers.firebase;

import android.util.Log;
import android.widget.ListAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.Comparator;
import java.util.List;

import ch.epfl.sdp.peakar.database.Database;
import ch.epfl.sdp.peakar.rankings.RankingItem;
import ch.epfl.sdp.peakar.rankings.RankingListAdapter;

/**
 * This class is an helper class designed to provide <code>RankingsActivity</code> with a synchronized list.
 */
public class FirebaseRankingsList {

    /**
     * Synchronize the list given in input so it changes correctly on every DB change and notifies the list adapter.
     * @param rankingItems list to synchronize.
     * @param listAdapter adapter to notify on changes.
     */
    public static void synchronizeRankings(List<RankingItem> rankingItems, RankingListAdapter listAdapter) {
        rankingItems.clear();
        DatabaseReference dbRef = Database.refRoot.child(Database.CHILD_USERS);
        dbRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                String uid = snapshot.getKey();
                String childUsername = snapshot.child("username").getValue(String.class);
                Long childScore = snapshot.child("score").getValue(Long.class);
                rankingItems.add(new RankingItem(uid, childUsername, childScore == null ? 0L : childScore));
                sortAndNotify(rankingItems, listAdapter);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                String uid = snapshot.getKey();
                String childUsername = snapshot.child("username").getValue(String.class);
                Integer childScore = snapshot.child("score").getValue(Integer.class);
                rankingItems.removeIf(x -> x.getUid().equals(uid));
                rankingItems.add(new RankingItem(uid, childUsername, childScore == null ? 0L : childScore));
                sortAndNotify(rankingItems, listAdapter);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                String uid = snapshot.getKey();
                rankingItems.removeIf(x -> x.getUid().equals(uid));
                sortAndNotify(rankingItems, listAdapter);
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                String uid = snapshot.getKey() != null ? snapshot.getKey() : "null";
                String childUsername = snapshot.child("username").getValue(String.class);
                Integer childScore = snapshot.child("score").getValue(Integer.class);
                rankingItems.removeIf(x -> x.getUid().equals(uid));
                rankingItems.add(new RankingItem(uid, childUsername, childScore == null ? 0L : childScore));
                sortAndNotify(rankingItems, listAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("RankingsActivity", "onCancelled: " + databaseError.getDetails());
            }
        });
    }

    /**
     * Helper method to sort the list and notify the adapter.
     */
    private static void sortAndNotify(List<RankingItem> rankingItems, RankingListAdapter listAdapter) {
        rankingItems.sort(Comparator.comparing(RankingItem::getPoints).reversed());
        listAdapter.notifyDataSetChanged();
    }
}
