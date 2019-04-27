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
import java.util.List;
import java.util.Map;

import de.javagl.obj.Mtl;
import de.javagl.obj.MtlReader;
import de.javagl.obj.Obj;
import de.javagl.obj.ObjData;
import de.javagl.obj.ObjReader;
import de.javagl.obj.ObjSplitting;
import de.javagl.obj.ObjUtils;

import static android.opengl.GLES20.GL_ONE_MINUS_SRC_ALPHA;
import static android.opengl.GLES20.GL_SRC_ALPHA;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.GL_UNSIGNED_SHORT;
import static android.opengl.GLES20.glDrawElements;

/**
 * A model for use as a drawn object in OpenGL ES 2.0.
 */
public class UiShip {
    private FloatBuffer metal_main_vrtx, metal_main_texr, metal_main_norm;
    private FloatBuffer map_vrtx, map_texr, map_norm;
    private FloatBuffer scale_vrtx, scale_texr, scale_norm;
    private FloatBuffer glass_vrtx, glass_texr, glass_norm;
    private FloatBuffer metal_blue_vrtx, metal_blue_texr, metal_blue_norm;
    private FloatBuffer metal_lattice_vrtx, metal_lattice_texr, metal_lattice_norm;
    private FloatBuffer metal_plate_blue_vrtx, metal_plate_blue_texr, metal_plate_blue_norm;

    private ShortBuffer metal_main_indx, metal_blue_indx, metal_lattice_indx,
            metal_plate_blue_indx, map_indx, scale_indx, glass_indx;

    private TextureLoader metal_main_tl;
    private TextureLoader metal_blue_tl;
    private TextureLoader metal_lattice_tl;
    private TextureLoader metal_plate_blue_tl;
    private TextureLoader map_tl;
    private TextureLoader scale_tl;
    private TextureLoader glass_tl;

    private final int glMainProgram;
    private final int glAlphaProgram;
    private int positionParam;
    private int normalParam;
    private int lightPosParam;
    private int lightColParam;
    private int modelViewProjParam;
    private int modelViewParam;
    private int textureParam;
    private int positionAlphaParam;
    private int normalAlphaParam;
    private int lightPosAlphaParam;
    private int lightColAlphaParam;
    private int modelViewProjAlphaParam;
    private int modelViewAlphaParam;
    private int textureAlphaParam;
    private final float[] mModelMatrix = new float[16];
    private final float[] mMVPMatrix = new float[16];

    private float[] lightPosition;
    private float[] lightColor;

    private float xScale;
    private float yScale;
    private float ratio;

    /**
     * Sets up the drawing object data for use in an OpenGL ES context.
     */
    public UiShip(Context context, float xScale, float yScale, float ratio) {
        this.xScale = xScale;
        this.yScale = yScale;
        this.ratio = ratio;

        lightColor = new float[] { 0, 0, 0, 1};
        lightPosition = new float[] {0, 0, 0 };

        // Prepare shaders and OpenGL program.
        int vertexShaderId = ShaderUtils.createShader(context, GLES30.GL_VERTEX_SHADER, R.raw.vs_user_ship_uv);
        int fragmentShaderId = ShaderUtils.createShader(context, GLES30.GL_FRAGMENT_SHADER, R.raw.fs_user_ship_uv);
        // Create empty OpenGL Program.
        glMainProgram = ShaderUtils.createProgram(vertexShaderId, fragmentShaderId);
        // get handle to vertex shader's vPosition member
        positionParam = GLES30.glGetAttribLocation(glMainProgram, "a_Position");
        normalParam = GLES30.glGetAttribLocation(glMainProgram, "a_Normal");
        lightPosParam = GLES30.glGetAttribLocation(glMainProgram, "uLightPos");
        lightColParam = GLES30.glGetAttribLocation(glMainProgram, "uLightCol");
        // get handle to fragment shader's vColor member
        textureParam = GLES30.glGetAttribLocation(glMainProgram, "a_UV");
        // get handle to shape's transformation matrix
        modelViewProjParam = GLES30.glGetUniformLocation(glMainProgram, "u_MVPMatrix");
        modelViewParam = GLES30.glGetUniformLocation(glMainProgram, "u_MVMatrix");

        int vertexShaderAlphaId = ShaderUtils.createShader(context, GLES30.GL_VERTEX_SHADER, R.raw.vs_user_ship_uv);
        int fragmentShaderAlphaId = ShaderUtils.createShader(context, GLES30.GL_FRAGMENT_SHADER, R.raw.fs_user_ship_glass_uv);
        // Create empty OpenGL Program.
        glAlphaProgram = ShaderUtils.createProgram(vertexShaderAlphaId, fragmentShaderAlphaId);

        positionAlphaParam = GLES30.glGetAttribLocation(glAlphaProgram, "a_Position");
        normalAlphaParam = GLES30.glGetAttribLocation(glAlphaProgram, "a_Normal");
        lightPosAlphaParam = GLES30.glGetAttribLocation(glAlphaProgram, "uLightPos");
        lightColAlphaParam = GLES30.glGetAttribLocation(glAlphaProgram, "uLightCol");
        // get handle to fragment shader's vColor member
        textureAlphaParam = GLES30.glGetAttribLocation(glAlphaProgram, "a_UV");
        // get handle to shape's transformation matrix
        modelViewProjAlphaParam = GLES30.glGetUniformLocation(glAlphaProgram, "u_MVPMatrix");
        modelViewAlphaParam = GLES30.glGetUniformLocation(glAlphaProgram, "u_MVMatrix");

        // Load and parse Blander object.
        this.prepareData(context);
    }

