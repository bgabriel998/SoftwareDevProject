package ch.epfl.sdp.peakar.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.util.SizeF;

import androidx.camera.core.AspectRatio;
import androidx.core.util.Pair;

/**
 * Utility class for the Camera Activity, contains utility methods for the camera-preview and compass
 */
public final class CameraUtilities {

    /**
     * Calculate the aspect ratio of the display in function of the width and height of the screen
     * @param width width of the preview in Pixels
     * @param height height of the preview in Pixels
     * @return Aspect ratio of the phone
     */
    public static int aspectRatio(int width, int height){
        double previewRatio = (double)Math.max(width, height)/Math.min(width, height);
        double RATIO_16_9_VALUE = 16.0 / 9.0;
        double RATIO_4_3_VALUE = 4.0 / 3.0;
        return (Math.abs(previewRatio - RATIO_4_3_VALUE) <= Math.abs(previewRatio - RATIO_16_9_VALUE))
                ? AspectRatio.RATIO_4_3 : AspectRatio.RATIO_16_9;
    }

    /**
     * Get the horizontal and vertical field of view of the back-facing amera
     * @return null if there is no camera, else the horizontal and vertical
     * field of view in degrees
     */
    public static Pair<Float, Float> getFieldOfView(Context context) throws CameraAccessException {
        //Create package manager to check if the device has a camera
        PackageManager pm = context.getPackageManager();
        return (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)) ? calculateFOV(context) : null;
    }

    /**
     * Calculates the horizontal and vertical field of view of the back-facing camera
     * @return Pair of the horizontal and vertical fov
     */
    private static Pair<Float, Float> calculateFOV(Context context) throws CameraAccessException {
        //Create camera manager
        CameraManager cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        double horizontalAngle = 0;
        double verticalAngle = 0;
        //Go through every camera to get the back-facing camera
        for (final String cameraId : cameraManager.getCameraIdList()) {
            //Check if camera is back-facing
            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
            int lensFacing = characteristics.get(CameraCharacteristics.LENS_FACING);
            if (lensFacing == CameraCharacteristics.LENS_FACING_BACK) {
                //If camera is back-facing, calculate the fov
                //Initialize horiontal and vertical fov
                //Get sizes of the lenses
                float focalLength = characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)[0];
                SizeF physicalSize = characteristics.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE);
                float width = physicalSize.getWidth();
                float height = physicalSize.getHeight();
                //Calculate the fovs
                horizontalAngle = 2 * Math.atan(width / (2 * focalLength));
                verticalAngle = 2 * Math.atan(height / (2 * focalLength));

            }
        }
        return new Pair<>((float) Math.toDegrees(horizontalAngle), (float) Math.toDegrees(verticalAngle));
    }

    /**
     * Used to get the string of the actual heading
     * @param degree degree to get the string
     * @return String for the degree
     */
    public static String selectHeadingString(int degree){
        switch (degree){
            case 0: case 360:
                return "N";
            case 90: case 450:
                return "E";
            case -180: case 180:
                return "S";
            case -90: case 270:
                return "W";
            case 45: case 405:
                return "NE";
            case -45: case 315:
                return "NW";
            case 135: case 495:
                return "SE";
            case -135: case 225:
                return "SW";
            default:
                return "";
        }
    }

    /**
     * Convert all values of array to degrees
     * @param orientationMat Array of values that gets converted from radians to degrees
     */
    public static void convertArrToDegrees(float[] orientationMat) {
        for(int i=0; i<orientationMat.length; i++){
            orientationMat[i] = (float)(Math.toDegrees(orientationMat[i]));
        }
    }

    /**
     * Combines two bitmaps into one
     *
     * @param base first bitmap
     * @param overlay second bitmap that is drawn on first bitmap
     * @return A bitmap that combine the two bitmaps
     */
    public static Bitmap combineBitmaps(Bitmap base, Bitmap overlay) {
        Bitmap bitmap = Bitmap.createBitmap(base.getWidth(), base.getHeight(), base.getConfig());
        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(base, new Matrix(), null);
        canvas.drawBitmap(overlay, new Matrix(), null);
        return bitmap;
    }
}