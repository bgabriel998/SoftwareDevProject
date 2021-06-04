package ch.epfl.sdp.peakar.social;

import android.view.View;
import android.widget.ListView;

import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.intent.matcher.IntentMatchers;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

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
<<<<<<< HEAD:app/src/androidTest/java/ch/epfl/sdp/peakar/social/SocialFragmentTest.java
import ch.epfl.sdp.peakar.general.MainActivity;
import ch.epfl.sdp.peakar.user.profile.NewProfileActivity;
=======
import ch.epfl.sdp.peakar.user.profile.ProfileActivity;
>>>>>>> b0c68852dfefa9f6d64cb68a6f72f8e049fd1334:app/src/androidTest/java/ch/epfl/sdp/peakar/social/SocialActivityTest.java
import ch.epfl.sdp.peakar.user.services.AuthAccount;
import ch.epfl.sdp.peakar.user.services.AuthService;
import ch.epfl.sdp.peakar.user.services.FirebaseAuthService;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isNotChecked;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static ch.epfl.sdp.peakar.database.DatabaseTest.databaseRefRoot;
import static ch.epfl.sdp.peakar.utils.MainPagerAdapter.SOCIAL_FRAGMENT_INDEX;
import static ch.epfl.sdp.peakar.utils.UserTestHelper.registerAuthUser;
import static ch.epfl.sdp.peakar.utils.UserTestHelper.removeAuthUser;
import static ch.epfl.sdp.peakar.utils.TestingConstants.BASIC_USERNAME;
import static ch.epfl.sdp.peakar.utils.TestingConstants.LONG_SLEEP_TIME;
import static ch.epfl.sdp.peakar.utils.UITestHelper.withBackgroundColor;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.not;

@RunWith(AndroidJUnit4.class)
public class SocialFragmentTest {
    private static String user1;
    private static String user2;
    private static final int user2score = 100;

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

    /* Helper method that checks if the list of social items under a social list adapter contains a user with a certain nickname */
    private static Matcher<View> containsUserWithNickname(String username) {
        return new TypeSafeMatcher<View>() {

            @Override
            protected boolean matchesSafely(View item) {
                // Retrieve the items
                ListView listView = (ListView)item;
                SocialListAdapter socialListAdapter = (SocialListAdapter)listView.getAdapter();
                int count = socialListAdapter.getCount();
                for(int i = 0; i < count; i++) {
                    if(socialListAdapter.getItem(i).getUsername().equals(username)) return true;

                }
                // If no more items, return false
                return false;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("User is in the list.");
            }
        };
    }

    @Rule
    public ActivityScenarioRule<MainActivity> testRule = new ActivityScenarioRule<>(MainActivity.class);

    /* Create test user */
    @BeforeClass
    public static void init() {
        /* Make sure no user is signed in before tests */
        AuthService.getInstance().signOut(InstrumentationRegistry.getInstrumentation().getTargetContext());

        registerAuthUser();

        user2 = (BASIC_USERNAME + AuthService.getInstance().getID()).substring(0, AuthAccount.NAME_MAX_LENGTH - 2);
    }

    /* Remove test user */
    @AfterClass
    public static void end() {
        removeTestUsers();
        removeAuthUser();
    }

    /* Make sure that an account is signed in and as new before each test */
    @Before
    public void createTestUser() {
        testRule.getScenario().onActivity(activity -> activity.setCurrentPagerItem(SOCIAL_FRAGMENT_INDEX));
        registerAuthUser();
        removeTestUsers();
        addMockUsers();
        FirebaseAuthService.getInstance().forceRetrieveData();
    }

    /* Make sure that mock users are not on the database after a test */
    public static void removeTestUsers() {
        databaseRefRoot.child(Database.CHILD_USERS).child(AuthService.getInstance().getID()).removeValue();
        databaseRefRoot.child(Database.CHILD_USERS).child(user2).removeValue();
    }

