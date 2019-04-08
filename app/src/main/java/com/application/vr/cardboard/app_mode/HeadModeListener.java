package com.application.vr.cardboard.app_mode;

import android.view.View;
import com.application.vr.cardboard.control.events.HeadModeEvent;
import org.greenrobot.eventbus.EventBus;

public class HeadModeListener implements View.OnClickListener {
    private int HEAD_MODE;

    public HeadModeListener() {
        this.HEAD_MODE = HeadMode.PITCH_ROLL;
    }

    @Override
    public void onClick(View v) {
        if (HEAD_MODE == HeadMode.PITCH_ROLL) {
            HEAD_MODE = HeadMode.FREE_HEAD;
        } else if (HEAD_MODE == HeadMode.FREE_HEAD) {
            HEAD_MODE = HeadMode.PITCH_ROLL;
        }
        EventBus.getDefault().post(new HeadModeEvent(HEAD_MODE));
    }
}
