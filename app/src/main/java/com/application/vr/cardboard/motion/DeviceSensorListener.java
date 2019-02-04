package com.application.vr.cardboard.motion;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class DeviceSensorListener implements SensorEventListener {

    private MotionInterpolator interpolator;
    private float x;
    private float y;
    private float z;

    public DeviceSensorListener(SensorManager mSensorManage, Sensor accelerom, Sensor magnetic) {
        interpolator = new MotionInterpolator();
        mSensorManage.registerListener(this, accelerom, SensorManager.SENSOR_DELAY_GAME);
        mSensorManage.registerListener(this, magnetic, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float[] updatedValues = interpolator.interpolate(event.values);
            x = updatedValues[0];
            y = updatedValues[1];
            z = updatedValues[2];
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    public float getX() {
        return x;
    }
    public float getY() {
        return y;
    }
    public float getZ() {
        return z;
    }
}
