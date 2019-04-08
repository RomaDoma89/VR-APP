package com.application.vr.cardboard.motion;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class GyroscopeListener implements SensorEventListener {
    private float[] updatedValuesGYR = new float[3];

    public GyroscopeListener(SensorManager mSensorManage, Sensor gyroscope) {
        mSensorManage.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            updatedValuesGYR = event.values;
        }
    }

    public float getA() {
        return updatedValuesGYR[0];
    }
    public float getB() {
        return updatedValuesGYR[1];
    }
    public float getC() {
        return updatedValuesGYR[2];
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}
