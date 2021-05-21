package ch.epfl.sdp.peakar.camera;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.Surface;

import ch.epfl.sdp.peakar.utils.AngleLowpassFilter;
import ch.epfl.sdp.peakar.utils.CameraUtilities;

/**
 * Compass calculates the horizontal and vertical degree of the device
 */
public class Compass implements SensorEventListener {
    //Compass listener to update the compass heading
    private CompassListener compassListener;

    //SensorManager to access the sensors
    private final SensorManager sensorManager;

    //rotation Matrix
    private final float[] rotMat = new float[16];
    //orientation Matrix
    private final float[] orientationMat = new float[3];
    //Horizontal and vertical lowpass filters
    private final AngleLowpassFilter horizontalFilter = new AngleLowpassFilter();
    private final AngleLowpassFilter verticalFilter = new AngleLowpassFilter();

    private final Activity activity;
    private int axisX;
    private int axisZ;

    /**
     * Compass constructor, initializes the device sensors and registers the listener
     * @param context Context of the activity
     */
    public Compass(Context context) {
        //Initialize device sensors
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        //Initialize rotation vector
        Sensor rotation = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        //Register the listener for the rotation type vector
        sensorManager.registerListener(this, rotation, SensorManager.SENSOR_DELAY_GAME);

        activity = (Activity) context;
    }

    /**
     * Unregisters the sensor listener
     */
    public void stop() {
        sensorManager.unregisterListener(this);
    }

    /**
     * Sets the compass listener
     * @param listener CompassListener
     */
    public void setListener(CompassListener listener) {
        this.compassListener = listener;
    }

    /**
     * Handle sensor changes for the rotation type vector
     * TYPE_ROTATION_VECTOR combines the accelerometer, magnetometer and gyroscope
     * to get the best values and eliminates the gimbal lock
     * @param event contains the data that has changed
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        //Get the rotation vector data
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            //Matrix to cache the rotation vector
            float[] rotMatFromVector = new float[16];

            // Convert the rotation-vector to a 4x4 matrix.
            SensorManager.getRotationMatrixFromVector(rotMatFromVector, event.values);

            setAxisSensorManager();

            //Rotates the rotation matrix to be expressed in a different coordinate system
            SensorManager.remapCoordinateSystem(rotMatFromVector, axisX,
                    axisZ, rotMat);

            //Compute the device orientation with the rotation matrix
            SensorManager.getOrientation(rotMat, orientationMat);

            //Apply low-pass filter to the event.values
            applyLowPassFilter(orientationMat);

            //Convert values to degrees
            CameraUtilities.convertArrToDegrees(orientationMat);

            //Add 360° to only get positive values
            float headingHorizontal = (orientationMat[0] + 360) % 360;

            //Multiply by -1 to get increasing values when inclining the device
            //Add 90° to get values from 0° to 180°
            float headingVertical = orientationMat[1]*(-1) + 90;

            //Update the horizontal and vertical heading
            if (compassListener != null) {
                compassListener.onNewHeading(headingHorizontal, headingVertical);
            }
        }
    }

    /**
     * Sets the correct X and Z axis to remap the coordinates system
     */
    private void setAxisSensorManager(){
        int screenRotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        switch (screenRotation){
            case Surface.ROTATION_0:
                axisX = SensorManager.AXIS_X;
                axisZ = SensorManager.AXIS_Z;
                break;

            case Surface.ROTATION_90:
                axisX = SensorManager.AXIS_Z;
                axisZ = SensorManager.AXIS_MINUS_X;
                break;

            case Surface.ROTATION_180:
                axisX = SensorManager.AXIS_MINUS_X;
                axisZ = SensorManager.AXIS_MINUS_Z;
                break;

            case Surface.ROTATION_270:
                axisX = SensorManager.AXIS_MINUS_Z;
                axisZ = SensorManager.AXIS_X;
                break;
        }
    }

    /**
     * Adds the horizontal and vertical angle to their respective lowpass filter and updates
     * the given matrix
     * @param mat Matrix for which the lowpass filter gets applied
     */
    private void applyLowPassFilter(float[] mat){
        horizontalFilter.add(mat[0]);
        verticalFilter.add(mat[1]);

        mat[0] = horizontalFilter.average();
        mat[1] = verticalFilter.average();
    }

    /**
     * Not used
     * @param sensor Sensor
     * @param accuracy Accuracy of sensor
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
