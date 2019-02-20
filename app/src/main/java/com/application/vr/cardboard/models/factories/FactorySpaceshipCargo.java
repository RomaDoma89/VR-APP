package com.application.vr.cardboard.models.factories;

import android.content.Context;

import com.application.vr.cardboard.models.SpaceshipCargo;
import com.application.vr.cardboard.models.interfaces.CreatorModel;
import com.application.vr.cardboard.models.interfaces.DynamicModel;

public class FactorySpaceshipCargo implements CreatorModel {
    @Override
    public DynamicModel create(Context context, float translationX, float translationY, float translationZ,
                               float rotationX, float rotationY, float rotationZ, float scale) {
        return new SpaceshipCargo(context, translationX, translationY, translationZ, rotationX, rotationY, rotationZ, scale);
    }
}
