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

public class Galaxy implements StaticModel {
    private FloatBuffer vertexData;

    private final int mProgram;
    private final int mColorHandle;
    private final int mPositionHandle;
    private final int mMVPMatrixHandle;

    private final float[] mModelMatrix = new float[16];
    private final float[] mMVPMatrix = new float[16];

    private final Random random = new Random();
    private int stars_amount = 5000;
    /**
     * Sets up the drawing object data for use in an OpenGL ES context.
     */
    public Galaxy(Context context) {
        prepareData();
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

    public void prepareModel(){
        Matrix.setIdentityM(mModelMatrix, 0);
        // We can transform, rotate or scale the mModelMatrix here.

        // Multiply the MVP and the DynamicModel matrices.
        Matrix.setIdentityM(mMVPMatrix, 0);
    }

    /**
     * Encapsulates the OpenGL ES instructions for drawing this shape.
     */
    public void draw(float[] mVPMatrix) {
        // Add program to OpenGL environment
        GLES30.glUseProgram(mProgram);

        //Drawing of the model
        drawModel();

        Matrix.multiplyMM(mMVPMatrix, 0, mVPMatrix, 0, mModelMatrix, 0);
        GLES30.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);
    }

    private double rnd;
    public double getRandom() {
//        this.rnd ^= (this.rnd << 21);
//        this.rnd ^= (this.rnd >>> 35);
//        this.rnd ^= (this.rnd << 4);
        return this.rnd;
    }

    private void prepareData() {
        float[] vertex = new float[stars_amount*3];

        double a = 1; //коэффициент умножения косинуса, пока не вижу зачем делать его отличным от 1
        double b = 10000; //коэффициент призванный регулировать длину волны (ширину галактических рукавов) порробовать начать с 10000
        double c = 10000; //сдвиг рукава (закрутка) рлролблвать начать с 10000
        double d = 0.5d; //коэффициент для увеличения значения косинуса (вытягивания из отрицательных значений) думаю, что около 0.5 будет норм

        double x = 0;
        double y = 0;
        double z = 0;

        double ro = 0;
        double phi = 0;

        double dC = 1;
        double sigma = 1;

        double f = 1; //попробовать от 1 до 3

        for (int i=0; i<30000; i++) {
            x = random.nextDouble();
            y = random.nextDouble();
            z = random.nextDouble();

            ro = Math.sqrt(x * x + y * y);
            if (x == 0) phi= 0;
            else phi = Math.atan(y / x);
            z = z;

            double v3 = random.nextGaussian();
            double v1 = a * Math.cos(b * ro) + c * phi + d;
//            double v2 = dC * Math.pow(Math.E, (Math.pow(z, 2)  / 2 / sigma ^ 2) / sigma / (2 * Math.PI) ^ 0.5;

//            if (v1 + v2 + v3 > f) {
                x = x + random.nextGaussian();
                y = y + random.nextGaussian();
                z = z + random.nextGaussian();

//            }
        }

        //TODO put all xyz into vertex array

        vertexData = ByteBuffer
                .allocateDirect(vertex.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        vertexData.put(vertex);
        vertexData.position(0);
    }

    private void drawModel() {
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
