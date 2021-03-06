package ch.epfl.sdp.peakar.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.action.GeneralLocation;
import androidx.test.espresso.action.GeneralSwipeAction;
import androidx.test.espresso.action.Press;
import androidx.test.espresso.action.Swipe;
import androidx.test.espresso.matcher.BoundedMatcher;

import ch.epfl.sdp.peakar.R;
import ch.epfl.sdp.peakar.gallery.ZoomableImageView;

import org.hamcrest.Description;
import org.hamcrest.Matcher;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static org.junit.Assert.fail;

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
     * Matcher for image views to test image paths.
     * @param expectedImagePath path for image
     * @return matcher
     */
    public static Matcher<View> withImagePath(final String expectedImagePath) {
        return new BoundedMatcher<View, ImageView>(ImageView.class) {

            @Override
            protected boolean matchesSafely(ImageView view) {
                Bitmap expectedBitmap = BitmapFactory.decodeFile(expectedImagePath);
                return expectedBitmap.sameAs(((BitmapDrawable)view.getDrawable()).getBitmap());
            }

            @Override
            public void describeTo(final Description description) {
                description.appendText("with image id: ");
                description.appendValue(expectedImagePath);
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
     * Matcher for ZoomableImageView, test if current scale is equal to given value
     * @param expectedScale the expected scale value
     * @return true if current scale is expected value
     */
    public static Matcher<View> withScaleEqualTo(final float expectedScale) {
        return new BoundedMatcher<View, ZoomableImageView>(ZoomableImageView.class) {
            @Override
            protected boolean matchesSafely(ZoomableImageView view) {
                return view.getCurrentScale() <= expectedScale;
            }

            @Override
            public void describeTo(final Description description) {
                description.appendText("zoom views scale is equal to: ");
                description.appendValue(expectedScale);
            }
        };
    }

    /**
     * Matcher for ZoomableImageView, test if current scale is greater than given value
     * @param expectedScale the expected scale value
     * @return true if current scale is greater then expected value
     */
    public static Matcher<View> withScaleGreaterThan(final float expectedScale) {
        return new BoundedMatcher<View, ZoomableImageView>(ZoomableImageView.class) {
            @Override
            protected boolean matchesSafely(ZoomableImageView view) {
                return view.getCurrentScale() > expectedScale;
            }

            @Override
            public void describeTo(final Description description) {
                description.appendText("zoom views scale is not greater than: ");
                description.appendValue(expectedScale);
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

    /**
     * Create a file with the temp_diablerets as image, given a filename
     * @param filename the filename
     */
    public static void AddImageFile(String filename) {
        try {
            Drawable image = ContextCompat.getDrawable(ApplicationProvider.getApplicationContext(),
                    R.drawable.temp_diablerets);
            assert image != null;
            Bitmap bitmap = getBitmap(image);

            File imageFile = new File(StorageHandler.getOutputDirectoryMedia(ApplicationProvider.getApplicationContext()),
                    filename);
            FileOutputStream outputStream = new FileOutputStream(imageFile);
            int quality = 100;
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
            outputStream.flush();
            outputStream.close();
        }
        catch (IOException e) {
            e.printStackTrace();
            fail("Could not create image file");
        }
    }

    /**
     * Removes all files in gallery
     */
    public static void ClearGallery(){
        File[] imgFiles = StorageHandler.getOutputDirectoryMedia(ApplicationProvider.getApplicationContext()).listFiles();
        if (imgFiles != null) {
            for (File imgFile : imgFiles) {
                imgFile.delete();
            }
        }
    }

    /**
     * Gets the stored files
     * @return A array of files
     */
    public static File[] getGalleryFiles(){
        return StorageHandler.getOutputDirectoryMedia(ApplicationProvider.getApplicationContext()).listFiles();
    }

    /**
     * Swipes from right to left
     * @return viewAction
     */
    public static ViewAction rightToLeftSwipe() {
        return new GeneralSwipeAction(Swipe.FAST, GeneralLocation.CENTER_RIGHT,
                GeneralLocation.CENTER_LEFT, Press.FINGER);
    }

    /**
     * Swipes from left to right
     * @return viewAction
     */
    public static ViewAction leftToRightSwipe() {
        return new GeneralSwipeAction(Swipe.FAST, GeneralLocation.CENTER_LEFT,
                GeneralLocation.CENTER_RIGHT, Press.FINGER);
    }
}
