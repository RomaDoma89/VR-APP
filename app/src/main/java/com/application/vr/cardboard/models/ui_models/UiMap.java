package com.application.vr.cardboard.models.ui_models;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.List;

import android.content.Context;
import android.opengl.GLES30;
import android.opengl.Matrix;

import com.application.vr.cardboard.R;
import com.application.vr.cardboard.file_utils.ShaderUtils;
import com.application.vr.cardboard.models.interfaces.DynamicModel;

/**
 * A test model for use as a drawn object in OpenGL ES 2.0.
 */
public class UiMap {
    private final int MAP_CIRCLE_POINTS = 364;
    private final int MAP_USER_POINTS = 368;
    private FloatBuffer vertexData;

    private final int mProgram;
    private int mPositionHandle;
    private int mColorHandle;
    private int mMVPMatrixHandle;
    private float[] mModelMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];
    private float[] mEmptyVPMatrix = new float[16];


    private float[] scaleMatrix = new float[16];
    private float[] translationMatrix = new float[16];
    private float[] rotationMatrix = new float[16];

    private final float vertices[] = new float[371 * 3];
    private final float map_main_border_color[] = { 0.250f, 0.462f, 0.466f, 0.5f };
    private final float map_user_point_color[] = { 0.560f, 0.078f, 0.078f, 1.0f };


    /**
     * Sets up the drawing object data for use in an OpenGL ES context.
     */
    public UiMap(Context context) {
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
        Matrix.scaleM(scaleMatrix, 0, 0.7f, 0.7f, 0.7f);

        Matrix.setIdentityM(translationMatrix, 0);
        Matrix.translateM(translationMatrix, 0, 0f, -0.65f, 0f);

        Matrix.setIdentityM(rotationMatrix, 0);
        Matrix.rotateM(rotationMatrix, 0, 70, 1f, 0f, 0f);

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.multiplyMM(mModelMatrix, 0, scaleMatrix, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mModelMatrix, 0, rotationMatrix, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mModelMatrix, 0, translationMatrix, 0, mModelMatrix, 0);
        //Multiply the MVP and the DynamicModel matrices.
        Matrix.setIdentityM(mMVPMatrix, 0);
    }
    /**
     * Encapsulates the OpenGL ES instructions for drawing this shape.
     */
    public void draw(List<DynamicModel> dynamicModels) {
        // Add program to OpenGL environment
        GLES30.glUseProgram(mProgram);

        drawModel(dynamicModels);

        // Apply the projection and view transformation
        Matrix.multiplyMM(mMVPMatrix, 0, mModelMatrix,0, mEmptyVPMatrix,0);
        GLES30.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);
    }

    private void prepareData() {
        int i = 0;
        // map area (circle)
        for(; i <364; i++){
            vertices[(i * 3)+ 0] = (float) (0.5 * Math.cos((3.14/180) * (float)i ));
            vertices[(i * 3)+ 1] = (float) (0.5 * Math.sin((3.14/180) * (float)i ));
            vertices[(i * 3)+ 2] = 0;
        }

        //left top line
        vertices[(i * 3)+ 0] = -2;
        vertices[(i * 3)+ 1] = 0.55f;
        vertices[(i * 3)+ 2] = 0f;
        vertices[(i * 3)+ 3] = -2/5f;
        vertices[(i * 3)+ 4] = 0.55f;
        vertices[(i * 3)+ 5] = 0f;
        //right top line
        vertices[(i * 3)+ 6] = 2/5f;
        vertices[(i * 3)+ 7] = 0.55f;
        vertices[(i * 3)+ 8] = 0f;
        vertices[(i * 3)+ 9] = 2;
        vertices[(i * 3)+ 10] = 0.55f;
        vertices[(i * 3)+ 11] = 0f;
        //red triangle in the map
        vertices[(i * 3)+ 12] = 0f;
        vertices[(i * 3)+ 13] = 0.05f;
        vertices[(i * 3)+ 14] = 0f;
        vertices[(i * 3)+ 15] = -0.04f;
        vertices[(i * 3)+ 16] = -0.04f;
        vertices[(i * 3)+ 17] = 0f;
        vertices[(i * 3)+ 18] = 0.04f;
        vertices[(i * 3)+ 19] = -0.04f;
        vertices[(i * 3)+ 20] = 0f;

        //first scale
        vertices[(i * 3)+ 12] = 0f;
        vertices[(i * 3)+ 13] = 0.05f;
        vertices[(i * 3)+ 14] = 0f;
        vertices[(i * 3)+ 15] = -0.04f;
        vertices[(i * 3)+ 16] = -0.04f;
        vertices[(i * 3)+ 17] = 0f;
        vertices[(i * 3)+ 18] = 0.04f;
        vertices[(i * 3)+ 19] = -0.04f;
        vertices[(i * 3)+ 20] = 0f;

        vertexData = ByteBuffer
                .allocateDirect(vertices.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        vertexData.put(vertices);
        vertexData.position(0);
    }

    private void drawModel(List<DynamicModel> dynamicModels) {
        // Set width for lines
        GLES30.glLineWidth(1f);
        // Enable a handle to the triangle vertices
        GLES30.glEnableVertexAttribArray(mPositionHandle);
        // Prepare the model coordinate data
        GLES30.glVertexAttribPointer(mPositionHandle, 3, GLES30.GL_FLOAT, false,12, vertexData);
        // Set color for drawing
        GLES30.glUniform4fv(mColorHandle, 1, map_main_border_color, 0);
        // Draw the triangle
        GLES30.glDrawArrays(GLES30.GL_LINE_STRIP, 0, MAP_CIRCLE_POINTS);
        // Draw the triangle
        GLES30.glDrawArrays(GLES30.GL_LINES, MAP_CIRCLE_POINTS, 4);
        // Set color for drawing
        GLES30.glUniform4fv(mColorHandle, 1, map_user_point_color, 0);
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, MAP_USER_POINTS, 3);
        // Disable vertex array
        GLES30.glDisableVertexAttribArray(mPositionHandle);
    }
}
