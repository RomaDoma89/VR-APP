package com.application.vr.cardboard.control.events;

public class RightEvent {
    private boolean isReached;
    private String param;

    public RightEvent(String param, boolean isReached) {
        this.param = param;
        this.isReached = isReached;
    }
    public boolean isReached() {
        return isReached;
    }
    public String getParam() {
        return param;
    }
}
