package ch.epfl.sdp.peakar.camera;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.intent.matcher.IntentMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import ch.epfl.sdp.peakar.R;
import ch.epfl.sdp.peakar.database.Database;
import ch.epfl.sdp.peakar.points.ComputePOIPoints;
import ch.epfl.sdp.peakar.user.profile.ProfileActivity;
import ch.epfl.sdp.peakar.user.profile.ProfileLauncherActivity;
import ch.epfl.sdp.peakar.user.services.Account;
import ch.epfl.sdp.peakar.user.services.AuthService;
import ch.epfl.sdp.peakar.user.services.providers.firebase.FirebaseAuthService;

import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static ch.epfl.sdp.peakar.TestingConstants.BASIC_USERNAME;
import static ch.epfl.sdp.peakar.user.AccountTest.registerAuthUser;
import static ch.epfl.sdp.peakar.user.AccountTest.removeAuthUser;


@RunWith(AndroidJUnit4.class)
public class CameraActivityTest {

    @Rule
    public ActivityScenarioRule<CameraActivity> testRule = new ActivityScenarioRule<>(CameraActivity.class);

    /* Setup environment */
    @BeforeClass
    public static void computePOIPoints(){
        Context context = ApplicationProvider.getApplicationContext();
        new ComputePOIPoints(context);
    }


    /* Create Intent */
    @Before
    public void setup(){
        Intents.init();
    }

    /* Release Intent */
    @After
    public void cleanUp(){
        Intents.release();
    }

    /* Test that pressing the profile button when signed-out launches the ProfileLaunchActivity */
    @Test
    public void TestProfileButtonNotSignedIn(){
        /* Make sure no user is signed in before tests */
        AuthService.getInstance().signOut(InstrumentationRegistry.getInstrumentation().getTargetContext());
        removeAuthUser();
        ViewInteraction button = Espresso.onView(withId(R.id.profileButton));
        button.perform(ViewActions.click());
        intended(IntentMatchers.hasComponent(ProfileLauncherActivity.class.getName()));
    }

    /* Test that pressing the profile button when signed in launches the ProfileLaunchActivity */
    @Test
    public void TestProfileButtonSignedIn(){
        if(FirebaseAuth.getInstance().getCurrentUser() == null) {
            AuthService.getInstance().signOut(InstrumentationRegistry.getInstrumentation().getTargetContext());
            registerAuthUser();
        }
        else {
            FirebaseAuthService.getInstance().forceRetrieveData();
        }

        /* Make sure no user is signed in before tests */
        AuthService.getInstance().signOut(InstrumentationRegistry.getInstrumentation().getTargetContext());

        /* Create a new one */
        registerAuthUser();

        String user1 = (BASIC_USERNAME + AuthService.getInstance().getID()).substring(0, Account.NAME_MAX_LENGTH - 1);

        Task<Void> task1 = Database.refRoot.child(Database.CHILD_USERS).child(AuthService.getInstance().getID()).child(Database.CHILD_USERNAME).setValue(user1);
        try {
            Tasks.await(task1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        FirebaseAuthService.getInstance().forceRetrieveData();

        ViewInteraction button = Espresso.onView(withId(R.id.profileButton));
        button.perform(ViewActions.click());

        intended(IntentMatchers.hasComponent(ProfileActivity.class.getName()));

        task1 = Database.refRoot.child(Database.CHILD_USERS).child(AuthService.getInstance().getID()).removeValue();
        try {
            Tasks.await(task1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        task1 = Database.refRoot.child(Database.CHILD_USERS).child(AuthService.getInstance().getID()).removeValue();
        try {
            Tasks.await(task1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
