package ch.epfl.sdp.peakar.user.services.providers.firebase;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import java.util.Optional;

import ch.epfl.sdp.peakar.database.Database;
import ch.epfl.sdp.peakar.general.remote.RemoteOutcome;
import ch.epfl.sdp.peakar.general.remote.RemoteResource;
import ch.epfl.sdp.peakar.points.CountryHighPoint;
import ch.epfl.sdp.peakar.points.POIPoint;
import ch.epfl.sdp.peakar.user.services.AccountData;
import ch.epfl.sdp.peakar.user.services.AuthAccount;

public class FirebaseAccountDataFactory implements RemoteResource {
    private final AccountData accountData;

    private DatabaseReference dbRefUser;
    private DataSnapshot data;

    private FirebaseAccountDataFactory(DataSnapshot dataSnapshot) {
        this.accountData = new AccountData();
        this.data = dataSnapshot;
    }

    private FirebaseAccountDataFactory(AccountData accountData, DatabaseReference dbRefUser) {
        this.accountData = accountData;
        this.dbRefUser = dbRefUser;
    }

    public static AccountData generateAccountData(DataSnapshot dataSnapshot) {
        FirebaseAccountDataFactory newFactory = new FirebaseAccountDataFactory(dataSnapshot);
        newFactory.loadData();
        return newFactory.accountData;
    }

    public static RemoteOutcome retrieveAccountData(AccountData accountData, DatabaseReference dbRefUser) {
        FirebaseAccountDataFactory newFactory = new FirebaseAccountDataFactory(accountData, dbRefUser);
        return newFactory.retrieveData();
    }

    @Override
    public RemoteOutcome retrieveData() {
        Task<DataSnapshot> retrieveTask = dbRefUser.get();
        Log.d("RegisterUserTest", "retrieveData: entered");

        try {
            // Wait for task to finish
            Tasks.await(retrieveTask);
            Log.d("RegisterUserTest", "retrieveData: waited");
            // Get the obtained data
            DataSnapshot data = retrieveTask.getResult();
            assert data != null;
            Log.d("RegisterUserTest", "retrieveData: asserted");
            // If there is no data on DB, return such outcome
            if(!data.exists()) {
                new AccountData();
                return RemoteOutcome.NOT_FOUND;
            }
            Log.d("RegisterUserTest", "retrieveData: exists");
            // Otherwise, update the attributes with retrieve data
            this.data = data;
            loadData();
            Log.d("RegisterUserTest", "retrieveData: loaded");
            return RemoteOutcome.FOUND;

        } catch (Exception e) {
            Log.d("AUTH", "retrieveData: failed " + e.getMessage());
            return RemoteOutcome.FAIL;
        }
    }

    public void loadData() {
        // Load username
        accountData.setUsername((Optional.ofNullable(data.child(Database.CHILD_USERNAME).getValue(String.class)).orElse(AuthAccount.USERNAME_BEFORE_REGISTRATION)));
        Log.d("RegisterUserTest", "Factory - loadData: username after = " + accountData.getUsername());

        // Load score
        accountData.setScore(Optional.ofNullable(data.child(Database.CHILD_SCORE).getValue(long.class)).orElse(0L));

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
            accountData.getDiscoveredPeaks().add(newPeak);
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
            accountData.getDiscoveredCountryHighPoint().put(newCountryHighPoint.getCountryName(), newCountryHighPoint);
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
            accountData.getDiscoveredPeakHeights().add(newHeight);
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
            accountData.getFriends().add(newFriendItem);
        }
    }
}
