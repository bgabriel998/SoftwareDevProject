package ch.epfl.sdp.peakar.database;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.preference.PreferenceManager;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.atomic.AtomicBoolean;

import ch.epfl.sdp.peakar.R;
import ch.epfl.sdp.peakar.database.providers.firebase.FirebaseDatabaseReference;

import static ch.epfl.sdp.peakar.database.providers.firebase.FirebaseDatabaseReference.DATABASE_ADDRESS;

/**
 * This class represents a Database.
 * It implements the singleton pattern so only one Database object can exist.
 */
public class Database {
    /* CHILD PATH CONSTANTS */
    public static final String CHILD_USERS = "users/";
    public static final String CHILD_FRIENDS = "friends/";
    public static final String CHILD_PHOTO_URL = "photo";
    public static final String CHILD_DISCOVERED_PEAKS = "DiscoveredPeaks";
    public static final String CHILD_DISCOVERED_PEAKS_HEIGHTS = "DiscoveredHeights";
    public static final String CHILD_USERNAME = "username";
    public static final String CHILD_SCORE = "score";
    public static final String CHILD_COUNTRY_HIGH_POINT = "CountryHighPoint";
    public static final String CHILD_COUNTRY_HIGH_POINT_NAME = "countryHighPoint";
    public static final String CHILD_ATTRIBUTE_HIGH_POINT_HEIGHT = "highPointHeight";
    public static final String CHILD_ATTRIBUTE_COUNTRY_NAME = "countryName";
    public static final String CHILD_ATTRIBUTE_PEAK_NAME = "name";
    public static final String CHILD_ATTRIBUTE_PEAK_LATITUDE = "latitude";
    public static final String CHILD_ATTRIBUTE_PEAK_LONGITUDE = "longitude";
    public static final String CHILD_ATTRIBUTE_PEAK_ALTITUDE = "altitude";
    public static final String CHILD_CHALLENGES = "challenges/";
    public static final String CHILD_CHALLENGE_GOAL = "goal";
    public static final String CHILD_CHALLENGE_STATUS = "challengeStatus";
    public static final String CHILD_CHALLENGE_START = "startTimeAndDate";
    public static final String CHILD_CHALLENGE_CREATION = "creationTimeAndDate";
    public static final String CHILD_CHALLENGE_FINISH = "finishTimeAndDate";
    public static final String CHILD_CHALLENGE_DURATION = "durationInDays";
    //public final static String VALUE_POINTS_CHALLENGE = "points_challenge";

    /* SINGLETON ATTRIBUTES */
    private static Database instance;
    private final DatabaseReference reference;
    private final AtomicBoolean online = new AtomicBoolean(true);

    private Database() {
        // Enable persistence
        FirebaseDatabase.getInstance(DATABASE_ADDRESS).setPersistenceEnabled(true);
        reference = new FirebaseDatabaseReference();
        // Start connection listener, that will automatically set the online var to false whenever the db is not connected and viceversa
        FirebaseDatabase.getInstance(DATABASE_ADDRESS).getReference(".info/connected").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);
                online.set(connected);
                Log.d("Database", "connected = " + connected);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.d("Database", "onCancelled: error");
            }
        });
    }

    /**
     * Get a database instance.
     */
    public static Database getInstance() {
        if(instance == null) instance = new Database();
        return instance;
    }

    /**
     * Init method for the database. Handle the offline mode settings and enables persistence.
     *
     * IMPORTANT: this method <b>MUST</b> be called in the <code>InitActivity</code> before other calls to the <code>Database</code> class are performed.
     *
     * @param context context of the application.
     */
    public static void init(Context context) {
        // Check that if offline mode is active, and in this case call the right method
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean offlineModeValue = prefs.getBoolean(context.getResources().getString(R.string.offline_mode_key), false);

        Database instance = getInstance();

        if (offlineModeValue) {
            instance.setOfflineMode();
        }
    }

    /**
     * Get a reference to the root of the database.
     */
    public DatabaseReference getReference() {
        return reference;
    }

    /**
     * Enable the DB offline mode.
     * Requests for which data is cached can still be answered.
     * Other requests will be enqueued until online mode is set.
     */
    public void setOfflineMode() {
        FirebaseDatabase.getInstance(DATABASE_ADDRESS).goOffline();
    }

    /**
     * Enable the DB online mode.
     */
    public void setOnlineMode() {
        FirebaseDatabase.getInstance(DATABASE_ADDRESS).goOnline();
    }

    public boolean isOnline() {
        return online.get();
    }
}
