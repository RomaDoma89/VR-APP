package com.application.vr.cardboard;

import android.opengl.Matrix;
import com.application.vr.cardboard.motion.MotionCalculator;

public class Camera {

    private MotionCalculator mCalculator;

    private float speed = 0.2f;
    // Keeps all previous rotations and translations.
    private final float[] rotationMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];

    private float[] newPitchRotationM = new float[16];
    private float[] newRollRotation = new float[16];
    private float[] newTranslateMatrix = new float[16];

    public Camera(MotionCalculator mCalculator) {
        this.mCalculator = mCalculator;
        Matrix.setIdentityM(rotationMatrix, 0);
    }

    public float[] draw(float[] mVPMatrix, float[] mProjectionMatrix) {
        // Set mViewMatrix at the origin and set a new view point.
        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, 0, 0, 0, -100, 0, 1, 0);
        // Matrix.setLookAtM(mViewMatrix, 0, 5, 5, 40, 0, 0, -100, 0f, 1f, 0f);

        // Store a multiplication of the rotationMatrix and new Pitch rotation.
        Matrix.setIdentityM(newPitchRotationM, 0);
        Matrix.rotateM(newPitchRotationM, 0, mCalculator.getPitch(), 1.0f, 0.0f, 0.0f);
        Matrix.multiplyMM(rotationMatrix, 0, newPitchRotationM , 0, rotationMatrix, 0);

        // Store the multiplication of the rotationMatrix and new Roll rotation.
        Matrix.setIdentityM(newRollRotation, 0);
        Matrix.rotateM(newRollRotation, 0, mCalculator.getRoll(), 0.0f, 0.0f, 1.0f);
        Matrix.multiplyMM(rotationMatrix, 0, newRollRotation , 0, rotationMatrix, 0);

        float[] mRotatedMatrix = rotationMatrix.clone();

        // Store the multiplication of the rotationMatrix and new translation.
        Matrix.setIdentityM(newTranslateMatrix, 0);
        Matrix.translateM(newTranslateMatrix, 0, 0.0f, 0.0f, speed);
        Matrix.multiplyMM(rotationMatrix, 0, newTranslateMatrix , 0, rotationMatrix, 0);

        // Multiply mViewMatrix with the final rotations and translations.
        Matrix.multiplyMM(mViewMatrix, 0, rotationMatrix, 0, mViewMatrix, 0);

        //Apply mProjectionMatrix and the updated mViewMatrix.
        Matrix.multiplyMM(mVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);
        return mRotatedMatrix;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }
}
