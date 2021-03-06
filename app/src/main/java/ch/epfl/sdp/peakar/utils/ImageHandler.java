package ch.epfl.sdp.peakar.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import androidx.exifinterface.media.ExifInterface;

import java.io.IOException;

/**
 * Utility class to retrieve pictures in their correct orientation
 */
public final class ImageHandler {

    private static final int ROTATE_DEGREES = 90;

    /**
     * Get an image bitmap from a image path, where the image is the correct rotation
     * @param imagePath path to image
     * @return bitmap correctly rotated.
     */
    public static Bitmap getBitmapUpwards(String imagePath) {
        int orientation = getOrientation(imagePath);
        Bitmap imageBitmap = BitmapFactory.decodeFile(imagePath);
        if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
            imageBitmap = rotateBitmap(imageBitmap);
        }
        return imageBitmap;
    }

    /**
     * Returns the orientation of an image in on an image path
     * @param imagePath the path to image
     * @return the orientation
     */
    private static int getOrientation(String imagePath) {
        try {
            ExifInterface exifInterface = new ExifInterface(imagePath);
            return exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
        } catch (IOException e) {
            e.printStackTrace();
            return ExifInterface.ORIENTATION_NORMAL;
        }
    }

    /**
     * Rotate an image 90 degrees.
     * @param imageBitmap the image to rotate
     * @return the same image rotated 90 degrees
     */
    public static Bitmap rotateBitmap(Bitmap imageBitmap) {
        Matrix matrix = new Matrix();
        matrix.postRotate(ROTATE_DEGREES);
        return Bitmap.createBitmap(imageBitmap, 0, 0,
                imageBitmap.getWidth(), imageBitmap.getHeight(), matrix, true);
    }
}
