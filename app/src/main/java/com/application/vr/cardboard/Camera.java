package com.application.vr.cardboard;

import android.opengl.Matrix;
import android.util.Log;

import com.application.vr.cardboard.motion.MotionCalculator;

public class Camera {
    private MotionCalculator mCalculator;
    private float step = 0f;
    private long lastTime = System.nanoTime();
    // Keeps all previous rotations and translations.
    private final float[] completeTransformMatrix = new float[16];
    // Final rotated matrix.
    private float[] mCompleteViewMatrix = new float[16];
    private float[] mUiViewMatrix = new float[16];

    private final float[] newPitchRotationM = new float[16];
    private final float[] newRollRotation = new float[16];

    public Camera(MotionCalculator mCalculator) {
        this.mCalculator = mCalculator;
        Matrix.setIdentityM(completeTransformMatrix, 0);
        Matrix.setIdentityM(mUiViewMatrix, 0);
        Matrix.setLookAtM(mUiViewMatrix, 0, 0, 0, 0f, 0, 0, -10, 0, 1, 0);
    }

    public void transform() {
        // Set all view matrices at the origin and set a new view point.
        Matrix.setLookAtM(mCompleteViewMatrix, 0, 0, 0, 0, 0, 0, -1100, 0, 1, 0);

        // Store a multiplication of the completeTransformMatrix and new Pitch rotation.
        Matrix.setIdentityM(newPitchRotationM, 0);
        Matrix.rotateM(newPitchRotationM, 0, mCalculator.getPitch(), 1.0f, 0.0f, 0.0f);
        Matrix.multiplyMM(completeTransformMatrix, 0, newPitchRotationM , 0, completeTransformMatrix, 0);

        // Store the multiplication of the completeTransformMatrix and new Roll rotation.
        Matrix.setIdentityM(newRollRotation, 0);
        Matrix.rotateM(newRollRotation, 0, mCalculator.getRoll(), 0.0f, 0.0f, 1.0f);
        Matrix.multiplyMM(completeTransformMatrix, 0, newRollRotation , 0, completeTransformMatrix, 0);

        // Multiply mCompleteViewMatrix with the final rotations and translations.
        Matrix.multiplyMM(mCompleteViewMatrix, 0, completeTransformMatrix, 0, mCompleteViewMatrix, 0);
    }

    public float getSpeed() {
        long time = System.nanoTime();
        float deltaTime = (float)(time - lastTime) / 100000000f;
        lastTime = time;
        return step * deltaTime * 2;
    }

    public void getForwardVec(float[] forwardVec) {
        forwardVec[0] = -mCompleteViewMatrix[2];
        forwardVec[1] = -mCompleteViewMatrix[6];
        forwardVec[2] = -mCompleteViewMatrix[10];
    }

    /**
     * Implementation of the method is based on <class>com.google.vr.sdk.base.HeadTransform<class/>
     * @see  ## getEulerAngles(float[] eulerAngles, int offset) method.
     **/
    public void getEulerAngles(float[] eulerAngles) {
        float pitch = (float)Math.asin((double) mCompleteViewMatrix[6]);
        float yaw;
        float roll;
        if (Math.sqrt((double)(1.0F - mCompleteViewMatrix[6] * mCompleteViewMatrix[6])) >= 0.009999999776482582D) {
            yaw = (float)Math.atan2((double)(-mCompleteViewMatrix[2]), (double)mCompleteViewMatrix[10]);
            roll = (float)Math.atan2((double)(-mCompleteViewMatrix[4]), (double)mCompleteViewMatrix[5]);
        } else {
            yaw = 0.0F;
            roll = (float)Math.atan2((double) mCompleteViewMatrix[1], (double) mCompleteViewMatrix[0]);
        }
        eulerAngles[0] = -pitch;
        eulerAngles[1] = -yaw;
        eulerAngles[2] = -roll;
    }

    public float[] getCompleteViewMatrix() {
        return mCompleteViewMatrix.clone();
    }

    public float[] getUiViewMatrix() {
        return mUiViewMatrix;
    }

    public void speedUp() {
        if ((step += 0.2f) > 1f) step = 1f;
    }

    public void speedDown() {
        if ((step -= 0.2f) < 0f) step = 0f;
    }

    public int getSpeedScaleVal() {
//        Log.e("SPEED", ((int) (step/(0.8f/20f))+" "));
        return (int) (step/(1/10f));
    }
}
