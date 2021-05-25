package ch.epfl.sdp.peakar.user.services;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import ch.epfl.sdp.peakar.database.Database;
import ch.epfl.sdp.peakar.database.DatabaseReference;
import ch.epfl.sdp.peakar.database.DatabaseSnapshot;
import ch.epfl.sdp.peakar.general.remote.RemoteOutcome;
import ch.epfl.sdp.peakar.general.remote.RemoteResource;
import ch.epfl.sdp.peakar.points.CountryHighPoint;
import ch.epfl.sdp.peakar.points.POIPoint;

import ch.epfl.sdp.peakar.social.RemoteFriendItem;

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
        String stringUri = Optional.ofNullable(data.child(Database.CHILD_PHOTO_URL).getValue(String.class)).orElse("");
        accountData.setPhotoUrl(stringUri.equals("") ? Uri.EMPTY : Uri.parse(stringUri));
        Log.d("RemoteAccountDataFactory", "loadData: current photo = " + Uri.parse(Optional.ofNullable(data.child(Database.CHILD_PHOTO_URL).getValue(String.class)).orElse("")));
        Log.d("RemoteAccountDataFactory", "loadData: new photo = " + AuthService.getInstance().getPhotoUrl().toString());
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
            Log.d("FirebaseAccountDataFactory", "loadChallenges: entered if");
            DatabaseSnapshot retrieveChallenge = Database.getInstance().getReference().child(Database.CHILD_CHALLENGES).child(challengeId).get();
            loadPointsChallenge(retrieveChallenge);
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
        //get name
        String challengeName = Optional.ofNullable(data.child(Database.CHILD_CHALLENGE_NAME).getValue(String.class)).orElse("");

        //get founder
        String founderID = Optional.ofNullable(data.child(Database.CHILD_CHALLENGE_FOUNDER).getValue(String.class)).orElse("");

        // Get start
        LocalDateTime startDateTime = null;
        String startDateTimeStr = Optional.ofNullable(data.child(Database.CHILD_CHALLENGE_START).getValue(String.class)).orElse("");
        if(!startDateTimeStr.equals(""))
            startDateTime = LocalDateTime.parse(startDateTimeStr);
        // Get finish
        LocalDateTime finishDateTime = null;
        String finishDateTimeStr = Optional.ofNullable(data.child(Database.CHILD_CHALLENGE_FINISH).getValue(String.class)).orElse("");
        if(!finishDateTimeStr.equals(""))
            finishDateTime = LocalDateTime.parse(finishDateTimeStr);
        // Get creation Date
        LocalDateTime creationDateTime = null;
        String creationDateTimeStr = Optional.ofNullable(data.child(Database.CHILD_CHALLENGE_CREATION).getValue(String.class)).orElse("");
        if(!finishDateTimeStr.equals(""))
            creationDateTime = LocalDateTime.parse(creationDateTimeStr);

        // Get challenge duration
        int durationInDays = Optional.ofNullable(data.child(Database.CHILD_CHALLENGE_DURATION).getValue(Integer.class)).orElse(0);

        // Get challenge status
        int challengeStatus = Optional.ofNullable(data.child(Database.CHILD_CHALLENGE_STATUS).getValue(Integer.class)).orElse(0);

        // Compute challenge ranking
        HashMap<String, Integer> challengeRanking = computeChallengeRanking(id,users);

        // Retrieve usernames
        HashMap<String,String> userIDUserNames = retrieveEnrolledUserNames(users);

        // Add the challenge
        accountData.addChallenge(new RemotePointsChallenge(id,founderID,challengeName, users,
                challengeStatus,creationDateTime,durationInDays,startDateTime, finishDateTime,challengeRanking,userIDUserNames));
        Log.d("FirebaseAccountDataFactory", "loadPointsChallenge: new challenges size = " + accountData.getChallenges().size());
    }

    /**
     * Compute the actual ranking of the challenge
     * @param challengeID challenge ID
     * @param users list of enrolled users
     * @return HashMap containing user ID and current score for the challenge
     */
    private HashMap<String,Integer> computeChallengeRanking(String challengeID, List<String> users){
        if(users.size() == 1) return null; // only one user in challenge return immediately
        HashMap<String,Integer> retChallengeRanking = new HashMap<>();
        for(String user : users){
            int userScore = Optional.ofNullable(Database.getInstance().getReference().child(Database.CHILD_USERS)
                    .child(user).child(Database.CHILD_SCORE).get().getValue(Integer.class)).orElse(0);
            int userInitScore = Optional.ofNullable(Database.getInstance().getReference().child(Database.CHILD_USERS)
                    .child(user).child(Database.CHILD_CHALLENGES).child(challengeID).get().getValue(Integer.class)).orElse(0);

            retChallengeRanking.put(user,userScore - userInitScore);
        }
        return retChallengeRanking;
    }

    /**
     * Retrieve the userNames of the participants
     * @param users list of user IDs
     * @return HashMap containing UID as key and username as value
     */
    private HashMap<String,String> retrieveEnrolledUserNames(List<String> users){
        if(users.size() == 1) return null; // only one user in challenge return immediately
        HashMap<String,String> retChallengeUserNames = new HashMap<>();
        for(String user : users) {
            String username = Optional.ofNullable(Database.getInstance().getReference().child(Database.CHILD_USERS)
                    .child(user).child(Database.CHILD_USERNAME).get().getValue(String.class)).orElse("");
            retChallengeUserNames.put(user,username);
        }
        return retChallengeUserNames;
    }
}
