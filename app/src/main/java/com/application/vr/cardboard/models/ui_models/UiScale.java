package com.application.vr.cardboard.models.ui_models;

import android.content.Context;
import android.opengl.GLES30;
import android.opengl.Matrix;

import com.application.vr.cardboard.exceptions.IncorrectValueException;
import com.application.vr.cardboard.R;
import com.application.vr.cardboard.file_utils.ShaderUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * A test model for use as a drawn object in OpenGL ES 2.0.
 */
public class UiScale {
    private final int SCALE_AMOUNT = 20;

    private FloatBuffer vertexData;
    private final int mProgram;
    private final int mPositionHandle;
    private final int mColorHandle;
    private final int mMVPMatrixHandle;
    private final float[] mModelMatrix = new float[16];
    private final float[] mMVPMatrix = new float[16];
    private final float[] mEmptyVPMatrix = new float[16];

    private final float[] scaleMatrix = new float[16];
    private final float[] translationMatrix = new float[16];

    private float vertices[] = new float[SCALE_AMOUNT * 3];

    private float [] place;
    private float [] color;
    private float xScale;
    private float yScale;

    /**
     * Sets up the drawing object data for use in an OpenGL ES context.
     */
    public UiScale(Context context, float xScale, float yScale, float [] place, float [] color) {
        this.xScale = xScale;
        this.yScale = yScale;
        this.place = place;
        this.color = color;
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
        Matrix.setIdentityM(mEmptyVPMatrix, 0);
    }

    public void prepareModel() {
        Matrix.setIdentityM(scaleMatrix, 0);
        Matrix.scaleM(scaleMatrix, 0, 0.2f*xScale, 0.2f*xScale, 0.2f);

        Matrix.setIdentityM(translationMatrix, 0);
        Matrix.translateM(translationMatrix, 0, place[0]*xScale, place[1]*yScale-(xScale+xScale/6), place[2]);

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.multiplyMM(mModelMatrix, 0, scaleMatrix, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mModelMatrix, 0, translationMatrix, 0, mModelMatrix, 0);

        //Multiply the MVP and the DynamicModel matrices.
        Matrix.setIdentityM(mMVPMatrix, 0);
    }

    public void draw(float[] uiVPMatrix, int amount) throws IncorrectValueException {
        if (amount > SCALE_AMOUNT || amount < 0) throw new IncorrectValueException(amount);
        // Add program to OpenGL environment
        GLES30.glUseProgram(mProgram);
        prepareModel();
        drawModel(amount);

        // Apply the projection and view transformation
        Matrix.multiplyMM(mMVPMatrix, 0, uiVPMatrix,0, mModelMatrix,0);
        GLES30.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);
    }

    private void prepareData() {
        int i = 0;
        // map area (circle)
        for(; i <SCALE_AMOUNT; i++){
            vertices[(i * 3)+ 0] = (i%2 > 0) ? 0f+(i-1)/10f : 0f+(i)/10f;
            vertices[(i * 3)+ 1] = (i%2 > 0) ? 0.1f : 0f;
            vertices[(i * 3)+ 2] = 0f;
        }
        vertexData = ByteBuffer
                .allocateDirect(vertices.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        vertexData.put(vertices);
        vertexData.position(0);
    }

    private void drawModel(int amount) {
        // Set width for lines
        GLES30.glLineWidth(1f);
        // Enable a handle to the triangle vertices
        GLES30.glEnableVertexAttribArray(mPositionHandle);
        // Prepare the model coordinate data
        GLES30.glVertexAttribPointer(mPositionHandle, 3, GLES30.GL_FLOAT, false,12, vertexData);
        // Set color for drawing
        GLES30.glUniform4fv(mColorHandle, 1, color, 0);
        GLES30.glDrawArrays(GLES30.GL_LINES, 0, SCALE_AMOUNT);
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, amount);
        // Disable vertex array
        GLES30.glDisableVertexAttribArray(mPositionHandle);
    }

    public interface Place {
        float [] LEFT_1 = {-0.95f, 0.12f, -2f};
        float [] LEFT_2 = {-0.95f, 0f, -2f};
        float [] LEFT_3 = {-0.95f, -0.12f, -2f};
        float [] RIGHT_1 = {0.6f, 0.12f, -2f};
        float [] RIGHT_2 = {0.6f, -0f, -2f};
        float [] RIGHT_3 = {0.6f, -0.12f, -2f};
    }

    public interface Color {
        float [] RED = { 0.560f, 0.078f, 0.078f, 1.0f };
        float [] GREEN = { 0.137f, 0.411f, 0.070f, 1.0f };
        float [] BLUE = { 0.250f, 0.462f, 0.466f, 1.0f };
        float [] YELLOW = {0.701f, 0.533f, 0.035f, 1.0f };
    }
}
