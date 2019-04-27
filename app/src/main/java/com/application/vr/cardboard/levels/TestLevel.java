package com.application.vr.cardboard.levels;

import android.content.Context;

import com.application.vr.cardboard.models.Galaxy;
import com.application.vr.cardboard.models.Stars;
import com.application.vr.cardboard.models.factories.FactoryAsteroid;
import com.application.vr.cardboard.models.factories.FactoryPlanet;
import com.application.vr.cardboard.models.factories.FactorySpaceshipCargo;
import com.application.vr.cardboard.models.factories.FactorySpaceshipHunter;
import com.application.vr.cardboard.models.factories.FactorySun;
import com.application.vr.cardboard.models.interfaces.DynamicModel;
import com.application.vr.cardboard.models.interfaces.Model;
import com.application.vr.cardboard.models.interfaces.StaticModel;

import java.util.ArrayList;
import java.util.List;

public class TestLevel {
    private static final FactorySun sunFactory = new FactorySun();
    private static final FactoryPlanet planetFactory = new FactoryPlanet();
    private static final FactoryAsteroid asteroidFactory = new FactoryAsteroid();
    private static final FactorySpaceshipCargo cargoFactory = new FactorySpaceshipCargo();
    private static final FactorySpaceshipHunter hunterFactory = new FactorySpaceshipHunter();


    public static List<Model> generateAll(Context context, List<DynamicModel> dynamicModels, List<StaticModel> staticModels) {
        staticModels.add(sunFactory.create(context, 60, 95, -500, 0f, 1f, 0f, 10f));

        dynamicModels.add(asteroidFactory.create(context, 70, -55, -60, 0f, 0.5f, 0.5f, 1f));
        dynamicModels.add(asteroidFactory.create(context, -25, -70, -66, 0.5f, 0f, 0.5f, 2f));
        dynamicModels.add(asteroidFactory.create(context, -40, 35, 30, 0.5f, 0.5f, 0f, 3f));
        dynamicModels.add(asteroidFactory.create(context, 40, 68, 90, 0.5f, 0.5f, 0f, 2f));

        dynamicModels.add(cargoFactory.create(context, 256f, -355f, -400f, 0f, 0f, 0f, 1f));
        dynamicModels.add(cargoFactory.create(context, -100f, -355f, -195f, 0f, 0f, 0f, 1f));
        dynamicModels.add(cargoFactory.create(context, -339f, -250f, -110f, 0f, 0f, 0f, 1f));

        dynamicModels.add(hunterFactory.create(context, -40f, 5f, -160f,  0f, 0f, 0f, 1f));
        dynamicModels.add(hunterFactory.create(context, -20f, 0f, -160f,  0f, 0f, 0f, 1f));
        dynamicModels.add(hunterFactory.create(context, 20f, 0f, -160f,  0f, 0f, 0f, 1f));
        dynamicModels.add(hunterFactory.create(context, 40f, 0f, -160f,  0f, 0f, 0f, 1f));

        staticModels.add(planetFactory.create(context, 200, 150, -200, 0f, 1f, 0f, 70f));

        staticModels.add(new Galaxy(context, Galaxy.Density.TEN, Galaxy.Color.YELLOW, -10f, -555f, 0f, 1f, 1f, 0f, 0.5f));
        staticModels.add(new Galaxy(context, Galaxy.Density.TEN, Galaxy.Color.BLUE, 150f, 555f, 95f, 1f, 0f, 1f, 1f));
//        staticModels.add(new Galaxy(context, Galaxy.Density.THIRTY, Galaxy.Color.WHITE, 0, 0f, -1000f, 0f, 0f, 1f, 800f));
        staticModels.add(new Stars(context, 0, 300f));

        List<Model> allModels = new ArrayList<>();
        allModels.addAll(dynamicModels);
        allModels.addAll(staticModels);

        return allModels;
    }
}
