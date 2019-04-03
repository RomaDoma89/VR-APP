package com.application.vr.cardboard.models.interfaces;

import org.jetbrains.annotations.Nullable;

public interface DynamicModel extends Model {
    void moveByCamera(@Nullable float[] forwardVector, float speed);
    float[] getPosition();
    float[] getMapColor();
}
