package com.application.vr.cardboard;

import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;

import com.google.vr.sdk.base.GvrActivity;
import com.google.vr.sdk.base.GvrView;


public class MainActivity extends GvrActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.common_ui);

        SensorManager mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        Sensor accelerom = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor magnetic = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        //Prepare View
        GvrView gvrView = findViewById(R.id.gvr_view);
        gvrView.setEGLConfigChooser(8, 8, 8, 8, 16, 8);
        setGvrView(gvrView);

        //Prepare renderer
        SceneRenderer mRenderer = new SceneRenderer(mSensorManager, accelerom, magnetic);
        gvrView.setRenderer(mRenderer);
    }

    @Override
    public void onCardboardTrigger() {
        super.onCardboardTrigger();
    }
}
