package com.application.vr.cardboard.models.interfaces;

public interface Model {
    void prepareModel();
    void draw(float[] matrixViewProjection);
}
