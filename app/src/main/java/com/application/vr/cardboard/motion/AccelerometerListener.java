package com.application.vr.cardboard.motion;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class AccelerometerListener implements SensorEventListener {
    private float[] updatedValuesACC = new float[3];

    public AccelerometerListener(SensorManager mSensorManage, Sensor accelerom) {
        mSensorManage.registerListener(this, accelerom, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            updatedValuesACC = MotionInterpolator.interpolateXYZ(event.values);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    public float getX() {
        return updatedValuesACC[0];
    }
    public float getY() {
        return updatedValuesACC[1];
    }
    public float getZ() {
        return updatedValuesACC[2];
    }
}
