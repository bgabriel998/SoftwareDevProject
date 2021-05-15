package ch.epfl.sdp.peakar.database;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;

import java.util.Iterator;

/**
 * This class represents a Database Snapshot, i.e. a copy of a database child retrieved and available locally.
 * Its implementation makes use of Firebase API, so needs to be modified if database provider is changed.
 */
public class NewDatabaseSnapshot {
    private final DataSnapshot firebaseDatabaseSnapshot;

    protected NewDatabaseSnapshot(DataSnapshot firebaseDatabaseSnapshot) {
        this.firebaseDatabaseSnapshot = firebaseDatabaseSnapshot;
        //return databaseReference.firebaseReference.get();
    }

    public NewDatabaseSnapshot child(String path) {
        return new NewDatabaseSnapshot(firebaseDatabaseSnapshot.child(path));
    }

    public String getKey() {
        return firebaseDatabaseSnapshot.getKey();
    }

    public <T> T getValue(@NonNull Class<T> valueType) {
        return firebaseDatabaseSnapshot.getValue(valueType);
    }

    public Object getValue() {
        return firebaseDatabaseSnapshot.getValue();
    }

    public boolean exists() {
        return firebaseDatabaseSnapshot.exists();
    }

    public Iterable<NewDatabaseSnapshot> getChildren() {
        final Iterator<DataSnapshot> iter = firebaseDatabaseSnapshot.getChildren().iterator();

        return () -> new Iterator<NewDatabaseSnapshot>() {
            @Override
            public boolean hasNext() {
                return iter.hasNext();
            }

            @Override
            @NonNull
            public NewDatabaseSnapshot next() {
                DataSnapshot childNode = iter.next();
                return new NewDatabaseSnapshot(childNode);
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("DatabaseSnapshot: remove called on immutable collection");
            }
        };
    }
}
