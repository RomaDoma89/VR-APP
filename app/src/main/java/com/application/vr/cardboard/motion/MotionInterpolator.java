package com.application.vr.cardboard.motion;

public class MotionInterpolator {

    private float [] accelerometer = {0.0f, 0.0f, 0.0f};

    public float[] interpolate(float[] values) {
        //Interpolation to avoid drifts
        accelerometer[0] = values[0] * 0.2f + accelerometer[0] * 0.8f;
        accelerometer[1] = values[1] * 0.2f + accelerometer[1] * 0.8f;
        accelerometer[2] = values[2] * 0.2f + accelerometer[2] * 0.8f;
        // Return new x, y, z values
        return new float[] {accelerometer[0], accelerometer[1], accelerometer[2]};
    }
}