    public void transformModel() {
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.scaleM(mModelMatrix,0,4,4,4);
        Matrix.scaleM(mModelMatrix, 0, xScale, xScale, 1f + ratio/10);
        Matrix.translateM(mModelMatrix, 0, 0f, -0.42f, -0.35f);
    }

    /**
     * Encapsulates the OpenGL ES instructions for drawing this shape.
     */
    public void draw(float[] uiVPMatrix, float[] viewMatrix) {
        // Add program to OpenGL environment.
        GLES30.glUseProgram(glMainProgram);

        // Translation, scaling and rotation of the model.
        transformModel();

        // Multiply the MVP and the model matrices.
        Matrix.setIdentityM(mMVPMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, uiVPMatrix,0, mModelMatrix,0);
        // Multiply the View matrix and the model matrices.
        Matrix.multiplyMM(mModelMatrix, 0, viewMatrix,0, mModelMatrix,0);
        // Past a ModelViewProjection matrix to the shader parameter
        GLES30.glUniformMatrix4fv(modelViewProjParam, 1, false, mMVPMatrix, 0);
        // Past a ModelView matrix to the shader parameter
        GLES30.glUniformMatrix4fv(modelViewParam, 1, false, mModelMatrix, 0);
        // Past a local light position matrix to the shader parameter
        GLES30.glUniform3fv(lightPosParam, 1, lightPosition, 0);
        // Past a local light color matrix to the shader parameter
        GLES30.glUniform4fv(lightColParam, 1, lightColor, 0);

        // Enable texture parameter
        GLES30.glEnableVertexAttribArray(positionParam);
        GLES30.glEnableVertexAttribArray(normalParam);
        GLES30.glEnableVertexAttribArray(textureParam);

        // Draw the vertices and the textures for each material of the object
        metal_main_tl.bind();
        drawVertices(positionParam, metal_main_vrtx,
                    normalParam, metal_main_norm,
                    textureParam, metal_main_texr,
                    metal_main_indx);
        metal_main_tl.unbind();

        map_tl.bind();
        drawVertices(positionParam, map_vrtx,
                    normalParam, map_norm,
                    textureParam, map_texr,
                    map_indx);
        map_tl.unbind();

        scale_tl.bind();
        drawVertices(positionParam, scale_vrtx,
                    normalParam, scale_norm,
                    textureParam, scale_texr,
                    scale_indx);
        scale_tl.unbind();

        metal_blue_tl.bind();
        drawVertices(positionParam, metal_blue_vrtx,
                    normalParam, metal_blue_norm,
                    textureParam, metal_blue_texr,
                    metal_blue_indx);
        metal_blue_tl.unbind();

        metal_lattice_tl.bind();
        drawVertices(positionParam, metal_lattice_vrtx,
                    normalParam, metal_lattice_norm,
                    textureParam, metal_lattice_texr,
                    metal_lattice_indx);
        metal_lattice_tl.unbind();

        metal_plate_blue_tl.bind();
        drawVertices(positionParam, metal_plate_blue_vrtx,
                    normalParam, metal_plate_blue_norm,
                    textureParam, metal_plate_blue_texr,
                    metal_plate_blue_indx);
        metal_plate_blue_tl.unbind();

        GLES30.glDisableVertexAttribArray(positionParam);
        GLES30.glDisableVertexAttribArray(normalParam);
        GLES30.glDisableVertexAttribArray(textureParam);

//        // Add program to OpenGL environment.
//        GLES30.glUseProgram(glAlphaProgram);
//        // Translation, scaling and rotation of the model.
//
//        // Past a ModelViewProjection matrix to the shader parameter
//        GLES30.glUniformMatrix4fv(modelViewProjAlphaParam, 1, false, mMVPMatrix, 0);
//        // Past a ModelView matrix to the shader parameter
//        GLES30.glUniformMatrix4fv(modelViewAlphaParam, 1, false, mModelMatrix, 0);
//        // Past a local light position matrix to the shader parameter
//        GLES30.glUniform3fv(lightPosAlphaParam, 1, globalLightPosition, 0);
//        // Past a local light color matrix to the shader parameter
//        GLES30.glUniform4fv(lightColAlphaParam, 1, globalLightColor, 0);
//
//        // Enable texture parameter
//        GLES30.glEnableVertexAttribArray(positionAlphaParam);
//        GLES30.glEnableVertexAttribArray(normalAlphaParam);
//        GLES30.glEnableVertexAttribArray(textureAlphaParam);
//
//        GLES30.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
////        // Enable blending
//        GLES30.glEnable(GLES30.GL_BLEND);
//        glass_tl.bind();
//        drawVertices(positionAlphaParam, glass_vrtx,
//                    normalAlphaParam, glass_norm,
//                    textureAlphaParam, glass_texr,
//                    glass_indx);
//        glass_tl.unbind();
//
//        // Disable texture parameter
//        GLES30.glDisableVertexAttribArray(positionAlphaParam);
//        GLES30.glDisableVertexAttribArray(normalAlphaParam);
//        GLES30.glDisableVertexAttribArray(textureAlphaParam);
//
//        // Enable blending
//        GLES30.glDisable(GLES30.GL_BLEND);
    }

