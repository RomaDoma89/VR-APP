package com.application.vr.cardboard.models.ui_models;

import android.content.Context;
import android.opengl.GLES30;
import android.opengl.Matrix;
import android.util.Log;

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
    private FloatBuffer chair_vrtx, chair_texr, chair_norm;
    private FloatBuffer glass_vrtx, glass_texr, glass_norm;
    private FloatBuffer metal_blue_vrtx, metal_blue_texr, metal_blue_norm;
    private FloatBuffer metal_lattice_vrtx, metal_lattice_texr, metal_lattice_norm;
    private FloatBuffer metal_plate_blue_vrtx, metal_plate_blue_texr, metal_plate_blue_norm;

    private ShortBuffer metal_main_indx, metal_blue_indx, metal_lattice_indx,
            metal_plate_blue_indx, map_indx, scale_indx, chair_indx, glass_indx;

    private TextureLoader metal_main_tl;
    private TextureLoader metal_blue_tl;
    private TextureLoader metal_lattice_tl;
    private TextureLoader metal_plate_blue_tl;
    private TextureLoader map_tl;
    private TextureLoader scale_tl;
    private TextureLoader chair_tl;
    private TextureLoader glass_tl;

//    private final int glAlphaProgram;
    private final int glProgram;
    private int glPositionParam;
    private int glNormalParam;
    private int glLightPosParam;
    private int glLightColParam;
    private int glMVPMatrixParam;
    private int glModelMatrixParam;
    private int glTextureParam;
    private int positionAlphaParam;
    private int normalAlphaParam;
    private int lightPosAlphaParam;
    private int lightColAlphaParam;
    private int modelViewProjAlphaParam;
    private int modelViewAlphaParam;
    private int textureAlphaParam;
    private final float[] mModelMatrix = new float[16];
    private final float[] mMVPMatrix = new float[16];

    private final float[] lightPosition = new float[3];
    private final float[] lightColor = new float[3];

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

        // Prepare shaders and OpenGL program.
        int vertexShaderId = ShaderUtils.createShader(context, GLES30.GL_VERTEX_SHADER, R.raw.vs_user_ship_uv);
        int fragmentShaderId = ShaderUtils.createShader(context, GLES30.GL_FRAGMENT_SHADER, R.raw.fs_user_ship_uv);
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

        Log.e("L_POS", glLightPosParam+" ");
        Log.e("L_COL", glLightColParam+" ");
        Log.e("L_MVP", glMVPMatrixParam+" ");

