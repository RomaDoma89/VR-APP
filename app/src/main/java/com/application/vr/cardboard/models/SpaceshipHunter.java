package com.application.vr.cardboard.models;

import android.content.Context;
import android.opengl.GLES30;
import android.opengl.Matrix;

import com.application.vr.cardboard.R;
import com.application.vr.cardboard.file_utils.ShaderUtils;
import com.application.vr.cardboard.file_utils.TextureLoader;
import com.application.vr.cardboard.models.interfaces.ModelSpaceship;

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
public class SpaceshipHunter implements ModelSpaceship {
    private List<FloatBuffer> corpusVertexList, corpusTextureList, engineVertexList,
            engineTextureList, lightVertexList, lightTextureList;
    private List<ShortBuffer> corpusIndicesList, engineIndicesList, lightIndicesList;

    private TextureLoader corpusTextureLoader;
    private TextureLoader lightsTextureLoader;

    private final int mProgram;
    private int mPositionHandle;
    private int mUVHandle;
    private float[] mModelMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];

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
    float rotation = 0f;
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

        float[] tranM = new float[16];
        Matrix.setIdentityM(tranM, 0);
        Matrix.translateM(tranM, 0, translateX, translateY, translateZ);
        float[] rotM = new float[16];
        Matrix.setIdentityM(rotM, 0);
//        Matrix.rotateM(rotM, 0, rotation, 0f, 0f, 0.5f);
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.multiplyMM(mModelMatrix, 0, tranM, 0, rotM, 0);

        for (int i=0; i<corpusVertexList.size(); i++)
            drawModel(corpusTextureLoader, corpusVertexList.get(i), corpusTextureList.get(i), corpusIndicesList.get(i));

        for (int i=0; i<engineVertexList.size(); i++)
            drawModel(corpusTextureLoader, engineVertexList.get(i), engineTextureList.get(i), engineIndicesList.get(i));

        for (int i=0; i<lightVertexList.size(); i++)
            drawModel(lightsTextureLoader, lightVertexList.get(i), lightTextureList.get(i), lightIndicesList.get(i));

        Matrix.setIdentityM(mMVPMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mVPMatrix, 0, mModelMatrix, 0);
        GLES30.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);
    }

    private void prepareData(Context context) {
        corpusVertexList = new ArrayList<>();
        corpusTextureList = new ArrayList<>();
        corpusIndicesList = new ArrayList<>();
        engineVertexList = new ArrayList<>();
        engineTextureList = new ArrayList<>();
        engineIndicesList = new ArrayList<>();
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

                if (materialName.contains("corpus") || materialName.contains("weapon")) {
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
                } else if (materialName.contains("engine_body")) {
                    engineVertexList.add(ObjData.getVertices(material));
                    engineTextureList.add(ObjData.getTexCoords(material, 2));
                    IntBuffer intIndices = ObjData.getFaceVertexIndices(material);
                    ShortBuffer indices = ByteBuffer.allocateDirect(intIndices.limit() * 2).order(ByteOrder.nativeOrder()).asShortBuffer();
                    while (intIndices.hasRemaining()) indices.put((short) intIndices.get());
                    indices.rewind();
                    engineIndicesList.add(indices);
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

    private Mtl findMtlForName(Iterable<? extends Mtl> mtls, String name) {
        for (Mtl mtl : mtls) {
            if (mtl.getName().equals(name)) return mtl;
        }
        return null;
    }
}
