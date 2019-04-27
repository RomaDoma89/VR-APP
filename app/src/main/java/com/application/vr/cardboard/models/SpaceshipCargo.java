package com.application.vr.cardboard.models;

import android.content.Context;
import android.opengl.GLES30;
import android.opengl.Matrix;

import com.application.vr.cardboard.R;
import com.application.vr.cardboard.file_utils.ShaderUtils;
import com.application.vr.cardboard.file_utils.TextureLoader;
import com.application.vr.cardboard.models.interfaces.DynamicModel;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.javagl.obj.FloatTuple;
import de.javagl.obj.Mtl;
import de.javagl.obj.MtlReader;
import de.javagl.obj.Obj;
import de.javagl.obj.ObjData;
import de.javagl.obj.ObjReader;
import de.javagl.obj.ObjSplitting;
import de.javagl.obj.ObjUtils;

import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.GL_UNSIGNED_SHORT;
import static android.opengl.GLES20.glDrawElements;

/**
 * A model for use as a drawn object in OpenGL ES 2.0.
 */
public class SpaceshipCargo implements DynamicModel {
    private FloatBuffer corpus_vrtx, corpus_texr, corpus_norm, window_vrtx, window_texr, window_norm;
    private ShortBuffer corpus_indx, window_indx;

    private TextureLoader corpus_tl;
    private TextureLoader window_tl;

    private final int glProgram;
    private int glPositionParam;
    private int glNormalParam;
    private int glLightPosParam;
    private int glLightColParam;
    private int glMVPMatrixParam;
    private int glModelMatrixParam;
    private int glTextureParam;

    private final float[] mModelMatrix = new float[16];
    private final float[] mMVPMatrix = new float[16];
    private final float[] translationMatrix = new float[16];
    private final float[] rotationMatrix = new float[16];
    private final float[] map_color = new float[]{ 0.494f, 0.419f, 0.188f, 1.0f };

    private final float[] lightPosition = new float[3];
    private final float[] lightColor = new float[3];

    private float translationX, translationY, translationZ;
    private float rotationX, rotationY, rotationZ;
    private float scale;
    private float rotation = 0f;

    /**
     * Sets up the drawing object data for use in an OpenGL ES context.
     */
    public SpaceshipCargo(Context context, float translateX, float translateY, float translateZ,
                         float rotationX, float rotationY, float rotationZ, float scale) {
        this.translationX = translateX;
        this.translationY = translateY;
        this.translationZ = translateZ;
        this.rotationX = rotationX;
        this.rotationY = rotationY;
        this.rotationZ = rotationZ;
        this.scale = scale;

        // Prepare shaders and OpenGL program.
        int vertexShaderId = ShaderUtils.createShader(context, GLES30.GL_VERTEX_SHADER, R.raw.vs_base_model_uv);
        int fragmentShaderId = ShaderUtils.createShader(context, GLES30.GL_FRAGMENT_SHADER, R.raw.fs_base_model_uv);
        // Create empty OpenGL Program.
        glProgram = ShaderUtils.createProgram(vertexShaderId, fragmentShaderId);
        // get handle to vertex shader's vPosition member
        glPositionParam = GLES30.glGetAttribLocation(glProgram, "a_Position");
        glNormalParam = GLES30.glGetAttribLocation(glProgram, "a_Normal");
        // get handle to fragment shader's texture member
        glTextureParam = GLES30.glGetAttribLocation(glProgram, "a_UV");

        // get handle to shape's transformation matrix
        glMVPMatrixParam = GLES30.glGetUniformLocation(glProgram, "u_MVPMatrix");
        glModelMatrixParam = GLES30.glGetUniformLocation(glProgram, "u_MVMatrix");
        glLightPosParam = GLES30.glGetUniformLocation(glProgram, "a_Light_Pos");
        glLightColParam = GLES30.glGetUniformLocation(glProgram, "a_Light_Col");

        // Load and parse Blander object.
        this.prepareData(context);
    }
    private void prepareModel(float[] globalLightPosition, float[] globalLightColor) {
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, translationX, translationY, translationZ);
        Matrix.scaleM(mModelMatrix, 0, scale, scale, scale);

        lightPosition[0] = globalLightPosition[0] - translationX;
        lightPosition[1] = globalLightPosition[1] - translationY;
        lightPosition[2] = globalLightPosition[2] - translationZ;

