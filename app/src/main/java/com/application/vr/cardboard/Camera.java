package com.application.vr.cardboard;

import android.opengl.Matrix;

import com.application.vr.cardboard.motion.MotionManager;

public class Camera {
    private MotionManager mManager;
    private float step = 0f;
    private long lastTime = System.nanoTime();
    // Keeps all previous rotations and translations.
    private final float[] PRY_matrix = new float[16];
    private final float[] Y_matrix = new float[16];
    // Final rotated matrix.
    private final float[] viewPitchRollYaw = new float[16];
    private final float[] viewStraight = new float[16];
    private final float[] viewYaw = new float[16];

    private final float[] newPitchRotationM = new float[16];
    private final float[] newRollRotation = new float[16];
    private final float[] newYawRotation = new float[16];

    public Camera(MotionManager mManager) {
        this.mManager = mManager;
        Matrix.setIdentityM(Y_matrix, 0);
        Matrix.setIdentityM(PRY_matrix, 0);
        // Should be initialized with initial values only once.
        Matrix.setLookAtM(viewStraight, 0, 0, 0, 0.1f, 0, 0, -10000, 0, 1, 0);
    }

    public void transform() {
        // Set all view matrices at the origin and set a new view point.
        Matrix.setLookAtM(viewPitchRollYaw, 0, 0, 0, 0.1f, 0, 0, -10000, 0, 1, 0);
        Matrix.setLookAtM(viewYaw, 0, 0, 0, 0.1f, 0, 0, -10000, 0, 1, 0);

        Matrix.setIdentityM(newPitchRotationM, 0);
        Matrix.rotateM(newPitchRotationM, 0, mManager.getPitch(), 1.0f, 0.0f, 0.0f);
        Matrix.multiplyMM(PRY_matrix, 0, newPitchRotationM , 0, PRY_matrix, 0);

        Matrix.setIdentityM(newRollRotation, 0);
        Matrix.rotateM(newRollRotation, 0, mManager.getRoll(), 0.0f, 0.0f, 1.0f);
        Matrix.multiplyMM(PRY_matrix, 0, newRollRotation , 0, PRY_matrix, 0);

        Matrix.setIdentityM(newYawRotation, 0);
        Matrix.rotateM(newYawRotation, 0, mManager.getYaw(), 0.0f, 1.0f, 0.0f);
        Matrix.multiplyMM(Y_matrix, 0, newYawRotation , 0, Y_matrix, 0);
        Matrix.multiplyMM(PRY_matrix, 0, newYawRotation , 0, PRY_matrix, 0);

        // Multiply viewPitchRoll with the final rotations and translations.
        Matrix.multiplyMM(viewPitchRollYaw, 0, PRY_matrix, 0, viewPitchRollYaw, 0);
        Matrix.multiplyMM(viewYaw, 0, Y_matrix, 0, viewYaw, 0);
    }

    /**
     * Puts forward vector matrix
     * @param forwardVec - an array to keep result values
     **/
    public void getForwardVec(float[] forwardVec) {
        forwardVec[0] = -PRY_matrix[2];
        forwardVec[1] = -PRY_matrix[6];
        forwardVec[2] = -PRY_matrix[10];
    }
    public float[] getYawView() {
        return viewYaw.clone();
    }
    public float[] getCompleteView() {
        return viewPitchRollYaw.clone();
    }
    public float[] getStraightView() {
        return viewStraight;
    }

    /**
     * Use in the render methods.
     * Don NOT use in cycles!
     * */
    public float getSpeed() {
        long time = System.nanoTime();
        float deltaTime = (float)(time - lastTime) / 100000000f;
        lastTime = time;
        return step * deltaTime * 2;
    }

    public void speedUp() {
        if ((step += 0.2f) > 1f) step = 1f;
    }

    public void speedDown() {
        if ((step -= 0.2f) < 0f) step = 0f;
    }

    public int getSpeedScaleVal() {
        return (int) (step/(1/10f));
    }
}
