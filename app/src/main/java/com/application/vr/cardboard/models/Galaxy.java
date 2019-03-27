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


/**
 * Created by using the link :
 *
 * http://itinerantgames.tumblr.com/post/78592276402/a-2d-procedural-galaxy-with-c
 * */
public class Galaxy implements StaticModel, Runnable {
    private FloatBuffer vertexData;
    private Thread loadingDataThread;

    private final int mProgram;
    private final int mColorHandle;
    private final int mPositionHandle;
    private final int mMVPMatrixHandle;

    private final float[] mModelMatrix = new float[16];
    private final float[] mMVPMatrix = new float[16];
    private float [] color;

    private final Random random = new Random();
    private int stars_amount;
    private float translateX, translateY, translateZ;
    private float rotationX, rotationY, rotationZ;
    private float scale;
    /**
     * Sets up the drawing object data for use in an OpenGL ES context.
     */
    public Galaxy(Context context, int size, float [] color, float translationX, float translationY, float translationZ,
                  float rotationX, float rotationY, float rotationZ, float scale) {
        this.translateX = translationX;
        this.translateY = translationY;
        this.translateZ = translationZ;
        this.rotationX = rotationX;
        this.rotationY = rotationY;
        this.rotationZ = rotationZ;
        this.scale = scale;
        this.stars_amount = size;
        this.color = color;

        loadingDataThread = new Thread(this, "Galaxy");
        loadingDataThread.start();

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
        Matrix.setIdentityM(mModelMatrix, 0);
        // We can transform, rotate or scale the mModelMatrix here.
        Matrix.translateM(mModelMatrix, 0, translateX, translateY, translateZ);
        if (0 != rotationX) Matrix.rotateM(mModelMatrix, 0, 25, rotationX, 0f, 0f);
        if (0 != rotationY) Matrix.rotateM(mModelMatrix, 0, 25, 0f, rotationY, 0f);
        if (0 != rotationZ) Matrix.rotateM(mModelMatrix, 0, 25, 0f, 0f, rotationZ);
        Matrix.scaleM(mModelMatrix, 0,  scale, scale, scale);

        // Multiply the MVP and the DynamicModel matrices.
        Matrix.setIdentityM(mMVPMatrix, 0);

        // Add program to OpenGL environment
        GLES30.glUseProgram(mProgram);

        //Drawing of the model
        drawModel();

        Matrix.multiplyMM(mMVPMatrix, 0, mVPMatrix, 0, mModelMatrix, 0);
        GLES30.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);
    }

    private void prepareData() {
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
        if (null != vertexData) {
            // Enable vertex array
            GLES30.glEnableVertexAttribArray(mPositionHandle);
            GLES30.glVertexAttribPointer(mPositionHandle, 3, GL_FLOAT, false, 0, vertexData);
            glLineWidth(1);

            // Set color for drawing
            GLES30.glUniform4fv(mColorHandle, 1, color, 0);
            glDrawArrays(GL_POINTS, 0, (int) Math.pow(stars_amount, 3));
            // Disable vertex array
            GLES30.glDisableVertexAttribArray(mPositionHandle);
        }
    }

    @Override
    public void run() {
        prepareData();
    }

    public interface Density {
        int TEN = 10;
        int TWENTY = 20;
        int THIRTY = 30;
        int FORTY = 40;
        int FIFTY = 50;
    }

    public interface Color {
        float [] WHITE = { 1f, 1f, 1f, 1.0f };
        float [] BLUE = { 0.933f, 0.949f, 0.988f, 1.0f };
        float [] YELLOW = {0.988f, 0.984f, 0.933f, 1.0f };
        float [] RED = { 0.988f, 0.949f, 0.933f, 1.0f };
    }
}
