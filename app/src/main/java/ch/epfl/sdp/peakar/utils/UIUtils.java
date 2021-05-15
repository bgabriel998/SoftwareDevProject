package ch.epfl.sdp.peakar.utils;

import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

/**
 * Class with only static utility functions for UI.
 */
public class UIUtils {

    private final static int INTEGER_CHUNK = 3;
    private final static String SPACE = " ";

    /**
     * Convert an given integer into a string follow the pattern of
     * 12 345 678
     * @param number Integer number to convert to string
     * @return String representing int.
     */
    public static String IntegerConvert(Integer number) {
        String numberString = number.toString();
        int length = numberString.length();

        StringBuilder builder = new StringBuilder();
        int counter = length % INTEGER_CHUNK;

        if (counter > 0) {
            builder.append(numberString.substring(0, counter));
            builder.append(SPACE);
        }

        while (counter < length) {
            builder.append(numberString.substring(counter, counter + INTEGER_CHUNK));
            builder.append(SPACE);
            counter += INTEGER_CHUNK;
        }
        return builder.toString();
    }

    /**
     * Convert an given long into a string follow the pattern of
     * 12 345 678
     * @param number Long number to convert to string
     * @return String representing int.
     */
    public static String IntegerConvert(Long number) {
        String numberString = number.toString();
        int length = numberString.length();

        StringBuilder builder = new StringBuilder();
        int counter = length % INTEGER_CHUNK;

        if (counter > 0) {
            builder.append(numberString.substring(0, counter));
            builder.append(SPACE);
        }

        while (counter < length) {
            builder.append(numberString.substring(counter, counter + INTEGER_CHUNK));
            builder.append(SPACE);
            counter += INTEGER_CHUNK;
        }
        return builder.toString();
    }

    /**
     * Sets the tint color on an svg.
     * @param v view with svg.
     * @param colorId color to set tint to.
     */
    public static void setTintColor(ImageView v, int colorId) {
        Drawable d = v.getDrawable();
        d.setColorFilter(
                new PorterDuffColorFilter(v.getResources().getColor(colorId, null),
                        PorterDuff.Mode.SRC_ATOP));
        v.setImageDrawable(d);
    }
}
