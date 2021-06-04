package ch.epfl.sdp.peakar.database;

import androidx.annotation.NonNull;

/**
 * This interface represents a Database Snapshot, i.e. a copy of a database child retrieved and available locally.
 */
public interface DatabaseSnapshot {
    /**
     * Get a snapshot of the given child.
     * @param path path of the new child from the current snapshot.
     */
    DatabaseSnapshot child(String path);

    /**
     * Get the key of the current DB snapshot.
     */
    String getKey();

    /**
     * Get the value of the current DB snapshot.
     * @param valueType type of the retrieved value.
     * @param <T> type of the retrieved value.
     */
    <T> T getValue(@NonNull Class<T> valueType);

    /**
     * Get the value of the current DB snapshot.
     * @return value as an object.
     */
    Object getValue();

    /**
     * Check if the current DB snapshot exists.
     * @return true if the snapshot is not empty, false otherwise.
     */
    boolean exists();

    /**
     * Give access to all of the immediate children of this snapshot.
     */
    Iterable<DatabaseSnapshot> getChildren();
}
