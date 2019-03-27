package com.application.vr.cardboard.app_mode;

import android.view.View;

import com.google.vr.sdk.base.GvrView;

public class ScreenModeListener implements View.OnClickListener {
    private int defMode = ScreenMode.STEREO;
    private GvrView gvrView;
    public ScreenModeListener(GvrView gvrView) {
        this.gvrView = gvrView;
    }
    @Override
    public void onClick(View v) {
        switch (defMode) {
            case ScreenMode.STEREO:
                gvrView.setStereoModeEnabled(false);
                defMode = ScreenMode.MONO;
                break;
            case ScreenMode.MONO:
                gvrView.setStereoModeEnabled(true);
                defMode = ScreenMode.STEREO;
        }
    }
}
