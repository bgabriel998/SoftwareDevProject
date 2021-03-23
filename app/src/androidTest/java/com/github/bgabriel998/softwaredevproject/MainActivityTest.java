package com.github.bgabriel998.softwaredevproject;

import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.intent.matcher.IntentMatchers;
import androidx.test.espresso.matcher.RootMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;


import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {
    @Rule
    public ActivityScenarioRule<MainActivity> testRule = new ActivityScenarioRule<>(MainActivity.class);
    //@Rule
    //public GrantPermissionRule grantCameraPermissionRule = GrantPermissionRule.grant(Manifest.permission.CAMERA);
    @Rule
    public GrantPermissionRule grantLocation1PermissionRule = GrantPermissionRule.grant(Manifest.permission.ACCESS_FINE_LOCATION);
    @Rule
    public GrantPermissionRule grantLocation2PermissionRule = GrantPermissionRule.grant(Manifest.permission.ACCESS_COARSE_LOCATION);
    //@Rule
    //public ActivityScenarioRule<MainActivity> activityScenarioRule = new ActivityScenarioRule<>(new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class));


    @Before
    public void setup(){
        Intents.init();
    }

    @After
    public void cleanUp(){
        Intents.release();
    }

//    @Test
//    public void grantCameraPermission() {
//        launchActivity();
//        String PERMISSION_CAMERA = Manifest.permission.CAMERA;
//        verifyPermissionNotGranted(PERMISSION_CAMERA);
//        requestPermission(PERMISSION_CAMERA);
//        PermissionGranter.allowPermissionsIfNeeded(PERMISSION_CAMERA);
//        verifyPermissionGranted(PERMISSION_CAMERA);
//    }

    void requestPermission(String permission) {
        ActivityScenario<MainActivity> activityScenario = testRule.getScenario();
        activityScenario.onActivity(activity -> ActivityCompat.requestPermissions(activity, permission.split(" "), 1));
    }


    private void verifyPermissionNotGranted(String permission) {
        ActivityScenario<MainActivity> activityScenario = testRule.getScenario();
        activityScenario.onActivity(activity -> {
            int permissionValue = ContextCompat.checkSelfPermission(activity, permission);
            assertEquals("Permission" + permission + "expected to be denied but was granted;", PackageManager.PERMISSION_DENIED, permissionValue);
        });
    }

    private void verifyPermissionGranted(String permission) {
        ActivityScenario<MainActivity> activityScenario = testRule.getScenario();
        activityScenario.onActivity(activity ->{
                int permissionValue = ContextCompat.checkSelfPermission(activity, permission);
                assertEquals("Expected permission" + permission + "was not granted;", PackageManager.PERMISSION_GRANTED, permissionValue);
        });
    }

    private void launchActivity() {
        //ActivityScenario<MainActivity> activityScenario =
        ActivityScenario.launch(MainActivity.class);
    }


    @Test
    public void intentIsFiredWhenUserClicksOnButton1() {
        //Intents.init();
        //PermissionGranter.allowPermissionsIfNeeded(Manifest.permission.CAMERA);
        onView(withId(R.id.button1)).perform(click());
        Intents.intended(IntentMatchers.hasComponent(Button1Activity.class.getName()));
        //Intents.release();
    }

    @Test
    public void cameraPermissionRequested() throws NoSuchMethodException {

        Method method = MainActivity.class.getDeclaredMethod("requestCameraPermission");
        method.setAccessible(true);
        ActivityScenario<MainActivity> scenario = testRule.getScenario();
        scenario.onActivity(activity -> {
            try {
                method.invoke(activity);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        });
        onView(withText("Camera permission required!")).inRoot(RootMatchers.isDialog()).check(matches(isDisplayed()));
        //Button1 is postive button of alertDialog
        onView(withId(android.R.id.button1)).perform(click());


        }

    @Test
    public void cameraPermissionGrantedAfterRequest() throws NoSuchMethodException {
        Method method = MainActivity.class.getDeclaredMethod("onRequestPermissionsResult", int.class, String[].class, int[].class);
        method.setAccessible(true);
        ActivityScenario<MainActivity> scenario = testRule.getScenario();
        scenario.onActivity(activity -> {
            try {
                int CAMERA_REQUEST_CODE = 100;
                String[] cameraPermission = new String[]{Manifest.permission.CAMERA};
                int[] grantResults = new int[]{PackageManager.PERMISSION_GRANTED};
                method.invoke(activity, CAMERA_REQUEST_CODE, cameraPermission, grantResults);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        });
        Intents.intended(IntentMatchers.hasComponent(Button1Activity.class.getName()));
    }


    @Test
    public void intentIsFiredWhenUserClicksOnButton2() {
        //Intents.init();

        onView(withId(R.id.button2)).perform(click());
        Intents.intended(IntentMatchers.hasComponent(Button2Activity.class.getName()));
        //Intents.release();
    }

    @Test
    public void intentIsFiredWhenUserClicksOnButton3() {
        //Intents.init();

        onView(withId(R.id.button3)).perform(click());
        Intents.intended(IntentMatchers.hasComponent(Button3Activity.class.getName()));
        //Intents.release();
    }

    @Test
    public void intentIsFiredWhenUserClicksOnButton4() {
        //Intents.init();

        onView(withId(R.id.button4)).perform(click());
        Intents.intended(IntentMatchers.hasComponent(Button4Activity.class.getName()));
        //Intents.release();
    }

    @Test
    public void intentIsFiredWhenUserClicksOnButton5() {
        //Intents.init();

        onView(withId(R.id.button5)).perform(click());
        Intents.intended(IntentMatchers.hasComponent(Button5Activity.class.getName()));
        //Intents.release();
    }

    @Test
    public void intentIsFiredWhenUserClicksOnButton6() {
        //Intents.init();

        onView(withId(R.id.button6)).perform(click());
        Intents.intended(IntentMatchers.hasComponent(Button6Activity.class.getName()));
        //Intents.release();
    }

    @Test
    public void intentIsFiredWhenUserClicksOnButton7() {
        //Intents.init();

        onView(withId(R.id.button7)).perform(click());
        Intents.intended(IntentMatchers.hasComponent(Button7Activity.class.getName()));
        //Intents.release();
    }

    @Test
    public void intentIsFiredWhenUserClicksOnButton8() {
        //Intents.init();

        onView(withId(R.id.button8)).perform(click());
        Intents.intended(IntentMatchers.hasComponent(Button8Activity.class.getName()));
        //Intents.release();
    }
}

