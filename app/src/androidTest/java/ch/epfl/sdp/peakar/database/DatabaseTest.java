package ch.epfl.sdp.peakar.database;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

import ch.epfl.sdp.peakar.user.services.AuthAccount;
import ch.epfl.sdp.peakar.user.services.AuthService;

import static ch.epfl.sdp.peakar.user.AuthAccountTest.registerAuthUser;
import static ch.epfl.sdp.peakar.user.AuthAccountTest.removeAuthUser;
import static ch.epfl.sdp.peakar.utils.TestingConstants.BASIC_USERNAME;
import static ch.epfl.sdp.peakar.utils.TestingConstants.LONG_SLEEP_TIME;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class DatabaseTest {
    private static String user1;
    private static String user2;
    public static final DatabaseReference databaseRefRoot = Database.getInstance().getReference();


    /* Set up the environment */
    @BeforeClass
    public static void init() {
        /* Make sure no user is signed in before tests */
        AuthService.getInstance().signOut(InstrumentationRegistry.getInstrumentation().getTargetContext());


        /* Create a new one */
        registerAuthUser();

        user1 = (BASIC_USERNAME + AuthService.getInstance().getID()).substring(0, AuthAccount.NAME_MAX_LENGTH - 1);
        user2 = (BASIC_USERNAME + AuthService.getInstance().getID()).substring(0, AuthAccount.NAME_MAX_LENGTH - 2);
    }

    /* Clean environment */
    @AfterClass
    public static void end() {
        Database.getInstance().setOnlineMode();
        removeTestUsers();
        removeAuthUser();
    }

    /* Make sure that an account is signed in and as new before each test */
    @Before
    public void createTestUser() {
        Database.getInstance().setOnlineMode();
        registerAuthUser();
        removeTestUsers();
    }

    /* Make sure that mock users are not on the database after a test */
    public static void removeTestUsers() {
        Database.getInstance().setOnlineMode();
        databaseRefRoot.child(Database.CHILD_USERS).child(AuthService.getInstance().getID()).removeValue();
        databaseRefRoot.child(Database.CHILD_USERS).child(user2).removeValue();
    }

    /* Test that exists method works */
    @Test
    public void existsTest() {
        DatabaseReference usernameReference = databaseRefRoot.child(Database.CHILD_USERS).child(AuthService.getInstance().getID()).child(Database.CHILD_USERNAME);
        // Set the username
        usernameReference.setValue(user1);

        // Check that the username exists
        assertTrue(usernameReference.get().exists());

        // Check that a non existing child does not return true
        assertFalse(usernameReference.child(user2).get().exists());
    }

    /* Test that setValue method works */
    @Test
    public void setValueTest() {
        DatabaseReference usernameReference = databaseRefRoot.child(Database.CHILD_USERS).child(AuthService.getInstance().getID()).child(Database.CHILD_USERNAME);

        assertFalse(usernameReference.get().exists());

        usernameReference.setValue(user1);

        assertTrue(usernameReference.get().exists());
    }

    /* Test that removeValue method works */
    @Test
    public void removeValueTest() {
        DatabaseReference usernameReference = databaseRefRoot.child(Database.CHILD_USERS).child(AuthService.getInstance().getID()).child(Database.CHILD_USERNAME);

        usernameReference.setValue(user1);

        assertTrue(usernameReference.get().exists());

        usernameReference.removeValue();

        assertFalse(usernameReference.get().exists());
    }

    /* Test that when offline mode is enabled, connection to the DB is indeed interrupted */
    @Test
    public void offlineModeTest() {
        Database.getInstance().setOfflineMode();
        Task<Void> setTask = Database.getInstance().getReference().child(Database.CHILD_USERS).child(user2).child(Database.CHILD_SCORE).setValueAsync(200);
        try {
            Tasks.await(setTask, LONG_SLEEP_TIME, TimeUnit.MILLISECONDS);
            fail();
        } catch (Exception ignored) {
        }
    }
}
