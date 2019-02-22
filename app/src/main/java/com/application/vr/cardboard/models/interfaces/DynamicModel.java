package com.application.vr.cardboard.models.interfaces;

import java.nio.FloatBuffer;

public interface DynamicModel extends Model {
    float[] getModelMatrix();
    FloatBuffer getModelVertex();
    float getTranslationX();
    float getTranslationY();
    float getTranslationZ();
}