    private void drawVertices(int positionParam, FloatBuffer vertexBuff,
                              int normalParam, FloatBuffer normalBuffer,
                              int textureParam, FloatBuffer textureBuffer,
                              ShortBuffer indices) {
        GLES30.glVertexAttribPointer(positionParam, 3, GLES30.GL_FLOAT, false, 0, vertexBuff);
        GLES30.glVertexAttribPointer(normalParam, 3, GLES30.GL_FLOAT, false, 0, normalBuffer);
        GLES30.glVertexAttribPointer(textureParam, 2, GLES30.GL_FLOAT, false, 0, textureBuffer);
        glDrawElements(GL_TRIANGLES, indices.limit(), GL_UNSIGNED_SHORT, indices);
    }

    private void prepareData(Context context) {
        List<Mtl> mtlList = null;
        Map<String, Obj> materials = null;
        try {
            InputStream objInputStream = context.getAssets().open("objects/user_ship.obj");
            Obj obj = ObjUtils.convertToRenderable(ObjReader.read(objInputStream));
            objInputStream.close();

            if (null != obj) {
                materials = ObjSplitting.splitByMaterialGroups(obj);
                InputStream mtlInputStream = context.getAssets().open("objects/user_ship.mtl");
                mtlList = MtlReader.read(mtlInputStream);
            }
            metal_main_tl = new TextureLoader(context, "textures/user_metal_black.jpg");
            metal_blue_tl = new TextureLoader(context, "textures/user_metal_blue.jpg");
            metal_lattice_tl = new TextureLoader(context, "textures/user_metal_lattice.jpg");
            metal_plate_blue_tl = new TextureLoader(context, "textures/user_plate_blue.jpg");
            map_tl = new TextureLoader(context, "textures/user_map_txr.jpg");
            scale_tl = new TextureLoader(context, "textures/user_scale_txr.jpg");
            glass_tl = new TextureLoader(context, "textures/user_glass.png");
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (null != materials && null != mtlList) {
            for (Map.Entry<String, Obj> entry : materials.entrySet()) {
                String materialName = entry.getKey();
                Obj material = entry.getValue();
                Mtl mtl = findMtlForName(mtlList, materialName);

                if (materialName.contains("metal_main")) {
                    // Extract the geometry data. This data can be used to create
                    // the vertex buffer objects and vertex array objects for OpenGL
                    metal_main_vrtx = (ObjData.getVertices(material));
                    metal_main_texr = (ObjData.getTexCoords(material, 2));
                    metal_main_norm = (ObjData.getNormals(material));
                    IntBuffer intIndices = ObjData.getFaceVertexIndices(material);
                    ShortBuffer indices = ByteBuffer.allocateDirect(intIndices.limit() * 2).order(ByteOrder.nativeOrder()).asShortBuffer();
                    while (intIndices.hasRemaining()) indices.put((short) intIndices.get());
                    indices.rewind();
                    metal_main_indx = (indices);
                } else if (materialName.contains("panel_map")) {
                    map_vrtx = (ObjData.getVertices(material));
                    map_texr = (ObjData.getTexCoords(material, 2));
                    map_norm = (ObjData.getNormals(material));
                    IntBuffer intIndices = ObjData.getFaceVertexIndices(material);
                    ShortBuffer indices = ByteBuffer.allocateDirect(intIndices.limit() * 2).order(ByteOrder.nativeOrder()).asShortBuffer();
                    while (intIndices.hasRemaining()) indices.put((short) intIndices.get());
                    indices.rewind();
                    map_indx = (indices);
                } else if (materialName.contains("panel_scale")) {
                    scale_vrtx = (ObjData.getVertices(material));
                    scale_texr = (ObjData.getTexCoords(material, 2));
                    scale_norm = (ObjData.getNormals(material));
                    IntBuffer intIndices = ObjData.getFaceVertexIndices(material);
                    ShortBuffer indices = ByteBuffer.allocateDirect(intIndices.limit() * 2).order(ByteOrder.nativeOrder()).asShortBuffer();
                    while (intIndices.hasRemaining()) indices.put((short) intIndices.get());
                    indices.rewind();
                    scale_indx = (indices);
                } else if (materialName.contains("metal_blue_lights")) {
                    metal_blue_vrtx = (ObjData.getVertices(material));
                    metal_blue_texr = (ObjData.getTexCoords(material, 2));
                    metal_blue_norm = (ObjData.getNormals(material));
                    IntBuffer intIndices = ObjData.getFaceVertexIndices(material);
                    ShortBuffer indices = ByteBuffer.allocateDirect(intIndices.limit() * 2).order(ByteOrder.nativeOrder()).asShortBuffer();
                    while (intIndices.hasRemaining()) indices.put((short) intIndices.get());
                    indices.rewind();
                    metal_blue_indx = (indices);
                } else if (materialName.contains("metal_plate_blue")) {
                    metal_plate_blue_vrtx = (ObjData.getVertices(material));
                    metal_plate_blue_texr = (ObjData.getTexCoords(material, 2));
                    metal_plate_blue_norm = (ObjData.getNormals(material));
                    IntBuffer intIndices = ObjData.getFaceVertexIndices(material);
                    ShortBuffer indices = ByteBuffer.allocateDirect(intIndices.limit() * 2).order(ByteOrder.nativeOrder()).asShortBuffer();
                    while (intIndices.hasRemaining()) indices.put((short) intIndices.get());
                    indices.rewind();
                    metal_plate_blue_indx = (indices);
                } else if (materialName.contains("metal_lattice")) {
                    metal_lattice_vrtx = (ObjData.getVertices(material));
                    metal_lattice_texr = (ObjData.getTexCoords(material, 2));
                    metal_lattice_norm = (ObjData.getNormals(material));
                    IntBuffer intIndices = ObjData.getFaceVertexIndices(material);
                    ShortBuffer indices = ByteBuffer.allocateDirect(intIndices.limit() * 2).order(ByteOrder.nativeOrder()).asShortBuffer();
                    while (intIndices.hasRemaining()) indices.put((short) intIndices.get());
                    indices.rewind();
                    metal_lattice_indx = (indices);
                } else if (materialName.contains("glass_main")) {
                    glass_vrtx = (ObjData.getVertices(material));
                    glass_texr = (ObjData.getTexCoords(material, 2));
                    glass_norm = (ObjData.getNormals(material));
                    IntBuffer intIndices = ObjData.getFaceVertexIndices(material);
                    ShortBuffer indices = ByteBuffer.allocateDirect(intIndices.limit() * 2).order(ByteOrder.nativeOrder()).asShortBuffer();
                    while (intIndices.hasRemaining()) indices.put((short) intIndices.get());
                    indices.rewind();
                    glass_indx = (indices);
                }
            }
        }
    }

    private Mtl findMtlForName(Iterable<? extends Mtl> mtls, String name) {
        for (Mtl mtl : mtls) {
            if (mtl.getName().equals(name)) return mtl;
        }
        return null;
    }
}
