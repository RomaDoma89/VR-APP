package com.application.vr.cardboard.models.interfaces;

public interface Model {
    void draw(float[] matrixViewProjection, float[] matrixView,
              float[] globalLightPosition, float[] globalLightColor);
}
