package com.application.vr.cardboard.motion;

public class MotionCalculator {

    private DeviceSensorListener sensorListener;

    public MotionCalculator(DeviceSensorListener sensorListener) {
        this.sensorListener = sensorListener;
    }

    public float getPitch() {
        float x = sensorListener.getX();
        float y = sensorListener.getY();
        float z = sensorListener.getZ();

        // Calculations based on new accelerometer data.
        float pitch = (float) Math.atan(x/Math.sqrt(Math.pow(y,2) + Math.pow(z,2)));
        float azim = (float) Math.atan(z/Math.sqrt(Math.pow(y,2) + Math.pow(x,2)));

        // Convert radians into degrees
        float pitchDegrees = (float)(Math.toDegrees(pitch));
        float azimDegrees = (float) (Math.toDegrees(azim));

        // Normalization of the rotation angle
        float xAngle = 0f;
        if (pitchDegrees < 80 && azimDegrees > 5) xAngle = (90 - pitchDegrees)/90;
        if (pitchDegrees < 80 && azimDegrees < -5) xAngle = -(90 - pitchDegrees)/90;

        return xAngle;
    }

    public float getRoll() {
        float x = sensorListener.getX();
        float y = sensorListener.getY();
        float z = sensorListener.getZ();

        // Calculations based on new accelerometer data.
        float roll = (float) Math.atan(y/Math.sqrt(Math.pow(x,2) + Math.pow(z,2)));

        // Convert radians into degrees
        float rollDegrees = (float) (Math.toDegrees(roll));

        // Normalization of the rotation angle
        float zAngle = 0f;
        if (rollDegrees < -5 || rollDegrees > 5) zAngle = (rollDegrees)/60;

        return zAngle;
    }
}
