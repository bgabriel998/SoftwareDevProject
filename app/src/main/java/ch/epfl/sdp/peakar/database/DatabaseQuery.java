package ch.epfl.sdp.peakar.database;

/**
 * This interface represents a Database query.
 */
public interface DatabaseQuery {
    /**
     * Create a query constrained to only return childs with the given value.
     */
    DatabaseQuery equalTo(String value);

    /**
     * Get the database snapshot given by the current query.
     * Note that this method is a blocking method, meaning that it cannot be used on the UI thread.
     * You may want to run this method on a new thread and, after that, do something else on the UI thread.
     */
    DatabaseSnapshot get();
}
