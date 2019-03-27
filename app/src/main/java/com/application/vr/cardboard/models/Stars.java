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

    private final float[] mModelMatrix = new float[16];
    private final float[] mMVPMatrix = new float[16];

    private final Random random = new Random();
    private int stars_amount = Galaxy.Density.FIFTY;
    /**
     * Sets up the drawing object data for use in an OpenGL ES context.
     */
    public Stars(Context context) {
        thread = new Thread(this, "PREPARE");
        thread.start();

        // Prepare shaders and OpenGL program.
        int vertexShaderId = ShaderUtils.createShader(context, GLES30.GL_VERTEX_SHADER, R.raw.vertex_shader);
        int fragmentShaderId = ShaderUtils.createShader(context, GLES30.GL_FRAGMENT_SHADER, R.raw.fragment_shader);
        // Create empty OpenGL Program.
        mProgram = ShaderUtils.createProgram(vertexShaderId, fragmentShaderId);
        // get handle to vertex shader's vPosition member
        mPositionHandle = GLES30.glGetAttribLocation(mProgram, "vPosition");
        // get handle to fragment shader's vColor member
        mColorHandle = GLES30.glGetUniformLocation(mProgram, "vColor");
        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES30.glGetUniformLocation(mProgram, "uMVPMatrix");

        Matrix.setIdentityM(mMVPMatrix, 0);
    }

    /**
     * Encapsulates the OpenGL ES instructions for drawing this shape.
     */
    public void draw(float[] mVPMatrix) {
        // Add program to OpenGL environment
        GLES30.glUseProgram(mProgram);

        Matrix.setIdentityM(mModelMatrix, 0);
        // We can transform, rotate or scale the mModelMatrix here.

        //Drawing of the model
        drawModel();

        // Multiply the MVP and the DynamicModel matrices.
        Matrix.setIdentityM(mMVPMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mVPMatrix, 0, mModelMatrix, 0);
        GLES30.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);
    }

    private void prepareData() {
//        float[] vertex = new float[stars_amount*3];
//
//        for (int i=0; i<stars_amount; i+=3) {
//            float x = (random.nextFloat());
//            float y = (random.nextFloat());
//            float z = (random.nextFloat());
//            float r = (float) Math.sqrt(x*x + y*y + z*z);
//
//            x /= r;
//            y /= r;
//            z /= r;
//
//            boolean isNeg1 = random.nextBoolean();
//            if (isNeg1) vertex[i] = x*-1000;
//            else vertex[i] = x*1000;
//            boolean isNeg2 = random.nextBoolean();
//            if (isNeg2) vertex[i+1] = y*-1000;
//            else vertex[i+1] = y*1000;
//            boolean isNeg3 = random.nextBoolean();
//            if (isNeg3) vertex[i+2] = z*-1000;
//            else vertex[i+2] = z*1000;
//        }
//
//        vertexData = ByteBuffer
//                .allocateDirect(vertex.length * 4)
//                .order(ByteOrder.nativeOrder())
//                .asFloatBuffer();
//        vertexData.put(vertex);
//        vertexData.position(0);

        int size = (int) Math.pow(stars_amount, 3) * 3;
        float[] vertex = new float[size];

        int numArms = 5;
        float armSeparationDistance = (float) (2 * Math.PI / numArms);
        float armOffsetMax = 1f;
        float rotationFactor = 5;
        float randomOffsetXY = 0.02f;

        int index = 0;
        for (int i=0; i<stars_amount; i++) {
            for (int j=0; j<stars_amount; j++) {
                for (int k=0; k<stars_amount; k++) {
                    // Choose a distance from the center of the galaxy.
                    float distance = random.nextFloat();
                    distance = (float) Math.pow(distance, 2);

                    // Choose an angle between 0 and 2 * PI.
                    float angle = random.nextFloat() * (float) (2 * Math.PI);
                    float armOffset = random.nextFloat() * armOffsetMax;
                    armOffset = armOffset - armOffsetMax / 2;
                    armOffset = armOffset * (1 / distance);

                    float squaredArmOffset = (float)Math.pow(armOffset, 2);
                    if(armOffset < 0)
                        squaredArmOffset = squaredArmOffset * -1;
                    armOffset = squaredArmOffset;

                    float rotation = distance * rotationFactor;

                    angle = (int)(angle / armSeparationDistance) * armSeparationDistance + armOffset + rotation;

                    // Convert polar coordinates to 2D cartesian coordinates.
                    float starX = (float) Math.cos(angle) * distance;
                    float starY = (float) Math.sin(angle) * distance;
                    float starZ = (float) k/5 * random.nextFloat();

                    float randomOffsetX = random.nextFloat() * randomOffsetXY;
                    float randomOffsetY = random.nextFloat() * randomOffsetXY;

                    starX += randomOffsetX;
                    starY += randomOffsetY;

                    // Now we can assign xy coords.
                    vertex[index] = starX * 100;
                    ++index;
                    vertex[index] = starY * 100;
                    ++index;
                    vertex[index] = starZ;
                    ++index;
                }
            }
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
