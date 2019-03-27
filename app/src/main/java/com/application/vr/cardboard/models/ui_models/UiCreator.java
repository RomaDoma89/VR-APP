package com.application.vr.cardboard.models.ui_models;

import android.content.Context;
import android.util.Log;

import com.application.vr.cardboard.exceptions.IncorrectValueException;
import com.application.vr.cardboard.models.interfaces.DynamicModel;

import java.util.List;

public class UiCreator {
    private UiAim uiAim;
    private UiMap uiMap;
    private UiScale scale1;
    private UiScale scale2;
    private UiScale scale3;
    private UiScale scale4;
    private UiScale scale5;
    private UiScale scale6;

    public UiCreator(Context context, float xScale, float yScale) {
        uiAim = new UiAim(context, xScale, yScale);
        uiMap = new UiMap(context, xScale, yScale);
        scale1 = new UiScale(context, xScale, yScale, UiScale.Place.LEFT_1, UiScale.Color.YELLOW);
        scale2 = new UiScale(context, xScale, yScale, UiScale.Place.LEFT_2, UiScale.Color.GREEN);
        scale3 = new UiScale(context, xScale, yScale, UiScale.Place.LEFT_3, UiScale.Color.BLUE);
        scale4 = new UiScale(context, xScale, yScale, UiScale.Place.RIGHT_1, UiScale.Color.YELLOW);
        scale5 = new UiScale(context, xScale, yScale, UiScale.Place.RIGHT_2, UiScale.Color.GREEN);
        scale6 = new UiScale(context, xScale, yScale, UiScale.Place.RIGHT_3, UiScale.Color.BLUE);
    }

    public void draw(float[] uiVPMatrix,
                     float [] headView,
                     List<DynamicModel> dynamicModels,
                     int leftFirsAmount,
                     int leftSecondAmount,
                     int leftThirdAmount,
                     int rightFistAmount,
                     int rightSecondAmount,
                     int rightThirdAmount) {
        uiAim.draw(uiVPMatrix);
        uiMap.draw(uiVPMatrix, headView, dynamicModels);
        try {
            scale1.draw(uiVPMatrix, leftFirsAmount);
            scale2.draw(uiVPMatrix, leftSecondAmount);
            scale3.draw(uiVPMatrix, leftThirdAmount);
            scale4.draw(uiVPMatrix, rightFistAmount);
            scale5.draw(uiVPMatrix, rightSecondAmount);
            scale6.draw(uiVPMatrix, rightThirdAmount);
        } catch (IncorrectValueException ex) {
            Log.e("onDrawEye(Eye eye)", ex.toString());
        }

    }
}
