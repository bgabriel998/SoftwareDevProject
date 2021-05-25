package ch.epfl.sdp.peakar.database.providers.firebase;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.Query;

import ch.epfl.sdp.peakar.database.DatabaseQuery;
import ch.epfl.sdp.peakar.database.DatabaseSnapshot;

/**
 * This is a concrete implementation of DatabaseQuery.
 * Its implementation makes use of Firebase API, so needs to be modified if database provider is changed.
 */
public class FirebaseDatabaseQuery implements DatabaseQuery {
    private final Query firebaseQuery;

    /**
     * Constructor of the class.
     * @param firebaseQuery firebase query to encapsulate.
     */
    protected FirebaseDatabaseQuery(Query firebaseQuery) {
        this.firebaseQuery = firebaseQuery;
    }

    @Override
    public DatabaseQuery equalTo(String value) {
        return new FirebaseDatabaseQuery(firebaseQuery.equalTo(value));
    }

    @Override
    public DatabaseSnapshot get() {
        Task<DataSnapshot> getTask = firebaseQuery.get();
        try {
            Tasks.await(getTask);
            return new FirebaseDatabaseSnapshot(getTask.getResult());
        } catch(Exception e) {
            e.printStackTrace();
            throw new RuntimeException("DBReference: error getting data snapshot");
        }
    }
}
