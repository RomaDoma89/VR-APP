package com.application.vr.cardboard;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import android.opengl.GLES30;

import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_LINES;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glLineWidth;
import static android.opengl.GLES20.glUniform4f;

/**
 * A two-dimensional square for use as a drawn object in OpenGL ES 2.0.
 */
public class Model {

    private final String vertexShaderCode =
            // This matrix member variable provides a hook to manipulate
            // the coordinates of the objects that use this vertex shader
            "uniform mat4 uMVPMatrix;" +
                    "attribute vec4 vPosition;" +
                    "void main() {" +
                    // The matrix must be included as a modifier of gl_Position.
                    // Note that the uMVPMatrix factor *must be first* in order
                    // for the matrix multiplication product to be correct.
                    " gl_Position = uMVPMatrix * vPosition;" +
                    "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "}";

    private FloatBuffer vertexData;

    private final int mProgram;
    private int mPositionHandle;
    private int mColorHandle;
    private int mMVPMatrixHandle;
    private float[] mModelMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];

    /**
     * Sets up the drawing object data for use in an OpenGL ES context.
     */
    public Model() {
        prepareData();
        // prepare shaders and OpenGL program
        int vertexShader = loadShader(
                GLES30.GL_VERTEX_SHADER,
                vertexShaderCode);
        int fragmentShader = loadShader(
                GLES30.GL_FRAGMENT_SHADER,
                fragmentShaderCode);

        mProgram = GLES30.glCreateProgram();             // create empty OpenGL Program
        GLES30.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
        GLES30.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
        GLES30.glLinkProgram(mProgram);                  // create OpenGL program executables
    }


    /**
     * Encapsulates the OpenGL ES instructions for drawing this shape.
     *
     * @param mMVPMatrix - The View matrix
     */
    public void draw(float[] mMVPMatrix) {
        // Add program to OpenGL environment
        GLES30.glUseProgram(mProgram);
        // get handle to vertex shader's vPosition member
        mPositionHandle = GLES30.glGetAttribLocation(mProgram, "vPosition");
        // get handle to fragment shader's vColor member
        mColorHandle = GLES30.glGetUniformLocation(mProgram, "vColor");

        // Enable vertex array
        GLES30.glEnableVertexAttribArray(mPositionHandle);
        GLES30.glVertexAttribPointer(mPositionHandle, 3, GL_FLOAT, false, 0, vertexData);
        drawModel();
        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES30.glGetUniformLocation(mProgram, "uMVPMatrix");

        GLES30.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);

        // Disable vertex array
        GLES30.glDisableVertexAttribArray(mPositionHandle);

    }

    private static int loadShader(int type, String shaderCode) {
        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES30.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES30.glShaderSource(shader, shaderCode);
        GLES30.glCompileShader(shader);

        return shader;
    }


    private void prepareData() {
        float l = 3;
        float xScale1 = 0f;  // удаление по оси X
        float yScale1 = 0f;  // удаление по оси Y
        float zScale1 = 10f;  // удаление по оси Z

        float xScale2 = 0f;  // удаление по оси X
        float yScale2 = -10f;  // удаление по оси Y
        float zScale2 = 0f;  // удаление по оси Z

        float xScale3 = 0f;  // удаление по оси X
        float yScale3 = 10f;  // удаление по оси Y
        float zScale3 = 0f;  // удаление по оси Z

        float xScale4 = 0f;  // удаление по оси X
        float yScale4 = 0f;  // удаление по оси Y
        float zScale4 = -10f;  // удаление по оси Z

        float[] vertices = {
                // Отдоление от 0,0,0 по оси Z+  = Спереди
                // ось X
                -l+xScale1, yScale1,zScale1,
                l+xScale1,yScale1,zScale1,
                // ось Y
                xScale1,-l+yScale1,zScale1,
                xScale1,l+yScale1,zScale1,
                // ось Z
                xScale1,yScale1,-l+zScale1,
                xScale1,yScale1,l+zScale1,

                // Отдоление от 0,0,0 по оси Y-  = Снизу
                // ось X
                -l+xScale2, yScale2, zScale2,
                l+xScale2,yScale2, zScale2,
                // ось Y
                xScale2,-l+yScale2, zScale2,
                xScale2,l+yScale2, zScale2,
                // ось Z
                xScale2,yScale2,-l+zScale2,
                xScale2,yScale2,l+zScale2,

                // Отдоление от 0,0,0 по оси Y+  = Сверху
                // ось X
                -l+xScale3, yScale3, zScale3,
                l+xScale3,yScale3, zScale3,
                // ось Y
                xScale3,-l+yScale3,zScale3,
                xScale3,l+yScale3,zScale3,
                // ось Z
                xScale3,yScale3,-l+zScale3,
                xScale3,yScale3,l+zScale3,
//
                // Отдоление от 0,0,0 по оси Z-  = Сзади
                // ось X
                -l+xScale4, yScale4, zScale4,
                l+xScale4,yScale4, zScale4,
                // ось Y
                xScale4,-l+yScale4,zScale4,
                xScale4,l+yScale4,zScale4,
                // ось Z
                xScale4,yScale4,-l+zScale4,
                xScale4,yScale4,l+zScale4,

                //triangles BACK  parallel to XY plane GREEN !!!!!!!!!!!
                -0.5f+xScale1, 0.5f+yScale1, zScale1+0.5f,
                -0.5f+xScale1, -0.5f+yScale1, zScale1+0.5f,
                0.5f+xScale1, 0.5f+yScale1, zScale1+0.5f,

                -0.5f+xScale1, -0.5f+yScale1, zScale1+0.5f,
                0.5f+xScale1, 0.5f+yScale1, zScale1+0.5f,
                0.5f+xScale1, -0.5f+yScale1, zScale1+0.5f,

                //triangles TOP parallel to XZ plane PURPLE !!!!!
                -0.5f+xScale1, 0.5f+yScale1, zScale1+0.5f,
                -0.5f+xScale1, 0.5f+yScale1, zScale1-3.5f,
                0.5f+xScale1, 0.5f+yScale1, zScale1-3.5f,

                -0.5f+xScale1, 0.5f+yScale1, zScale1+0.5f,
                0.5f+xScale1, 0.5f+yScale1, zScale1-3.5f,
                0.5f+xScale1, 0.5f+yScale1, zScale1+0.5f,

                //triangles BOTTOM parallel to XZ plane YELLOW !!!!!
                -0.5f+xScale1, -0.5f+yScale1, zScale1+0.5f,
                -0.5f+xScale1, -0.5f+yScale1, zScale1-3.5f,
                0.5f+xScale1, -0.5f+yScale1, zScale1-3.5f,

                -0.5f+xScale1, -0.5f+yScale1, zScale1+0.5f,
                0.5f+xScale1, -0.5f+yScale1, zScale1-3.5f,
                0.5f+xScale1, -0.5f+yScale1, zScale1+0.5f,
        };

        vertexData = ByteBuffer
                .allocateDirect(vertices.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        vertexData.put(vertices);
        vertexData.position(0);
    }

    private void drawModel() {
        glLineWidth(1);

        // x axis                      BLUE !!!!!!!!!!!
        glUniform4f(mColorHandle, 0.0f, 1.0f, 1.0f, 1.0f);
        glDrawArrays(GL_LINES, 0, 2);
        // y axis                      PINK !!!!!!!!!!!
        glUniform4f(mColorHandle, 0.0f, 1.0f, 1.0f, 1.0f);
        glDrawArrays(GL_LINES, 2, 2);
        // z axis                      ORANGE !!!!!!!!!!!!!!!!!!!!
        glUniform4f(mColorHandle, 0.0f, 1.0f, 1.0f, 1.0f);
        glDrawArrays(GL_LINES, 4, 2);

        // x axis                      ON BOTTOM PURPLE
        glUniform4f(mColorHandle, 0.5f, 0.5f, 1f, 1.0f);
        glDrawArrays(GL_LINES, 6, 2);
        // y axis                      ON BOTTOM PURPLE
        glUniform4f(mColorHandle, 0.5f, 0.5f, 1f, 1.0f);
        glDrawArrays(GL_LINES, 8, 2);
        // z axis                      ON BOTTOM PURPLE
        glUniform4f(mColorHandle, 0.5f, 0.5f, 1f, 1.0f);
        glDrawArrays(GL_LINES, 10, 2);

        // x axis                      ON TOP GREEN
        glUniform4f(mColorHandle, 0.5f, 1f, 0.5f, 1.0f);
        glDrawArrays(GL_LINES, 12, 2);
        // y axis                      ON TOP GREEN
        glUniform4f(mColorHandle, 0.5f, 1f, 0.5f, 1.0f);
        glDrawArrays(GL_LINES, 14, 2);
        // z axis                      ON TOP GREEN
        glUniform4f(mColorHandle, 0.5f, 1f, 0.5f, 1.0f);
        glDrawArrays(GL_LINES, 16, 2);

        // x axis                      ON BACK RED
        glUniform4f(mColorHandle, 1f, 0f, 0f, 1.0f);
        glDrawArrays(GL_LINES, 18, 2);
        // y axis                      ON BACK RED
        glUniform4f(mColorHandle, 1f, 0f, 0f, 1.0f);
        glDrawArrays(GL_LINES, 20, 2);
        // z axis                      ON BACK RED
        glUniform4f(mColorHandle, 1f, 0f, 0f, 1.0f);
        glDrawArrays(GL_LINES, 22, 2);

        //BACK SQUARE  GREEN !!!!!!!!!!!
        glUniform4f(mColorHandle, 0.5f, 1f, 0.5f, 1.0f);
        glDrawArrays(GL_TRIANGLES, 24, 3);
        glUniform4f(mColorHandle, 0.5f, 1f, 0.5f, 1.0f);
        glDrawArrays(GL_TRIANGLES, 27, 3);

        //TOP SQUARE  PURPLE !!!!!
        glUniform4f(mColorHandle, 0.5f, 0.5f, 1f, 1.0f);
        glDrawArrays(GL_TRIANGLES, 30, 3);
        glUniform4f(mColorHandle, 0.5f, 0.5f, 1f, 1.0f);
        glDrawArrays(GL_TRIANGLES, 33, 3);

        //BOTTOM SQUARE YELLOW !!!!!
        glUniform4f(mColorHandle, 1f, 1f, 0.5f, 1.0f);
        glDrawArrays(GL_TRIANGLES, 36, 3);
        glUniform4f(mColorHandle, 1f, 1f, 0.5f, 1.0f);
        glDrawArrays(GL_TRIANGLES, 39, 3);
    }
}
