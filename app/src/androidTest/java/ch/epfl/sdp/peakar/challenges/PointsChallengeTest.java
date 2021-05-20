package ch.epfl.sdp.peakar.challenges;

import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import ch.epfl.sdp.peakar.database.Database;
import ch.epfl.sdp.peakar.user.challenge.Challenge;
import ch.epfl.sdp.peakar.user.challenge.ChallengeHandler;
import ch.epfl.sdp.peakar.user.challenge.goal.PointsChallenge;
import ch.epfl.sdp.peakar.user.challenge.goal.RemotePointsChallenge;
import ch.epfl.sdp.peakar.user.services.AuthAccount;
import ch.epfl.sdp.peakar.user.services.AuthService;
import ch.epfl.sdp.peakar.user.services.FirebaseAuthService;
import ch.epfl.sdp.peakar.utils.TestingConstants;

import static ch.epfl.sdp.peakar.user.AuthAccountTest.registerAuthUser;
import static ch.epfl.sdp.peakar.user.AuthAccountTest.removeAuthUser;
import static ch.epfl.sdp.peakar.utils.TestingConstants.BASIC_USERNAME;
import static ch.epfl.sdp.peakar.utils.TestingConstants.CHALLENGE_DURATION_DAYS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class PointsChallengeTest {
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
    public static void end() throws InterruptedException {
        removeTestUsers();
        removeAuthUser();
    }

    /* Make sure that an account is signed in and as new before each test */
    @Before
    public void createTestUser() {
        registerAuthUser();
        removeTestUsers();
        removeTestChallenges();
    }

    /* Make sure that mock users are not on the database after a test */
    public static void removeTestUsers() {
        Database.getInstance().getReference().child(Database.CHILD_USERS).child(AuthService.getInstance().getID()).removeValue();
        Database.getInstance().getReference().child(Database.CHILD_USERS).child(user2).removeValue();
    }

    /* Remove test challenges */
    public static void removeTestChallenges(){
        List<Challenge> challengeList = AuthService.getInstance().getAuthAccount().getChallenges();
        for(Challenge challenge : challengeList){
            Database.getInstance().getReference().child(Database.CHILD_CHALLENGES).child(challenge.getID()).removeValue();
            Database.getInstance().getReference().child(Database.CHILD_USERS)
                    .child(AuthService.getInstance().getID()).child(Database.CHILD_CHALLENGES)
                    .child(challenge.getID()).removeValue();
        }
    }


    /* Test that generating and updating a challenge works */
    @Test
    public void generateNewChallengeTest() {
        Challenge challenge = RemotePointsChallenge.generateNewChallenge(user2, CHALLENGE_DURATION_DAYS);

        try {
            // Test initial awarded points are 0 and only user2 has joined it
            assertSame(0L, challenge.getPoints());
            assertSame(1, challenge.getUsers().size());
            assertEquals(user2, challenge.getUsers().get(0));

            String challengeID = challenge.getID();

            // Make the mock user join the challenge
            challenge.join();

            // Check if the user has been put in correctly
            assertSame(2, challenge.getUsers().size());
            assertTrue(challenge.getUsers().contains(AuthService.getInstance().getID()));
            assertEquals(challengeID, AuthService.getInstance().getAuthAccount().getChallenges().get(0).getID());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Be sure to remove the challenge after the test is over.
            removeTestChallenges();
        }
    }

    @Test
    public void retrieveChallengeTest() {
        Challenge challenge = RemotePointsChallenge.generateNewChallenge(AuthService.getInstance().getID(), CHALLENGE_DURATION_DAYS);
        String challengeID = challenge.getID();

        // Make sure the challenge has been added correctly locally
        assertSame(1, AuthService.getInstance().getAuthAccount().getChallenges().size());

        try {
            // Force retrieve data
            FirebaseAuthService.getInstance().forceRetrieveData();
            Thread.sleep(5000);
            // Get retrieved challenge
            assertSame(1, AuthService.getInstance().getAuthAccount().getChallenges().size());
            PointsChallenge retrievedChallenge = (PointsChallenge)AuthService.getInstance().getAuthAccount().getChallenges().get(0);

            // Test that the challenge retrieved after a retrieveData is the same as the one generated
            assertSame(challenge.getPoints(), retrievedChallenge.getPoints());
            assertSame(challenge.getUsers().size(), retrievedChallenge.getUsers().size());
            assertEquals(challenge.getUsers().get(0), retrievedChallenge.getUsers().get(0));
            assertEquals(challengeID,retrievedChallenge.getID());

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Be sure to remove the challenge after the test is over.
            removeTestChallenges();
        }
    }

    /* Test that a challenge is ended successfuly */
    @Test
    public void checkFinishedTest() {
        AuthAccount authAccount = AuthService.getInstance().getAuthAccount();
        Challenge challenge = RemotePointsChallenge.generateNewChallenge(user2, 0);

        try {

            // Make the mock user join the challenge
            challenge.join();


            //Mock user score (set as winner)
            Database.getInstance().getReference().child(Database.CHILD_USERS).child(AuthService.getInstance().getID())
                    .child(Database.CHILD_SCORE).setValue(authAccount.getScore()+200);


            Thread.sleep(TestingConstants.THREAD_SLEEP_6S);
            //instantiate the challenge handler
            ChallengeHandler.getInstance();

            Thread.sleep(TestingConstants.THREAD_SLEEP_6S);
            int score = Database.getInstance().getReference().child(Database.CHILD_USERS).child(AuthService.getInstance().getID())
                    .child(Database.CHILD_SCORE).get().getValue(Integer.class);
            //Check that user score is previous user score +  Bonus of 50k
            assertEquals(50200,score);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Be sure to remove the challenge after the test is over.
            removeTestChallenges();
        }
    }

}
