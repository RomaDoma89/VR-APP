package com.application.vr.cardboard.control.events;

public class TopEvent {
    private float x;
    private float y;
    private float z;
    private int scaleValue;
    private boolean isReached;

    public TopEvent(float x, float y, float z, int scaleValue, boolean isReached) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.scaleValue = scaleValue;
        this.isReached = isReached;
    }

    public int getValue() {
        return scaleValue;
    }
    public float[] getXYZ() {
        return new float[]{x, y, z};
    }
    public boolean isReached() {
        return isReached;
    }
}
