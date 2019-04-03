package com.application.vr.cardboard.control.events;

public class LeftEvent {
    private boolean isReached;

    public LeftEvent(boolean isReached) {
        this.isReached = isReached;
    }
    public boolean isReached() {
        return isReached;
    }
}
