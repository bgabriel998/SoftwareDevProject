package com.github.bgabriel998.softwaredevproject;

import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.test.espresso.matcher.BoundedMatcher;

import org.hamcrest.Description;
import org.hamcrest.Matcher;

/**
 * Class to help create matchers for espresso
 */
public class UITestHelper {

    /**
     * Matcher for textViews text color.
     * @param expectedColorId id for color
     * @return matcher
     */
    public static Matcher<View> withTextColor(final int expectedColorId) {
        return new BoundedMatcher<View, TextView>(TextView.class) {

            @Override
            protected boolean matchesSafely(TextView textView) {
                int color = ContextCompat.getColor(textView.getContext(), expectedColorId);
                return textView.getCurrentTextColor() == color;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("with text color: ");
                description.appendValue(expectedColorId);
            }
        };
    }

    public static Matcher<View> withBackgroundColor(final int expectedColorId) {
        return new BoundedMatcher<View, View>(View.class) {

            @Override
            protected boolean matchesSafely(View view) {
                int color = ContextCompat.getColor(view.getContext(), expectedColorId);
                return ((ColorDrawable) view.getBackground()).getColor() == color;
            }

            @Override
            public void describeTo(final Description description) {
                description.appendText("with background color: ");
                description.appendValue(expectedColorId);
            }
        };
    }
}
