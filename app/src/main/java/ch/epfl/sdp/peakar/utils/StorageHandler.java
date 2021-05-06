package ch.epfl.sdp.peakar.utils;

import android.content.Context;
import android.graphics.Bitmap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Utility class to store and retrieve files
 */
public final class StorageHandler {

    private static final String FILENAME_PHOTO = "yyyy-MM-dd-HH-mm-ss-SSS";
    private static final String PHOTO_EXTENSION = ".jpg";

    /**
     * Stores the bitmap on the device.
     *
     * @param context context of the application
     * @param bitmap Bitmap that is to be stored
     * @throws IOException thrown when bitmap could not be stores
     */
    public static void storeBitmap(Context context, Bitmap bitmap) throws IOException {
        File screenshotFile = createPhotoFile(context);
        FileOutputStream outputStream = new FileOutputStream(screenshotFile);
        int quality = 100;
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
        outputStream.flush();
        outputStream.close();
    }

    /**
     * Retrieves all paths the images and sends back a list of them.
     * @return list of all image paths.
     */
    public static List<String> getImagePaths(Context context){
        List<String> imagePaths = new ArrayList<>();
        File[] imgFiles = StorageHandler.getOutputDirectoryMedia(context).listFiles();
        if (imgFiles != null) {
            for (File imgFile : imgFiles) {
                imagePaths.add(imgFile.getPath());
            }
        }
        imagePaths.sort(Collections.reverseOrder());
        return imagePaths;
    }

    /**
     * Creates a file which will be used to store the bitmaps
     *
     * @param context context of the application
     * @return File where the photo will be stored
     */
    public static File createPhotoFile(Context context) {
        return new File(getOutputDirectoryMedia(context),
                new SimpleDateFormat(FILENAME_PHOTO, Locale.ENGLISH)
                        .format(System.currentTimeMillis()) + PHOTO_EXTENSION);
    }

    /**
     * Returns outpudirectory to store images. Use externel media if it is available, our app's
     * file directory otherwise
     *
     * @return outputdirectory as a File
     */
    private static File getOutputDirectoryMedia(Context context) {
        Context appContext = context.getApplicationContext();
        File mediaDir;
        File[] mediaDirs = context.getExternalMediaDirs();
        mediaDir = mediaDirs != null ? mediaDirs[0] : null;
        return (mediaDir != null && mediaDir.exists()) ? mediaDir : appContext.getFilesDir();
    }
}
