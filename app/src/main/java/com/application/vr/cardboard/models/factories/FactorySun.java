package com.application.vr.cardboard.models.factories;

import android.content.Context;

import com.application.vr.cardboard.models.Planet;
import com.application.vr.cardboard.models.interfaces.CreatorModel;
import com.application.vr.cardboard.models.interfaces.StaticModel;

public class FactorySun implements CreatorModel {
    @Override
    public StaticModel create(Context context, float translationX, float translationY, float translationZ,
                              float rotationX, float rotationY, float rotationZ, float scale) {
        return new Planet(context, Planet.Texture.SUN, translationX, translationY, translationZ,
                rotationX, rotationY, rotationZ, scale);
    }
}
