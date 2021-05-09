package ch.epfl.sdp.peakar;

import android.Manifest;

import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class TestMenuBarActivityTest {
    @Rule
    public ActivityScenarioRule<TestMenuBarActivity> testRule = new ActivityScenarioRule<>(TestMenuBarActivity.class);

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
        MenuBarTestHelper.TestClickableIconButton(R.id.menu_bar_settings);
    }

    /* Test that menu bars gallery icon works as intended */
    @Test
    public void TestMenuBarGallery(){
        MenuBarTestHelper.TestClickableIconButton(R.id.menu_bar_gallery);
    }

    /* Test that menu bars camera icon works as intended */
    /*@Test
    public void TestMenuBarCamera(){
        MenuBarTestHelper.TestClickableIconButton(R.id.menu_bar_camera);
    }*/

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
}
