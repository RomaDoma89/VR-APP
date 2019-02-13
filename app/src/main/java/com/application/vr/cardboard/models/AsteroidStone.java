package com.application.vr.cardboard.models;

import android.content.Context;
import android.opengl.GLES30;
import android.opengl.Matrix;

import com.application.vr.cardboard.R;
import com.application.vr.cardboard.file_utils.ShaderUtils;
import com.application.vr.cardboard.file_utils.TextureLoader;
import com.application.vr.cardboard.models.interfaces.ModelAsteroid;

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

import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.GL_UNSIGNED_SHORT;
import static android.opengl.GLES20.glDrawElements;

/**
 * A test model for use as a drawn object in OpenGL ES 2.0.
 */
public class AsteroidStone implements ModelAsteroid {
    private List<FloatBuffer> corpusVertexList, corpusTextureList;
    private List<ShortBuffer> corpusIndicesList;

    private TextureLoader corpusTextureLoader;

    private final int mProgram;
    private int mPositionHandle;
    private int mUVHandle;
    private float[] mModelMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];
    private float translationX, translationY, translationZ;
    private float rotationX, rotationY, rotationZ;
    private float scale;

    /**
     * Sets up the drawing object data for use in an OpenGL ES context.
     */
    public AsteroidStone(Context context, float translationX, float translationY, float translationZ,
                         float rotationX, float rotationY, float rotationZ, float scale) {
        this.translationX = translationX;
        this.translationY = translationY;
        this.translationZ = translationZ;
        this.rotationX = rotationX;
        this.rotationY = rotationY;
        this.rotationZ = rotationZ;
        this.scale = scale;
        // Load and parse Blander object.
        prepareData(context);
        // Prepare shaders and OpenGL program.
        int vertexShaderId = ShaderUtils.createShader(context, GLES30.GL_VERTEX_SHADER, R.raw.vertex_shader_uv);
        int fragmentShaderId = ShaderUtils.createShader(context, GLES30.GL_FRAGMENT_SHADER, R.raw.fragment_shader_uv);
        // Create empty OpenGL Program.
        mProgram = ShaderUtils.createProgram(vertexShaderId, fragmentShaderId);
    }

    /**
     * Encapsulates the OpenGL ES instructions for drawing this shape.
     */
    private float rotation = 0f;
    public void draw(float[] mVPMatrix) {
        rotation += 0.3f;
        // Add program to OpenGL environment
        GLES30.glUseProgram(mProgram);
        // get handle to vertex shader's vPosition member
        mPositionHandle = GLES30.glGetAttribLocation(mProgram, "vPosition");
        // get handle to fragment shader's vColor member
        mUVHandle = GLES30.glGetAttribLocation(mProgram, "a_UV");
        // get handle to shape's transformation matrix
        int mMVPMatrixHandle = GLES30.glGetUniformLocation(mProgram, "uMVPMatrix");

        float[] scaleM = new float[16];
        Matrix.setIdentityM(scaleM, 0);
        Matrix.scaleM(scaleM, 0, scale, scale, scale);
        float[] translM = new float[16];
        Matrix.setIdentityM(translM, 0);
        Matrix.translateM(translM, 0, translationX, translationY, translationZ);
        float[] rotateM = new float[16];
        Matrix.setIdentityM(rotateM, 0);
        Matrix.rotateM(rotateM, 0, rotation, rotationX, rotationY, rotationZ);

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.multiplyMM(mModelMatrix, 0, scaleM, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mModelMatrix, 0, rotateM, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mModelMatrix, 0, translM, 0, mModelMatrix, 0);

        for (int i=0; i<corpusVertexList.size(); i++)
            drawModel(corpusTextureLoader, corpusVertexList.get(i), corpusTextureList.get(i), corpusIndicesList.get(i));

        Matrix.setIdentityM(mMVPMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mVPMatrix, 0, mModelMatrix, 0);
        GLES30.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);
    }

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
    }

    private void drawModel(TextureLoader textureLoader, FloatBuffer vertexBuff, FloatBuffer textureBuffer, ShortBuffer indices) {
        // Enable vertex array
        textureLoader.bind();
        GLES30.glEnableVertexAttribArray(mPositionHandle);
        GLES30.glVertexAttribPointer(mPositionHandle, 3, GLES30.GL_FLOAT, false, 0, vertexBuff);

        GLES30.glEnableVertexAttribArray(mUVHandle);
        GLES30.glVertexAttribPointer(mUVHandle, 2, GLES30.GL_FLOAT, false, 0, textureBuffer);

        glDrawElements(GL_TRIANGLES, indices.limit(), GL_UNSIGNED_SHORT, indices);
        // Disable vertex array
        GLES30.glDisableVertexAttribArray(mPositionHandle);
        GLES30.glDisableVertexAttribArray(mUVHandle);
    }
}
