package com.application.vr.cardboard.models.ui_models;

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

import de.javagl.obj.Obj;
import de.javagl.obj.ObjData;
import de.javagl.obj.ObjReader;
import de.javagl.obj.ObjUtils;

import static android.opengl.GLES20.GL_ONE_MINUS_SRC_ALPHA;
import static android.opengl.GLES20.GL_SRC_ALPHA;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.GL_UNSIGNED_SHORT;
import static android.opengl.GLES20.glDrawElements;

public class TipsHead {
    private FloatBuffer vertices, textures;
    private ShortBuffer indices;
    private TextureLoader texFront, texUp, texDown, texLeft, texRight;

    private final int mProgram;
    private int mUVHandle;
    private int mPositionHandle;
    private int mMVPMatrixHandle;
    private float[] mModelMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];
    private float xScale;
    private float yScale;
    private int direction;

    /**
     * Sets up the drawing object data for use in an OpenGL ES context.
     */
    public TipsHead(Context context, float xScale, float yScale, int direction) {
        this.xScale = xScale;
        this.yScale = yScale;
        this.direction = direction;
        prepareData(context);
        // Prepare shaders and OpenGL program.
        int vertexShaderId = ShaderUtils.createShader(context, GLES30.GL_VERTEX_SHADER, R.raw.vs_simple_uv);
        int fragmentShaderId = ShaderUtils.createShader(context, GLES30.GL_FRAGMENT_SHADER, R.raw.fs_simple_uv);
        // Create empty OpenGL Program.
        mProgram = ShaderUtils.createProgram(vertexShaderId, fragmentShaderId);
        // get handle to vertex shader's vPosition member
        mPositionHandle = GLES30.glGetAttribLocation(mProgram, "vPosition");
        // get handle to fragment shader's vColor member
        mUVHandle = GLES30.glGetAttribLocation(mProgram, "a_UV");
        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES30.glGetUniformLocation(mProgram, "uMVPMatrix");
    }
    private int count = 0;
    private boolean isFront = true;
    public void prepareModel() {
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.rotateM(mModelMatrix, 0, -90, 0f, 0f, 1f);
        Matrix.scaleM(mModelMatrix, 0, xScale/2f, yScale/2f, 1f);
        switch (direction) {
            case Direction.UP :
                Matrix.translateM(mModelMatrix, 0, -1.2f, 0f, -5f);
                break;
            case Direction.DOWN :
                Matrix.translateM(mModelMatrix, 0, 1.2f, 0f, -5f);
                break;
//            case Direction.LEFT :
//                Matrix.translateM(mModelMatrix, 0, 0f, -1.2f, -5f);
//                break;
//            case Direction.RIGHT :
//                Matrix.translateM(mModelMatrix, 0, 0f, 1.2f, -5f);
        }



        if (count <= 60) isFront = true;
        else if (count <= 90) {
            isFront = false;
        } else count = 0;
        count++;
    }

    /**
     * Encapsulates the OpenGL ES instructions for drawing this shape.
     */
    public void draw(float[] mViewPorjMatrix) {
        // Add program to OpenGL environment
        GLES30.glUseProgram(mProgram);

        prepareModel();

        Matrix.setIdentityM(this.mMVPMatrix, 0);
        Matrix.multiplyMM(this.mMVPMatrix, 0, mViewPorjMatrix,0, mModelMatrix,0);
        GLES30.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, this.mMVPMatrix, 0);

        GLES30.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        GLES30.glEnable(GLES30.GL_BLEND);

        switch (direction) {
            case Direction.UP :
                if (isFront) drawModel(texFront, vertices, textures, indices);
                if (!isFront) drawModel(texUp, vertices, textures, indices);
                break;
            case Direction.DOWN :
                if (isFront) drawModel(texFront, vertices, textures, indices);
                if (!isFront) drawModel(texDown, vertices, textures, indices);
                break;
//            case Direction.LEFT :
//                if (isFront) drawModel(texFront, vertices, textures, indices);
//                if (!isFront) drawModel(texLeft, vertices, textures, indices);
//                break;
//            case Direction.RIGHT :
//                if (isFront) drawModel(texFront, vertices, textures, indices);
//                if (!isFront) drawModel(texRight, vertices, textures, indices);
        }
    }

    private void prepareData(Context context) {
        Obj obj = null;
        try {
            InputStream objInputStream = context.getAssets().open("objects/plane.obj");
            obj = ObjUtils.convertToRenderable(ObjReader.read(objInputStream));
            objInputStream.close();
            texFront = new TextureLoader(context, "textures/head_front.png");
            texDown = new TextureLoader(context, "textures/head_down.png");
            texUp = new TextureLoader(context, "textures/head_top.png");
            texLeft = new TextureLoader(context, "textures/head_left.png");
            texRight = new TextureLoader(context, "textures/head_right.png");
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

    public int getDirection() {
        return direction;
    }
    public interface Direction {
        int UP = 1;
        int DOWN = 2;
//        int LEFT = 3;
//        int RIGHT = 4;
    }
}
