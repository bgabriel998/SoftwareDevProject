package ch.epfl.sdp.peakar.general;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.intent.matcher.IntentMatchers;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import ch.epfl.sdp.peakar.R;
import ch.epfl.sdp.peakar.database.Database;
import ch.epfl.sdp.peakar.user.profile.ProfileActivity;
import ch.epfl.sdp.peakar.user.profile.ProfileLauncherActivity;
import ch.epfl.sdp.peakar.user.services.AuthService;
import ch.epfl.sdp.peakar.utils.MenuBarTestHelperFragments;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static ch.epfl.sdp.peakar.utils.MainPagerAdapter.SETTINGS_FRAGMENT_INDEX;
import static ch.epfl.sdp.peakar.utils.UserTestHelper.registerAuthUser;
import static ch.epfl.sdp.peakar.utils.UserTestHelper.removeAuthUser;
import static org.junit.Assert.assertEquals;


@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    @Rule
    public ActivityScenarioRule<MainActivity> testRule = new ActivityScenarioRule<>(MainActivity.class);

    /* Setup environment */
    @BeforeClass
    public static void computePOIPoints(){
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
    @Test
    public void TestMenuBarSettings(){
        int id = MenuBarTestHelperFragments.TestClickableIconButton(R.id.menu_bar_settings);
        assertEquals(-1, id);
        testRule.getScenario().onActivity(activity -> TestCase.assertEquals(SETTINGS_FRAGMENT_INDEX, activity.getCurrentPagerItem()));
    }

    /* Test that menu bars gallery icon works as intended */
    @Test
    public void TestMenuBarGallery(){
        int id = MenuBarTestHelperFragments.TestClickableIconButton(R.id.menu_bar_gallery);
    }

    /* Test that menu bars gallery icon works as intended */
    @Test
    public void TestMenuBarCamera(){
        MenuBarTestHelperFragments.TestSelectedIconButton(R.id.menu_bar_camera);
    }

    /* Test that menu bars map icon works as intended */
    @Test
    public void TestMenuBarMap(){
        int id = MenuBarTestHelperFragments.TestClickableIconButton(R.id.menu_bar_map);
        onView(ViewMatchers.withId(id)).check(matches(isCompletelyDisplayed()));
    }

    /* Test that menu bars social icon works as intended */
    @Test
    public void TestMenuBarSocial(){
        int id = MenuBarTestHelperFragments.TestClickableIconButton(R.id.menu_bar_social);
        onView(ViewMatchers.withId(id)).check(matches(isCompletelyDisplayed()));
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

        intended(IntentMatchers.hasComponent(ProfileActivity.class.getName()));
    }

    public void createTestUser() {
        registerAuthUser();
        removeTestUsers();
    }
}
