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
    public static final int MAX_SCALE_VALUE = 5;

    private FloatBuffer vertexScale;
    private FloatBuffer vertexLine;
    private final int mProgram;
    private final int mPositionHandle;
    private final int mColorHandle;
    private final int mMVPMatrixHandle;
    private final float[] mModelMatrix = new float[16];
    private final float[] mMVPMatrix = new float[16];
    private final float[] mEmptyVPMatrix = new float[16];

    private final float[] scaleMatrix = new float[16];
    private final float[] translationMatrix = new float[16];

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
        Matrix.setIdentityM(mEmptyVPMatrix, 0);
    }

    public void prepareModel() {
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, place[0]*(xScale*3f), place[1]*(xScale*1.2f)-xScale/1.5f, place[2]*5);
        Matrix.scaleM(mModelMatrix, 0, 2.5f*xScale, 3f*yScale, 1f);
    }

    public void draw(float[] uiVPMatrix, int amount) throws IncorrectValueException {
        if (amount > 10 || amount < 0) throw new IncorrectValueException(amount);
        // Add program to OpenGL environment
        GLES30.glUseProgram(mProgram);

        // Apply the projection and view transformation
        Matrix.setIdentityM(mMVPMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, uiVPMatrix,0, mModelMatrix,0);
        GLES30.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);

        prepareModel();
        drawModel(amount);
    }

    private void prepareData() {
        float scaleVertex[] = new float[MAX_SCALE_VALUE * 6];
        float lineVertex[] = new float[6];
        int i=0;
        //Points for scale
        for(; i<MAX_SCALE_VALUE; i++){
            scaleVertex[(i * 6)+ 0] = i/50f;
            scaleVertex[(i * 6)+ 1] = 0f;
            scaleVertex[(i * 6)+ 2] = 0f;
            scaleVertex[(i * 6)+ 3] = (i+1)/50f;
            scaleVertex[(i * 6)+ 4] = 0f;
            scaleVertex[(i * 6)+ 5] = 0f;
        }
        //Points for bottom lines
        lineVertex[0] = 0;
        lineVertex[1] = 0;
        lineVertex[2] = 0;
        lineVertex[3] = scaleVertex[scaleVertex.length-3];
        lineVertex[4] = scaleVertex[scaleVertex.length-2];
        lineVertex[5] = scaleVertex[scaleVertex.length-1];

        vertexScale = ByteBuffer
                .allocateDirect(scaleVertex.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        vertexScale.put(scaleVertex);
        vertexScale.position(0);

        vertexLine= ByteBuffer
                .allocateDirect(lineVertex.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        vertexLine.put(lineVertex);
        vertexLine.position(0);
    }

    private void drawModel(int amount) {
        // Enable a handle to the triangle vertices
        GLES30.glEnableVertexAttribArray(mPositionHandle);
        // Prepare the model coordinate data
        GLES30.glVertexAttribPointer(mPositionHandle, 3, GLES30.GL_FLOAT, false,0, vertexScale);
        // Set color for drawing
        GLES30.glUniform4fv(mColorHandle, 1, color, 0);
        // Set width for lines
        GLES30.glLineWidth(10f * xScale);
        GLES30.glDrawArrays(GLES30.GL_LINES, 0, amount);

        // Disable vertex array
        GLES30.glDisableVertexAttribArray(mPositionHandle);

        // Enable a handle to the triangle vertices
        GLES30.glEnableVertexAttribArray(mPositionHandle);
        // Prepare the model coordinate data
        GLES30.glVertexAttribPointer(mPositionHandle, 3, GLES30.GL_FLOAT, false,0, vertexLine);
        // Set color for drawing
        GLES30.glUniform4fv(mColorHandle, 1, color, 0);
        // Set width for lines
        GLES30.glLineWidth(2f);
        GLES30.glDrawArrays(GLES30.GL_LINES, 0, 2);
        // Disable vertex array
        GLES30.glDisableVertexAttribArray(mPositionHandle);
    }

    public interface Place {
        float [] LEFT_1 = {-0.19f, 0.0f, -0.25f};
        float [] LEFT_2 = {-0.20f, -0.1f, -0.25f};
        float [] LEFT_3 = {-0.21f, -0.2f, -0.25f};
        float [] RIGHT_1 = {0.11f, 0.0f, -0.25f};
        float [] RIGHT_2 = {0.12f, -0.1f, -0.25f};
        float [] RIGHT_3 = {0.13f, -0.2f, -0.25f};
    }

    public interface Color {
        float [] RED = { 0.560f, 0.078f, 0.078f, 1.0f };
        float [] GREEN = { 0.137f, 0.411f, 0.070f, 1.0f };
        float [] BLUE = { 0.250f, 0.462f, 0.466f, 1.0f };
        float [] YELLOW = {0.701f, 0.533f, 0.035f, 1.0f };
    }
}
