package com.application.vr.cardboard.models.factories;

import android.content.Context;

import com.application.vr.cardboard.models.SpaceshipHunter;
import com.application.vr.cardboard.models.interfaces.CreatorModel;
import com.application.vr.cardboard.models.interfaces.Model;

public class FactorySpaceshipHunter implements CreatorModel {
    @Override
    public Model create(Context context, float translationX, float translationY, float translationZ,
                        float rotationX, float rotationY, float rotationZ, float scale) {
        return new SpaceshipHunter(context, translationX, translationY, translationZ, rotationX, rotationY, rotationZ, scale);
    }
}
