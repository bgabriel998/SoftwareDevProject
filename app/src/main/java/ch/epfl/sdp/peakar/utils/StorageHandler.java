package ch.epfl.sdp.peakar.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
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
    public static final String OFFLINE_CONTENT_FILE =  "offline_content.txt";

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
     * Helper method to load the downloaded json of the OfflineContentContainer.
     *
     * @return an OfflineContainer containing the downloaded content.
     */
     public static OfflineContentContainer readOfflineContentContainer(Context context) throws IOException {

        Gson gson = new Gson();

        String ret = "";

        InputStream inputStream =  context.openFileInput(OFFLINE_CONTENT_FILE);
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

    /**
     * Saves the OfflineContentContainer as a .txt file.
     *
     * @param saveObject  json to save.
     */
    public static void saveOfflineContentContainer(OfflineContentContainer saveObject, Context context) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonString = gson.toJson(saveObject);
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(OFFLINE_CONTENT_FILE, Context.MODE_PRIVATE));
            outputStreamWriter.write(jsonString);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }
}
