package com.application.vr.cardboard.models;

import android.content.Context;
import android.opengl.GLES30;
import android.opengl.Matrix;

import com.application.vr.cardboard.R;
import com.application.vr.cardboard.file_utils.ShaderUtils;
import com.application.vr.cardboard.file_utils.TextureLoader;
import com.application.vr.cardboard.models.interfaces.DynamicModel;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;

import de.javagl.obj.Obj;
import de.javagl.obj.ObjData;
import de.javagl.obj.ObjReader;
import de.javagl.obj.ObjUtils;

import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_POINTS;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.GL_TRIANGLE_STRIP;
import static android.opengl.GLES20.GL_UNSIGNED_SHORT;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glDrawElements;
import static android.opengl.GLES20.glUniform4f;

/**
 * A test model for use as a drawn object in OpenGL ES 2.0.
 */
public class AsteroidStone implements DynamicModel {
    private List<FloatBuffer> corpusVertexList, corpusTextureList;
    private FloatBuffer mapVertices;
    private List<ShortBuffer> corpusIndicesList;

    private TextureLoader corpusTextureLoader;

    private final int mMainProgram;
    private final int mMapProgram;
    private int mMainPositionHandle;
    private int mMapPositionHandle;
    private int mMainMVPMatrixHandle;
    private int mMapMVPMatrixHandle;
    private int mUVHandle;
    private int mColorHandle;
    private float[] mModelMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];
    private float[] translationMatrix = new float[16];
    private float[] rotationMatrix = new float[16];
    private float[] scaleMatrix = new float[16];

    private float translateX, translateY, translateZ;
    private float rotationX, rotationY, rotationZ;
    private float scale;
    private float rotation = 0f;

    /**
     * Sets up the drawing object data for use in an OpenGL ES context.
     */
    public AsteroidStone(Context context, float translationX, float translationY, float translationZ,
                         float rotationX, float rotationY, float rotationZ, float scale) {
        this.translateX = translationX;
        this.translateY = translationY;
        this.translateZ = translationZ;
        this.rotationX = rotationX;
        this.rotationY = rotationY;
        this.rotationZ = rotationZ;
        this.scale = scale;

        // Prepare shaders and OpenGL program.
        int vertexShaderId = ShaderUtils.createShader(context, GLES30.GL_VERTEX_SHADER, R.raw.vertex_shader_uv);
        int fragmentShaderId = ShaderUtils.createShader(context, GLES30.GL_FRAGMENT_SHADER, R.raw.fragment_shader_uv);
        // Create empty OpenGL Program.
        mMainProgram = ShaderUtils.createProgram(vertexShaderId, fragmentShaderId);
        // get handle to vertex shader's vPosition member
        mMainPositionHandle = GLES30.glGetAttribLocation(mMainProgram, "vPosition");
        // get handle to fragment shader's vColor member
        mUVHandle = GLES30.glGetAttribLocation(mMainProgram, "a_UV");
        // get handle to shape's transformation matrix
        mMainMVPMatrixHandle = GLES30.glGetUniformLocation(mMainProgram, "uMVPMatrix");

        int vertexShaderIdForMap = ShaderUtils.createShader(context, GLES30.GL_VERTEX_SHADER, R.raw.vertex_shader_map);
        int fragmentShaderIdForMap = ShaderUtils.createShader(context, GLES30.GL_FRAGMENT_SHADER, R.raw.fragment_shader_map);
        // Create empty OpenGL Program.
        mMapProgram = ShaderUtils.createProgram(vertexShaderIdForMap, fragmentShaderIdForMap);
        // get handle to vertex shader's vPosition member
        mMapPositionHandle = GLES30.glGetAttribLocation(mMapProgram, "vPosition");
        // get handle to fragment shader's vColor member
        // get handle to fragment shader's vColor member
        mColorHandle = GLES30.glGetUniformLocation(mMapProgram, "vColor");
        // get handle to shape's transformation matrix
        mMapMVPMatrixHandle = GLES30.glGetUniformLocation(mMapProgram, "uMVPMatrix");

        // Load and parse Blander object.
        this.prepareData(context);
    }

    @Override
    public void prepareModel(){}

    /**
     * Encapsulates the OpenGL ES instructions for drawing this shape.
     */
    @Override
    public void draw(float[] mVPMatrix) {
        rotation += 1.3f;
        Matrix.setIdentityM(scaleMatrix, 0);
        Matrix.scaleM(scaleMatrix, 0, scale, scale, scale);
        Matrix.setIdentityM(translationMatrix, 0);
        Matrix.translateM(translationMatrix, 0, translateX, translateY, translateZ);
        Matrix.setIdentityM(rotationMatrix, 0);
        Matrix.rotateM(rotationMatrix, 0, rotation, rotationX, rotationY, rotationZ);

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.multiplyMM(mModelMatrix, 0, scaleMatrix, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mModelMatrix, 0, rotationMatrix, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mModelMatrix, 0, translationMatrix, 0, mModelMatrix, 0);

        // Multiply the MVP and the DynamicModel matrices.
        Matrix.setIdentityM(mMVPMatrix, 0);

        // Add program to OpenGL environment
        GLES30.glUseProgram(mMainProgram);
        // Draw the vertices and the textures
        // Enable vertex array
        corpusTextureLoader.bind();
        GLES30.glEnableVertexAttribArray(mMainPositionHandle);
        GLES30.glEnableVertexAttribArray(mUVHandle);
        for (int i=0; i<corpusVertexList.size(); i++)
            drawModel(corpusVertexList.get(i), corpusTextureList.get(i), corpusIndicesList.get(i));
        // Disable vertex array
        GLES30.glDisableVertexAttribArray(mMainPositionHandle);
        GLES30.glDisableVertexAttribArray(mUVHandle);
        corpusTextureLoader.unbind();

        // Multiply the MVP and the DynamicModel matrices.
        Matrix.setIdentityM(mMVPMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mVPMatrix, 0, mModelMatrix, 0);
        GLES30.glUniformMatrix4fv(mMainMVPMatrixHandle, 1, false, mMVPMatrix, 0);

//        drawForMap(mEmptyVPMatrix, mVPMatrix);
    }