    /* Add two mock users to the rankings */
    public void addMockUsers() {
        user1 = (BASIC_USERNAME + AuthService.getInstance().getID()).substring(0, AuthAccount.NAME_MAX_LENGTH - 1);
        DatabaseReference dbRef = Database.getInstance().getReference().child(Database.CHILD_USERS).child(AuthService.getInstance().getID());
        dbRef.child(Database.CHILD_USERNAME).setValue(user1);
        dbRef.child(Database.CHILD_SCORE).setValue(0);

        user2 = (BASIC_USERNAME + AuthService.getInstance().getID()).substring(0, AuthAccount.NAME_MAX_LENGTH - 2);
        DatabaseReference dbRef2 = Database.getInstance().getReference().child(Database.CHILD_USERS).child(user2);
        dbRef2.child(Database.CHILD_USERNAME).setValue(user2);
        dbRef2.child(Database.CHILD_SCORE).setValue(user2score);
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

<<<<<<< HEAD:app/src/androidTest/java/ch/epfl/sdp/peakar/social/SocialFragmentTest.java
=======

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

>>>>>>> b0c68852dfefa9f6d64cb68a6f72f8e049fd1334:app/src/androidTest/java/ch/epfl/sdp/peakar/social/SocialActivityTest.java
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

    /* Test that friends are correctly displayed */
    @Test
    public void friendsDisplayedTest() {
        removeAuthUser();
        registerAuthUser();
        testRule.getScenario().recreate();
        onView(ViewMatchers.withId(R.id.top_bar_switch_button)).perform(click());

        AuthService.getInstance().getAuthAccount().addFriend(user2);

        try {
            Thread.sleep(LONG_SLEEP_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Check that the friend is present
        onView(ViewMatchers.withId(R.id.social_list)).check(matches(containsUserWithNickname(user2)));
    }


    /* Test that if another user item is clicked, an intent to its profile is started */
    @Test
    public void clickOtherUserTest() {
        onView(withId(R.id.social_search_bar)).perform(replaceText(user2));
        try {
            Thread.sleep(LONG_SLEEP_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        onData(anything())
                .inAdapterView(allOf(withId(R.id.social_list), isCompletelyDisplayed()))
                .atPosition(0).perform(click());
        try {
            Thread.sleep(LONG_SLEEP_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // Capture the intent
        intended(allOf(IntentMatchers.hasComponent(ProfileActivity.class.getName()),
                IntentMatchers.hasExtra(ProfileActivity.AUTH_INTENT, false),
                IntentMatchers.hasExtra(ProfileActivity.OTHER_INTENT, user2)));
    }

    /* Test that if a auth user item is clicked, an intent to its profile is started */
    @Test
    public void clickAuthUserTest() {
        onView(withId(R.id.social_search_bar)).perform(replaceText(user1));
        try {
            Thread.sleep(LONG_SLEEP_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        onData(anything())
                .inAdapterView(allOf(withId(R.id.social_list), isCompletelyDisplayed()))
                .atPosition(0).perform(click());
        try {
            Thread.sleep(LONG_SLEEP_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // Capture the intent
        intended(allOf(IntentMatchers.hasComponent(ProfileActivity.class.getName()),
                IntentMatchers.hasExtra(ProfileActivity.AUTH_INTENT, true)));
    }

    /* Test the search bar works */
    @Test
    public void searchBarTest() {
        onView(withId(R.id.social_search_bar)).perform(replaceText(user1));
        try {
            Thread.sleep(LONG_SLEEP_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Check that the user is present
        onView(ViewMatchers.withId(R.id.social_list)).check(matches(containsUserWithNickname(user1)));

        // Now, type a user that cannot be in the list
        onView(withId(R.id.social_search_bar)).perform(replaceText("@"));
        try {
            Thread.sleep(LONG_SLEEP_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        onView(withId(R.id.social_list))
                .check(matches(isDisplayed()))
                .check(matches(not(hasDescendant(any(View.class)))));
    }
}
