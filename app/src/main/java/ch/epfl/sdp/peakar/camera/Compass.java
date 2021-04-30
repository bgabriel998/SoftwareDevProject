package ch.epfl.sdp.peakar.camera;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import ch.epfl.sdp.peakar.utils.AngleLowpassFilter;

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

            //Rotates the rotation matrix to be expressed in a different coordinate system
            SensorManager.remapCoordinateSystem(rotMatFromVector, SensorManager.AXIS_X,
                    SensorManager.AXIS_Z, rotMat);

            //Compute the device orientation with the rotation matrix
            SensorManager.getOrientation(rotMat, orientationMat);

            //Apply low-pass filter to the event.values
            applyLowPassFilter(orientationMat);

            //Convert values to degrees
            convertArrToDegrees(orientationMat);

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
     * Convert all values of array to degrees
     * @param orientationMat Array of values that gets converted from radians to degrees
     */
    private void convertArrToDegrees(float[] orientationMat) {
        for(int i=0; i<orientationMat.length; i++){
            orientationMat[i] = (float)(Math.toDegrees(orientationMat[i]));
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
