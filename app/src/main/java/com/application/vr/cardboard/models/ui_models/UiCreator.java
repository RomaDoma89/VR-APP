package com.application.vr.cardboard.models.ui_models;

import android.content.Context;
import android.util.Log;

import com.application.vr.cardboard.exceptions.IncorrectValueException;
import com.application.vr.cardboard.models.interfaces.DynamicModel;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class UiCreator {
    private UiShip uiShip;
    private UiAim uiAim;
    private UiMap uiMap;
    private List<TipsHead> tipsHeadList;
    private UiScale scale1, scale2, scale3, scale4, scale5, scale6;

    public UiCreator(Context context, float xScale, float yScale, float ration) {
        uiShip = new UiShip(context, xScale, yScale, ration);
        uiAim = new UiAim(context, xScale, yScale);
        uiMap = new UiMap(context, xScale, yScale);
        tipsHeadList = new ArrayList<>();
        tipsHeadList.add(new TipsHead(context, xScale, yScale, TipsHead.Direction.UP));
        tipsHeadList.add(new TipsHead(context, xScale, yScale, TipsHead.Direction.DOWN));
//        tipsHeadList.add(new TipsHead(context, xScale, yScale, TipsHead.Direction.LEFT));
//        tipsHeadList.add(new TipsHead(context, xScale, yScale, TipsHead.Direction.RIGHT));
        scale1 = new UiScale(context, xScale, yScale, UiScale.Place.LEFT_1, UiScale.Color.YELLOW);
        scale2 = new UiScale(context, xScale, yScale, UiScale.Place.LEFT_2, UiScale.Color.GREEN);
        scale3 = new UiScale(context, xScale, yScale, UiScale.Place.LEFT_3, UiScale.Color.BLUE);
        scale4 = new UiScale(context, xScale, yScale, UiScale.Place.RIGHT_1, UiScale.Color.YELLOW);
        scale5 = new UiScale(context, xScale, yScale, UiScale.Place.RIGHT_2, UiScale.Color.GREEN);
        scale6 = new UiScale(context, xScale, yScale, UiScale.Place.RIGHT_3, UiScale.Color.RED);
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
        uiShip.draw(uiVPMatrix, headView);
        uiAim.draw(uiVPMatrix);
        for (TipsHead uh : tipsHeadList) uh.draw(uiVPMatrix);
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

    public void removeUiHead(int direction) {
        ListIterator<TipsHead> it = tipsHeadList.listIterator();
        while (it.hasNext())
            if (it.next().getDirection() == direction) it.remove();

    }

    public void changeColorScheme() {
        uiMap.changeColor();
    }

    public void showMap() {
        uiMap.showMap();
    }
}