        lightColor[0] = globalLightColor[0];
        lightColor[1] = globalLightColor[1];
        lightColor[2] = globalLightColor[2];
    }

    @Override
    public float[] getPosition() {
        return new float[] {translationX/800, translationY/800, translationZ/800};
    }

    @Override
    public float[] getMapColor() {
        return map_color;
    }
    @Override
    public void moveByCamera(@Nullable float[] forwardVec, float speed) {
        if (null != forwardVec) {
            translationX -= forwardVec[0] * speed;
            translationY -= forwardVec[1] * speed;
            translationZ -= forwardVec[2] * speed;
        }
    }
    /**
     * Encapsulates the OpenGL ES instructions for drawing this shape.
     */
    @Override
    public void draw(float[] mVPMatrix, float[] mViewMatrix, float[] globalLightPosition, float[] globalLightColor) {
        // Add program to OpenGL environment
        GLES30.glUseProgram(glProgram);

        // Translation, scaling and rotation of the model.
        prepareModel(globalLightPosition, globalLightColor);

        // Multiply the MVP and the model matrices.
        Matrix.setIdentityM(mMVPMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mVPMatrix,0, mModelMatrix,0);
        // Past a ModelViewProjection matrix to the shader parameter
        GLES30.glUniformMatrix4fv(glMVPMatrixParam, 1, false, mMVPMatrix, 0);
        // Past a ModelView matrix to the shader parameter
        GLES30.glUniformMatrix4fv(glModelMatrixParam, 1, false, mModelMatrix, 0);
        GLES30.glUniform3fv(glLightPosParam, 1, lightPosition, 0);
        GLES30.glUniform3fv(glLightColParam, 1, lightColor, 0);

        // Enable vertex array
        GLES30.glEnableVertexAttribArray(glPositionParam);
        GLES30.glEnableVertexAttribArray(glNormalParam);
        GLES30.glEnableVertexAttribArray(glTextureParam);

        // Draw the vertices and the textures for each material of the object
        corpus_tl.bind();
        drawVertices(corpus_vrtx, corpus_norm, corpus_texr, corpus_indx);
        corpus_tl.unbind();

        window_tl.bind();
        drawVertices(window_vrtx, window_norm, window_texr, window_indx);
        window_tl.unbind();

        // Disable vertex array
        GLES30.glDisableVertexAttribArray(glPositionParam);
        GLES30.glDisableVertexAttribArray(glNormalParam);
        GLES30.glDisableVertexAttribArray(glTextureParam);
    }

    private void drawVertices(FloatBuffer vertexBuff, FloatBuffer normalsBuff, FloatBuffer textureBuffer, ShortBuffer indices) {
        GLES30.glVertexAttribPointer(glPositionParam, 3, GLES30.GL_FLOAT, false, 0, vertexBuff);
        GLES30.glVertexAttribPointer(glNormalParam, 3, GLES30.GL_FLOAT, false, 0, normalsBuff);
        GLES30.glVertexAttribPointer(glTextureParam, 2, GLES30.GL_FLOAT, false, 0, textureBuffer);
        GLES30.glDrawElements(GL_TRIANGLES, indices.limit(), GL_UNSIGNED_SHORT, indices);
    }

    private Mtl findMtlForName(Iterable<? extends Mtl> mtls, String name) {
        for (Mtl mtl : mtls) {
            if (mtl.getName().equals(name)) return mtl;
        }
        return null;
    }

    private void prepareData(Context context) {
        List<Mtl> mtlList = null;
        Map<String, Obj> materials = null;
        try {
            InputStream objInputStream = context.getAssets().open("objects/cargo.obj");
            Obj obj = ObjUtils.convertToRenderable(ObjReader.read(objInputStream));
            objInputStream.close();
            if (null != obj) materials = ObjSplitting.splitByMaterialGroups(obj);
            InputStream mtlInputStream = context.getAssets().open("objects/cargo.mtl");
            mtlList = MtlReader.read(mtlInputStream);

            corpus_tl = new TextureLoader(context, "textures/spaceship_corpus_txr.jpg");
            window_tl = new TextureLoader(context, "textures/spaceship_lights_txr.jpg");
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (null != materials && null != mtlList) {
            for (Map.Entry<String, Obj> entry : materials.entrySet()) {
                String materialName = entry.getKey();
                Obj material = entry.getValue();
                Mtl mtl = findMtlForName(mtlList, materialName);

                if (materialName.contains("corpus")) {
                    // Extract the relevant material properties. These properties can
                    // be used to set up the renderer. For example, they may be passed
                    // as uniform variables to a shader
                    FloatTuple diffuseColor = mtl.getKd();
                    FloatTuple specularColor = mtl.getKs();

                    // Extract the geometry data. This data can be used to create
                    // the vertex buffer objects and vertex array objects for OpenGL
                    corpus_vrtx = (ObjData.getVertices(material));
                    corpus_texr = (ObjData.getTexCoords(material, 2));
                    corpus_norm = (ObjData.getNormals(material));
                    IntBuffer intIndices = ObjData.getFaceVertexIndices(material);
                    ShortBuffer indices = ByteBuffer.allocateDirect(intIndices.limit() * 2).order(ByteOrder.nativeOrder()).asShortBuffer();
                    while (intIndices.hasRemaining()) indices.put((short) intIndices.get());
                    indices.rewind();
                    corpus_indx = (indices);
                } else if (materialName.contains("window")) {
                    window_vrtx = (ObjData.getVertices(material));
                    window_texr = (ObjData.getTexCoords(material, 2));
                    window_norm = (ObjData.getNormals(material));
                    IntBuffer intIndices = ObjData.getFaceVertexIndices(material);
                    ShortBuffer indices = ByteBuffer.allocateDirect(intIndices.limit() * 2).order(ByteOrder.nativeOrder()).asShortBuffer();
                    while (intIndices.hasRemaining()) indices.put((short) intIndices.get());
                    indices.rewind();
                    window_indx = (indices);
                }
            }
        }
    }
}