//        int vertexShaderAlphaId = ShaderUtils.createShader(context, GLES30.GL_VERTEX_SHADER, R.raw.vs_user_ship_uv);
//        int fragmentShaderAlphaId = ShaderUtils.createShader(context, GLES30.GL_FRAGMENT_SHADER, R.raw.fs_user_ship_glass_uv);
//        // Create empty OpenGL Program.
//        glAlphaProgram = ShaderUtils.createProgram(vertexShaderAlphaId, fragmentShaderAlphaId);
//
//        positionAlphaParam = GLES30.glGetAttribLocation(glAlphaProgram, "a_Position");
//        normalAlphaParam = GLES30.glGetAttribLocation(glAlphaProgram, "a_Normal");
//        lightPosAlphaParam = GLES30.glGetAttribLocation(glAlphaProgram, "uLightPos");
//        lightColAlphaParam = GLES30.glGetAttribLocation(glAlphaProgram, "uLightCol");
//        // get handle to fragment shader's vColor member
//        textureAlphaParam = GLES30.glGetAttribLocation(glAlphaProgram, "a_UV");
//        // get handle to shape's transformation matrix
//        modelViewProjAlphaParam = GLES30.glGetUniformLocation(glAlphaProgram, "u_MVPMatrix");
//        modelViewAlphaParam = GLES30.glGetUniformLocation(glAlphaProgram, "u_MVMatrix");

        // Load and parse Blander object.
        this.prepareData(context);
    }

    public void prepareModel(float[] globalLightPosition, float[] globalLightColor) {
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.scaleM(mModelMatrix,0,4,4,4);
        Matrix.scaleM(mModelMatrix, 0, xScale, xScale, 1f + ratio/10);
        Matrix.translateM(mModelMatrix, 0, 0f, -0.42f, -0.35f);

        lightPosition[0] = globalLightPosition[0];
        lightPosition[1] = globalLightPosition[1];
        lightPosition[2] = globalLightPosition[2];

        lightColor[0] = globalLightColor[0];
        lightColor[1] = globalLightColor[1];
        lightColor[2] = globalLightColor[2];
    }

    /**
     * Encapsulates the OpenGL ES instructions for drawing this shape.
     */
    public void draw(float[] uiVPMatrix, float[] viewMatrix, float[] globalLightPosition, float[] globalLightColor) {
        // Add program to OpenGL environment
        GLES30.glUseProgram(glProgram);

        // Multiply the MVP and the model matrices.
        Matrix.setIdentityM(mMVPMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, uiVPMatrix,0, mModelMatrix,0);
        // Past a ModelViewProjection matrix to the shader parameter
        GLES30.glUniformMatrix4fv(glMVPMatrixParam, 1, false, mMVPMatrix, 0);
        // Apply a view matrix to the model matrix
        Matrix.multiplyMM(mModelMatrix, 0, viewMatrix,0, mModelMatrix,0);
        // Past a ModelView matrix to the shader parameter
        GLES30.glUniformMatrix4fv(glModelMatrixParam, 1, false, mModelMatrix, 0);
        GLES30.glUniform3fv(glLightPosParam, 1, lightPosition, 0);
        GLES30.glUniform3fv(glLightColParam, 1, lightColor, 0);

        // Translation, scaling and rotation of the model.
        prepareModel(globalLightPosition, globalLightColor);

        // Enable texture parameter
        GLES30.glEnableVertexAttribArray(glPositionParam);
        GLES30.glEnableVertexAttribArray(glNormalParam);
        GLES30.glEnableVertexAttribArray(glTextureParam);

        // Draw the vertices and the textures for each material of the object
        metal_main_tl.bind();
        drawVertices(glPositionParam, metal_main_vrtx,
                    glNormalParam, metal_main_norm,
                    glTextureParam, metal_main_texr,
                    metal_main_indx);
        metal_main_tl.unbind();

        map_tl.bind();
        drawVertices(glPositionParam, map_vrtx,
                glNormalParam, map_norm,
                glTextureParam, map_texr,
                    map_indx);
        map_tl.unbind();

        scale_tl.bind();
        drawVertices(glPositionParam, scale_vrtx,
                glNormalParam, scale_norm,
                glTextureParam, scale_texr,
                    scale_indx);
        scale_tl.unbind();

        metal_blue_tl.bind();
        drawVertices(glPositionParam, metal_blue_vrtx,
                glNormalParam, metal_blue_norm,
                glTextureParam, metal_blue_texr,
                    metal_blue_indx);
        metal_blue_tl.unbind();

        metal_lattice_tl.bind();
        drawVertices(glPositionParam, metal_lattice_vrtx,
                glNormalParam, metal_lattice_norm,
                glTextureParam, metal_lattice_texr,
                    metal_lattice_indx);
        metal_lattice_tl.unbind();

        metal_plate_blue_tl.bind();
        drawVertices(glPositionParam, metal_plate_blue_vrtx,
                glNormalParam, metal_plate_blue_norm,
                glTextureParam, metal_plate_blue_texr,
                    metal_plate_blue_indx);
        metal_plate_blue_tl.unbind();

        chair_tl.bind();
        drawVertices(glPositionParam, chair_vrtx,
                glNormalParam, chair_norm,
                glTextureParam, chair_texr,
                    chair_indx);
        chair_tl.unbind();

        // Disable vertex array
        GLES30.glDisableVertexAttribArray(glPositionParam);
        GLES30.glDisableVertexAttribArray(glNormalParam);
        GLES30.glDisableVertexAttribArray(glTextureParam);

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
            chair_tl = new TextureLoader(context, "textures/user_chair.jpg");
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
                } else if (materialName.contains("chair_main")) {
                    chair_vrtx = (ObjData.getVertices(material));
                    chair_texr = (ObjData.getTexCoords(material, 2));
                    chair_norm = (ObjData.getNormals(material));
                    IntBuffer intIndices = ObjData.getFaceVertexIndices(material);
                    ShortBuffer indices = ByteBuffer.allocateDirect(intIndices.limit() * 2).order(ByteOrder.nativeOrder()).asShortBuffer();
                    while (intIndices.hasRemaining()) indices.put((short) intIndices.get());
                    indices.rewind();
                    chair_indx = (indices);
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
