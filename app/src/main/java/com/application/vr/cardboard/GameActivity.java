package com.application.vr.cardboard;

import android.content.pm.PackageManager;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.ImageButton;

import com.application.vr.cardboard.app_mode.InputMode;
import com.application.vr.cardboard.app_mode.ScreenModeListener;
import com.application.vr.cardboard.render.SceneRenderer;
import com.google.vr.sdk.base.AndroidCompat;
import com.google.vr.sdk.base.GvrActivity;
import com.google.vr.sdk.base.GvrView;

public class GameActivity extends GvrActivity {

    private int INPUT_MODE = InputMode.ACCELEROM;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        //Prepare View
        GvrView gvrView = findViewById(R.id.gvr_view);
        gvrView.setEGLConfigChooser(8, 8, 8, 8, 16, 8);

        gvrView.setDistortionCorrectionEnabled(true);

        // Enable the low-latency
        gvrView.setAsyncReprojectionEnabled(true);

        AndroidCompat.setSustainedPerformanceMode(this, true);

        PackageManager packageManager = getPackageManager();
        boolean gyroExists = packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_GYROSCOPE);
        boolean acceExists = packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_ACCELEROMETER);

        if (acceExists) INPUT_MODE = InputMode.ACCELEROM;
        if (gyroExists) INPUT_MODE = InputMode.GYROSCOPE;

        //Prepare renderer
        SceneRenderer mRenderer = new SceneRenderer(getApplicationContext(), INPUT_MODE);
        gvrView.setRenderer(mRenderer);
        setGvrView(gvrView);

        ImageButton stereoSwitcher = findViewById(R.id.stereo_mono_switch);
        stereoSwitcher.setOnClickListener(new ScreenModeListener(gvrView));

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        Log.e("SIZE", size.x +" "+ size.y);
//        disabledDefaultButtons();
    }

    @Override
    public void onCardboardTrigger() {
//        disabledDefaultButtons();
        super.onCardboardTrigger();
    }

    private void disabledDefaultButtons() {
        // Configure button
        ImageButton settingsButton = this.getGvrView().findViewById(R.id.ui_settings_button);
        settingsButton.setVisibility(View.GONE);
    }
}