package com.github.bgabriel998.softwaredevproject;

import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.Display;
import android.view.OrientationEventListener;
import android.view.WindowManager;

public class Compass implements SensorEventListener {

    private CompassListener listener;

    private final SensorManager sensorManager;

    private final Sensor accelerometer;
    private final Sensor magnetometer;
    private final Sensor rotation;

    OrientationEventListener orientationEventListener;
    private int orientation;

    //Query Constants
    private static final float ALPHA = 0.9f;

    //inclination Matrix
    float[] incMat = new float[9];
    //rotation Matrix
    float[] rotMat = new float[9];
    //Accelerometer Matrix
    private final float[] accMat = new float[3];
    //Magnetometer Matrix
    private final float[] magMat = new float[3];
    //Quaternion matrix
    private final float[] qMat = new float[4];



    /**
     * Compass constructor, initializes the device sensors and starts the compass
     * @param context Context of the activity
     */
    public Compass(Context context) {
        //Initialize device sensors
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        //Initialize accelerometer
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        //Initialize magnetometer
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        //Initialize rotation vector
        rotation = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        //Register the listeners for the compass and the orientation
        registerListeners(context);
    }

    /**
     * Register the accelerometer and magnetometer listeners
     */
    private void registerListeners(Context context){

        registerOrientationListener(context);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, rotation, SensorManager.SENSOR_DELAY_GAME);
    }

    private void registerOrientationListener(Context context){
        orientationEventListener = new OrientationEventListener(context)
        {
            @Override
            public void onOrientationChanged(int newOrientation){
                if (newOrientation <= 45) {
                    orientation = Configuration.ORIENTATION_PORTRAIT;
                } else if (newOrientation <= 135) {
                    orientation = Configuration.ORIENTATION_LANDSCAPE;
                } else if (newOrientation <= 225) {
                    orientation = Configuration.ORIENTATION_PORTRAIT;
                } else if (newOrientation <= 315) {
                    orientation = Configuration.ORIENTATION_LANDSCAPE;
                } else {
                    orientation = Configuration.ORIENTATION_PORTRAIT;
                }
            }
        };
        orientationEventListener.enable();
    }

    /**
     * Unregisters the sensor listener
     */
    public void stop() {
        orientationEventListener.disable();
        sensorManager.unregisterListener(this);
    }

    /**
     * Sets the compass listener
     * @param listener CompassListener
     */
    public void setListener(CompassListener listener) {
        this.listener = listener;
    }

    /**
     * Handle sensor changes for the accelerometer and magnetometer
     * @param event contains the data that has changed
     */
    @Override
    public void onSensorChanged(SensorEvent event) {

        //Get the accelerometer data, use filter to smooth the data
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            updateSensorValues(accMat, event);
        }

        //Get the accelerometer data, use filter to smooth the data
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            updateSensorValues(magMat, event);
        }

        //Get the rotation vecor data
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            updateSensorValues(qMat, event);
        }

        //See https://developer.android.com/reference/android/hardware/SensorManager#getRotationMatrix(float[],%20float[],%20float[],%20float[])
        boolean success = SensorManager.getRotationMatrix(incMat, rotMat, accMat, magMat);
        if (success) {
            //Update the horizontal and vertical heading
            if (listener != null) {
                listener.onNewHeading(updateHeadingHorizontal(incMat, qMat), updateHeadingVertical(incMat));
            }
        }
    }

    private float updateHeadingHorizontal(float[] inclinationMatrix, float[] quaternionMatrix){
        float[] orientation = new float[3];
        float[] q = new float[4];
        float heading;

        if(this.orientation == Configuration.ORIENTATION_LANDSCAPE){
            SensorManager.getOrientation(inclinationMatrix, orientation);
            heading = (float) Math.toDegrees(orientation[0]);
            //+360 to get only positive degrees
            //+90 to use the compass in landscape mode correctly
            heading = (heading + 360 + 90) % 360;
        }
        else{
            SensorManager.getQuaternionFromVector(q, quaternionMatrix);
            Quaternion quaternion = new Quaternion(q);
            EulerAngles eulerAngles = quaternion.toEulerAngles();
            heading = (float)Math.toDegrees(eulerAngles.yaw);
            heading = (heading * (-1) + 360) % 360;
        }
        return heading;
    }

    private float updateHeadingVertical(float[] inclinationMatrix){
        float[] orientation = new float[3];
        float heading;
        if(this.orientation == Configuration.ORIENTATION_LANDSCAPE){
            SensorManager.getOrientation(inclinationMatrix, orientation);
            heading = (float) Math.toDegrees(orientation[2]);
            //+360 to get only positive degrees
            //+90 to use the compass in landscape mode correctly
            heading = (heading * (-1));
        }
        else{
            heading = (float) Math.toDegrees(SensorManager.getInclination(inclinationMatrix));
            heading = (heading * (-1) + 360) % 360;
        }
        return heading;
    }

    /**
     * Update the values of the Matrices
     * @param mat Output matrix
     * @param event Input sensor event
     */
    private void updateSensorValues(float [] mat, SensorEvent event){
        for(int i=0; i<mat.length; i++)
            mat[i] = ALPHA * mat[i] + (1 - ALPHA) * event.values[i];
    }

    /**
     * Not used
     * @param sensor
     * @param accuracy
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
