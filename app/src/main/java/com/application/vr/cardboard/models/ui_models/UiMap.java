package com.application.vr.cardboard.models.ui_models;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.List;

import android.content.Context;
import android.opengl.GLES30;
import android.opengl.Matrix;
import android.util.Log;

import com.application.vr.cardboard.R;
import com.application.vr.cardboard.file_utils.ShaderUtils;
import com.application.vr.cardboard.models.interfaces.DynamicModel;

/**
 * A test model for use as a drawn object in OpenGL ES 2.0.
 */
public class UiMap {
    private final int MAP_CIRCLE = 12;
    private final int MAP_USER = 16;
    private final int MAP_ALL_POINTS = 19;
    private FloatBuffer vertexData;

    private final int mProgramMap;
    private final int mProgramObj;
    private int mPositionHandleMap;
    private int mColorHandleMap;
    private int mMVPHandleMap;

    private int mPositionHandleObj;
    private int mColorHandleObj;
    private int mMVPHandleObj;

    private float[] mModelMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];
    private float[] mEmptyVPMatrix = new float[16];
    private float[] scaleMatrix = new float[16];
    private float[] translationMatrix = new float[16];
    private float[] rotationMatrix = new float[16];

    private final float vertices[] = new float[MAP_ALL_POINTS * 3];
    private final float map_main_border_color[] = { 0.250f, 0.462f, 0.466f, 0.5f };
    private final float map_user_point_color[] = { 0.560f, 0.078f, 0.078f, 1.0f };

    private boolean isChanged = false;
    private boolean showMap = true;

    private float xScale;
    private float yScale;

    private float translateX = 0f, translateY = -0.5f, translateZ = -5f;
    /**
     * Sets up the drawing object data for use in an OpenGL ES context.
     */
    public UiMap(Context context, float xScale, float yScale) {
        this.xScale = xScale;
        this.yScale = yScale;
        prepareData();
        // Prepare shaders and OpenGL program.
        int vertexShaderId = ShaderUtils.createShader(context, GLES30.GL_VERTEX_SHADER, R.raw.vertex_shader_map);
        int fragmentShaderId = ShaderUtils.createShader(context, GLES30.GL_FRAGMENT_SHADER, R.raw.fragment_shader_map);
        // Create empty OpenGL Program.
        mProgramMap = ShaderUtils.createProgram(vertexShaderId, fragmentShaderId);
        // get handle to vertex shader's vPosition member
        mPositionHandleMap = GLES30.glGetAttribLocation(mProgramMap, "vPosition");
        // get handle to fragment shader's vColor member
        mColorHandleMap = GLES30.glGetUniformLocation(mProgramMap, "vColor");
        // get handle to shape's transformation matrix
        mMVPHandleMap = GLES30.glGetUniformLocation(mProgramMap, "uMVPMatrix");

        // Create empty OpenGL Program.
        mProgramObj = ShaderUtils.createProgram(vertexShaderId, fragmentShaderId);
        // get handle to vertex shader's vPosition member
        mPositionHandleObj = GLES30.glGetAttribLocation(mProgramObj, "vPosition");
        // get handle to fragment shader's vColor member
        mColorHandleObj = GLES30.glGetUniformLocation(mProgramObj, "vColor");
        // get handle to shape's transformation matrix
        mMVPHandleObj = GLES30.glGetUniformLocation(mProgramObj, "uMVPMatrix");

        Matrix.setIdentityM(mEmptyVPMatrix, 0);
    }

    private void prepareModel() {
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 0f, -xScale/5.0f - xScale, -2.0f);
        Matrix.scaleM(mModelMatrix, 0, 5f, 5f, 5f);
        Matrix.scaleM(mModelMatrix, 0, 0.08f + xScale/10, 0.08f, 0.08f);
        Matrix.rotateM(mModelMatrix, 0, 15, 1f, 0f, 0);
    }
    /**
     * Encapsulates the OpenGL ES instructions for drawing this shape.
     */
    public void draw(float[] uiVPMatrix,
                     float [] headView,
                     List<DynamicModel> dynamicModels) {
        drawMap(uiVPMatrix);
        drawInners(uiVPMatrix, headView, dynamicModels);
    }

    private void drawMap(float[] uiVPMatrix) {
//        Matrix.setIdentityM(mModelMatrix, 0);
        // Add program to OpenGL environment
        GLES30.glUseProgram(mProgramMap);

        prepareModel();
        //Multiply the MVP and the DynamicModel matrices.
        Matrix.setIdentityM(mMVPMatrix, 0);
        // Apply the projection and view transformation
        Matrix.multiplyMM(mMVPMatrix, 0, uiVPMatrix, 0, mModelMatrix, 0);
        GLES30.glUniformMatrix4fv(mMVPHandleMap, 1, false, mMVPMatrix, 0);

        // Set width for lines
        GLES30.glLineWidth(3f);
        // Enable a handle to the triangle vertices
        GLES30.glEnableVertexAttribArray(mPositionHandleMap);
        // Prepare the model coordinate data
        GLES30.glVertexAttribPointer(mPositionHandleMap, 3, GLES30.GL_FLOAT, false,12, vertexData);
        GLES30.glUniform4fv(mColorHandleMap, 1, getUserColor(), 0);
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 3);
        // Disable vertex array
        GLES30.glDisableVertexAttribArray(mPositionHandleMap);
    }

    private void drawInners(float[] uiVPMatrix, float [] headView, List<DynamicModel> dynamicModels) {
        // Add program to OpenGL environment
        GLES30.glUseProgram(mProgramObj);

        prepareModel();
        //Multiply the MVP and the DynamicModel matrices.
        Matrix.setIdentityM(mMVPMatrix, 0);
        // Apply the projection and view transformation
        Matrix.multiplyMM(mMVPMatrix, 0, uiVPMatrix, 0, mModelMatrix, 0);
        GLES30.glUniformMatrix4fv(mMVPHandleObj, 1, false, mMVPMatrix, 0);

        if (showMap) drawDynamicModel(dynamicModels, headView);
    }

    private void prepareData() {
        int index = 0;
        //red triangle in the map
        vertices[index++] = 0f;
        vertices[index++] = 0f;
        vertices[index++] = -0.06f;

        vertices[index++] = -0.04f;
        vertices[index++] = 0f;
        vertices[index++] = 0.04f;

        vertices[index++] = 0.04f;
        vertices[index++] = 0f;
        vertices[index++] = 0.04f;

        vertexData = ByteBuffer
                .allocateDirect(vertices.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        vertexData.put(vertices);
        vertexData.position(0);
    }

    private void drawDynamicModel(List<DynamicModel> dynamicModels, float [] headView) {
        float[] dmVertices = new float[dynamicModels.size()*3];
        float[] dmLines = new float[dynamicModels.size()*2*3];
        float distance = 0.45f;
        for (int i=0; i<dynamicModels.size(); i++) {
            //Get a model position
            float[] modelPos = dynamicModels.get(i).getPosition();
            //Create a vector to represent the model's position.
            float[] curPosVec = {modelPos[0], modelPos[1], modelPos[2], 1};
            //Create a result vector to keep new position of the model.
            float[] newPosVec = new float[4];
            //Multiplying of the view matrix and the model's vector to get a new position after view was transformed.
            Matrix.multiplyMV(newPosVec, 0, headView, 0, curPosVec, 0);

            //Check if a point is within the range displayed by the map
            if (newPosVec[0] < distance && newPosVec[0] > -distance)
                if (newPosVec[1] < distance && newPosVec[1] > -distance)
                    if (newPosVec[2] < distance && newPosVec[2] > -distance) {
                        dmLines[i*6+0] = dmVertices[i*3+0] = newPosVec[0];
                        dmLines[i*6+1] = dmVertices[i*3+1] = newPosVec[1];
                        dmLines[i*6+2] = dmVertices[i*3+2] = newPosVec[2];
                        dmLines[i*6+3] = newPosVec[0];
                        dmLines[i*6+4] = 0f;
                        dmLines[i*6+5] = newPosVec[2];
                    }
        }

        // FloatBuffer to keep the points.
        FloatBuffer bufferVrtx = ByteBuffer
                .allocateDirect(dmVertices.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        bufferVrtx.put(dmVertices);
        bufferVrtx.position(0);
        // FloatBuffer to keep the lines.
        FloatBuffer bufferLines = ByteBuffer
                .allocateDirect(dmLines.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        bufferLines.put(dmLines);
        bufferLines.position(0);

        // Enable a handle to the triangle vertices
        GLES30.glEnableVertexAttribArray(mPositionHandleObj);
        // Prepare the model coordinate data
        GLES30.glVertexAttribPointer(mPositionHandleObj, 3, GLES30.GL_FLOAT, false,12, bufferVrtx);
        for (int i=0; i<dynamicModels.size(); i++) {
            GLES30.glUniform4fv(mColorHandleObj, 1, dynamicModels.get(i).getMapColor(), 0);
            GLES30.glDrawArrays(GLES30.GL_POINTS, i, 1);
        }

        // Prepare the lines coordinate data
        GLES30.glVertexAttribPointer(mPositionHandleObj, 3, GLES30.GL_FLOAT, false,12, bufferLines);
        GLES30.glUniform4fv(mColorHandleObj, 1, map_main_border_color, 0);
        GLES30.glDrawArrays(GLES30.GL_LINES, 0, dynamicModels.size()*2);

        // Disable vertex array
        GLES30.glDisableVertexAttribArray(mPositionHandleObj);
    }

    public void showMap() {
        showMap = !showMap;
    }

    public void changeColor() {
        if (!isChanged) isChanged = true;
        else isChanged = false;
    }
    private float[] getMainColor() {
        return isChanged ?  map_user_point_color : map_main_border_color;
    }
    private float[] getUserColor() {
        return isChanged ? map_main_border_color : map_user_point_color;
    }
}
