package com.application.vr.cardboard.control.events;

public class HeadModeEvent {
    private int HEAD_MODE;

    public HeadModeEvent(int HEAD_MODE) {
        this.HEAD_MODE = HEAD_MODE;
    }

    public int getHeadMode() {
        return HEAD_MODE;
    }
}
