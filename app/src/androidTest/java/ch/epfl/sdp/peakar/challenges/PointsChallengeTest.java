package ch.epfl.sdp.peakar.challenges;

import androidx.test.platform.app.InstrumentationRegistry;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.epfl.sdp.peakar.database.Database;
import ch.epfl.sdp.peakar.user.challenge.Challenge;
import ch.epfl.sdp.peakar.user.challenge.ChallengeOutcome;
import ch.epfl.sdp.peakar.user.challenge.goal.PointsChallenge;
import ch.epfl.sdp.peakar.user.services.AuthAccount;
import ch.epfl.sdp.peakar.user.services.AuthService;
import ch.epfl.sdp.peakar.user.services.providers.firebase.FirebaseAuthService;
import ch.epfl.sdp.peakar.user.services.providers.firebase.FirebasePointsChallenge;

import static ch.epfl.sdp.peakar.utils.TestingConstants.BASIC_USERNAME;
import static ch.epfl.sdp.peakar.user.AuthAccountTest.registerAuthUser;
import static ch.epfl.sdp.peakar.user.AuthAccountTest.removeAuthUser;
import static org.junit.Assert.*;

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
        Task<Void> task1 = Database.refRoot.child(Database.CHILD_USERS).child(AuthService.getInstance().getID()).removeValue();
        Task<Void> task2 = Database.refRoot.child(Database.CHILD_USERS).child(user2).removeValue();
        try {
            Tasks.await(task1);
            Tasks.await(task2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* Test that generating and updating a challenge works */
    @Test
    public void generateNewChallengeTest() {
        long goalPoints = 100;
        Challenge challenge = FirebasePointsChallenge.generateNewChallenge(user2, goalPoints);

        try {
            // Test initial awarded points are 0 and only user2 has joined it
            assertSame(0L, challenge.getPoints());
            assertSame(1, challenge.getUsers().size());
            assertSame(goalPoints, ((PointsChallenge)challenge).getGoalPoints());
            assertEquals(user2, challenge.getUsers().get(0));

            String challengeID = challenge.getID();

            // Make the mock user join the challenge
            challenge.join();

            // Check if the user has been put in correctly
            assertSame(Challenge.AWARDED_POINTS_PER_USER, challenge.getPoints());
            assertSame(2, challenge.getUsers().size());
            assertTrue(challenge.getUsers().contains(AuthService.getInstance().getID()));
            assertEquals(challengeID, AuthService.getInstance().getAuthAccount().getChallenges().get(0).getID());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Be sure to remove the challenge after the test is over.
            Task<Void> removeChallengeTask = Database.refRoot.child(Database.CHILD_CHALLENGES).child(challenge.getID()).removeValue();
            try {
                Tasks.await(removeChallengeTask);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void retrieveChallengeTest() {
        long goalPoints = 100;
        Challenge challenge = FirebasePointsChallenge.generateNewChallenge(AuthService.getInstance().getID(), goalPoints);
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
            assertSame(((PointsChallenge)challenge).getGoalPoints(), retrievedChallenge.getGoalPoints());
            assertEquals(challenge.getUsers().get(0), retrievedChallenge.getUsers().get(0));
            assertEquals(challengeID,retrievedChallenge.getID());

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Be sure to remove the challenge after the test is over.
            Task<Void> removeChallengeTask = Database.refRoot.child(Database.CHILD_CHALLENGES).child(challenge.getID()).removeValue();
            try {
                Tasks.await(removeChallengeTask);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /* Test that a successful claimVictory() works */
    @Test
    public void successfulClaimVictoryTest() {
        long goalPoints = 0;
        AuthAccount authAccount = AuthService.getInstance().getAuthAccount();
        Challenge challenge = FirebasePointsChallenge.generateNewChallenge(user2, goalPoints);

        try {
            assertSame(0, authAccount.getChallenges().size());

            // Make the mock user join the challenge
            challenge.join();

            assertSame(1, authAccount.getChallenges().size());

            // Claim victory
            assertSame(ChallengeOutcome.AWARDED, challenge.claimVictory());


            // Check effects
            assertSame(Challenge.AWARDED_POINTS_PER_USER, authAccount.getScore());
            assertSame(0, authAccount.getChallenges().size());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Be sure to remove the challenge after the test is over.
            Task<Void> removeChallengeTask = Database.refRoot.child(Database.CHILD_CHALLENGES).child(challenge.getID()).removeValue();
            try {
                Tasks.await(removeChallengeTask);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /* Test that if the requirements for a claim are not met, claimVictory() works properly */
    @Test
    public void missingRequirementsClaimVictoryTest() {
        long goalPoints = 100;
        AuthAccount authAccount = AuthService.getInstance().getAuthAccount();
        Challenge challenge = FirebasePointsChallenge.generateNewChallenge(user2, goalPoints);

        try {
            assertSame(0, authAccount.getChallenges().size());

            // Make the mock user join the challenge
            challenge.join();

            assertSame(1, authAccount.getChallenges().size());

            // Claim victory
            assertSame(ChallengeOutcome.MISSING_REQUIREMENTS, challenge.claimVictory());


            // Check effects
            assertSame(0L, authAccount.getScore());
            assertSame(1, authAccount.getChallenges().size());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Be sure to remove the challenge after the test is over.
            Task<Void> removeChallengeTask = Database.refRoot.child(Database.CHILD_CHALLENGES).child(challenge.getID()).removeValue();
            try {
                Tasks.await(removeChallengeTask);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /* Test that if the challenge was already claimed, claimVictory() works properly */
    @Test
    public void alreadyAwardedClaimVictoryTest() {
        long goalPoints = 0;
        AuthAccount authAccount = AuthService.getInstance().getAuthAccount();
        Challenge challenge = FirebasePointsChallenge.generateNewChallenge(user2, goalPoints);

        try {
            assertSame(0, authAccount.getChallenges().size());

            // Make the mock user join the challenge
            challenge.join();

            assertSame(1, authAccount.getChallenges().size());

            // Remove the challenge so it will look as already over
            Task<Void> removeChallengeTask = Database.refRoot.child(Database.CHILD_CHALLENGES).child(challenge.getID()).removeValue();
            try {
                Tasks.await(removeChallengeTask);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Claim victory
            assertSame(ChallengeOutcome.ALREADY_OVER, challenge.claimVictory());

            // Check effects
            assertSame(0L, authAccount.getScore());
            assertSame(0, authAccount.getChallenges().size());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Be sure to remove the challenge after the test is over.
            Task<Void> removeChallengeTask = Database.refRoot.child(Database.CHILD_CHALLENGES).child(challenge.getID()).removeValue();
            try {
                Tasks.await(removeChallengeTask);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
