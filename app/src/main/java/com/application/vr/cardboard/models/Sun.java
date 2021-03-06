package com.application.vr.cardboard.models;

import android.content.Context;
import android.opengl.GLES30;
import android.opengl.Matrix;
import android.util.Log;

import com.application.vr.cardboard.R;
import com.application.vr.cardboard.file_utils.ShaderUtils;
import com.application.vr.cardboard.file_utils.TextureLoader;
import com.application.vr.cardboard.models.interfaces.StaticModel;

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

import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.GL_UNSIGNED_SHORT;
import static android.opengl.GLES20.glDrawElements;

public class Sun implements StaticModel {
    private FloatBuffer verticesBuff, texturesBuff, normalsBuff;
    private ShortBuffer indicesBuff;
    private TextureLoader textureLoader;

    private final int glProgram;
    private int glPositionParam;
    private int glMVPMatrixParam;
    private int glTextureParam;

    private float[] mModelMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];
    private float[] translationMatrix = new float[16];
    private float[] rotationMatrix = new float[16];
    private float[] scaleMatrix = new float[16];

    private float translationX, translationY, translationZ;
    private float rotationX, rotationY, rotationZ;
    private float scale;

    /**
     * Sets up the drawing object data for use in an OpenGL ES context.
     */
    public Sun(Context context, String texture, float translationX, float translationY, float translationZ,
               float rotationX, float rotationY, float rotationZ, float scale) {
        this.translationX = translationX;
        this.translationY = translationY;
        this.translationZ = translationZ;
        this.rotationX = rotationX;
        this.rotationY = rotationY;
        this.rotationZ = rotationZ;
        this.scale = scale;

        // Prepare shaders and OpenGL program.
        int vertexShaderId = ShaderUtils.createShader(context, GLES30.GL_VERTEX_SHADER, R.raw.vs_simple_uv);
        int fragmentShaderId = ShaderUtils.createShader(context, GLES30.GL_FRAGMENT_SHADER, R.raw.fs_simple_uv);
        // Create empty OpenGL Program.
        glProgram = ShaderUtils.createProgram(vertexShaderId, fragmentShaderId);
        // get handle to vertex shader's vPosition member
        glPositionParam = GLES30.glGetAttribLocation(glProgram, "vPosition");
        // get handle to fragment shader's vColor member
        glTextureParam = GLES30.glGetAttribLocation(glProgram, "a_UV");
        // get handle to shape's transformation matrix
        glMVPMatrixParam = GLES30.glGetUniformLocation(glProgram, "uMVPMatrix");

        // Load and parse Blander object.
        this.prepareData(context, texture);
    }

    private float rotation = 0f;
    private void prepareModel() {
        rotation += 0.03f;
//        if (rotation > 360) rotation -= 360f;
        Matrix.setIdentityM(scaleMatrix, 0);
        Matrix.setIdentityM(translationMatrix, 0);
        Matrix.setIdentityM(rotationMatrix, 0);

        Matrix.scaleM(scaleMatrix, 0, scale, scale, scale);
        Matrix.translateM(translationMatrix, 0, translationX, translationY, translationZ);
        Matrix.rotateM(rotationMatrix, 0, rotation, rotationX, rotationY, rotationZ);

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.multiplyMM(mModelMatrix, 0, scaleMatrix, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mModelMatrix, 0, rotationMatrix, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mModelMatrix, 0, translationMatrix, 0, mModelMatrix, 0);
    }

    /**
     * Encapsulates the OpenGL ES instructions for drawing this shape.
     */
    public void draw(float[] mVPMatrix, float[] mViewMatrix, float[] globalLightPosition, float[] globalLightColor) {
        // Add program to OpenGL environment
        GLES30.glUseProgram(glProgram);

        // Multiply the MVP and the model matrices.
        Matrix.setIdentityM(mMVPMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mVPMatrix,0, mModelMatrix,0);
        // Past a ModelViewProjection matrix to the shader parameter
        GLES30.glUniformMatrix4fv(glMVPMatrixParam, 1, false, mMVPMatrix, 0);

        // Translation, scaling and rotation of the model.
        prepareModel();

        // Draw the vertices and the textures for each material of the object
        drawModel(verticesBuff, texturesBuff, indicesBuff);
    }

    private void drawModel(FloatBuffer vertexBuff, FloatBuffer textureBuffer, ShortBuffer indices) {
        // Enable vertex array
        GLES30.glEnableVertexAttribArray(glPositionParam);
        GLES30.glEnableVertexAttribArray(glTextureParam);

        textureLoader.bind();
        GLES30.glVertexAttribPointer(glPositionParam, 3, GLES30.GL_FLOAT, false, 0, vertexBuff);
        GLES30.glVertexAttribPointer(glTextureParam, 2, GLES30.GL_FLOAT, false, 0, textureBuffer);
        glDrawElements(GL_TRIANGLES, indices.limit(), GL_UNSIGNED_SHORT, indices);
        textureLoader.unbind();

        // Disable vertex array
        GLES30.glDisableVertexAttribArray(glPositionParam);
        GLES30.glDisableVertexAttribArray(glTextureParam);
    }

    private void prepareData(Context context, String texture) {
        List<Mtl> mtlList = null;
        Map<String, Obj> materials = null;
        try {
            InputStream objInputStream = context.getAssets().open("objects/planet.obj");
            Obj obj = ObjUtils.convertToRenderable(ObjReader.read(objInputStream));
            objInputStream.close();

            if (null != obj) {
                materials = ObjSplitting.splitByMaterialGroups(obj);
                InputStream mtlInputStream = context.getAssets().open("objects/planet.mtl");
                mtlList = MtlReader.read(mtlInputStream);
            }
            textureLoader = new TextureLoader(context, texture);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (null != materials && null != mtlList) {
            for (Map.Entry<String, Obj> entry : materials.entrySet()) {
                String materialName = entry.getKey();
                Obj material = entry.getValue();
                Mtl mtl = findMtlForName(mtlList, materialName);

                // the vertex buffer objects and vertex array objects for OpenGL
                verticesBuff = ObjData.getVertices(material);
                texturesBuff = ObjData.getTexCoords(material, 2);
                normalsBuff = ObjData.getNormals(material);
                IntBuffer intIndices = ObjData.getFaceVertexIndices(material);
                ShortBuffer indices = ByteBuffer.allocateDirect(intIndices.limit() * 2).order(ByteOrder.nativeOrder()).asShortBuffer();
                while (intIndices.hasRemaining()) indices.put((short) intIndices.get());
                indices.rewind();
                indicesBuff = indices;
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
