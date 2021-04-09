package com.github.bgabriel998.softwaredevproject;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
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

    /**
     * Matcher for views to test background color.
     * @param expectedColorId id for color
     * @return matcher
     */
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

    /**
     * Matcher for image views to test drawable image.
     * @param expectedImageId id for image
     * @return matcher
     */
    public static Matcher<View> withDrawable(final int expectedImageId) {
        return new BoundedMatcher<View, ImageView>(ImageView.class) {

            @Override
            protected boolean matchesSafely(ImageView view) {
                Drawable image = ContextCompat.getDrawable(view.getContext(), expectedImageId);
                Bitmap expectedBitmap = getBitmap(image);
                return expectedBitmap.sameAs(getBitmap(view.getDrawable()));
            }

            @Override
            public void describeTo(final Description description) {
                description.appendText("with image id: ");
                description.appendValue(expectedImageId);
            }
        };
    }

    /**
     * Gets the Bitmap of a drawable, by creating it.
     * @param drawable the drawable to create the bitmap off
     * @return the Bitmap
     */
    private static Bitmap getBitmap(Drawable drawable){
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }
}
