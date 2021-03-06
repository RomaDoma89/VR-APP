package com.application.vr.cardboard;

import android.content.pm.PackageManager;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import com.application.vr.cardboard.app_mode.HeadMode;
import com.application.vr.cardboard.app_mode.HeadModeListener;
import com.application.vr.cardboard.app_mode.InputMode;
import com.application.vr.cardboard.app_mode.ScreenModeListener;
import com.application.vr.cardboard.control.events.DownEvent;
import com.application.vr.cardboard.control.events.HeadModeEvent;
import com.application.vr.cardboard.control.events.LeftEvent;
import com.application.vr.cardboard.control.events.RightEvent;
import com.application.vr.cardboard.control.events.TopEvent;
import com.application.vr.cardboard.render.SceneRenderer;
import com.google.vr.sdk.base.AndroidCompat;
import com.google.vr.sdk.base.GvrActivity;
import com.google.vr.sdk.base.GvrView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class GameActivity extends GvrActivity {

    private int INPUT_MODE = InputMode.ACCELEROM;
    private int HEAD_MODE = HeadMode.PITCH_ROLL;
    private SceneRenderer mRenderer;
    private TextView left, right;
    private TextView headSwitch;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        left = findViewById(R.id.leftText);
        right = findViewById(R.id.rightText);
        headSwitch = findViewById(R.id.head_switch);

        //Prepare View
        GvrView gvrView = findViewById(R.id.gvr_view);
        gvrView.setEGLConfigChooser(8, 8, 8, 8, 16, 8);
        gvrView.setDistortionCorrectionEnabled(true);
        // Enable the low-latency
        gvrView.setAsyncReprojectionEnabled(true);
        AndroidCompat.setSustainedPerformanceMode(this, true);

        PackageManager packageManager = getPackageManager();
        boolean acceExists = packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_ACCELEROMETER);
        if (acceExists) INPUT_MODE = InputMode.ACCELEROM;
        boolean gyroExists = packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_GYROSCOPE);
        if (gyroExists) {
            INPUT_MODE = InputMode.GYROSCOPE;
            headSwitch.setVisibility(View.VISIBLE);
            headSwitch.setOnClickListener(new HeadModeListener());
        }

        //Prepare renderer
        mRenderer = new SceneRenderer(getApplicationContext(), HEAD_MODE);
        gvrView.setRenderer(mRenderer);
        setGvrView(gvrView);

        ImageButton stereoSwitcher = findViewById(R.id.stereo_mono_switch);
        stereoSwitcher.setOnClickListener(new ScreenModeListener(gvrView));
        prepareTextViews();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(HeadModeEvent event) {
        if (INPUT_MODE == InputMode.GYROSCOPE) {
            if (event.getHeadMode() == HeadMode.PITCH_ROLL) {
                headSwitch.setText(this.getString(R.string.mode_pitch_roll));
                mRenderer.setHeadMode(HeadMode.PITCH_ROLL);
            } else if (event.getHeadMode() == HeadMode.FREE_HEAD) {
                headSwitch.setText(this.getString(R.string.mode_free_head));
                mRenderer.setHeadMode(HeadMode.FREE_HEAD);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(DownEvent event) {
        mRenderer.putDownEvent(event);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(TopEvent event) {
        mRenderer.putTopEvent(event);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(RightEvent event) {
        mRenderer.putRightEvent(event);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(LeftEvent event) {
        mRenderer.putLeftEvent(event);
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onCardboardTrigger() {
//        disabledDefaultButtons();
        super.onCardboardTrigger();
    }

    private void prepareTextViews() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        FrameLayout.LayoutParams leftP = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        leftP.gravity = Gravity.BOTTOM;
        leftP.setMargins(size.x/6, 0, 0, size.y/20);
        left.setLayoutParams(leftP);

        FrameLayout.LayoutParams rightP = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        rightP.gravity = Gravity.BOTTOM;
        rightP.setMargins(size.x/6*4, 0, 0, size.y/20);
        right.setLayoutParams(rightP);
    }

    private void disabledDefaultButtons() {
        // Configure button
        ImageButton settingsButton = this.getGvrView().findViewById(R.id.ui_settings_button);
        settingsButton.setVisibility(View.GONE);
    }
}