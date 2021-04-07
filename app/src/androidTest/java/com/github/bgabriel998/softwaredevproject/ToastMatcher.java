package com.github.bgabriel998.softwaredevproject;

import android.os.IBinder;
import android.view.WindowManager;

import androidx.test.espresso.Root;
import androidx.test.espresso.ViewInteraction;
import androidx.test.filters.Suppress;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.ViewMatchers.withText;


public class ToastMatcher extends TypeSafeMatcher<Root> {
    private static int DEFAULT_MAX_FAILURES = 100;
    private int failures = 0;

    @Override
    public void describeTo(Description description) {
        description.appendText("is toast");
    }

    @Override
    public boolean matchesSafely(Root root) {
        int type = root.getWindowLayoutParams().get().type;
        if (type == WindowManager.LayoutParams.TYPE_TOAST || type == WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY) {
            IBinder windowToken = root.getDecorView().getWindowToken();
            IBinder appToken = root.getDecorView().getApplicationWindowToken();
            if (windowToken == appToken) {
                // windowToken == appToken means this window isn't contained by any other windows.
                // if it was a window for an activity, it would have TYPE_BASE_APPLICATION.
                return true;
            }
        }
        //return (++failures >= DEFAULT_MAX_FAILURES);
        return false;
    }

//    public static Matcher<Root> isToast() {
//        return new ToastMatcher();
//    }

//    public static ViewInteraction onToast(String text){
//        return onView(withText(text)).inRoot(isToast());
//    }
}