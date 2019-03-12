package com.application.vr.cardboard.models;

import android.content.Context;
import android.opengl.GLES30;
import android.opengl.Matrix;
import android.util.Log;

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
import java.util.Map;

import de.javagl.obj.FloatTuple;
import de.javagl.obj.Mtl;
import de.javagl.obj.MtlReader;
import de.javagl.obj.Obj;
import de.javagl.obj.ObjData;
import de.javagl.obj.ObjReader;
import de.javagl.obj.ObjSplitting;
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
 * A model for use as a drawn object in OpenGL ES 2.0.
 */
public class SpaceshipHunter implements DynamicModel {
    private List<FloatBuffer> corpusVertexList, corpusTextureList, lightVertexList, lightTextureList;
    private FloatBuffer mapVertices;
    private List<ShortBuffer> corpusIndicesList, lightIndicesList;

    private TextureLoader corpusTextureLoader;
    private TextureLoader lightsTextureLoader;

    private final int mMainProgram;
    private int mMainPositionHandle;
    private int mMainMVPMatrixHandle;
    private int mUVHandle;
    private float[] mModelMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];
    private float[] translationMatrix = new float[16];
    private float[] rotationMatrix = new float[16];

    private float translateX, translateY, translateZ;
    private float rotationX, rotationY, rotationZ;
    private float scale;

    /**
     * Sets up the drawing object data for use in an OpenGL ES context.
     */
    public SpaceshipHunter(Context context, float translateX, float translateY, float translateZ,
                           float rotationX, float rotationY, float rotationZ, float scale) {
        this.translateX = translateX;
        this.translateY = translateY;
        this.translateZ = translateZ;
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
        Matrix.setIdentityM(translationMatrix, 0);
        Matrix.translateM(translationMatrix, 0, translateX, translateY, translateZ);
        Matrix.setIdentityM(rotationMatrix, 0);
//        Matrix.rotateM(rotationMatrix, 0, rotation, 0.0f, 0.0f, 0.0f);
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.multiplyMM(mModelMatrix, 0, translationMatrix, 0, rotationMatrix, 0);
        // Multiply the MVP and the DynamicModel matrices.
        Matrix.setIdentityM(mMVPMatrix, 0);

        // Add program to OpenGL environment
        GLES30.glUseProgram(mMainProgram);
        // Draw the vertices and the textures for each material of the object
        corpusTextureLoader.bind();

        // Enable vertex array
        GLES30.glEnableVertexAttribArray(mUVHandle);
        GLES30.glEnableVertexAttribArray(mMainPositionHandle);
        for (int i=0; i<corpusVertexList.size(); i++)
            drawModel(corpusVertexList.get(i), corpusTextureList.get(i), corpusIndicesList.get(i));
        corpusTextureLoader.unbind();
        lightsTextureLoader.bind();
        for (int i=0; i<lightVertexList.size(); i++)
            drawModel(lightVertexList.get(i), lightTextureList.get(i), lightIndicesList.get(i));
        lightsTextureLoader.unbind();
        // Multiply the MVP and the DynamicModel matrices.
        Matrix.setIdentityM(mMVPMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mVPMatrix, 0, mModelMatrix, 0);
        GLES30.glUniformMatrix4fv(mMainMVPMatrixHandle, 1, false, mMVPMatrix, 0);
    }

    @Override
    public void drawMapModel(int positionHandle, int colorHandle) {
        GLES30.glEnableVertexAttribArray(positionHandle);
        GLES30.glVertexAttribPointer(positionHandle, 3, GL_FLOAT, false, 0, mapVertices);
        glUniform4f(colorHandle, 0.0f, 1.0f, 1.0f, 1.0f);
        glDrawArrays(GL_POINTS, 0, 1);
        // Disable vertex array
        GLES30.glDisableVertexAttribArray(positionHandle);
    }

    private void drawModel(FloatBuffer vertexBuff, FloatBuffer textureBuffer, ShortBuffer indices) {
        GLES30.glVertexAttribPointer(mMainPositionHandle, 3, GLES30.GL_FLOAT, false, 0, vertexBuff);
        GLES30.glVertexAttribPointer(mUVHandle, 2, GLES30.GL_FLOAT, false, 0, textureBuffer);
        glDrawElements(GL_TRIANGLES, indices.limit(), GL_UNSIGNED_SHORT, indices);
    }

    private void prepareData(Context context) {
        corpusVertexList = new ArrayList<>();
        corpusTextureList = new ArrayList<>();
        corpusIndicesList = new ArrayList<>();
        lightVertexList = new ArrayList<>();
        lightTextureList = new ArrayList<>();
        lightIndicesList = new ArrayList<>();

        List<Mtl> mtlList = null;
        Map<String, Obj> materials = null;
        try {
            InputStream objInputStream = context.getAssets().open("objects/hunter.obj");
            Obj obj = ObjUtils.convertToRenderable(ObjReader.read(objInputStream));
            objInputStream.close();

            if (null != obj) {
                materials = ObjSplitting.splitByMaterialGroups(obj);
                InputStream mtlInputStream = context.getAssets().open("objects/hunter.mtl");
                mtlList = MtlReader.read(mtlInputStream);
            }
            corpusTextureLoader = new TextureLoader(context, "textures/spaceship_corpus_txr.jpg");
            lightsTextureLoader = new TextureLoader(context, "textures/spaceship_lights_txr.jpg");
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (null != materials && null != mtlList) {
            for (Map.Entry<String, Obj> entry : materials.entrySet()) {
                String materialName = entry.getKey();
                Obj material = entry.getValue();
                Mtl mtl = findMtlForName(mtlList, materialName);

                if (materialName.contains("corpus") || materialName.contains("engine_body")) {
                    // Extract the relevant material properties. These properties can
                    // be used to set up the renderer. For example, they may be passed
                    // as uniform variables to a shader
                    FloatTuple diffuseColor = mtl.getKd();
                    FloatTuple specularColor = mtl.getKs();

                    // Extract the geometry data. This data can be used to create
                    // the vertex buffer objects and vertex array objects for OpenGL
                    corpusVertexList.add(ObjData.getVertices(material));
                    corpusTextureList.add(ObjData.getTexCoords(material, 2));
                    IntBuffer intIndices = ObjData.getFaceVertexIndices(material);
                    ShortBuffer indices = ByteBuffer.allocateDirect(intIndices.limit() * 2).order(ByteOrder.nativeOrder()).asShortBuffer();
                    while (intIndices.hasRemaining()) indices.put((short) intIndices.get());
                    indices.rewind();
                    corpusIndicesList.add(indices);
                } else if (materialName.contains("engine_light") || materialName.contains("windows")) {
                    lightVertexList.add(ObjData.getVertices(material));
                    lightTextureList.add(ObjData.getTexCoords(material, 2));
                    IntBuffer intIndices = ObjData.getFaceVertexIndices(material);
                    ShortBuffer indices = ByteBuffer.allocateDirect(intIndices.limit() * 2).order(ByteOrder.nativeOrder()).asShortBuffer();
                    while (intIndices.hasRemaining()) indices.put((short) intIndices.get());
                    indices.rewind();
                    lightIndicesList.add(indices);
                }
            }
        }

        float [] vertex = {translateX/800, translateY/800, translateZ/800};
        mapVertices = ByteBuffer.allocateDirect(vertex.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mapVertices.put(vertex);
        mapVertices.position(0);
    }

    private Mtl findMtlForName(Iterable<? extends Mtl> mtls, String name) {
        for (Mtl mtl : mtls) {
            if (mtl.getName().equals(name)) return mtl;
        }
        return null;
    }
}
