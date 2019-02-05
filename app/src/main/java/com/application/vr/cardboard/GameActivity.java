package com.application.vr.cardboard;

import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import com.application.vr.cardboard.app_mode.ModeListener;
import com.application.vr.cardboard.render.SceneRenderer;
import com.google.vr.sdk.base.GvrActivity;
import com.google.vr.sdk.base.GvrView;


public class GameActivity extends GvrActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        SensorManager mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        Sensor accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor magnetic = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        //Prepare View
        GvrView gvrView = findViewById(R.id.gvr_view);
        gvrView.setEGLConfigChooser(8, 8, 8, 8, 16, 8);
        setGvrView(gvrView);
        disabledDefaultButtons(gvrView);

        ImageButton stereoSwitcher = findViewById(R.id.stereo_mono_switch);
        stereoSwitcher.setOnClickListener(new ModeListener(gvrView));

        //Prepare renderer
        SceneRenderer mRenderer = new SceneRenderer(getApplicationContext(), mSensorManager, accelerometer, magnetic);
        gvrView.setRenderer(mRenderer);
    }

    @Override
    public void onCardboardTrigger() {
        super.onCardboardTrigger();
    }

    private void disabledDefaultButtons(GvrView gvrView) {
        // Configure button
        ImageButton settingsButton = gvrView.findViewById(R.id.ui_settings_button);
        settingsButton.setVisibility(View.GONE);

//         Back Button
//        ImageButton backButton = gvrView.findViewById(R.id.ui_back_button);
//        backButton.setVisibility(View.GONE);
    }
}