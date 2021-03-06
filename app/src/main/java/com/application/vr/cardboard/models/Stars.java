package com.application.vr.cardboard.models;

import android.content.Context;
import android.opengl.GLES30;
import android.opengl.Matrix;

import com.application.vr.cardboard.R;
import com.application.vr.cardboard.file_utils.ShaderUtils;
import com.application.vr.cardboard.models.interfaces.StaticModel;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Random;


import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_POINTS;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glLineWidth;
import static android.opengl.GLES20.glUniform4f;

public class Stars implements StaticModel, Runnable {
    private FloatBuffer vertexData;
    private Thread thread;

    private final int mProgram;
    private final int mColorHandle;
    private final int mPositionHandle;
    private final int mMVPMatrixHandle;

    private float[] translateM= new float[16];
    private float[] scaleM = new float[16];
    private final float[] mModelMatrix = new float[16];
    private final float[] mMVPMatrix = new float[16];

    private final Random random = new Random();
    private int stars_amount = 5000;
    private float translate, scale;
    /**
     * Sets up the drawing object data for use in an OpenGL ES context.
     */
    public Stars(Context context, float translate, float scale) {
        thread = new Thread(this, "PREPARE");
        thread.start();
        this.translate = translate;
        this.scale = scale;

        // Prepare shaders and OpenGL program.
        int vertexShaderId = ShaderUtils.createShader(context, GLES30.GL_VERTEX_SHADER, R.raw.vs_non_texture);
        int fragmentShaderId = ShaderUtils.createShader(context, GLES30.GL_FRAGMENT_SHADER, R.raw.fs_non_texture);
        // Create empty OpenGL Program.
        mProgram = ShaderUtils.createProgram(vertexShaderId, fragmentShaderId);
        // get handle to vertex shader's vPosition member
        mPositionHandle = GLES30.glGetAttribLocation(mProgram, "vPosition");
        // get handle to fragment shader's vColor member
        mColorHandle = GLES30.glGetUniformLocation(mProgram, "vColor");
        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES30.glGetUniformLocation(mProgram, "uMVPMatrix");
    }

    private void prepareModel() {
        Matrix.setIdentityM(scaleM, 0);
        Matrix.scaleM(scaleM, 0, scale, scale, scale);
        Matrix.setIdentityM(translateM, 0);
        Matrix.translateM(translateM, 0, 0f, 0f, translate);
        // We can transform, rotate or scale the mModelMatrix here.
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.multiplyMM(mModelMatrix, 0, scaleM, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mModelMatrix, 0, translateM, 0, mModelMatrix, 0);
    }
    /**
     * Encapsulates the OpenGL ES instructions for drawing this shape.
     */
    public void draw(float[] mVPMatrix, float[] mViewMatrix, float[] globalLightPosition, float[] globalLightColor) {
        // Add program to OpenGL environment
        GLES30.glUseProgram(mProgram);

        // Multiply the MVP and the DynamicModel matrices.
        Matrix.setIdentityM(mMVPMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mVPMatrix, 0, mModelMatrix, 0);
        GLES30.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);

        //Transform model matrix
        prepareModel();
        //Drawing of the model
        drawModel();
    }

    private void prepareData() {
        float[] vertex = new float[stars_amount*3];

        for (int i=0; i<stars_amount; i+=3) {
            float x = (random.nextFloat());
            float y = (random.nextFloat());
            float z = (random.nextFloat());
            float r = (float) Math.sqrt(x*x + y*y + z*z);

            x /= r;
            y /= r;
            z /= r;

            boolean isNeg1 = random.nextBoolean();
            if (isNeg1) vertex[i] = -x*2;
            else vertex[i] = x*2;
            boolean isNeg2 = random.nextBoolean();
            if (isNeg2) vertex[i+1] = -y*2;
            else vertex[i+1] = y*2;
            boolean isNeg3 = random.nextBoolean();
            if (isNeg3) vertex[i+2] = -z*2;
            else vertex[i+2] = z*2;
        }

        vertexData = ByteBuffer
                .allocateDirect(vertex.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        vertexData.put(vertex);
        vertexData.position(0);
    }

    private void drawModel() {
        if (!thread.isAlive()) {
            // Enable vertex array
            GLES30.glEnableVertexAttribArray(mPositionHandle);
            GLES30.glVertexAttribPointer(mPositionHandle, 3, GL_FLOAT, false, 0, vertexData);
            glLineWidth(1);
            glUniform4f(mColorHandle, 1.0f, 1.0f, 1.0f, 1.0f);
            glDrawArrays(GL_POINTS, 0, stars_amount);
            // Disable vertex array
            GLES30.glDisableVertexAttribArray(mPositionHandle);
        }
    }

    @Override
    public void run() {
        prepareData();
    }
}
