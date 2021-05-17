package ch.epfl.sdp.peakar.social;

import android.view.View;

import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import ch.epfl.sdp.peakar.R;
import ch.epfl.sdp.peakar.utils.MenuBarTestHelper;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isNotChecked;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static ch.epfl.sdp.peakar.utils.UITestHelper.withBackgroundColor;

@RunWith(AndroidJUnit4.class)
public class SocialActivityTest {
    @Rule
    public ActivityScenarioRule<SocialActivity> testRule = new ActivityScenarioRule<>(SocialActivity.class);

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

    /* Test that the switch is visible and has the correct text. */
    @Test
    public void TestSwitchNames() {
        onView(ViewMatchers.withId(R.id.top_bar_switch)).check(matches(isDisplayed()));
        onView(ViewMatchers.withId(R.id.top_bar_switch_text_left)).check(matches(withText("All")));
        onView(ViewMatchers.withId(R.id.top_bar_switch_text_right)).check(matches(withText("Friends")));
    }

    /* Test that the switch is changes when clicking. */
    @Test
    public void TestSwitchToFriends() {
        onView(ViewMatchers.withId(R.id.top_bar_switch_button)).check(matches(isNotChecked()));
        onView(ViewMatchers.withId(R.id.top_bar_switch_button)).perform(click());
        onView(ViewMatchers.withId(R.id.top_bar_switch_button)).check(matches(isChecked()));
    }

}
