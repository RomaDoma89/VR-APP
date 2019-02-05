package com.application.vr.cardboard.app_mode;

import android.view.View;

import com.application.vr.cardboard.app_mode.AppMode;
import com.google.vr.sdk.base.GvrView;

public class ModeListener implements View.OnClickListener {
    private int defMode = AppMode.STEREO;
    private GvrView gvrView;
    public ModeListener(GvrView gvrView) {
        this.gvrView = gvrView;
    }
    @Override
    public void onClick(View v) {
        switch (defMode) {
            case AppMode.STEREO:
                gvrView.setStereoModeEnabled(false);
                defMode = AppMode.MONO;
                break;
            case AppMode.MONO:
                gvrView.setStereoModeEnabled(true);
                defMode = AppMode.STEREO;
        }
    }
}
