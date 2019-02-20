package com.application.vr.cardboard;

import android.opengl.Matrix;
import com.application.vr.cardboard.motion.MotionCalculator;

public class Camera {

    private MotionCalculator mCalculator;

    private float speed = 0.3f;
    // Keeps all previous rotations and translations.
    private final float[] completeTransformMatrix = new float[16];
    // Keeps all previous rotations only. Will be use for the "static" models.
    private final float[] onlyRotatedMatrix = new float[16];
    private final float[] mCompleteViewMatrix = new float[16];
    private final float[] mRotatedViewMatrix = new float[16];

    private final float[] newPitchRotationM = new float[16];
    private final float[] newRollRotation = new float[16];
    private final float[] newTranslateMatrix = new float[16];

    public Camera(MotionCalculator mCalculator) {
        this.mCalculator = mCalculator;
        Matrix.setIdentityM(completeTransformMatrix, 0);
        Matrix.setIdentityM(onlyRotatedMatrix, 0);
    }

    public void transform() {
        // Set mCompleteViewMatrix at the origin and set a new view point.
        Matrix.setLookAtM(mCompleteViewMatrix, 0, 0, 0, 0, 0, 0, -300, 0, 1, 0);
        Matrix.setLookAtM(mRotatedViewMatrix, 0, 0, 0, 0, 0, 0, -300, 0, 1, 0);
        // Matrix.setLookAtM(mCompleteViewMatrix, 0, 5, 5, 40, 0, 0, -100, 0f, 1f, 0f);

        // Store a multiplication of the completeTransformMatrix and new Pitch rotation.
        Matrix.setIdentityM(newPitchRotationM, 0);
        Matrix.rotateM(newPitchRotationM, 0, mCalculator.getPitch(), 1.0f, 0.0f, 0.0f);
        Matrix.multiplyMM(completeTransformMatrix, 0, newPitchRotationM , 0, completeTransformMatrix, 0);
        Matrix.multiplyMM(onlyRotatedMatrix, 0, newPitchRotationM , 0, onlyRotatedMatrix, 0);

        // Store the multiplication of the completeTransformMatrix and new Roll rotation.
        Matrix.setIdentityM(newRollRotation, 0);
        Matrix.rotateM(newRollRotation, 0, mCalculator.getRoll(), 0.0f, 0.0f, 1.0f);
        Matrix.multiplyMM(completeTransformMatrix, 0, newRollRotation , 0, completeTransformMatrix, 0);
        Matrix.multiplyMM(onlyRotatedMatrix, 0, newRollRotation , 0, onlyRotatedMatrix, 0);

        // Store the multiplication of the completeTransformMatrix and new translation.
        Matrix.setIdentityM(newTranslateMatrix, 0);
        Matrix.translateM(newTranslateMatrix, 0, 0.0f, 0.0f, speed);
        Matrix.multiplyMM(completeTransformMatrix, 0, newTranslateMatrix , 0, completeTransformMatrix, 0);

        // Multiply mCompleteViewMatrix with the final rotations and translations.
        Matrix.multiplyMM(mCompleteViewMatrix, 0, completeTransformMatrix, 0, mCompleteViewMatrix, 0);
        // Multiply mCompleteViewMatrix with the rotations.
        Matrix.multiplyMM(mRotatedViewMatrix, 0, onlyRotatedMatrix, 0, mRotatedViewMatrix, 0);
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public float[] getRotatedViewMatrix() {
        return mRotatedViewMatrix;
    }
    public float[] getCompleteViewMatrix() {
        return mCompleteViewMatrix;
    }
}
