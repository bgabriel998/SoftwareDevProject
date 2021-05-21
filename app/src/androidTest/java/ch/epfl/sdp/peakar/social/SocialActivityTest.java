package ch.epfl.sdp.peakar.social;

import android.view.View;
import android.widget.ListView;

import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import ch.epfl.sdp.peakar.R;
import ch.epfl.sdp.peakar.database.Database;
import ch.epfl.sdp.peakar.database.DatabaseReference;
import ch.epfl.sdp.peakar.user.services.AuthAccount;
import ch.epfl.sdp.peakar.user.services.AuthService;
import ch.epfl.sdp.peakar.utils.MenuBarTestHelper;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static ch.epfl.sdp.peakar.user.AuthAccountTest.registerAuthUser;
import static ch.epfl.sdp.peakar.user.AuthAccountTest.removeAuthUser;
import static ch.epfl.sdp.peakar.utils.TestingConstants.BASIC_USERNAME;
import static ch.epfl.sdp.peakar.utils.TestingConstants.LONG_SLEEP_TIME;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isNotChecked;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static ch.epfl.sdp.peakar.utils.UITestHelper.withBackgroundColor;

@RunWith(AndroidJUnit4.class)
public class SocialActivityTest {

    /* Helper method that checks if the list of social items under a social list adapter is sorted by user score */
    private static Matcher<View> isSortedByUserScore() {
        return new TypeSafeMatcher<View>() {

            @Override
            protected boolean matchesSafely(View item) {
                // Retrieve the items
                ListView listView = (ListView)item;
                SocialListAdapter socialListAdapter = (SocialListAdapter)listView.getAdapter();
                List<SocialItem> socialItems = new ArrayList<>();
                int count = socialListAdapter.getCount();
                for(int i = 0; i < count; i++) {
                    socialItems.add(socialListAdapter.getItem(i));
                }
                // Sort the list
                List<SocialItem> sortedSocialItems = new ArrayList<>(socialItems);
                sortedSocialItems.sort(Comparator.comparing(SocialItem::getScore).reversed());
                // Check if list before sorting and list after sorting are equal
                return socialItems.equals(sortedSocialItems);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("Users are sorted by their score");
            }
        };
    }

    @Rule
    public ActivityScenarioRule<SocialActivity> testRule = new ActivityScenarioRule<>(SocialActivity.class);

    /* Create test user */
    @BeforeClass
    public static void init() {
        registerAuthUser();
        String user1 = (BASIC_USERNAME + AuthService.getInstance().getID()).substring(0, AuthAccount.NAME_MAX_LENGTH - 1);
        DatabaseReference dbRef = Database.getInstance().getReference().child(Database.CHILD_USERS).child(AuthService.getInstance().getID());
        dbRef.child(Database.CHILD_USERNAME).setValue(user1);
        dbRef.child(Database.CHILD_SCORE).setValue(0);
    }

    /* Remove test user */
    @AfterClass
    public static void end() {
        Database.getInstance().getReference().child(Database.CHILD_USERS).child(AuthService.getInstance().getID()).removeValue();
        removeAuthUser();
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

    /* Test that menu bars map icon works as intended */
    @Test
    public void TestMenuBarMap(){
        MenuBarTestHelper.TestClickableIconButton(R.id.menu_bar_map);
    }

    /* Test that menu bars social icon works as intended */
    @Test
    public void TestMenuBarSocial(){
        MenuBarTestHelper.TestSelectedIconButton(R.id.menu_bar_social);
    }

    /* Test that the top bar color is correct */
    @Test
    public void TestTopBarColor() {
        onView(ViewMatchers.withId(R.id.top_bar)).check(matches(withBackgroundColor(R.color.LightGrey)));
    }

    /* Test that the items in the global rankings are sorted correctly */
    @Test
    public void TestSortingInGlobalRankings() {
        try {
            Thread.sleep(LONG_SLEEP_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // Check that the list is sorted
        onView(ViewMatchers.withId(R.id.social_list)).check(matches(isSortedByUserScore()));
        // Change user score
        Database.getInstance().getReference().child(Database.CHILD_USERS).child(AuthService.getInstance().getID()).child(Database.CHILD_SCORE).setValue(10000000);
        try {
            Thread.sleep(LONG_SLEEP_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // Check that the list is still sorted
        onView(ViewMatchers.withId(R.id.social_list)).check(matches(isSortedByUserScore()));
    }

    /* Test that the switch is visible and has the correct text. */
    @Test
    public void TestSwitchNames() {
        onView(ViewMatchers.withId(R.id.top_bar_switch)).check(matches(isDisplayed()));
        onView(ViewMatchers.withId(R.id.top_bar_switch_text_left)).check(matches(withText(R.string.switch_all)));
        onView(ViewMatchers.withId(R.id.top_bar_switch_text_right)).check(matches(withText(R.string.switch_friends)));
    }

    /* Test that the switch is changed when clicking. */
    @Test
    public void TestSwitchToFriends() {
        onView(ViewMatchers.withId(R.id.top_bar_switch_button)).check(matches(isNotChecked()));
        onView(ViewMatchers.withId(R.id.top_bar_switch_button)).perform(click());
        onView(ViewMatchers.withId(R.id.top_bar_switch_button)).check(matches(isChecked()));
    }

}