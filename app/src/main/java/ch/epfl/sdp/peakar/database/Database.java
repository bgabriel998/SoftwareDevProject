package ch.epfl.sdp.peakar.database;

import ch.epfl.sdp.peakar.database.providers.firebase.FirebaseDatabaseReference;

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
    public final static String VALUE_POINTS_CHALLENGE = "points_challenge";

    /* SINGLETON ATTRIBUTES */
    private static Database instance;
    private final DatabaseReference reference;

    private Database() {
        reference = new FirebaseDatabaseReference();
    }

    /**
     * Get a database instance.
     */
    public static Database getInstance() {
        if(instance == null) instance = new Database();
        return instance;
    }

    /**
     * Get a reference to the root of the database.
     */
    public DatabaseReference getReference() {
        return reference;
    }
}
