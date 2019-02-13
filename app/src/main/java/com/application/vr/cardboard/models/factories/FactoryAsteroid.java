package com.application.vr.cardboard.models.factories;

import android.content.Context;

import com.application.vr.cardboard.models.AsteroidStone;
import com.application.vr.cardboard.models.interfaces.CreatorModel;
import com.application.vr.cardboard.models.interfaces.Model;

public class FactoryAsteroid implements CreatorModel {
    @Override
    public Model create(Context context, float translationX, float translationY, float translationZ,
                        float rotationX, float rotationY, float rotationZ, float scale) {
        return new AsteroidStone(context, translationX, translationY, translationZ,
                rotationX, rotationY, rotationZ, scale);
    }
}
