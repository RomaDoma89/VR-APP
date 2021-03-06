package com.application.vr.cardboard.control;

import android.media.AudioManager;
import android.media.ToneGenerator;
import android.util.Log;

import com.application.vr.cardboard.control.events.TopEvent;
import com.application.vr.cardboard.models.ui_models.UiScale;
import com.application.vr.cardboard.motion.AccelerometerListener;

import org.greenrobot.eventbus.EventBus;

public class TopController implements IController {
    private static final float NORMAL_VAL = 9.8f;
    private static final float BOUNDARY_VAL = 10.5f;
    private static final float MAXIMUM_VAL = 14f;

    private AccelerometerListener listener;
    private ToneGenerator toneG;
    private boolean isReadyToListening;
    private float scaleStep;

    public TopController(AccelerometerListener listener) {
        this.listener = listener;
        this.isReadyToListening = true;
        this.toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 30);
        this.scaleStep = (MAXIMUM_VAL - NORMAL_VAL)/UiScale.MAX_SCALE_VALUE;
    }

    @Override
    public void run() {
        while (isReadyToListening) {
            // Get some current values of the accelerometer.
            float x = listener.getX();
            float y = listener.getY();
            float z = listener.getZ();

            int scaleValue = 0;
            boolean isReached = false;
            if (x >= BOUNDARY_VAL && z < -0.5) {
                toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_INCALL_LITE, 200);
                scaleValue = UiScale.MAX_SCALE_VALUE;
                isReached = true;
            }
            if (x >= NORMAL_VAL) {
                scaleValue = (int) ((x - NORMAL_VAL)/scaleStep);
            }

            EventBus.getDefault().post(new TopEvent(x, y, z, scaleValue, isReached));

            // Makes listening more stable.
            synchronized(this) {
                try {
                    // The thread is waiting for the accelerometer data stabilization
                    // to avoid multiple fixations of a single nod.
                    if (isReached) wait(800);
                    // Reducing the amount of data read.
                    else wait(100);
                } catch (InterruptedException e) {
                    Log.e(this.getClass().getSimpleName()+"class", e.getMessage());
                }
            }
        }
    }

    @Override
    public void stopListening() {
        isReadyToListening = false;
    }
}
