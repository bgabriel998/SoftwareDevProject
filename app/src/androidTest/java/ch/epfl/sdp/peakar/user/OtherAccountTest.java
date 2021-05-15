package ch.epfl.sdp.peakar.user;

import android.net.Uri;

import androidx.test.platform.app.InstrumentationRegistry;


import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static ch.epfl.sdp.peakar.database.DatabaseTest.databaseRefRoot;
import static org.junit.Assert.*;

import ch.epfl.sdp.peakar.database.Database;
import ch.epfl.sdp.peakar.user.services.Account;
import ch.epfl.sdp.peakar.user.services.AuthAccount;
import ch.epfl.sdp.peakar.user.services.AuthService;
import ch.epfl.sdp.peakar.user.services.OtherAccount;

import static ch.epfl.sdp.peakar.utils.TestingConstants.BASIC_USERNAME;
import static ch.epfl.sdp.peakar.user.AuthAccountTest.registerAuthUser;
import static ch.epfl.sdp.peakar.user.AuthAccountTest.removeAuthUser;

public class OtherAccountTest {
    private static String user2;

    /* Set up the environment */
    @BeforeClass
    public static void init() {
        /* Make sure no user is signed in before tests */
        AuthService.getInstance().signOut(InstrumentationRegistry.getInstrumentation().getTargetContext());


        /* Create a new one */
        registerAuthUser();

        user2 = (BASIC_USERNAME + AuthService.getInstance().getID()).substring(0, AuthAccount.NAME_MAX_LENGTH - 2);
    }

    /* Clean environment */
    @AfterClass
    public static void end() {
        removeTestUsers();
        removeAuthUser();
    }

    /* Make sure that an account is signed in and as new before each test */
    @Before
    public void createTestUser() {
        registerAuthUser();
        removeTestUsers();
    }

    /* Make sure that mock users are not on the database after a test */
    public static void removeTestUsers() {
        databaseRefRoot.child(Database.CHILD_USERS).child(AuthService.getInstance().getID()).removeValue();
        databaseRefRoot.child(Database.CHILD_USERS).child(user2).removeValue();
    }

    /* Test that if the other user does not exist, an empty account will be returned */
    @Test
    public void emptyOtherAccountTest() {
        Account account = OtherAccount.getInstance(user2);

        assertTrue(account.getChallenges().isEmpty());
        assertTrue(account.getDiscoveredCountryHighPoint().isEmpty());
        assertTrue(account.getDiscoveredCountryHighPointNames().isEmpty());
        assertTrue(account.getDiscoveredPeakHeights().isEmpty());
        assertTrue(account.getDiscoveredPeaks().isEmpty());
        assertTrue(account.getFriends().isEmpty());
        assertSame(0L, account.getScore());
        assertEquals(Account.USERNAME_BEFORE_REGISTRATION, account.getUsername());

        // Check that getting another instance of the same user, you get the same object
        OtherAccount otherAccount = OtherAccount.getInstance(user2);
        assertSame(account, otherAccount);

        // Check getters of OtherAccount
        assertSame(Uri.EMPTY, otherAccount.getPhotoUrl());
        assertEquals(user2, otherAccount.getUserID());
    }

    /* Test that if the other user exists, it is retrieved correctly */
    @Test
    public void getOtherAccountTest() {
        long score = 10;

        AuthAccount authAccount = AuthService.getInstance().getAuthAccount();

        // Modify the auth account
        authAccount.setScore(score);
        authAccount.changeUsername(user2);

        // Retrieve as an other account the same account
        OtherAccount otherAccount = OtherAccount.getInstance(AuthService.getInstance().getID());

        // Check that the ID and the modified fields are the same
        assertEquals(AuthService.getInstance().getID(), otherAccount.getUserID());
        assertSame(score, otherAccount.getScore());
        assertEquals(user2, otherAccount.getUsername());
    }
}