//    @Override
//    public void draw(float[] mVPMatrix, float[] matrixRotation) {
//        rotation += 1.3f;
//
//        Matrix.setIdentityM(scaleMatrix, 0);
//        Matrix.scaleM(scaleMatrix, 0, scale, scale, scale);
//        Matrix.setIdentityM(translationMatrix, 0);
//        Matrix.translateM(translationMatrix, 0, translateX, translateY, translateZ);
//        Matrix.setIdentityM(rotationMatrix, 0);
//        Matrix.rotateM(rotationMatrix, 0, rotation, rotationX, rotationY, rotationZ);
//
//        Matrix.setIdentityM(mModelMatrix, 0);
//        Matrix.multiplyMM(mModelMatrix, 0, scaleMatrix, 0, mModelMatrix, 0);
//        Matrix.multiplyMM(mModelMatrix, 0, rotationMatrix, 0, mModelMatrix, 0);
//        Matrix.multiplyMM(mModelMatrix, 0, translationMatrix, 0, mModelMatrix, 0);
//
//        // Multiply the MVP and the DynamicModel matrices.
//        Matrix.setIdentityM(mMVPMatrix, 0);
//
//        // Add program to OpenGL environment
//        GLES30.glUseProgram(mMainProgram);
//        // Draw the vertices and the textures
//        // Enable vertex array
//        corpusTextureLoader.bind();
//        GLES30.glEnableVertexAttribArray(mMainPositionHandle);
//        GLES30.glEnableVertexAttribArray(mUVHandle);
//        for (int i=0; i<corpusVertexList.size(); i++)
//            drawModel(corpusVertexList.get(i), corpusTextureList.get(i), corpusIndicesList.get(i));
//        // Disable vertex array
//        GLES30.glDisableVertexAttribArray(mMainPositionHandle);
//        GLES30.glDisableVertexAttribArray(mUVHandle);
//        corpusTextureLoader.unbind();
//
//        // Multiply the MVP and the DynamicModel matrices.
//        Matrix.setIdentityM(mMVPMatrix, 0);
//        Matrix.multiplyMM(mMVPMatrix, 0, mVPMatrix, 0, mModelMatrix, 0);
//        GLES30.glUniformMatrix4fv(mMainMVPMatrixHandle, 1, false, mMVPMatrix, 0);
//
//        drawForMap(mEmptyVPMatrix, matrixRotation);
//    }

    private void prepareData(Context context) {
        corpusVertexList = new ArrayList<>();
        corpusTextureList = new ArrayList<>();
        corpusIndicesList = new ArrayList<>();

        Obj obj = null;
        try {
            InputStream objInputStream = context.getAssets().open("objects/asteroid_1.obj");
            obj = ObjUtils.convertToRenderable(ObjReader.read(objInputStream));
            objInputStream.close();
            corpusTextureLoader = new TextureLoader(context, "textures/meteor_txr_2.jpg");
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (null != obj) {
            corpusVertexList = new ArrayList<>();
            corpusTextureList = new ArrayList<>();
            corpusIndicesList = new ArrayList<>();

            // Extract the geometry data. This data can be used to create
            // the vertex buffer objects and vertex array objects for OpenGL
            corpusVertexList.add(ObjData.getVertices(obj));
            corpusTextureList.add(ObjData.getTexCoords(obj, 2));
            IntBuffer intIndices = ObjData.getFaceVertexIndices(obj);
            ShortBuffer indices = ByteBuffer.allocateDirect(intIndices.limit() * 2).order(ByteOrder.nativeOrder()).asShortBuffer();
            while (intIndices.hasRemaining()) indices.put((short) intIndices.get());
            indices.rewind();
            corpusIndicesList.add(indices);
        }

        float [] vertex = {translateX/800, translateY/800, translateZ/800};
        mapVertices = ByteBuffer.allocateDirect(vertex.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mapVertices.put(vertex);
        mapVertices.position(0);
    }

    private void drawModel(FloatBuffer vertexBuff, FloatBuffer textureBuffer, ShortBuffer indices) {
        GLES30.glVertexAttribPointer(mMainPositionHandle, 3, GLES30.GL_FLOAT, false, 0, vertexBuff);
        GLES30.glVertexAttribPointer(mUVHandle, 2, GLES30.GL_FLOAT, false, 0, textureBuffer);
        glDrawElements(GL_TRIANGLES, indices.limit(), GL_UNSIGNED_SHORT, indices);
    }

    @Override
    public void drawMapModel(int positionHandle, int colorHandle) {
//        // Add program to OpenGL environment
//        GLES30.glUseProgram(mMapProgram);

        GLES30.glEnableVertexAttribArray(positionHandle);
        GLES30.glVertexAttribPointer(positionHandle, 3, GL_FLOAT, false, 0, mapVertices);
        glUniform4f(colorHandle, 1.0f, 0.0f, 1.0f, 1.0f);
        glDrawArrays(GL_POINTS, 0, 1);
        // Disable vertex array
        GLES30.glDisableVertexAttribArray(positionHandle);
    }
}
