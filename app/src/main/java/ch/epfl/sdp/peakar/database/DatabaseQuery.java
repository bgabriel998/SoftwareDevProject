package ch.epfl.sdp.peakar.database;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.Query;

/**
 * This interface represents a Database query.
 */
public interface DatabaseQuery {
    /**
     * Create a query constrained to only return childs with the given value.
     */
    public DatabaseQuery equalTo(String value);

    /**
     * Get the database snapshot given by the current query.
     * Note that this method is a blocking method, meaning that it cannot be used on the UI thread.
     * You may want to run this method on a new thread and, after that, do something else on the UI thread.
     */
    public DatabaseSnapshot get();
}
