package ch.epfl.sdp.peakar.user.services;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Build;
import android.util.Log;


import androidx.annotation.RequiresApi;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import ch.epfl.sdp.peakar.database.Database;
import ch.epfl.sdp.peakar.database.DatabaseReference;
import ch.epfl.sdp.peakar.database.DatabaseSnapshot;
import ch.epfl.sdp.peakar.general.remote.RemoteOutcome;
import ch.epfl.sdp.peakar.general.remote.RemoteResource;
import ch.epfl.sdp.peakar.points.CountryHighPoint;
import ch.epfl.sdp.peakar.points.POIPoint;
import ch.epfl.sdp.peakar.user.challenge.Challenge;
import ch.epfl.sdp.peakar.user.friends.RemoteFriendItem;
import ch.epfl.sdp.peakar.user.challenge.goal.RemotePointsChallenge;

/**
 * This class implements helper methods that can generate an <code>AccountData</code> retrieving data from the DB.
 */
public class RemoteAccountDataFactory implements RemoteResource {
    private final AccountData accountData;

    private final DatabaseReference dbRefUser;
    private DatabaseSnapshot data;

    /**
     * Constructor.
     */
    private RemoteAccountDataFactory(AccountData accountData, DatabaseReference dbRefUser) {
        this.accountData = accountData;
        this.dbRefUser = dbRefUser;
    }

    /**
     * Retrieve the account data from the remote resource indicated.
     * @param accountData account data to modify with the remote resource.
     * @param dbRefUser database reference to the user.
     */
    public static RemoteOutcome retrieveAccountData(AccountData accountData, DatabaseReference dbRefUser) {
        RemoteAccountDataFactory newFactory = new RemoteAccountDataFactory(accountData, dbRefUser);
        return newFactory.retrieveData();
    }

    @Override
    public RemoteOutcome retrieveData() {
        // Get the obtained data
        DatabaseSnapshot data = dbRefUser.get();
        assert data != null;
        // If there is no data on DB, return such outcome
        if(!data.exists()) {
            return RemoteOutcome.NOT_FOUND;
        }
        // Otherwise, update the attributes with retrieve data
        this.data = data;
        loadData();
        return RemoteOutcome.FOUND;
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
        if(dbRefUser.toString().equals(Database.getInstance().getReference().child(Database.CHILD_USERS).child(AuthService.getInstance().getID()).toString()) && !AuthService.getInstance().getPhotoUrl().equals(accountData.getPhotoUrl())) {
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
    private void loadPeaks(DatabaseSnapshot data) {
        for (DatabaseSnapshot peak : data.getChildren()) {
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
    private void loadCountryHighPoints(DatabaseSnapshot data) {
        for (DatabaseSnapshot countryHighPoint : data.getChildren()) {
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
    private void loadHeights(DatabaseSnapshot data) {
        for (DatabaseSnapshot heightEntry : data.getChildren()) {
            // Get the height
            int newHeight = Optional.ofNullable(heightEntry.child("0").getValue(Integer.class)).orElse(0);

            // Add the height locally
            accountData.addDiscoveredPeakHeight(newHeight);
        }
    }

    /**
     * Load added friends
     */
    private void loadFriends(DatabaseSnapshot data) {
        for (DatabaseSnapshot friendEntry : data.getChildren()) {
            // Get the friend ID
            String uidFriend = friendEntry.getKey();

            // Create a friend item
            RemoteFriendItem newFriendItem = new RemoteFriendItem(uidFriend);

            // Add the friend
            accountData.addFriend(newFriendItem);
        }
    }

    /**
     * Load added challenges.
     */
    @SuppressLint("NewApi")
    private void loadChallenges(DatabaseSnapshot data) {
        Log.d("FirebaseAccountDataFactory", "loadChallenges: entered");
        for (DatabaseSnapshot challengeEntry : data.getChildren()) {
            Log.d("FirebaseAccountDataFactory", "loadChallenges: entered for");
            String challengeId = challengeEntry.getKey();
            assert challengeId != null;
            Log.d("FirebaseAccountDataFactory", "loadChallenges: challenge id = " + challengeId);
            String challengeType = challengeEntry.getValue(String.class);
            assert challengeType != null;
            Log.d("FirebaseAccountDataFactory", "loadChallenges: before if. Current challenge type = " + challengeType);
            if(challengeType.equals(Database.VALUE_POINTS_CHALLENGE)) {
                Log.d("FirebaseAccountDataFactory", "loadChallenges: entered if");
                DatabaseSnapshot retrieveChallenge = Database.getInstance().getReference().child(Database.CHILD_CHALLENGES).child(challengeId).get();
                loadPointsChallenge(retrieveChallenge);
            }
        }
    }

    /**
     * Load points challenge.
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void loadPointsChallenge(DatabaseSnapshot data) {
        Log.d("FirebaseAccountDataFactory", "loadPointsChallenge: entered");

        // Get ID of new challenge
        String id = data.getKey();
        assert id != null;
        Log.d("FirebaseAccountDataFactory", "loadPointsChallenge: id = " + id);

        // Get users
        List<String> users = new ArrayList<>();
        for (DatabaseSnapshot user : data.child(Database.CHILD_USERS).getChildren()) {
            Log.d("FirebaseAccountDataFactory", "loadPointsChallenge: added user = " + user.getKey());
            users.add(user.getKey());
        }

        // Get goal
        long goal = Optional.ofNullable(data.child(Database.CHILD_CHALLENGE_GOAL).getValue(Long.class)).orElse(0L);
        Log.d("FirebaseAccountDataFactory", "loadPointsChallenge: goal = " + goal);

        // Get start
        LocalDateTime startDateTime = Optional.ofNullable(data.child(Database.CHILD_CHALLENGE_START).getValue(LocalDateTime.class)).orElse(LocalDateTime.now());
        // Get finish
        LocalDateTime finishDateTime = Optional.ofNullable(data.child(Database.CHILD_CHALLENGE_FINISH).getValue(LocalDateTime.class)).orElse(LocalDateTime.now());

        // Compute prize
        long prize = (users.size() - 1) * Challenge.AWARDED_POINTS_PER_USER;
        Log.d("FirebaseAccountDataFactory", "loadPointsChallenge: prize = " + prize);

        // Add the challenge
        accountData.addChallenge(new RemotePointsChallenge(id, users, prize, goal,startDateTime, finishDateTime));
        Log.d("FirebaseAccountDataFactory", "loadPointsChallenge: new challenges size = " + accountData.getChallenges().size());
    }
}
