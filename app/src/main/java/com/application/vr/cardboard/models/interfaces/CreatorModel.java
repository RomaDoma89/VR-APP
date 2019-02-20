package com.application.vr.cardboard.models.interfaces;

import android.content.Context;

public interface CreatorModel {
    Model create(Context context, float translationX, float translationY, float translationZ,
                        float rotationX, float rotationY, float rotationZ, float scale);
}
