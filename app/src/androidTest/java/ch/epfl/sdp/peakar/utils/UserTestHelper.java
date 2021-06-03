package ch.epfl.sdp.peakar.utils;

import androidx.test.platform.app.InstrumentationRegistry;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import ch.epfl.sdp.peakar.database.Database;
import ch.epfl.sdp.peakar.user.services.AuthService;
import ch.epfl.sdp.peakar.user.services.FirebaseAuthService;

import static ch.epfl.sdp.peakar.database.DatabaseTest.databaseRefRoot;

public class UserTestHelper {
    /**
     * Helper method, register a new anonymous test user.
     * Note: this is a blocking method.
     */
    public static void registerAuthUser() {
        // Generate user or force retrieve data if a user is already here.
        if(FirebaseAuth.getInstance().getCurrentUser() == null) {
            AuthService.getInstance().signOut(InstrumentationRegistry.getInstrumentation().getTargetContext());
            AuthService.getInstance().authAnonymously();

        }
        else {
            FirebaseAuthService.getInstance().forceRetrieveData();
        }
    }

    /**
     * Helper method, delete the current user.
     * Note: this is a blocking method.
     */
    public static void removeAuthUser() {
        databaseRefRoot.child(Database.CHILD_USERS).child(AuthService.getInstance().getID()).removeValue();
        Task<Void> fbTask = null;
        FirebaseUser oldUser = FirebaseAuth.getInstance().getCurrentUser();
        AuthService.getInstance().signOut(InstrumentationRegistry.getInstrumentation().getTargetContext());
        if(oldUser != null) fbTask = oldUser.delete();
        try {
            if(oldUser!=null) {
                Tasks.await(fbTask);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
