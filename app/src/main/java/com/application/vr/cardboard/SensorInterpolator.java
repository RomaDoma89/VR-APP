package com.application.vr.cardboard;

public class SensorInterpolator {

    private float [] accelerometer = {0.0f, 0.0f, 0.0f};

    public float[] interpolate(float[] values) {
        //Interpolation to avoid drifts
        accelerometer[0] = values[0] * 0.1f + accelerometer[0] * 0.9f;
        accelerometer[1] = values[1] * 0.1f + accelerometer[1] * 0.9f;
        accelerometer[2] = values[2] * 0.1f + accelerometer[2] * 0.9f;
        // Return new x, y, z values
        return new float[] {accelerometer[0], accelerometer[1], accelerometer[2]};
    }
}
