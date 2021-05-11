package ch.epfl.sdp.peakar.user.services.providers.firebase;

import android.net.Uri;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import ch.epfl.sdp.peakar.database.Database;
import ch.epfl.sdp.peakar.general.remote.RemoteOutcome;
import ch.epfl.sdp.peakar.general.remote.RemoteResource;
import ch.epfl.sdp.peakar.points.CountryHighPoint;
import ch.epfl.sdp.peakar.points.POIPoint;
import ch.epfl.sdp.peakar.user.challenge.Challenge;
import ch.epfl.sdp.peakar.user.services.AccountData;
import ch.epfl.sdp.peakar.user.services.AuthAccount;
import ch.epfl.sdp.peakar.user.services.AuthService;

/**
 * This class implements helper methods that can generate an <code>AccountData</code> retrieving data from the DB.
 */
public class FirebaseAccountDataFactory implements RemoteResource {
    private final AccountData accountData;

    private final DatabaseReference dbRefUser;
    private DataSnapshot data;

    /**
     * Constructor.
     */
    private FirebaseAccountDataFactory(AccountData accountData, DatabaseReference dbRefUser) {
        this.accountData = accountData;
        this.dbRefUser = dbRefUser;
    }

    /**
     * Retrieve the account data from the remote resource indicated.
     * @param accountData account data to modify with the remote resource.
     * @param dbRefUser database reference to the user.
     */
    public static RemoteOutcome retrieveAccountData(AccountData accountData, DatabaseReference dbRefUser) {
        FirebaseAccountDataFactory newFactory = new FirebaseAccountDataFactory(accountData, dbRefUser);
        return newFactory.retrieveData();
    }

    @Override
    public RemoteOutcome retrieveData() {
        Task<DataSnapshot> retrieveTask = dbRefUser.get();

        try {
            // Wait for task to finish
            Tasks.await(retrieveTask);
            // Get the obtained data
            DataSnapshot data = retrieveTask.getResult();
            assert data != null;
            // If there is no data on DB, return such outcome
            if(!data.exists()) {
                return RemoteOutcome.NOT_FOUND;
            }
            // Otherwise, update the attributes with retrieve data
            this.data = data;
            loadData();
            return RemoteOutcome.FOUND;

        } catch (Exception e) {
            Log.d("FirebaseAccountDataFactory", "retrieveData: fail");
            return RemoteOutcome.FAIL;
        }
    }

    /**
     * Load retrieved data into the <code>AccountData</code> object.
     */
    public void loadData() {
        // Load username
        accountData.setUsername((Optional.ofNullable(data.child(Database.CHILD_USERNAME).getValue(String.class)).orElse(AuthAccount.USERNAME_BEFORE_REGISTRATION)));

        // Load score
        accountData.setScore(Optional.ofNullable(data.child(Database.CHILD_SCORE).getValue(long.class)).orElse(0L));

        // Load photo url
        accountData.setPhotoUrl(Uri.parse(Optional.ofNullable(data.child(Database.CHILD_PHOTO_URL).getValue(String.class)).orElse("")));
        // If this is loading the auth account, check if the photo is updated.
        // If photo has changed from last access, update it.
        if(dbRefUser.toString().equals(Database.refRoot.child(Database.CHILD_USERS).child(AuthService.getInstance().getID()).toString()) && !AuthService.getInstance().getPhotoUrl().equals(accountData.getPhotoUrl())) {
            // Locally
            accountData.setPhotoUrl(AuthService.getInstance().getPhotoUrl());
            // Remotely
            dbRefUser.child(Database.CHILD_PHOTO_URL).setValue(AuthService.getInstance().getPhotoUrl().toString());
            Log.d("FirebaseAccountDataFactory", "retrieveData: updating photo");
        }

        // Load challenges
        loadChallenges(data.child(Database.CHILD_CHALLENGES));

        // Load discovered peaks
        loadPeaks(data.child(Database.CHILD_DISCOVERED_PEAKS));

        // Load discovered country high points
        loadCountryHighPoints(data.child(Database.CHILD_COUNTRY_HIGH_POINT));

        // Load discovered heights
        loadHeights(data.child(Database.CHILD_DISCOVERED_PEAKS_HEIGHTS));

        // Load friends
        loadFriends(data.child(Database.CHILD_FRIENDS));

    }

    /**
     * Load discovered peaks.
     */
    private void loadPeaks(DataSnapshot data) {
        for (DataSnapshot peak : data.getChildren()) {
            POIPoint newPeak = new POIPoint(peak.child(Database.CHILD_ATTRIBUTE_PEAK_NAME).getValue(String.class),
                    Optional.ofNullable(peak.child(Database.CHILD_ATTRIBUTE_PEAK_LATITUDE).getValue(Double.class)).orElse(0.0),
                    Optional.ofNullable(peak.child(Database.CHILD_ATTRIBUTE_PEAK_LONGITUDE).getValue(Double.class)).orElse(0.0),
                    Optional.ofNullable(peak.child(Database.CHILD_ATTRIBUTE_PEAK_ALTITUDE).getValue(Long.class)).orElse(0L));

            // Add the peak
            accountData.addPeak(newPeak);
        }
    }

