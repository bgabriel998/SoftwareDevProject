package ch.epfl.sdp.peakar.database;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.Query;

/**
 * This class represents a Database query.
 * Its implementation makes use of Firebase API, so needs to be modified if database provider is changed.
 */
public class DatabaseQuery {
    private final Query firebaseQuery;

    protected DatabaseQuery(Query firebaseQuery) {
        this.firebaseQuery = firebaseQuery;
    }

    /**
     * Create a query constrained to only return childs with the given value.
     */
    public DatabaseQuery equalTo(String value) {
        return new DatabaseQuery(firebaseQuery.equalTo(value));
    }

    /**
     * Get the database snapshot given by the current query.
     * Note that this method is a blocking method, meaning that it cannot be used on the UI thread.
     * You may want to run this method on a new thread and, after that, do something else on the UI thread.
     */
    public DatabaseSnapshot get() {
        Task<DataSnapshot> getTask = firebaseQuery.get();
        try {
            Tasks.await(getTask);
            return new DatabaseSnapshot(getTask.getResult());
        } catch(Exception e) {
            e.printStackTrace();
            throw new RuntimeException("DBQuery: error getting data snapshot");
        }
    }
}
