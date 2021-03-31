package com.github.bgabriel998.softwaredevproject;

public class Quaternion {
    private final double w;
    private final double x;
    private final double y;
    private final double z;

    public Quaternion(float[] quaternionVector){
        this.w = quaternionVector[0];
        this.x = quaternionVector[1];
        this.y = quaternionVector[2];
        this.z = quaternionVector[3];
    }

    EulerAngles toEulerAngles(){
        EulerAngles eulerAngles = new EulerAngles();
        // roll (x-axis rotation)
        double sinrCosp = 2 * (w * x + y * z);
        double cosrCosp = 1 - 2 * (x * x + y * y);
        eulerAngles.roll = (float) Math.atan2(sinrCosp, cosrCosp);

        // pitch (y-axis rotation)
        double sinp = 2 * (w * x - y * z);
        if (Math.abs(sinp) >= 1)
            // use 90 degrees if out of range
            eulerAngles.pitch = (float) Math.copySign(Math.PI / 2, sinp);
        else
            eulerAngles.pitch = (float) Math.asin(sinp);

        // yaw (z-axis rotation)
        double sinyCosp = 2 * (w * z + x * y);
        double cosyCosp = 1 - 2 * (y * y + z * z);
        eulerAngles.yaw = (float) Math.atan2(sinyCosp, cosyCosp);
        
        return eulerAngles;
    }
}
