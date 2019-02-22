package com.application.vr.cardboard.models.ui_models;

import android.content.Context;
import android.util.Log;

import com.application.vr.cardboard.exceptions.IncorrectValueException;
import com.application.vr.cardboard.models.interfaces.DynamicModel;

import java.util.List;

public class UiCreator {
    private UiAim ui;
    private UiMap uiMap;
    private UiScale scale1;
    private UiScale scale2;
    private UiScale scale3;
    private UiScale scale4;
    private UiScale scale5;
    private UiScale scale6;

    public UiCreator(Context context) {
        ui = new UiAim(context);
        uiMap = new UiMap(context);
        scale1 = new UiScale(context, UiScale.Place.LEFT_1, UiScale.Color.YELLOW);
        scale2 = new UiScale(context, UiScale.Place.LEFT_2, UiScale.Color.GREEN);
        scale3 = new UiScale(context, UiScale.Place.LEFT_3, UiScale.Color.BLUE);
        scale4 = new UiScale(context, UiScale.Place.RIGHT_1, UiScale.Color.YELLOW);
        scale5 = new UiScale(context, UiScale.Place.RIGHT_2, UiScale.Color.GREEN);
        scale6 = new UiScale(context, UiScale.Place.RIGHT_3, UiScale.Color.BLUE);
    }

    public void prepareModel() {
        ui.prepareModel();
        uiMap.prepareModel();
        scale1.prepareModel();
        scale2.prepareModel();
        scale3.prepareModel();
        scale4.prepareModel();
        scale5.prepareModel();
        scale6.prepareModel();
    }

    public void draw(int leftFirsAmount,
                     int leftSecondAmount,
                     int leftThirdAmount,
                     int rightFistAmount,
                     int rightSecondAmount,
                     int rightThirdAmount,
                     List<DynamicModel> dynamicModels) {
        ui.draw();
        uiMap.draw(dynamicModels);
        try {
            scale1.draw(leftFirsAmount);
            scale2.draw(leftSecondAmount);
            scale3.draw(leftThirdAmount);
            scale4.draw(rightFistAmount);
            scale5.draw(rightSecondAmount);
            scale6.draw(rightThirdAmount);
        } catch (IncorrectValueException ex) {
            Log.e("onDrawEye(Eye eye)", ex.toString());
        }

    }
}
