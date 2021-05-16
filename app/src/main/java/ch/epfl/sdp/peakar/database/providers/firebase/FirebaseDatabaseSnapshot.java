package ch.epfl.sdp.peakar.database.providers.firebase;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;

import java.util.Iterator;

import ch.epfl.sdp.peakar.database.DatabaseSnapshot;

/**
 * This is a concrete implementation of DatabaseSnapshot.
 * Its implementation makes use of Firebase API, so needs to be modified if database provider is changed.
 */
public class FirebaseDatabaseSnapshot implements DatabaseSnapshot {
    private final DataSnapshot firebaseDatabaseSnapshot;

    protected FirebaseDatabaseSnapshot(DataSnapshot firebaseDatabaseSnapshot) {
        this.firebaseDatabaseSnapshot = firebaseDatabaseSnapshot;
        //return databaseReference.firebaseReference.get();
    }

    @Override
    public DatabaseSnapshot child(String path) {
        return new FirebaseDatabaseSnapshot(firebaseDatabaseSnapshot.child(path));
    }

    @Override
    public String getKey() {
        return firebaseDatabaseSnapshot.getKey();
    }

    @Override
    public <T> T getValue(@NonNull Class<T> valueType) {
        return firebaseDatabaseSnapshot.getValue(valueType);
    }

    @Override
    public Object getValue() {
        return firebaseDatabaseSnapshot.getValue();
    }

    @Override
    public boolean exists() {
        return firebaseDatabaseSnapshot.exists();
    }

    @Override
    public Iterable<DatabaseSnapshot> getChildren() {
        final Iterator<DataSnapshot> iter = firebaseDatabaseSnapshot.getChildren().iterator();

        return () -> new Iterator<DatabaseSnapshot>() {
            @Override
            public boolean hasNext() {
                return iter.hasNext();
            }

            @Override
            @NonNull
            public DatabaseSnapshot next() {
                DataSnapshot childNode = iter.next();
                return new FirebaseDatabaseSnapshot(childNode);
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("DatabaseSnapshot: remove called on immutable collection");
            }
        };
    }
}