    /**
     * Load discovered country high points.
     */
    private void loadCountryHighPoints(DataSnapshot data) {
        for (DataSnapshot countryHighPoint : data.getChildren()) {
            // Get the country name
            String countryName = Optional.ofNullable(countryHighPoint.child(Database.CHILD_ATTRIBUTE_COUNTRY_NAME).getValue(String.class)).orElse("null");

            /// Get the high point
            CountryHighPoint newCountryHighPoint = new CountryHighPoint(countryName,
                    Optional.ofNullable(countryHighPoint.child(Database.CHILD_COUNTRY_HIGH_POINT_NAME).getValue(String.class)).orElse("null"),
                    Optional.ofNullable(countryHighPoint.child(Database.CHILD_ATTRIBUTE_HIGH_POINT_HEIGHT).getValue(Long.class)).orElse(0L));

            // Add the high point
            accountData.addDiscoveredCountryHighPoint(newCountryHighPoint.getCountryName(), newCountryHighPoint);
        }
    }

    /**
     * Load discovered heights
     */
    private void loadHeights(DataSnapshot data) {
        for (DataSnapshot heightEntry : data.getChildren()) {
            // Get the height
            int newHeight = Optional.ofNullable(heightEntry.child("0").getValue(Integer.class)).orElse(0);

            // Add the height locally
            accountData.addDiscoveredPeakHeight(newHeight);
        }
    }

    /**
     * Load added friends
     */
    private void loadFriends(DataSnapshot data) {
        for (DataSnapshot friendEntry : data.getChildren()) {
            // Get the friend ID
            String uidFriend = friendEntry.getKey();

            // Create a friend item
            FirebaseFriendItem newFriendItem = new FirebaseFriendItem(uidFriend);

            // Add the friend
            accountData.addFriend(newFriendItem);
        }
    }

    /**
     * Load added challenges.
     */
    private void loadChallenges(DataSnapshot data) {
        Log.d("FirebaseAccountDataFactory", "loadChallenges: entered");
        for (DataSnapshot challengeEntry : data.getChildren()) {
            Log.d("FirebaseAccountDataFactory", "loadChallenges: entered for");
            String challengeId = challengeEntry.getKey();
            assert challengeId != null;
            Log.d("FirebaseAccountDataFactory", "loadChallenges: challenge id = " + challengeId);
            String challengeType = challengeEntry.getValue(String.class);
            assert challengeType != null;
            Log.d("FirebaseAccountDataFactory", "loadChallenges: before if. Current challenge type = " + challengeType);
            if(challengeType.equals(Database.VALUE_POINTS_CHALLENGE)) {
                Log.d("FirebaseAccountDataFactory", "loadChallenges: entered if");
                Task<DataSnapshot> retrieveChallenge = Database.refRoot.child(Database.CHILD_CHALLENGES).child(challengeId).get();
                try {
                    Tasks.await(retrieveChallenge);
                    loadPointsChallenge(Objects.requireNonNull(retrieveChallenge.getResult()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Load points challenge.
     */
    private void loadPointsChallenge(DataSnapshot data) {
        Log.d("FirebaseAccountDataFactory", "loadPointsChallenge: entered");

        // Get ID of new challenge
        String id = data.getKey();
        assert id != null;
        Log.d("FirebaseAccountDataFactory", "loadPointsChallenge: id = " + id);

        // Get users
        List<String> users = new ArrayList<>();
        for (DataSnapshot user : data.child(Database.CHILD_USERS).getChildren()) {
            Log.d("FirebaseAccountDataFactory", "loadPointsChallenge: added user = " + user.getKey());
            users.add(user.getKey());
        }

        // Get goal
        long goal = Optional.ofNullable(data.child(Database.CHILD_CHALLENGE_GOAL).getValue(Long.class)).orElse(0L);
        Log.d("FirebaseAccountDataFactory", "loadPointsChallenge: goal = " + goal);

        // Compute prize
        long prize = (users.size() - 1) * Challenge.AWARDED_POINTS_PER_USER;
        Log.d("FirebaseAccountDataFactory", "loadPointsChallenge: prize = " + prize);

        // Add the challenge
        accountData.addChallenge(new FirebasePointsChallenge(id, users, prize, goal));
        Log.d("FirebaseAccountDataFactory", "loadPointsChallenge: new challenges size = " + accountData.getChallenges().size());
    }
}
