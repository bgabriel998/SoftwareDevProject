package ch.epfl.sdp.peakar.points;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.platform.app.InstrumentationRegistry;

import com.google.firebase.auth.FirebaseAuth;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Random;
import java.util.concurrent.TimeoutException;

import ch.epfl.sdp.peakar.database.Database;
import ch.epfl.sdp.peakar.user.services.AuthService;
import ch.epfl.sdp.peakar.user.services.providers.firebase.FirebaseAuthService;

import static ch.epfl.sdp.peakar.utils.TestingConstants.SHORT_SLEEP_TIME;
import static ch.epfl.sdp.peakar.user.AuthAccountTest.registerAuthUser;
import static ch.epfl.sdp.peakar.user.AuthAccountTest.removeAuthUser;

public class UserPointTest {

    private static Context mContext;
    private static UserPoint userPoint;

    /* Set up the environment */
    @BeforeClass
    public static void init() {
        /* Make sure no user is signed in before tests */
        AuthService.getInstance().signOut(InstrumentationRegistry.getInstrumentation().getTargetContext());

        /* Create a new one */
        registerAuthUser();
    }

    /* Clean environment */
    @AfterClass
    public static void end() {
        removeAuthUser();
    }

    /* Make sure that an account is signed in and as new before each test */
    @Before
    public void createTestUser() {
        if(FirebaseAuth.getInstance().getCurrentUser() == null) {
            AuthService.getInstance().signOut(InstrumentationRegistry.getInstrumentation().getTargetContext());
            registerAuthUser();
        }
        else {
            FirebaseAuthService.getInstance().forceRetrieveData();
        }
    }

    /* Make sure that mock users are not on the database after a test */
    @After
    public void removeTestUsers() throws InterruptedException {
        Database.getInstance().getReference().child(Database.CHILD_USERS).child(AuthService.getInstance().getID()).removeValue();
        Thread.sleep(SHORT_SLEEP_TIME);
    }

    @BeforeClass
    public static void createInstance() {
        mContext = ApplicationProvider.getApplicationContext();
        userPoint = UserPoint.getInstance(mContext);
    }

    @Test
    public void getLocationTest() {

        // check if customLocation is off
        Assert.assertFalse(userPoint.isCustomLocation());

        // if the gps tracker is able to get the location, the test passes, otherwise
        // it will check if the default location is returned correctly
        if (userPoint.canGetLocation()) {
            return;
        } else {
            Assert.assertEquals(27.988056, userPoint.getLatitude(), 0);
            Assert.assertEquals(86.925278, userPoint.getLongitude(), 0);
            Assert.assertEquals(8848.86, userPoint.getAltitude(), 0);
            Assert.assertEquals(0.0, userPoint.getAccuracy(), 0);
        }

    }

    @Test
    public void setLocationTest() throws TimeoutException {

        Random r = new Random();

        double lat = r.nextDouble();
        double lon = r.nextDouble();
        double alt = r.nextDouble();
        double acc = r.nextDouble();

        // set the location to random location
        userPoint.setLocation(lat,lon,alt,acc);

        // check if the location is set correctly
        Assert.assertEquals(lat, userPoint.getLatitude(), 0);
        Assert.assertEquals(lon, userPoint.getLongitude(), 0);
        Assert.assertEquals(alt, userPoint.getAltitude(), 0);
        Assert.assertEquals(acc, userPoint.getAccuracy(), 0);
        // check if customLocation is enabled
        Assert.assertTrue(userPoint.isCustomLocation());

        // re enables real location
        userPoint.switchToRealLocation();

        // if the gps tracker is able to get the location, the test passes, otherwise
        // it will check if the default location is returned correctly
        if (!userPoint.canGetLocation()) {
            Assert.assertEquals(27.988056, userPoint.getLatitude(), 0);
            Assert.assertEquals(86.925278, userPoint.getLongitude(), 0);
            Assert.assertEquals(8848.86, userPoint.getAltitude(), 0);
            Assert.assertEquals(0.0, userPoint.getAccuracy(), 0);
        }

        // check if customLocation is off
        Assert.assertFalse(userPoint.isCustomLocation());

    }

}
