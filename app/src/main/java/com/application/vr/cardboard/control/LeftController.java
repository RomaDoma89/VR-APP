package com.application.vr.cardboard.control;

import android.media.AudioManager;
import android.media.ToneGenerator;
import android.util.Log;

import com.application.vr.cardboard.control.events.LeftEvent;
import com.application.vr.cardboard.motion.DeviceSensorListener;

import org.greenrobot.eventbus.EventBus;

public class LeftController implements Runnable {
    private static final float BOUNDARY_VAL = -5f;
    private static final float BOUNDARY_VAL_MAX = -6f;

    private DeviceSensorListener listener;
    private ToneGenerator toneG;
    private boolean isReadyToListening;

    public LeftController(DeviceSensorListener listener) {
        this.listener = listener;
        this.isReadyToListening = true;
        this.toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 30);
    }

    @Override
    public void run() {
        while (isReadyToListening) {
            // Get some current values of the accelerometer.
            float x = listener.getX();
            float y = listener.getY();
            float z = listener.getZ();

            boolean isReached = false;
            if (y <= BOUNDARY_VAL && y >= BOUNDARY_VAL_MAX) {
                if (x <= 9.8f) {
//                    toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_AUTOREDIAL_LITE, 200);
                    isReached = true;
                }
            }

            EventBus.getDefault().post(new LeftEvent(isReached));

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

    public void stopListening() {
        isReadyToListening = false;
    }
}
