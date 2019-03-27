package com.application.vr.cardboard.motion;

public class MotionInterpolator {

    private static float [] prevAccelData = new float[3];

    public static float[] interpolateXYZ(float[] values) {
        //Interpolation to avoid drifts
        prevAccelData[0] = values[0] * 0.2f + prevAccelData[0] * 0.8f;
        prevAccelData[1] = values[1] * 0.2f + prevAccelData[1] * 0.8f;
        prevAccelData[2] = values[2] * 0.2f + prevAccelData[2] * 0.8f;
        // Return new x, y, z values
        return prevAccelData.clone();
    }

    private static float [] prevHeadData = new float[16];
    public static float[] interpolateView(float[] headView) {
        //Interpolation to avoid drifts
        prevHeadData[0] = headView[0] * 0.2f + prevHeadData[0] * 0.8f;
        prevHeadData[1] = headView[1] * 0.2f + prevHeadData[1] * 0.8f;
        prevHeadData[2] = headView[2] * 0.2f + prevHeadData[2] * 0.8f;
        prevHeadData[3] = headView[3];

        prevHeadData[4] = headView[4] * 0.2f + prevHeadData[4] * 0.8f;
        prevHeadData[5] = headView[5] * 0.2f + prevHeadData[5] * 0.8f;
        prevHeadData[6] = headView[6] * 0.2f + prevHeadData[6] * 0.8f;
        prevHeadData[7] = headView[7];

        prevHeadData[8] = headView[8] * 0.2f + prevHeadData[8] * 0.8f;
        prevHeadData[9] = headView[9] * 0.2f + prevHeadData[9] * 0.8f;
        prevHeadData[10] = headView[10] * 0.2f + prevHeadData[10] * 0.8f;
        prevHeadData[11] = headView[11];

        prevHeadData[12] = headView[12] * 0.2f + prevHeadData[12] * 0.8f;
        prevHeadData[13] = headView[13] * 0.2f + prevHeadData[13] * 0.8f;
        prevHeadData[14] = headView[14] * 0.2f + prevHeadData[14] * 0.8f;
        prevHeadData[15] = headView[15];

        // Return new x, y, z values
        return prevHeadData.clone();
    }
}
