package com.application.vr.cardboard.levels;

import android.content.Context;

import com.application.vr.cardboard.models.Galaxy;
import com.application.vr.cardboard.models.factories.FactoryAsteroid;
import com.application.vr.cardboard.models.factories.FactoryPlanet;
import com.application.vr.cardboard.models.factories.FactorySpaceshipCargo;
import com.application.vr.cardboard.models.factories.FactorySpaceshipHunter;
import com.application.vr.cardboard.models.factories.FactorySun;
import com.application.vr.cardboard.models.interfaces.DynamicModel;
import com.application.vr.cardboard.models.interfaces.StaticModel;

import java.util.List;

public class TestLevel {
    private static final FactorySun sunFactory = new FactorySun();
    private static final FactoryPlanet planetFactory = new FactoryPlanet();
    private static final FactoryAsteroid asteroidFactory = new FactoryAsteroid();
    private static final FactorySpaceshipCargo cargoFactory = new FactorySpaceshipCargo();
    private static final FactorySpaceshipHunter hunterFactory = new FactorySpaceshipHunter();


    public static void generateAll(Context context, List<DynamicModel> dynamicModels, List<StaticModel> staticModels) {
        dynamicModels.add(asteroidFactory.create(context, 70, -55, -60, 0f, 0.5f, 0.5f, 1f));
        dynamicModels.add(asteroidFactory.create(context, -25, -100, -66, 0.5f, 0f, 0.5f, 4f));
        dynamicModels.add(asteroidFactory.create(context, -40, 35, 30, 0.5f, 0.5f, 0f, 3f));
        dynamicModels.add(asteroidFactory.create(context, 40, 68, 90, 0.5f, 0.5f, 0f, 2f));

        dynamicModels.add(cargoFactory.create(context, 16f, 55f, -100f, 0f, 0f, 0f, 0f));
        dynamicModels.add(cargoFactory.create(context, 0f, 55f, -95f, 0f, 0f, 0f, 0f));
        dynamicModels.add(cargoFactory.create(context, -19f, 55f, -110f, 0f, 0f, 0f, 0f));

        dynamicModels.add(hunterFactory.create(context, -40f, 5f, -200f,  0f, 0f, 0f, 0f));
        dynamicModels.add(hunterFactory.create(context, -20f, 0f, -200f,  0f, 0f, 0f, 0f));
        dynamicModels.add(hunterFactory.create(context, 20f, 0f, -200f,  0f, 0f, 0f, 0f));
        dynamicModels.add(hunterFactory.create(context, 40f, 0f, -200f,  0f, 0f, 0f, 0f));

        staticModels.add(sunFactory.create(context, 700, 15, -50, 0f, 1f, 0f, 30f));
        staticModels.add(planetFactory.create(context, 99, -105, -700, 0f, 1f, 0f, 100f));
        staticModels.add(new Galaxy(context, Galaxy.Density.TEN, Galaxy.Color.YELLOW, -100f, -550f, 0f, 1f, 1f, 0f, 0.8f));
        staticModels.add(new Galaxy(context, Galaxy.Density.TEN, Galaxy.Color.BLUE, 50f, 550f, 95f, 1f, 0f, 1f, 0.5f));
        staticModels.add(new Galaxy(context, Galaxy.Density.THIRTY, Galaxy.Color.WHITE, 0, 0f, -1000f, 0f, 0f, 1f, 800f));
    }
}
