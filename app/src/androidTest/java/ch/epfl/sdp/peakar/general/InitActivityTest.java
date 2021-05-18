package ch.epfl.sdp.peakar.general;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.intent.matcher.IntentMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.intent.Intents.intended;

@RunWith(AndroidJUnit4.class)
public class InitActivityTest {

    /* Create Intent */
    @Before
    public void setup() {
        Intents.init();
    }

    /* Release Intent */
    @After
    public void cleanUp() {
        Intents.release();
    }

    /* Test that the MainMenuActivity gets called */
    @Test
    public void TestMainMenuActivityIsCalled() throws InterruptedException {
        //Launch InitActivity
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), InitActivity.class);
        ActivityScenario.launch(intent);
        Thread.sleep(10);
        //Check that MainMenuActivity gets called
        intended(IntentMatchers.hasComponent(MainMenuActivity.class.getName()));
    }
}