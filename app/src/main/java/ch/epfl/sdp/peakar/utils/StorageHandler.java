package ch.epfl.sdp.peakar.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import ch.epfl.sdp.peakar.general.SettingsMapActivity;

/**
 * Utility class to store and retrieve files
 */
public final class StorageHandler {

    private static final String FILENAME_PHOTO = "yyyy-MM-dd-HH-mm-ss-SSS";
    private static final String PHOTO_EXTENSION = ".jpg";

    private static final int NO_COMPRESSION = 100;

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
        bitmap.compress(Bitmap.CompressFormat.JPEG, NO_COMPRESSION, outputStream);
        outputStream.flush();
        outputStream.close();
    }

    /**
     * Retrieves all paths the images and sends back a list of them.
     * @param context Context of the application
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
     * Returns outpudirectory to store images. Use external media if it is available, our app's
     * file directory otherwise
     *
     * @return outputdirectory as a File
     */
    public static File getOutputDirectoryMedia(Context context) {
        Context appContext = context.getApplicationContext();
        File mediaDir;
        File[] mediaDirs = context.getExternalMediaDirs();
        mediaDir = mediaDirs != null ? mediaDirs[0] : null;
        return (mediaDir != null && mediaDir.exists()) ? mediaDir : appContext.getFilesDir();
    }

    /**
     * Helper method to load the downloaded json.
     *
     * @return an OfflineContainer containing the downloaded content.
     */
     public static OfflineContentContainer readDownloadedPOIs(Context context) throws IOException {

        Gson gson = new Gson();

        String ret = "";

        InputStream inputStream =  context.openFileInput(SettingsMapActivity.OFFLINE_CONTENT_FILE);
        if ( inputStream != null ) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String receiveString;
            StringBuilder stringBuilder = new StringBuilder();
            while ( (receiveString = bufferedReader.readLine()) != null ) {
                stringBuilder.append(receiveString);
            }
            inputStream.close();
            ret = stringBuilder.toString();
        }

        Log.d("computePOIPointsInstance", "Offline content downloaded");
        return gson.fromJson(ret, OfflineContentContainer.class);
    }
}
