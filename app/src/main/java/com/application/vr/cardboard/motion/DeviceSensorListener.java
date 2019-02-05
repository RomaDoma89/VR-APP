package com.application.vr.cardboard.motion;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class DeviceSensorListener implements SensorEventListener {

    private MotionInterpolator interpolator;
    private float[] updatedValues = new float[3];

    public DeviceSensorListener(SensorManager mSensorManage, Sensor accelerom, Sensor magnetic) {
        interpolator = new MotionInterpolator();
        mSensorManage.registerListener(this, accelerom, SensorManager.SENSOR_DELAY_GAME);
        mSensorManage.registerListener(this, magnetic, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            updatedValues = interpolator.interpolate(event.values);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    public float getX() {
        return updatedValues[0];
    }
    public float getY() {
        return updatedValues[1];
    }
    public float getZ() {
        return updatedValues[2];
    }
}
