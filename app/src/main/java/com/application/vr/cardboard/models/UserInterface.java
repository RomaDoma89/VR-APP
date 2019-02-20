package com.application.vr.cardboard.models;

import android.content.Context;
import android.opengl.GLES30;
import android.opengl.Matrix;

import com.application.vr.cardboard.R;
import com.application.vr.cardboard.file_utils.ShaderUtils;
import com.application.vr.cardboard.file_utils.TextureLoader;

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

public class UserInterface {
    private FloatBuffer vertices, textures;
    private ShortBuffer indices;
    private TextureLoader textureLoader;

    private final int mProgram;
    private int mUVHandle;
    private int mPositionHandle;
    private int mMVPMatrixHandle;
    private float[] mModelMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];
    private float[] mEmptyVPMatrix = new float[16];

    /**
     * Sets up the drawing object data for use in an OpenGL ES context.
     */
    public UserInterface(Context context) {
        prepareData(context);
        // Prepare shaders and OpenGL program.
        int vertexShaderId = ShaderUtils.createShader(context, GLES30.GL_VERTEX_SHADER, R.raw.vertex_shader_uv);
        int fragmentShaderId = ShaderUtils.createShader(context, GLES30.GL_FRAGMENT_SHADER, R.raw.fragment_shader_uv);
        // Create empty OpenGL Program.
        mProgram = ShaderUtils.createProgram(vertexShaderId, fragmentShaderId);
        // get handle to vertex shader's vPosition member
        mPositionHandle = GLES30.glGetAttribLocation(mProgram, "vPosition");
        // get handle to fragment shader's vColor member
        mUVHandle = GLES30.glGetAttribLocation(mProgram, "a_UV");
        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES30.glGetUniformLocation(mProgram, "uMVPMatrix");

        Matrix.setIdentityM(mEmptyVPMatrix, 0);
    }

    public void prepareModel() {
        Matrix.setIdentityM(mModelMatrix, 0);
    }
    /**
     * Encapsulates the OpenGL ES instructions for drawing this shape.
     */
    public void draw() {
        // Add program to OpenGL environment
        GLES30.glUseProgram(mProgram);

        drawModel(textureLoader, vertices, textures, indices);

        Matrix.multiplyMM(mMVPMatrix, 0, mModelMatrix,0, mEmptyVPMatrix,0);
        GLES30.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);
    }

    private void prepareData(Context context) {


        Obj obj = null;
        try {
            InputStream objInputStream = context.getAssets().open("objects/ui.obj");
            obj = ObjUtils.convertToRenderable(ObjReader.read(objInputStream));
            objInputStream.close();
            textureLoader = new TextureLoader(context, "textures/user_interface_lights_txr.jpg");
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (null != obj) {
            IntBuffer intIndices = ObjData.getFaceVertexIndices(obj);
            ShortBuffer indices = ByteBuffer.allocateDirect(intIndices.limit() * 2).order(ByteOrder.nativeOrder()).asShortBuffer();
            while (intIndices.hasRemaining()) indices.put((short) intIndices.get());
            indices.rewind();
            // Extract the geometry data. This data can be used to create
            // the vertex buffer objects and vertex array objects for OpenGL
            this.vertices = ObjData.getVertices(obj);
            this.textures = ObjData.getTexCoords(obj, 2);
            this.indices = indices;
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
        textureLoader.unbind();
    }
}
