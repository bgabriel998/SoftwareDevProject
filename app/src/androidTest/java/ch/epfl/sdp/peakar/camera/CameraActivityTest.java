package ch.epfl.sdp.peakar.camera;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.intent.matcher.IntentMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import ch.epfl.sdp.peakar.R;
import ch.epfl.sdp.peakar.database.Database;
import ch.epfl.sdp.peakar.points.ComputePOIPoints;
import ch.epfl.sdp.peakar.user.profile.NewProfileActivity;
import ch.epfl.sdp.peakar.user.profile.ProfileLauncherActivity;
import ch.epfl.sdp.peakar.user.services.AuthService;
import ch.epfl.sdp.peakar.utils.MenuBarTestHelper;

import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static ch.epfl.sdp.peakar.utils.TestingConstants.SHORT_SLEEP_TIME;
import static ch.epfl.sdp.peakar.user.AuthAccountTest.registerAuthUser;
import static ch.epfl.sdp.peakar.user.AuthAccountTest.removeAuthUser;
import static org.junit.Assert.assertTrue;


@RunWith(AndroidJUnit4.class)
public class CameraActivityTest {

    @Rule
    public ActivityScenarioRule<CameraActivity> testRule = new ActivityScenarioRule<>(CameraActivity.class);

    private static SharedPreferences sharedPreferences;
    private static Context context;

    /* Setup environment */
    @BeforeClass
    public static void computePOIPoints(){
        context = ApplicationProvider.getApplicationContext();
        ComputePOIPoints.getInstance(context);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear().commit();

        /* Make sure no user is signed in before tests */
        AuthService.getInstance().signOut(InstrumentationRegistry.getInstrumentation().getTargetContext());
        /* Create a new one */
        registerAuthUser();
    }

    /* Clean environment */
    @AfterClass
    public static void end() {
        removeTestUsers();
        removeAuthUser();
    }

    private static void removeTestUsers() {
        Database.getInstance().getReference().child(Database.CHILD_USERS).child(AuthService.getInstance().getID()).removeValue();
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

    /* Test that menu bars settings icon works as intended */
// TODO Fix test.
    //@Test
    public void TestMenuBarSettings(){
        MenuBarTestHelper.TestClickableIconButton(R.id.menu_bar_settings);
    }

    /* Test that menu bars gallery icon works as intended */
    @Test
    public void TestMenuBarGallery(){
        MenuBarTestHelper.TestClickableIconButton(R.id.menu_bar_gallery);
    }

    /* Test that menu bars gallery icon works as intended */
    @Test
    public void TestMenuBarCamera(){
        MenuBarTestHelper.TestSelectedIconButton(R.id.menu_bar_camera);
    }

    /* Test that menu bars map icon works as intended */
    @Test
    public void TestMenuBarMap(){
        MenuBarTestHelper.TestClickableIconButton(R.id.menu_bar_map);
    }

    /* Test that menu bars social icon works as intended */
    @Test
    public void TestMenuBarSocial(){
        MenuBarTestHelper.TestClickableIconButton(R.id.menu_bar_social);
    }

    /* Test that the compass is changed when clicking on the compass button */
    @Test
    public void TestChangeCompassButton() throws InterruptedException {
        String displayCompassKey = context.getResources().getString(R.string.displayCompass_key);
        sharedPreferences.edit().putBoolean(displayCompassKey, false).apply();

        ViewInteraction button = Espresso.onView(withId(R.id.compassMiniature));
        button.perform(ViewActions.click());

        Thread.sleep(SHORT_SLEEP_TIME);

        boolean displayCompass = sharedPreferences.getBoolean(displayCompassKey, false);
        assertTrue(displayCompass);
    }

    /* Test that pressing the profile button when signed-out launches the ProfileLaunchActivity */
    @Test
    public void TestProfileButtonNotSignedIn(){
        ViewInteraction button = Espresso.onView(withId(R.id.top_bar_profile_button));
        button.perform(ViewActions.click());
        intended(IntentMatchers.hasComponent(ProfileLauncherActivity.class.getName()));
    }

    /* Test that pressing the profile button when signed in launches the ProfileLaunchActivity */
    @Test
    public void TestProfileButtonSignedIn(){
        createTestUser();

        ViewInteraction button = Espresso.onView(withId(R.id.top_bar_profile_button));
        button.perform(ViewActions.click());

        intended(IntentMatchers.hasComponent(NewProfileActivity.class.getName()));
    }

    public void createTestUser() {
        registerAuthUser();
        removeTestUsers();
    }
}
